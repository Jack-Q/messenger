package cn.jackq.messenger.audio;

import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.annotation.Cast;
import org.bytedeco.javacpp.annotation.Platform;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;

@Platform(library= "jniopus", cinclude={"<opus.h>","<opus_types.h>"})
public class OpusCodecs
{
    public static final int OPUS_APPLICATION_VOIP = 2048;

    public static final int OPUS_SET_BITRATE_REQUEST = 4002;
    public static final int OPUS_GET_BITRATE_REQUEST = 4003;
    public static final int OPUS_SET_VBR_REQUEST = 4006;

    public static native int opus_decoder_get_size(int channels);
    public static native Pointer opus_decoder_create(int fs, int channels, IntPointer error);
    public static native int opus_decoder_init(@Cast("OpusDecoder*") Pointer st, int fs, int channels);
    public static native int opus_decode(@Cast("OpusDecoder*") Pointer st, @Cast("const unsigned char*") ByteBuffer data, int len, short[] out, int frameSize, int decodeFec);
    public static native int opus_decode_float(@Cast("OpusDecoder*") Pointer st, @Cast("const unsigned char*") ByteBuffer data, int len, float[] out, int frameSize, int decodeFec);
    //public static native int opus_decoder_ctl(@Cast("OpusDecoder*") Pointer st,  int request);
    public static native void opus_decoder_destroy(@Cast("OpusDecoder*") Pointer st);
    //public static native int opus_packet_parse(@Cast("const unsigned char*") BytePointer data, int len, ...
    public static native int opus_packet_get_bandwidth(@Cast("const unsigned char*") byte[] data);
    public static native int opus_packet_get_samples_per_frame(@Cast("const unsigned char*") byte[] data, int fs);
    public static native int opus_packet_get_nb_channels(@Cast("const unsigned char*") byte[] data);
    public static native int opus_packet_get_nb_frames(@Cast("const unsigned char*") byte[] packet, int len);
    public static native int opus_packet_get_nb_samples(@Cast("const unsigned char*") byte[] packet, int len, int fs);


    public static native int opus_encoder_get_size(int channels);
    public static native Pointer opus_encoder_create(int fs, int channels, int application, IntPointer error);
    public static native int opus_encoder_init(@Cast("OpusEncoder*") Pointer st, int fs, int channels, int application);
    public static native int opus_encode(@Cast("OpusEncoder*") Pointer st, @Cast("const short*") short[] pcm, int frameSize, @Cast("unsigned char*") byte[] data, int maxDataBytes);
    public static native int opus_encode_float(@Cast("OpusEncoder*") Pointer st, @Cast("const float*") float[] pcm, int frameSize, @Cast("unsigned char*") byte[] data, int maxDataBytes);
    public static native void opus_encoder_destroy(@Cast("OpusEncoder*") Pointer st);
    public static native int opus_encoder_ctl(@Cast("OpusEncoder*") Pointer st, int request, Pointer value);
    public static native int opus_encoder_ctl(@Cast("OpusEncoder*") Pointer st, int request, @Cast("opus_int32") int value);

    static {
        Loader.load();
    }

    public static class Decoder implements IDecoder {

        private Pointer mState;

        public Decoder(int sampleRate, int channels) throws NativeAudioException {
            IntPointer error = new IntPointer(1);
            error.put(0);
            mState = opus_decoder_create(sampleRate, channels, error);
            if(error.get() < 0) throw new NativeAudioException("Opus decoder initialization failed with error: "+error.get());
        }

        @Override
        public int decodeFloat(ByteBuffer input, int inputSize, float[] output, int frameSize) throws NativeAudioException {
            int result = opus_decode_float(mState, input, inputSize, output, frameSize, 0);
            if(result < 0) throw new NativeAudioException("Opus decoding failed with error: "+result);
            return result;
        }

