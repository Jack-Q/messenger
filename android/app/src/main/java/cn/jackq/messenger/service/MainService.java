package cn.jackq.messenger.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import cn.jackq.messenger.audio.MessengerAudio;
import cn.jackq.messenger.network.PeerTransmission;
import cn.jackq.messenger.network.ServerConnection;
import cn.jackq.messenger.ui.CallActivity;

/**
 * Created on: 5/5/17.
 * Creator: Jack Q <qiaobo@outlook.com>
 */

public class MainService extends Service implements MessengerAudio.MessengerAudioListener, ServerConnection.ServerConnectionListener, PeerTransmission.PeerTransmissionListener {
    private static final String TAG = "MainService";

    @Override
    public void onPackageReceived(byte[] data, int size) {

    }

    @Override
    public void onError() {

    }

    @Override
    public void onServerConnected(String string) {

    }

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


    @Override
    public void onSendAudioFrame(ByteBuffer audioFrame) {
    }

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
        Log.d(TAG, "onCreate: create main service repeat");
        try {
            this.audio.init();
        }catch (Exception e){
            Log.e(TAG, "onCreate: Audio Exception", e);
        }
        Log.d(TAG, "onCreate: created");
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

    public void connectToServer(String host, int port) {
        Log.d(TAG, "connectToServer: Connect to server on response to client");
        this.serverConnection.connect(host, port);
    }

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

    private void startCallActivity() {
        Intent activityIntent = new Intent(this, CallActivity.class);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(activityIntent);
    }
}
