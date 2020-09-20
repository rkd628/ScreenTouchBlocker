package com.saykangstudio.screentouchblocker;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import java.lang.reflect.Method;

public class ScreenTouchBlockerTileService extends TileService {
    public final String TAG = "seokil::ScreenTouchBlockerTileService";

    @Override
    public void onCreate() {
        Log.d(TAG,"onCreate()");
    }

    @Override
    public void onClick() {
        Log.d(TAG,"onClick()");

        Intent notificationIntent = new Intent(this, ScreenTouchService.class);
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

    @Override
    public void onTileAdded() {
        Log.d(TAG,"onTileAdded");
        SharedPreferences prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        prefs.edit().putBoolean("TileAdded", true).apply();
    }

    @Override
    public void onTileRemoved() {
        Log.d(TAG,"onTileRemoved");
        SharedPreferences prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        prefs.edit().putBoolean("TileAdded", false).apply();
    }

    @Override
    public void onStartListening() {
        Log.d(TAG,"onStartListening");
        updateTile();
    }

    @Override
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

