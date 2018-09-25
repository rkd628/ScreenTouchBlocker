package com.whatchasay.screentouchblocker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            finish();
            return;
        }

//        WindowManager mWm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
//        LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View mView = mInflater.inflate(R.layout.wlayout, null);
//
//        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
//                WindowManager.LayoutParams.WRAP_CONTENT,//MATCH_PARENT
//                WindowManager.LayoutParams.WRAP_CONTENT,//MATCH_PARENT
//                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
//                 WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
//                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//                PixelFormat.TRANSLUCENT);
//        mWm.addView(mView,params);

        btn = (Button)findViewById(R.id.btn);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationChannel notificationChannel = new NotificationChannel("channel_id", "channel_name", NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.setDescription("channel description");
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.GREEN);
                notificationChannel.enableVibration(true);
                notificationChannel.setVibrationPattern(new long[]{100, 200, 100, 200});
                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                notificationManager.createNotificationChannel(notificationChannel);


                Notification.Builder builder = new Notification.Builder(getApplicationContext(), "channel_id")
                        .setContentTitle("MYTEST_TITLE")
                        .setContentText("MYTEST_TEXT")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setAutoCancel(true);

                notificationManager.notify(0,builder.build());
            }
        });


    }
}
