package com.saykangstudio.screentouchblocker;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    private String TAG = "seokil::MainActivity";

    NotificationManager notificationManager;

    final int MANAGE_OVERLAY_PERMISSION = 1;
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();

        btn = findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Settings.canDrawOverlays(getApplicationContext())) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, MANAGE_OVERLAY_PERMISSION);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Settings.canDrawOverlays(this)) {
            btn.setEnabled(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult requestCode = " + requestCode + " resultCode = " + resultCode);

        switch(requestCode) {
            case MANAGE_OVERLAY_PERMISSION:
//                if (Settings.canDrawOverlays(this)) {
//                    btn.setEnabled(false);
//                }
                break;
        }
    }

    private void createNotificationChannel() {
        CharSequence name = getString(R.string.channel_name);
        String description = getString(R.string.channel_description);

        NotificationChannel notificationChannel = new NotificationChannel(Utils.CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW);
        notificationChannel.setDescription(description);
        notificationChannel.setShowBadge(false);

        notificationManager.createNotificationChannel(notificationChannel);
    }

    private boolean isScreenTouchServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (ScreenTouchService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
