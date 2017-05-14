package cn.jackq.messenger.audio;

import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created on: 5/6/17.
 * Creator: Jack Q <qiaobo@outlook.com>
 */


class MessengerAudioImpl extends MessengerAudio implements MessengerAudioRecorder.MessengerAudioPackageListener {
    private static final String TAG = "MessengerAudioImpl";
    private MessengerAudioOutput mOutput = new MessengerAudioOutput();
    private MessengerAudioRecorder mRecorder = new MessengerAudioRecorder(this);
    private int recorderFrameIndex = 0;

    @Override
    public void init() {
        this.mOutput.init();
        this.mRecorder.init();
    }

    @Override
    public void startSession() throws AudioException {
        recorderFrameIndex = 0;
        this.mOutput.start();
        this.mRecorder.start();
    }

    @Override
    public void endSession() {
        this.mRecorder.stop();
        this.mOutput.stop();
    }

    @Override
    public void receiveAudioFrame(ByteBuffer audioFrame) {
        // this will change the position (pointer) of the audio frame to the initial position of compressed audio
        int index = MessengerAudioPacker.unpackAudioFrame(audioFrame);
        Log.d(TAG, "receiveAudioFrame: pos " + audioFrame.position() + " limit " + audioFrame.limit() + " length " + (audioFrame.limit() - audioFrame.position()));
        this.mOutput.bufferPacket(index, audioFrame.array(), audioFrame.position(), audioFrame.limit() - audioFrame.position());
    }

    @Override
    public void onAudioPackage(ByteBuffer buffer) {
        ByteBuffer packedFrame = MessengerAudioPacker.packAudioFrame(recorderFrameIndex++, buffer);
        this.onSendAudioFrame(packedFrame);
    }
}
