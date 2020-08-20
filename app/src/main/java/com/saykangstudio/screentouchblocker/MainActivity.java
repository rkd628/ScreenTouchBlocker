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

public class MainActivity extends Activity {

    private String TAG = "seokil::MainActivity";

    NotificationManager notificationManager;

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
