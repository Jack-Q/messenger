package cn.jackq.messenger.audio;

/**
 * Created on: 28/04/14.
 * Creator: Jack Q <qiaobo@outlook.com>
 */
public class AudioException extends Exception {
    public AudioException(String message) {
        super(message);
    }

    public AudioException(Throwable throwable) {
        super(throwable);
    }

    public AudioException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
