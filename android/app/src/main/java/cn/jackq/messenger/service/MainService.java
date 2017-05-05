package cn.jackq.messenger.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created on: 5/5/17.
 * Creator: Jack Q <qiaobo@outlook.com>
 */

public class MainService extends Service {
    private static final String TAG = "MainService";

    public class MainServiceBinder extends Binder{
        public MainService getService(){
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
}
