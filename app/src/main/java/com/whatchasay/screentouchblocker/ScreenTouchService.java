package com.whatchasay.screentouchblocker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by seokil on 18. 9. 27.
 */

public class ScreenTouchService extends Service {

    private String TAG = "ScreenTouchService";

    WindowManager mWm;
    LayoutInflater mInflater;
    View mView;

    private String CHANNEL_ID = "channel_id";
    private int flag;
    private int fromNotiFlag;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        // The service is being created
        mWm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = mInflater.inflate(R.layout.wlayout, null);
        flag = 1;
        fromNotiFlag = 1;

        Intent notificationIntent = new Intent(getApplicationContext(), ScreenTouchService.class);
        notificationIntent.putExtra("fromNotification", true);
        PendingIntent pendingIntent = PendingIntent.getForegroundService(getApplicationContext(), 0, notificationIntent, 0 );
        Notification.Builder builder = new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle("MYTEST_TITLE")
                .setContentText("MYTEST_TEXT")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent);

        startForeground(1, builder.build());
    }

    @Override
    public void onDestroy() {
        // The service is no longer used and is being destroyed
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()

        boolean fromNotification = intent.getBooleanExtra("fromNotification",false);
        if (fromNotification) {
            if (fromNotiFlag == 1) {
                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,//MATCH_PARENT
                        WindowManager.LayoutParams.MATCH_PARENT,//MATCH_PARENT
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
                fromNotiFlag = 0;
                mWm.addView(mView, params);
            } else {
                fromNotiFlag = 1;
                mWm.removeView(mView);
            }
        } else {
            if (flag == 1) {
                flag = 0;
            } else {
                stopSelf();
            }
        }
        return Service.START_STICKY;
    }
}
