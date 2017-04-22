package cn.jackq.messenger.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Process;
import android.util.Log;

/**
 * Created on: 4/22/17.
 * Creator: Jack Q <qiaobo@outlook.com>
 */

public class MessengerAudioRecorder implements Runnable {
    public interface MessengerAudioPackageListener {
        /**
         * @param buffer the buffer of the audio message.
         *               This buffer may be reused in serials of handling process.
         *               After finish the message handling method, the content
         *               should not be changed.
         * @param size   the length of audio package in length
         */
        void onAudioPackage(byte[] buffer, int size);
    }

    private static final String TAG = "MessengerAudioRecorder";

    private int source = MediaRecorder.AudioSource.MIC;
    private int sampleRate = 8000;
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSize = 3 * AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);

    private AudioRecord mRecord;
    private IEncoder mEncoder;

    private MessengerAudioPackageListener mListener;
    private final Object mStateLock = new Object();
    private Thread mThread;
    private boolean isRecording;

    public MessengerAudioRecorder(MessengerAudioPackageListener listener) {
        mListener = listener;
    }

    public void start() throws AudioException {
        if (mRecord == null) {
            mRecord = new AudioRecord(source, sampleRate, channelConfig, audioFormat, bufferSize);
        }
        if(mEncoder == null){
            try {
                int channels = 1;
                int frameSize = sampleRate / 100;
                int framesPerPacket = 10;
                int bitrate = 8 * 1024 * 64;
                int maxBufferSize = bufferSize * framesPerPacket;
                mEncoder = new OpusCodecs.Encoder(sampleRate, channels, frameSize, framesPerPacket, bitrate, maxBufferSize);
            } catch (NativeAudioException e) {
                e.printStackTrace();
                throw e;
            }
        }
        synchronized (mStateLock){
            if (isRecording) {
                Log.w(TAG, "start: failed to start recording function when the recording process is ongoing");
            } else {
                mThread = new Thread(this);
                isRecording = true;
                Log.d(TAG, "start: start audio mRecord thread");
            }
        }
    }

    public void stop() {
        if (mRecord == null) {
            Log.w(TAG, "stop: no running audio mRecord");
            return;
        }
        synchronized (mStateLock){
            if(isRecording){
                isRecording = false;
                try {
                    // Close pending IO operations
                    mThread.interrupt();
                    // wait the process to die
                    mThread.join(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mThread = null;
            }else{
                Log.w(TAG, "stop: failed to stop recording when no running task in ongoing");
            }
        }
        Log.d(TAG, "stop: stopped audio recording");
    }

    public void shutdown() {
        if (mRecord != null) {
            mRecord.release();
            mRecord = null;
        }
    }

    /**
     * This function should be handled on a separate process
     * <p>
     * Audio data collected from hardware encoded by OS will be sent to the
     * buffer of the {@link #mRecord}. Then in this thread, data will be
     * encoded via encoder and then packaged into general data package
     * (with no header data, which requires network related logic to process).
     */
    @Override
    public void run() {

        // Since the real-time requirement of the audio related feature,
        // the mRecord process requires some extra configuration of process
        // scheduling hint to OS
        Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);

        this.mRecord.startRecording();
        if (this.mRecord.getState() != AudioRecord.STATE_INITIALIZED)
            Log.e(TAG, "run: message record is not initialized on recording");

        short[] buffer = new short[bufferSize];
        byte[] encodedDataBuffer = new byte[bufferSize];

        while(this.isRecording){
            int readLength = mRecord.read(buffer, 0, buffer.length);
            try {
                mEncoder.encode(buffer, readLength);
            } catch (NativeAudioException e) {
                e.printStackTrace();
            }
            sendAudioPack(encodedDataBuffer);
        }
        try {
            mEncoder.terminate();
        } catch (NativeAudioException e) {
            e.printStackTrace();
        }
        sendAudioPack(encodedDataBuffer);
        this.mRecord.stop();
    }

    private void sendAudioPack(byte[] encodedDataBuffer) {
        if(mEncoder.isReady()){
            int bufferedFrames = mEncoder.getBufferedFrames();
            int dataSize = mEncoder.getEncodedData(encodedDataBuffer);
            Log.d(TAG, "run: Encoded " + bufferedFrames + " frames into a package of " + dataSize + " bytes");
            mListener.onAudioPackage(encodedDataBuffer, dataSize);
        }
    }
}
