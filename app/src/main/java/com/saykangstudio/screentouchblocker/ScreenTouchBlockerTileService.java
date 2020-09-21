package com.saykangstudio.screentouchblocker;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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
        boolean result = Utils.getSharedPreferences(this).edit().putBoolean(Utils.KEY_TILE_ADDED, true).commit();
        Log.d(TAG,"onTileAdded result = " + result);
        sendMessage("ADDED");
    }

    @Override
    public void onTileRemoved() {
        boolean result = Utils.getSharedPreferences(this).edit().putBoolean(Utils.KEY_TILE_ADDED, false).commit();
        Log.d(TAG,"onTileRemoved result = " + result);
        sendMessage("REMOVED");
    }

    private void sendMessage(String message) {
        Intent intent = new Intent(Utils.ACTION_QUICK_TILE_SETTING);
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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

