package cn.jackq.messenger.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import cn.jackq.messenger.R;
import cn.jackq.messenger.audio.MessengerAudio;
import cn.jackq.messenger.message.Message;
import cn.jackq.messenger.message.MessageManager;
import cn.jackq.messenger.network.PeerTransmission;
import cn.jackq.messenger.network.ServerConnection;
import cn.jackq.messenger.network.protocol.User;
import cn.jackq.messenger.ui.CallActivity;
import cn.jackq.messenger.ui.MainActivity;

/**
 * Created on: 5/5/17.
 * Creator: Jack Q <qiaobo@outlook.com>
 */

public class MainService extends Service implements MessengerAudio.MessengerAudioListener, ServerConnection.ServerConnectionListener, PeerTransmission.PeerTransmissionListener {


    public List<User> getBuddyList() {
        return buddyList;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    @Nullable
    public User getUserByName(String mUser) {
        for(User u : this.buddyList){
            if(u.getName().equals(mUser))
                return u;
        }
        return null;
    }


    public enum MainServiceStatus {
        IN_CALL, LOGIN_IDLE, LOGGING_IN, NOT_LOGIN, CONNECTING, NOT_CONNECTED
    }

    private static final String TAG = "MainService";
    // region binding management

    public class MainServiceBinder extends Binder {

        public MainService getService() {
            return MainService.this;
        }
    }

    private final MainServiceBinder serviceBinder = new MainServiceBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: bind service from ui");
        return this.serviceBinder;
    }

    // endregion

    private final MessengerAudio audio = MessengerAudio.create(this);

    private final ServerConnection serverConnection = new ServerConnection(this);
    private final PeerTransmission peerTransmission = new PeerTransmission(this);
    private final MessageManager messageManager = MessageManager.get();

    private String mConnectId = "";
    private String mSessionId = "";
    private String mUser = "";
    private String mToken = "";
    private List<User> buddyList = new ArrayList<>();
    private MainServiceStatus mStatus = MainServiceStatus.NOT_CONNECTED;
    private String mErrorMessage = "";

    // region Server connection event handle

    @Override
    public void onServerConnected(String string) {
        this.mStatus = MainServiceStatus.NOT_LOGIN;
        this.notifyStateChange();
    }

    @Override
    public void onUserAddResponse(boolean status, String message) {
        if (status) {
            Log.d(TAG, "onUserAddResponse: successfully registered, login automatically");
            this.userLogin(mUser, mToken);
        } else {
            mErrorMessage = message;
            this.notifyStateChange();
        }
    }

    @Override
    public void onUserLoginResponse(boolean status, String message, String connectId) {
        if (status) {
            Log.d(TAG, "onUserLoginResponse: login success, change stage");
            this.mConnectId = connectId;
            this.mStatus = MainServiceStatus.LOGIN_IDLE;
        } else {
            this.mStatus = MainServiceStatus.NOT_LOGIN;
            mErrorMessage = message;
        }
        this.notifyStateChange();
    }

    @Override
    public void onServerUpdateBuddyList(List<User> buddyList) {
        this.buddyList.clear();
        this.buddyList.addAll(buddyList);
        this.notifyStateChange();
    }

    @Override
    public void onServerMessageFromUser(String user, String connectId, String message) {
        this.messageManager.addMessage(user, Message.createReceive(message));
        this.addNotification("Message from " + user, message);
        this.notifyStateChange();
    }

    @Override
    public void onServerCallInit(boolean status, String message, String sessionId, String user, String address, int port) {
        this.mStatus = MainServiceStatus.IN_CALL;
        mSessionId = sessionId;
        this.startCallActivity();
        this.notifyStateChange();
    }

    @Override
    public void onServerCallPeerAddress(boolean status, String message, String connectId, String address, int port) {
        this.notifyStateChange();
    }

    @Override
    public void onServerCallConnected(boolean status, String message, String sessionId) {
        this.notifyStateChange();

    }

    @Override
    public void onServerCallEnd(boolean status, String message, String sessionId) {
        this.mStatus = MainServiceStatus.LOGIN_IDLE;
        this.mErrorMessage = message;
        this.notifyStateChange();
    }

