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
import cn.jackq.messager.network.NetworkOperationStatus;
import cn.jackq.messager.network.ServerConnection;
import java8.util.function.Consumer;

public class MainActivityHomePage {
    private View mRootView;
    private Context mContext;

    @BindView(R.id.status_text)
    TextView mStatusText;

    @OnClick(R.id.main_message_button)
    void messageButtonClickHandler(ToggleButton toggleButton) {
        if (toggleButton.isChecked()) {
            writeLog("enable audio recording");
        } else {
            writeLog("disable audio recording");
        }
    }

    @OnClick(R.id.main_button_call)
    void buttonClickHandler() {
        ServerConnection.get().testServer().thenAccept(new Consumer<NetworkOperationStatus>() {
            @Override
            public void accept(NetworkOperationStatus status) {
                MainActivityHomePage.this.writeLog(status.getMessage());
            }
        });
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
}
