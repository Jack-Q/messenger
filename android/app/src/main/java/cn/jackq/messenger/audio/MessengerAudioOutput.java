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

    private AudioTrack mTrack;
    private OpusCodec.Decoder mDecoder;

    private int sampleRateInHz = 8000;
    private int channelOutMono = AudioFormat.CHANNEL_OUT_MONO;
    private int encodingPcm16bit = AudioFormat.ENCODING_PCM_16BIT;
    private int minSize = AudioTrack.getMinBufferSize(sampleRateInHz, channelOutMono, encodingPcm16bit);

    private boolean isMuted = true;

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
        // first clean all of the pending audio data that was recorded during the pause
        mTrack.flush();
        mTrack.play();
        isMuted = false;
    }

    public void bufferPacket(byte[] buffer, int offset, int size) {
        // ignore packet when current mode is muted
        if(isMuted)
            return;

        // use decode short to create 16bit sample
        short[] decoderBuffer = new short[320];
        int decodeSize;
        try {
            decodeSize = mDecoder.decodeShort(ByteBuffer.wrap(buffer, offset, size), size, decoderBuffer, 320);
            mTrack.write(decoderBuffer, 0, decodeSize);
        } catch (NativeAudioException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
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
