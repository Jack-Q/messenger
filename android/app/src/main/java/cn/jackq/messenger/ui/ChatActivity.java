package cn.jackq.messenger.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.ListViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import cn.jackq.messenger.R;
import cn.jackq.messenger.message.Message;

public class ChatActivity extends AbstractMessengerActivity {
    private String mUser;
    private List<Message> mMessages;

    @BindView(R.id.chat_message_list)
    ListViewCompat mChatMessageList;
    @BindView(R.id.chat_text)
    TextView mChatText;
    @BindView(R.id.chat_send)
    Button mSendButton;

    @OnTextChanged(R.id.chat_text)
    void onChatTextChange() {
        if (mChatText.getText().toString().length() > 0) {
            mSendButton.setEnabled(true);
        } else {
            mSendButton.setEnabled(false);
        }
    }

    @OnClick(R.id.chat_send)
    void onSend() {
        String message = mChatText.getText().toString().trim();
        mChatText.setText("");

        if (message.length() == 0) {
            mChatText.requestFocus();
            return;
        }

        getMainService().sendMessageToUser(getMainService().getUserByName(mUser), message);
    }

    @OnClick(R.id.fab)
    void onFabClick(View view) {
        if(this.getMainService() == null)
            return;

        getMainService().callRequest(getMainService().getUserByName(mUser));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        this.mUser = this.getIntent().getStringExtra("user");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);
        this.setTitle(mUser);

    }

    class MessageListAdapter extends ArrayAdapter<Message> {

        public MessageListAdapter(@NonNull Context context, int resource, @NonNull List<Message> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) ChatActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_item_chat, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Message message = mMessages.get(position);
            holder.content.setText(message.getContent());
            holder.time.setText(message.getDate().toString());
            switch (message.getType()){
                case SEND:
                    holder.layout.setPadding(35, 5, 5, 5);
                    break;

                case RECEIVE:
                    holder.layout.setPadding(5, 5, 35, 5);
                    break;

                case SYSTEM:
                    holder.layout.setPadding(35, 5, 35, 5);
                    break;

            }

            return convertView;
        }

        class ViewHolder {
            @BindView(R.id.message_view_layout)
            LinearLayout layout;
            @BindView(R.id.message_content)
            AppCompatTextView content;
            @BindView(R.id.message_time)
            AppCompatTextView time;

            ViewHolder(View view) {
                ButterKnife.bind(this, view);
            }
        }
    }

    MessageListAdapter mListAdapter;

    @Override
    protected void onServiceBound() {
        super.onServiceBound();
        mMessages = getMainService().getMessageManager().getMessages(mUser);
        mListAdapter = new MessageListAdapter(this, R.layout.list_item_chat, mMessages);
        mChatMessageList.setAdapter(mListAdapter);
    }

    @Override
    public void onServerStateChange() {
        super.onServerStateChange();
        this.runOnUiThread(()-> this.mListAdapter.notifyDataSetChanged());
    }
}
