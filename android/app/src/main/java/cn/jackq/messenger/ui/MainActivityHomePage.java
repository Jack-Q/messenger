package cn.jackq.messenger.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jackq.messenger.R;
import cn.jackq.messenger.audio.AudioException;
import cn.jackq.messenger.audio.MessengerAudioRecorder;
import cn.jackq.messenger.network.ServerConnection;

public class MainActivityHomePage implements MessengerAudioRecorder.MessengerAudioPackageListener {
    private static final String TAG = "MainActivityHomePage";
    private View mRootView;
    private Context mContext;

    @BindView(R.id.status_text)
    TextView mStatusText;

    MessengerAudioRecorder mRecorder;

    @OnClick(R.id.main_message_button)
    void messageButtonClickHandler(ToggleButton toggleButton) {

        if (toggleButton.isChecked()) {
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

    public MainActivityHomePage(@NonNull Context context, @NonNull View rootView) {
        mContext = context;
        mRootView = rootView;
        ButterKnife.bind(this, rootView);
    }

    private void writeLog(String message) {
        if (!message.endsWith("\n")) message += '\n';
        mStatusText.append(message);
    }

    @Override
    public void onAudioPackage(byte[] buffer, int size) {
        Log.d(TAG, "onAudioPackage: audio package received from audio recorder thread");
        writeLog("receive package of size " + size);
    }
}
