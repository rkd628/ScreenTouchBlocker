package com.whatchasay.screentouchblocker;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Created by seokil on 20. 2. 29.
 */

public class ScreenTouchBlockerTileService extends TileService {

    private String TAG = "seokil::ScreenTouchService";

    boolean flag;

    private String CHANNEL_ID = "channel_id";
    private int NotificationID = 1;


    @Override
    public void onCreate() {
        Log.d(TAG,"onCreate()");
        flag = false;
    }

    @Override
    public void onClick() {
        Log.d(TAG,"onClick()");

        Intent notificationIntent = new Intent(getApplicationContext(), ScreenTouchService.class);
        notificationIntent.putExtra("fromQuickPanel", true);
        boolean turnsOn = !isScreenTouchServiceRunning();
        notificationIntent.putExtra("turnsOn", turnsOn);

        startForegroundService(notificationIntent);

        // Reflection code.
        try {
            Object service  = getSystemService("statusbar");
            Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
            Method collapse = statusbarManager.getMethod("collapsePanels");
            collapse.setAccessible(true);
            collapse.invoke(service);
        } catch(Exception ex) {
            Log.e(TAG,"failed collapsePanels reflection");
            ex.printStackTrace();
        }

        updateTile();
    }

    public void onTileAdded() {
        Log.d(TAG,"onTileAdded");
    }

    public void onTileRemoved() {
        Log.d(TAG,"onTileRemoved");
    }

    public void onStartListening() {
        Log.d(TAG,"onStartListening");
        updateTile();
    }

    public void onStopListening() {
        Log.d(TAG,"onStopListening");
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

    private void updateTile() {
        Tile tile = getQsTile();
        if (isScreenTouchServiceRunning()) {
            tile.setState(Tile.STATE_ACTIVE);
        } else {
            tile.setState(Tile.STATE_INACTIVE);
        }
        tile.updateTile();
    }

}
