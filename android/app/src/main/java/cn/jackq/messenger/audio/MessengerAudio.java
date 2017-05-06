package cn.jackq.messenger.audio;

import android.support.annotation.NonNull;

import java.nio.ByteBuffer;

/**
 * Created on: 5/6/17.
 * Creator: Jack Q <qiaobo@outlook.com>
 */

public abstract class MessengerAudio {
    public interface MessengerAudioListener {
        void onSendAudioFrame(ByteBuffer audioFrame);
    }

    private MessengerAudioListener listener;

    public abstract void init();

    public abstract void startSession() throws AudioException;

    public abstract void endSession();

    public abstract void receiveAudioFrame(ByteBuffer audioFrame);

    public void setListener(MessengerAudioListener listener) {
        this.listener = listener;
    }

    public MessengerAudioListener getListener() {
        return listener;
    }

    void onSendAudioFrame(ByteBuffer audioFrame) {
        if (this.getListener() != null)
            this.getListener().onSendAudioFrame(audioFrame);
    }

    @NonNull
    public static MessengerAudio create(MessengerAudioListener listener) {
        MessengerAudioImpl audio = new MessengerAudioImpl();
        audio.setListener(listener);
        return audio;
    }
}
