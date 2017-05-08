package cn.jackq.messenger.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jackq.messenger.R;
import cn.jackq.messenger.service.ChatSession;

public class CallActivity extends AbstractMessengerActivity {

    private static final String TAG = "CallActivity";

    @BindView(R.id.content_view)
    LinearLayout mContentView;
    @BindView(R.id.chat_time)
    TextView mChatTimeView;
    @BindView(R.id.chat_peer)
    TextView mChatPeerView;
    @BindView(R.id.chat_status)
    TextView mChatStatusView;

    @BindView(R.id.answer_button)
    Button mAnswerButton;
    @BindView(R.id.end_button)
    Button mEndButton;

    @OnClick(R.id.answer_button)
    void onAnswerCall() {
        getMainService().callAnswer();
    }

    @OnClick(R.id.end_button)
    void onCancelCall() {
        getMainService().callTerminate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_call);

        ButterKnife.bind(this);

        hide();

    }


    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }


    private void updateUi() {
        ChatSession session = this.getMainService().getChatSession();
        this.mChatPeerView.setText(session.getPeer().getName());
        this.mChatStatusView.setText(session.getStatusString());
        this.mChatTimeView.setText(session.getTimeString());

        this.mAnswerButton.setEnabled(session.isCanAnswer());
        this.mAnswerButton.setVisibility(session.isCanAnswer() ? View.VISIBLE : View.GONE);

        this.mEndButton.setEnabled(session.isCanEnd());
        this.mEndButton.setVisibility(session.isCanEnd() ? View.VISIBLE : View.GONE);

        Log.d(TAG, "updateUi: answer:" + session.isCanAnswer() + ", visibility:" + session.isCanEnd());
        if(session.getStatus() == ChatSession.ChatStatus.NULL){
            new Handler().postDelayed(CallActivity.this::finish, 2000);
        }
    }

    @Override
    public void onServerStateChange() {
        super.onServerStateChange();
        this.runOnUiThread(this::updateUi);
    }

    @Override
    protected void onServiceBound() {
        super.onServiceBound();
        this.runOnUiThread(this::updateUi);
    }
}
