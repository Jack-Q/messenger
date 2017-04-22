package cn.jackq.messager.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jackq.messager.R;
import cn.jackq.messager.audio.AudioException;
import cn.jackq.messager.audio.MessengerAudioRecorder;
import cn.jackq.messager.network.ServerConnection;

public class MainActivityHomePage implements MessengerAudioRecorder.MessengerAudioPackageListener {
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
        writeLog("receive package of size " + size);
    }
}
