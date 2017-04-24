package cn.jackq.messenger.ui;

import android.app.Activity;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jackq.messenger.R;
import cn.jackq.messenger.audio.AudioException;
import cn.jackq.messenger.audio.MessengerAudioRecorder;
import cn.jackq.messenger.network.PeerTransmission;
import cn.jackq.messenger.network.ServerConnection;

public class MainActivityHomePage implements MessengerAudioRecorder.MessengerAudioPackageListener, PeerTransmission.PeerTransmissionListener {
    private static final String TAG = "MainActivityHomePage";
    private View mRootView;
    private Activity mRootActivity;

    @BindView(R.id.status_text)
    TextView mStatusText;

    MessengerAudioRecorder mRecorder;
    PeerTransmission mPeerTransmission;

    @OnClick(R.id.main_message_button)
    void messageButtonClickHandler(ToggleButton toggleButton) {

        if (toggleButton.isChecked()) {
            if (mPeerTransmission == null) {
                writeLog("start peer transmission");
                Log.d(TAG, "messageButtonClickHandler: starting peer socket");
                try {
                    mPeerTransmission = new PeerTransmission(this);
                } catch (SocketException | UnknownHostException e) {
                    e.printStackTrace();
                    writeLog("failed to initiate peer socket: " + e.getMessage());
                    toggleButton.setChecked(false);
                }
            }

            writeLog("enable audio recording");
            if (mRecorder == null) mRecorder = new MessengerAudioRecorder(this);
            try {
                mRecorder.start();
            } catch (AudioException e) {
                e.printStackTrace();
                writeLog(e.getMessage());
            }
        } else {
            writeLog("disable audio recording");
            mRecorder.stop();
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
        Log.d(TAG, "onAudioPackage: audio package received from audio recorder thread");
        writeLog("receive package of size " + size + " from recorder");
        try {
            mPeerTransmission.sendPacket(buffer, size);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPackageReceived(byte[] data, int size) {
        writeLog("receive packet of size " + size + " from socket");
        Log.d(TAG, "onPackageReceived: " + Arrays.toString(data));
    }
}
