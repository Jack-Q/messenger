package cn.jackq.messenger.ui;

import android.content.Context;
import android.content.Intent;
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

import java.util.Arrays;
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
        if (this.getMainService() == null)
            return;

        // by requesting call, the backend service will start new activity
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

    interface IMessageViewHolder {
        void setMessageContent(Message message);
    }

    class MessageListAdapter extends ArrayAdapter<Message> {
        private Message.MessageType[] supportedTypes = new Message.MessageType[]{
                Message.MessageType.SEND, Message.MessageType.RECEIVE, Message.MessageType.SYSTEM};


        public MessageListAdapter(@NonNull Context context, int resource, @NonNull List<Message> objects) {
            super(context, resource, objects);
        }

        @Override
        public int getItemViewType(int position) {
            return getItemTypeIndex(mMessages.get(position).getType());
        }

        private int getItemTypeIndex(Message.MessageType type) {
            return Arrays.binarySearch(supportedTypes, type);
        }

        @Override
        public int getViewTypeCount() {
            return supportedTypes.length;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            IMessageViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) ChatActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                switch (mMessages.get(position).getType()){
                    case SEND:
                        convertView = inflater.inflate(R.layout.list_item_chat_send, parent, false);
                        holder = new MessageViewHolder(convertView);
                        break;
                    case RECEIVE:
                        convertView = inflater.inflate(R.layout.list_item_chat_receive, parent, false);
                        holder = new MessageViewHolder(convertView);
                        break;
                    case SYSTEM:
                    default:
                        convertView = inflater.inflate(R.layout.list_item_chat_system, parent, false);
                        holder = new SystemMessageViewHolder(convertView);
                        break;
                }
                convertView.setTag(holder);
            } else {
                holder = (IMessageViewHolder) convertView.getTag();
            }

            holder.setMessageContent(mMessages.get(position));

            return convertView;
        }

        class MessageViewHolder implements IMessageViewHolder{
            @BindView(R.id.message_view_layout)
            LinearLayout layout;
            @BindView(R.id.message_content)
            AppCompatTextView content;
            @BindView(R.id.message_time)
            AppCompatTextView time;

            MessageViewHolder(View view) {
                ButterKnife.bind(this, view);
            }

            @Override
            public void setMessageContent(Message message) {
                this.content.setText(message.getContent());
                this.time.setText(message.getDate().toString());
            }
        }

        class SystemMessageViewHolder implements IMessageViewHolder {
            @BindView(R.id.message_view_system_text)
            AppCompatTextView content;

            SystemMessageViewHolder(View view) {
                ButterKnife.bind(this, view);
            }

            @Override
            public void setMessageContent(Message message) {
                this.content.setText(message.getContent());
            }
        }
    }

    MessageListAdapter mListAdapter;

    @Override
    protected void onServiceBound() {
        super.onServiceBound();
        mMessages = getMainService().getMessageManager().getMessages(mUser);
        mListAdapter = new MessageListAdapter(this, R.layout.list_item_chat_receive, mMessages);
        mChatMessageList.setAdapter(mListAdapter);
        getMainService().getMessageManager().readAll(mUser);
    }

    @Override
    public void onServerStateChange() {
        super.onServerStateChange();
        this.runOnUiThread(() -> {
            this.mListAdapter.notifyDataSetChanged();
            getMainService().getMessageManager().readAll(mUser);
        });
    }
}
