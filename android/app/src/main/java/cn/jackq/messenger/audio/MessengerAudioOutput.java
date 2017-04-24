package cn.jackq.messenger.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created on: 4/24/17.
 * Creator: Jack Q <qiaobo@outlook.com>
 */

public class MessengerAudioOutput {
    private static final String TAG = "MessengerAudioOutput";

    private AudioTrack mTrack;
    private OpusCodec.Decoder mDecoder;

    private int sampleRateInHz = 8000;
    private int channelOutMono = AudioFormat.CHANNEL_OUT_MONO;
    private int encodingPcm16bit = AudioFormat.ENCODING_PCM_16BIT;
    private int minSize = AudioTrack.getMinBufferSize(sampleRateInHz, channelOutMono, encodingPcm16bit);

    public MessengerAudioOutput() {
    }

    public void init() {
        if (mTrack == null)
            mTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL,
                    sampleRateInHz, channelOutMono, encodingPcm16bit, minSize, AudioTrack.MODE_STREAM);
        if (mDecoder == null)
            try {
                Log.d(TAG, "init: create decoder");
                mDecoder = new OpusCodec.Decoder(sampleRateInHz, 1);
            } catch (NativeAudioException e) {
                e.printStackTrace();
            }
    }

    public void start() {
        mTrack.play();
    }

    public void bufferPacket(byte[] buffer, int offset, int size) {
        // use decode short to create 16bit sample
        short[] decoderBuffer = new short[320];
        int decodeSize;
        try {
            Log.d(TAG, "bufferPacket: decode packet of size " + size);
            decodeSize = mDecoder.decodeShort(ByteBuffer.wrap(buffer, offset, size), size, decoderBuffer, 320);
            Log.d(TAG, "bufferPacket: write decoded packet to audio track with size " + decodeSize);
            mTrack.write(decoderBuffer, 0, decodeSize);
        } catch (NativeAudioException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        mTrack.stop();
    }

}
