package cn.jackq.messenger.ui;

import android.app.Activity;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jackq.messenger.R;
import cn.jackq.messenger.audio.AudioException;
import cn.jackq.messenger.audio.MessengerAudioOutput;
import cn.jackq.messenger.audio.MessengerAudioRecorder;
import cn.jackq.messenger.network.PeerTransmission;
import cn.jackq.messenger.network.ServerConnection;

public class MainActivityHomePage implements MessengerAudioRecorder.MessengerAudioPackageListener, PeerTransmission.PeerTransmissionListener {
    private static final String TAG = "MainActivityHomePage";
    private View mRootView;
    private Activity mRootActivity;

    @BindView(R.id.status_text)
    TextView mStatusText;

    @BindView(R.id.edit_peer_ip)
    EditText mPeerIpEdit;

    @BindView(R.id.edit_peer_port)
    EditText mPeerPortEdit;

    MessengerAudioRecorder mRecorder;
    MessengerAudioOutput mOutput;
    PeerTransmission mPeerTransmission;

    @OnClick(R.id.main_clear_button)
    void clearLog() {
        mStatusText.setText("");
    }

    @OnClick(R.id.main_message_button)
    void messageButtonClickHandler(ToggleButton toggleButton) {

        if (toggleButton.isChecked()) {
            if (mPeerTransmission == null) {
                mPeerTransmission = new PeerTransmission(this);
                writeLog("start peer transmission");
                Log.d(TAG, "messageButtonClickHandler: starting peer socket");
                mPeerTransmission.create(mPeerIpEdit.getText().toString(), Integer.parseInt(mPeerPortEdit.getText().toString()), errorMessage -> {
                    if (errorMessage != null) {
                        writeLog("failed to initiate peer socket: " + errorMessage);
                        toggleButton.setChecked(false);
                        return;
                    }
                    enableAudioRecord();
                });
            } else {
                enableAudioRecord();
            }

        } else {
            disableAudioRecord();
        }
    }

    @OnClick(R.id.main_output_button)
    void playButtonClickHandler(ToggleButton toggleButton) {
        if (toggleButton.isChecked()) {
            // enable packet output
            if (mOutput == null) {
                mOutput = new MessengerAudioOutput();
                mOutput.init();
            }
            mOutput.start();
        } else {
            // disable packet output
            mOutput.stop();
        }
    }

    private void disableAudioRecord() {
        if (mRecorder != null) {
            writeLog("disable audio recording");
            mRecorder.stop();
        }

    }

    private void enableAudioRecord() {
        writeLog("enable audio recording");
        if (mRecorder == null) mRecorder = new MessengerAudioRecorder(this);
        try {
            mRecorder.start();
        } catch (AudioException e) {
            e.printStackTrace();
            writeLog(e.getMessage());
        }
    }

    @OnClick(R.id.main_button_call)
    void buttonClickHandler() {
        ServerConnection.get().testServer().thenAccept(status -> MainActivityHomePage.this.writeLog(status.getMessage()));
    }

    public MainActivityHomePage(@NonNull Activity activity, @NonNull View rootView) {
        mRootActivity = activity;
        mRootView = rootView;
        ButterKnife.bind(this, rootView);
    }

    private void writeLog(final String message) {
        // Dispatch UI interaction to UI thread if current one is not the UI thread
        if (Looper.getMainLooper() != Looper.myLooper()) {
            mRootActivity.runOnUiThread(() -> writeLog(message));
            return;
        }
        mStatusText.append(message.endsWith("\n") ? message : message + "\n");
    }

    @Override
    public void onAudioPackage(byte[] buffer, int size) {
        try {
            mPeerTransmission.sendPacket(buffer, size);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPackageReceived(byte[] data, int size) {
        if (mOutput != null) {
            mOutput.bufferPacket(data, 0, size);
        }
    }

    @Override
    public void onError() {
        writeLog("socket connection error occurred");
        Log.e(TAG, "onError: socket error");
        disableAudioRecord();
    }
}
