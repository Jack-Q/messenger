package cn.jackq.messager.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

/**
 * Created on: 4/22/17.
 * Creator: Jack Q <qiaobo@outlook.com>
 */

public class MessagerAudioRecorder {

    private AudioRecord record;
    private int source = MediaRecorder.AudioSource.MIC;
    private int sampleRate = 8000;
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSize = 3 * AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
    private byte[] buffer = new byte[bufferSize];

    public MessagerAudioRecorder() {

    }

    public void start() {
        record = new AudioRecord(source, sampleRate, channelConfig, audioFormat, bufferSize);

    }
}
