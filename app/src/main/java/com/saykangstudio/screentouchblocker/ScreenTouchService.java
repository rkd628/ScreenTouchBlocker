package com.saykangstudio.screentouchblocker;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class ScreenTouchService extends Service {

    private String TAG = "seokil::ScreenTouchService";

    WindowManager mWindowManager;
    LayoutInflater mInflater;
    View mView;
    ImageView mBrightButton;
    SeekBar mSeekBar;
    STBHandler mSTBHandler;

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

        mBrightButton = mView.findViewById(R.id.BrightBtn);
        mBrightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSeekBar.setVisibility(View.VISIBLE);
                mBrightButton.setVisibility(View.INVISIBLE);

                mSTBHandler.sendEmptyMessageDelayed(STBHandler.MSG_SHOW_BRIGHT_BUTTON, 2000);
            }
        });

        mSeekBar = mView.findViewById(R.id.BrightSeekBar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(TAG,"onProgressChanged progress = " + progress);

                String i = "#"+Integer.toHexString(progress) + "000000";
                RelativeLayout layout = mView.findViewById(R.id.STBlockerWindow);
                layout.setBackgroundColor(Color.parseColor(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d(TAG,"onStartTrackingTouch");
                mSTBHandler.removeMessages(STBHandler.MSG_SHOW_BRIGHT_BUTTON);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG,"onStopTrackingTouch");
                mSTBHandler.sendEmptyMessageDelayed(STBHandler.MSG_SHOW_BRIGHT_BUTTON, 2000);
            }
        });

        mSTBHandler = new STBHandler(mBrightButton, mSeekBar);


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
        boolean fromQuickPanel = intent.getBooleanExtra("fromQuickPanel", false);

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
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                        PixelFormat.TRANSLUCENT);


                mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

                mWindowManager.addView(mView, params);
            }
        } else if (fromButton) {
            Log.d(TAG, "fromButton");

            boolean turnsOn = intent.getBooleanExtra("turnsOn", false);
            if (!turnsOn) {
                stopSelf();
            }
        } else if (fromQuickPanel) {
            Log.d(TAG, "fromQuickPanel");

            boolean turnsOn = intent.getBooleanExtra("turnsOn", false);
            if (turnsOn) {
                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                        PixelFormat.TRANSLUCENT);

                // support cutout (notch)
                params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;

                mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

                mWindowManager.addView(mView, params);
            } else {
                mWindowManager.removeView(mView);

                stopSelf();
            }
        }
        return Service.START_STICKY;
    }

    private static class STBHandler extends Handler {
        public static final int MSG_SHOW_BRIGHT_BUTTON = 1;

        private ImageView mBrightButton;
        private SeekBar mSeekBar;

        STBHandler(ImageView imageView, SeekBar seekBar) {
            mBrightButton = imageView;
            mSeekBar = seekBar;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SHOW_BRIGHT_BUTTON:
                    mSeekBar.setVisibility(View.INVISIBLE);
                    mBrightButton.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }
}

