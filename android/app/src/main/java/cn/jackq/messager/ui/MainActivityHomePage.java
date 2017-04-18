package cn.jackq.messager.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jackq.messager.R;
import cn.jackq.messager.network.ServerConnection;

public class MainActivityHomePage {
    private View mRootView;
    private Context mContext;

    @BindView(R.id.status_text) TextView mStatusText;

    @OnClick(R.id.main_button_call) void buttonClickHandler() {
        ServerConnection.get().testServer().thenAccept(status -> mStatusText.append(status.getMessage()));
    }

    public MainActivityHomePage(@NonNull Context context, @NonNull View rootView) {
        mContext = context;
        mRootView = rootView;
        ButterKnife.bind(this, rootView);
    }
}
