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

    private String TAG = "seokil::ScreenTouchService";

    WindowManager mWindowManager;
    LayoutInflater mInflater;
    View mView;

    private String CHANNEL_ID = "channel_id";
    private int NotificationID = 1;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "ScreenTouchService onCreate()");

        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = mInflater.inflate(R.layout.wlayout, null);

        Intent notificationIntent = new Intent(getApplicationContext(), ScreenTouchService.class);
        notificationIntent.putExtra("fromNotification", true);

        PendingIntent pendingIntent = PendingIntent.getForegroundService(getApplicationContext(), 0, notificationIntent, 0 );
        Notification.Builder builder = new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_content))
                .setSmallIcon(R.mipmap.sct_launcher)
                .setContentIntent(pendingIntent);

        startForeground(NotificationID, builder.build());
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "ScreenTouchService onDestroy()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand intent = " + intent + " flags = " + flags + " startId = " + startId);

        boolean fromNotification = intent.getBooleanExtra("fromNotification",false);
        boolean fromButton = intent.getBooleanExtra("fromButton", false);

        if (fromNotification) {
            Log.d(TAG, "fromNotification");

            boolean viewExist = (mView.getParent() != null);
            Log.d(TAG, "viewExist = " + viewExist);

            if (viewExist) {
                mWindowManager.removeView(mView);
            } else {
                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);

                mWindowManager.addView(mView, params);
            }
        } else if (fromButton) {
            Log.d(TAG, "fromButton");

            boolean turnsOn = intent.getBooleanExtra("turnsOn", false);
            if (!turnsOn) {
                stopSelf();
            }
        }
        return Service.START_STICKY;
    }
}
