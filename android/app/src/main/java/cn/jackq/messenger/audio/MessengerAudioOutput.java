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

class MessengerAudioOutput {
    private static final String TAG = "MessengerAudioOutput";
    private static final int CODEC_SAMPLE_RATE = 48000;

    private AudioTrack mTrack;
    private OpusCodec.Decoder mDecoder;

    private int sampleRateInHz = 44100;
    private int channelOutMono = AudioFormat.CHANNEL_OUT_MONO;
    private int encodingPcm16bit = AudioFormat.ENCODING_PCM_16BIT;
    private int minSize = AudioTrack.getMinBufferSize(sampleRateInHz, channelOutMono, encodingPcm16bit);

    private boolean isMuted = true;

    // single byte array for network transfer
    private byte[] inDataBuffer = new byte[10240];
    // use decode short to create 16bit sample
    private short[] outDataBuffer = new short[10240];

    public MessengerAudioOutput() {
    }

    public void init() {
        if (mTrack == null)
            mTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL,
                    sampleRateInHz, channelOutMono, encodingPcm16bit, minSize, AudioTrack.MODE_STREAM);
        if (mDecoder == null)
            try {
                Log.d(TAG, "init: create decoder");
                mDecoder = new OpusCodec.Decoder(CODEC_SAMPLE_RATE, 1);
            } catch (NativeAudioException e) {
                e.printStackTrace();
            }
    }

    public void start() {
        // first clean all of the pending audio data that was recorded during the pause
        mTrack.flush();
        mTrack.play();
        isMuted = false;
    }

    public void bufferPacket(int index, byte[] buffer, int offset, int size) {
        // ignore packet when current mode is muted
        if (isMuted)
            return;

        byte[] rawData = buffer;
        if(offset != 0) {
            // The native binding of decoder required the audio frame
            // of data is started from the initial position of the array.
            // Thus an array copy id required for an frame with offset
            rawData = inDataBuffer;
            System.arraycopy(buffer, offset, inDataBuffer, 0, size);
        }

        try {
            int decodeSize;
            decodeSize = mDecoder.decodeShort(ByteBuffer.wrap(rawData, 0, size), size, outDataBuffer, outDataBuffer.length);
            mTrack.write(outDataBuffer, 0, decodeSize);
        } catch (NativeAudioException e) {
            e.printStackTrace();
            Log.e(TAG, "bufferPacket: Threads un-synchronized and null pointer exception occurred", e);
        }
    }

    public void stop() {
        // In order to stop playing the the streaming data, the data should be paused then usd flush to clear
        // the data content. For static audio files, the stop method should be used.
        mTrack.pause();
        mTrack.flush();
        isMuted = true;
    }

}