    @Override
    public void onServerDisconnected(String message) {
        this.mStatus = MainServiceStatus.NOT_CONNECTED;
        this.mErrorMessage = message;
        this.notifyStateChange();
    }


    // endregion

    // region audio thread event handle

    @Override
    public void onSendAudioFrame(ByteBuffer audioFrame) {
    }

    // endregion

    // region peer connection event handle

    @Override
    public void onPeerPackageReceived(byte[] data, int size) {

    }

    @Override
    public void onPeerTransmissionError() {

    }

    // endregion

    // region service lifecycle management

    @Override
    public void onCreate() {
        try {
            Log.d(TAG, "onCreate: Load native opus library");
            Class.forName("cn.jackq.messenger.audio.OpusCodec");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        super.onCreate();
        Log.d(TAG, "onCreate: create main service");
        try {
            this.audio.init();
        } catch (Exception e) {
            Log.e(TAG, "onCreate: Audio Exception", e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStartCommand: Start main service");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: destroy main service");
    }

    // endregion

    // region server connection method broker

    public void connectToServer(String host, int port) {
        Log.d(TAG, "connectToServer: Connect to server on response to client");
        this.mStatus = MainServiceStatus.CONNECTING;
        this.serverConnection.connect(host, port);
    }

    public void userLogin(String username, String token) {
        Log.d(TAG, "userLogin: login with " + username + " and token " + token);
        this.mStatus = MainServiceStatus.LOGGING_IN;
        this.mUser = username;
        this.mToken = token;
        this.serverConnection.sendUserLogin(username, token);
    }

    public void userRegister(String username, String token) {
        Log.d(TAG, "userRegister: register with " + username + " password " + token);
        this.mUser = username;
        this.mToken = token;
        this.serverConnection.sendUserAdd(username, token);
    }

    public void callRequest(User user) {
        Log.d(TAG, "callRequest: request call to " + user.getName());
        this.serverConnection.sendCallRequest(user, mConnectId);
    }

    public void sendMessageToUser(User user, String message) {
        Log.d(TAG, "sendMessageToUser: send message to user");
        this.messageManager.addMessage(user.getName(), Message.createSend(message));
        this.serverConnection.sendMessageToUser(user, message);
        notifyStateChange();
    }

    public void callAnswer() {
        Log.d(TAG, "callAnswer: Answer to call " + mSessionId);
        this.serverConnection.sendCallAnswer(mSessionId);
    }

    public void callPrepared() {
        Log.d(TAG, "callPrepared: prepare call " + mSessionId);
        this.serverConnection.sendCallPrepared(mSessionId);
    }

    public void callTerminate() {
        Log.d(TAG, "callTerminate: terminal call " + mSessionId);
        this.serverConnection.sendCallTerminate(mSessionId);
    }

    // endregion

    // region state management
    public interface MainServiceStateChangeListener {
        void onServerStateChange();
    }

    private List<MainServiceStateChangeListener> mainServiceStateChangeListenerList = new ArrayList<>();

    private void notifyStateChange() {
        for (MainServiceStateChangeListener l : mainServiceStateChangeListenerList)
            l.onServerStateChange();
    }

    public void subscribeStateChange(MainServiceStateChangeListener l) {
        Log.d(TAG, "subscribeStateChange: Add Subscriber " + l.getClass().getName());
        this.mainServiceStateChangeListenerList.add(l);
    }

    public void unSubscribeStateChange(MainServiceStateChangeListener l) {
        Log.d(TAG, "subscribeStateChange: Remove Subscriber " + l.getClass().getName());
        this.mainServiceStateChangeListenerList.remove(l);
    }
    // endregion

    // region ui interpolation

    private void startCallActivity() {
        Intent activityIntent = new Intent(this, CallActivity.class);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(activityIntent);
    }

    private void addNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle(title)
                .setContentText(message);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

    // endregion

    // region getters and setters

    public MainServiceStatus getStatus() {
        return mStatus;
    }

    public void setStatus(MainServiceStatus status) {
        this.mStatus = status;
    }

    public String getUser() {
        return mUser;
    }

    public String getErrorMessage() {
        return mErrorMessage;
    }

    // endregion
}
