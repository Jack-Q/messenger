package cn.jackq.messenger.ui;

import android.app.Activity;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jackq.messenger.R;
import cn.jackq.messenger.network.PeerTransmission;

public class MainActivityHomePage {
    private static final String TAG = "MainActivityHomePage";
    private View mRootView;
    private Activity mRootActivity;

    @BindView(R.id.status_text)
    TextView mStatusText;

    @BindView(R.id.edit_peer_ip)
    EditText mPeerIpEdit;

    @BindView(R.id.edit_peer_port)
    EditText mPeerPortEdit;

    PeerTransmission mPeerTransmission;

    @OnClick(R.id.main_clear_button)
    void clearLog() {
        mStatusText.setText("");
    }

    @OnClick(R.id.main_message_button)
    void messageButtonClickHandler(ToggleButton toggleButton) {
    }

    @OnClick(R.id.main_output_button)
    void playButtonClickHandler(ToggleButton toggleButton) {

    }

    private void disableAudioRecord() {

    }

    private void enableAudioRecord() {
        writeLog("enable audio recording");
    }

    @OnClick(R.id.main_button_call)
    void buttonClickHandler() {
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


}
