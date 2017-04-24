package cn.jackq.messenger.audio;

public class NativeAudioException extends AudioException {
    public NativeAudioException(String message) {
        super(message);
    }

    public NativeAudioException(Throwable throwable) {
        super(throwable);
    }

    public NativeAudioException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
