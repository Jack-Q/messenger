package cn.jackq.messenger.ui;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import cn.jackq.messenger.service.MainService;

/**
 * Created on: 5/6/17.
 * Creator: Jack Q <qiaobo@outlook.com>
 */

public abstract class AbstractMessengerActivity extends AppCompatActivity implements MainService.MainServiceStateChangeListener {
    private static final String TAG = "AbstractMessengerActivi";
    private static final String mIsServiceBoundKey = "IS_SERVICE_BOUND";
    private boolean mIsServiceBound = false;
    private MainService mMainService = null;
    private ServiceConnection mMainServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMainService = ((MainService.MainServiceBinder) service).getService();
            mMainService.subscribeStateChange(AbstractMessengerActivity.this);
            mIsServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMainService.unSubscribeStateChange(AbstractMessengerActivity.this);
            mMainService = null;
            mIsServiceBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if(!isMyServiceRunning(MainService.class)){
            Log.d(TAG, "onStart: start service");
            Intent intent = new Intent(this, MainService.class);
            startService(intent);
        }
        Intent intent = new Intent(this, MainService.class);
        bindService(intent, mMainServiceConnection, BIND_IMPORTANT);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mIsServiceBound){
            mIsServiceBound = false;
            mMainService.unSubscribeStateChange(this);
            unbindService(this.mMainServiceConnection);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(mIsServiceBoundKey, this.mIsServiceBound);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.mIsServiceBound = savedInstanceState.getBoolean(mIsServiceBoundKey, false);
    }

    @Override
    public void onServerStateChange() {
        Log.d(TAG, "onServerStateChange: Service State Change");
    }

    protected MainService getMainService(){
        return mMainService;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
