package cn.jackq.messenger.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

import cn.jackq.messenger.service.MainService;

/**
 * Created on: 5/6/17.
 * Creator: Jack Q <qiaobo@outlook.com>
 */

public abstract class AbstractMessengerActivity extends AppCompatActivity {
    private static final String mIsServiceBoundKey = "IS_SERVICE_BOUND";
    private boolean mIsServiceBound = false;
    private MainService mMainService = null;
    private ServiceConnection mMainServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMainService = ((MainService.MainServiceBinder) service).getService();
            mIsServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsServiceBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MainService.class);
        bindService(intent, mMainServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mIsServiceBound){
            unbindService(this.mMainServiceConnection);
            mIsServiceBound = false;
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
}