        @Override
        public int decodeShort(ByteBuffer input, int inputSize, short[] output, int frameSize) throws NativeAudioException {
            int result = opus_decode(mState, input, inputSize, output, frameSize, 0);
            if(result < 0) throw new NativeAudioException("Opus decoding failed with error: "+result);
            return result;
        }

        @Override
        public void destroy() {
            opus_decoder_destroy(mState);
        }
    }


    public static class Encoder implements IEncoder {
        private final byte[] mBuffer;
        private final short[] mAudioBuffer;
        private final int mFramesPerPacket;
        private final int mFrameSize;

        // Stateful
        private int mBufferedFrames;
        private int mEncodedLength;
        private boolean mTerminated;

        private Pointer mState;

        public Encoder(int sampleRate, int channels, int frameSize, int framesPerPacket,
                       int bitrate, int maxBufferSize) throws NativeAudioException {
            mBuffer = new byte[maxBufferSize];
            mAudioBuffer = new short[framesPerPacket * frameSize];
            mFramesPerPacket = framesPerPacket;
            mFrameSize = frameSize;
            mBufferedFrames = 0;
            mEncodedLength = 0;
            mTerminated = false;

            IntPointer error = new IntPointer(1);
            error.put(0);
            mState = opus_encoder_create(sampleRate, channels, OPUS_APPLICATION_VOIP, error);
            if(error.get() < 0) throw new NativeAudioException("Opus encoder initialization failed with error: "+error.get());
            opus_encoder_ctl(mState, OPUS_SET_VBR_REQUEST, 0);
            opus_encoder_ctl(mState, OPUS_SET_BITRATE_REQUEST, bitrate);
        }

        @Override
        public int encode(short[] input, int inputSize) throws NativeAudioException {
            if (mBufferedFrames >= mFramesPerPacket) {
                throw new BufferOverflowException();
            }

            if (inputSize != mFrameSize) {
                throw new IllegalArgumentException("This Opus encoder implementation requires a " +
                        "constant frame size.");
            }

            mTerminated = false;
            System.arraycopy(input, 0, mAudioBuffer, mFrameSize * mBufferedFrames, mFrameSize);
            mBufferedFrames++;

            if (mBufferedFrames == mFramesPerPacket) {
                return encode();
            }
            return 0;
        }

        private int encode() throws NativeAudioException {
            if (mBufferedFrames < mFramesPerPacket) {
                // If encoding is done before enough frames are buffered, fill rest of packet.
                Arrays.fill(mAudioBuffer, mFrameSize * mBufferedFrames, mAudioBuffer.length, (short)0);
                mBufferedFrames = mFramesPerPacket;
            }
            int result = opus_encode(mState, mAudioBuffer, mFrameSize * mBufferedFrames,
                    mBuffer, mBuffer.length);
            if(result < 0) throw new NativeAudioException("Opus encoding failed with error: "
                    + result);
            mEncodedLength = result;
            return result;
        }

        @Override
        public int getBufferedFrames() {
            return mBufferedFrames;
        }

        @Override
        public boolean isReady() {
            return mEncodedLength > 0;
        }

        @Override
        public int getEncodedData(byte[] packetBuffer) throws BufferUnderflowException {
            if (!isReady()) {
                throw new BufferUnderflowException();
            }

            int size = mEncodedLength;
            if(mTerminated)
                size |= 1 << 13;

            // Copy encoded data
            System.arraycopy(mBuffer, 0, packetBuffer, 0, mEncodedLength);

            mBufferedFrames = 0;
            mEncodedLength = 0;
            mTerminated = false;

            return size;
        }

        @Override
        public void terminate() throws NativeAudioException {
            mTerminated = true;
            if (mBufferedFrames > 0 && !isReady()) {
                // Perform encode operation on remaining audio if available.
                encode();
            }
        }

        public int getBitrate() {
            IntPointer ptr = new IntPointer(1);
            opus_encoder_ctl(mState, OPUS_GET_BITRATE_REQUEST, ptr);
            return ptr.get();
        }

        @Override
        public void destroy() {
            opus_encoder_destroy(mState);
        }
    }

}
