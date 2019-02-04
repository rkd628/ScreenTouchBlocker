package com.whatchasay.screentouchblocker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    NotificationManager notificationManager;
    String CHANNEL_ID = "channel_id";
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

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        createNotificationChannel();

        btn = (Button)findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent notificationIntent = new Intent(getApplicationContext(), ScreenTouchService.class);
                startForegroundService(notificationIntent);
            }
        });
    }

    private void createNotificationChannel() {
        CharSequence name = getString(R.string.channel_name);
        String description = getString(R.string.channel_description);

        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.setDescription(description);

        notificationManager.createNotificationChannel(notificationChannel);
    }
}
