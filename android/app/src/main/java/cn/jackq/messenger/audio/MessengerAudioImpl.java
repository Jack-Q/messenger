package cn.jackq.messenger.audio;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * Created on: 5/6/17.
 * Creator: Jack Q <qiaobo@outlook.com>
 */



class MessengerAudioImpl extends MessengerAudio implements MessengerAudioRecorder.MessengerAudioPackageListener {
    private MessengerAudioOutput mOutput = new MessengerAudioOutput();
    private MessengerAudioRecorder mRecorder = new MessengerAudioRecorder(this);

    @Override
    public void init() {
        this.mOutput.init();
        this.mRecorder.init();
    }

    @Override
    public void startSession() throws AudioException {
        this.mOutput.start();
        this.mRecorder.start();
    }

    @Override
    public void endSession(){

    }

    @Override
    public void receiveAudioFrame(ByteBuffer audioFrame){

    }

    @Override
    public void onAudioPackage(ByteBuffer buffer) {
        this.onSendAudioFrame(buffer);
    }
}
