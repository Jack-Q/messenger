package cn.jackq.messenger.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Process;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created on: 4/22/17.
 * Creator: Jack Q <qiaobo@outlook.com>
 */

class MessengerAudioRecorder implements Runnable {

    public interface MessengerAudioPackageListener {
        /**
         * @param buffer the buffer of the audio message.
         *               This buffer may be reused in serials of handling process.
         *               After finish the message handling method, the content
         *               should not be changed.
         */
        void onAudioPackage(ByteBuffer buffer);
    }

    private static final String TAG = "MessengerAudioRecorder";

    private int source = MediaRecorder.AudioSource.MIC;
    private int sampleRate = 44100;
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    // As the Opus codec requires, the bufferSize, which also determines the frame size, ought to be some selected discrete value
    // Feasible value including: 400, 200, 100, 50, 25, 50/3. 50/4 (12.5), 10, 50/6;
    private int packageRate = 25;
    private int encoderFrameSize =  1920;
    // The objective bit rate of the audio message to be encoded
    private int bitrate = 80 * 1024;

    private AudioRecord mRecord;
    private IEncoder mEncoder;

    private MessengerAudioPackageListener mListener;
    private final Object mStateLock = new Object();
    private Thread mThread;
    private boolean isRecording;

    MessengerAudioRecorder(MessengerAudioPackageListener listener) {
        mListener = listener;
    }


    public void init() {

    }

    public void start() throws AudioException {
        if (mRecord == null) {
            Log.d(TAG, "start: create new record instance");
            Log.d(TAG, "start: buffer size " + encoderFrameSize);
            mRecord = new AudioRecord(source, sampleRate, channelConfig, audioFormat, encoderFrameSize);
        }
        if(mEncoder == null){
            Log.d(TAG, "start: create new encoder instance");
            try {
                int framesPerPacket = 1;
                int channels = 1;
                mEncoder = new OpusCodec.Encoder(48000,
                        channels, // Current use single channel mode only
                        encoderFrameSize,
                        framesPerPacket,
                        bitrate,
                        480);
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
                mThread.start();
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
        Log.d(TAG, "run: begin record thread");

        // Since the real-time requirement of the audio related feature,
        // the mRecord process requires some extra configuration of process
        // scheduling hint to OS
        Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);

        this.mRecord.startRecording();
        if (this.mRecord.getState() != AudioRecord.STATE_INITIALIZED)
            Log.e(TAG, "run: message record is not initialized on recording");

        short[] buffer = new short[encoderFrameSize];
        byte[] encodedDataBuffer = new byte[encoderFrameSize];

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
            Log.d(TAG, "run: Terminate recorder");
            mEncoder.terminate();
        } catch (NativeAudioException e) {
            e.printStackTrace();
        }
        sendAudioPack(encodedDataBuffer);
        this.mRecord.stop();

        Log.d(TAG, "run: quit record thread");
    }

    private void sendAudioPack(byte[] encodedDataBuffer) {
        if(mEncoder.isReady()){
            // int bufferedFrames = mEncoder.getBufferedFrames();
            int dataSize = mEncoder.getEncodedData(encodedDataBuffer);
            mListener.onAudioPackage(ByteBuffer.wrap(encodedDataBuffer, 0, dataSize));
        }else{
            Log.d(TAG, "sendAudioPack: send request prematurely");
        }
    }
}
