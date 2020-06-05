package com.ideaxen.hr.ideasms.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;

public class AutoLocatorService extends Service {

    private Handler mHandler = new Handler();
    long notify_interval = 60000 * 10;
    public static final String BROADCAST_ACTION = "com.ideaxen.broadcastreceiver";

    public AutoLocatorService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Timer mTimer = new Timer();
        mTimer.schedule(new TimerTaskToGetLocation(), 0, notify_interval);
    }


    private class TimerTaskToGetLocation extends TimerTask{
        @Override
        public void run() {

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent();
                    intent.setAction(BROADCAST_ACTION);
                    intent.putExtra("auto","AUTO");
                    sendBroadcast(intent);
                    stopSelf();
                }
            });

        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
