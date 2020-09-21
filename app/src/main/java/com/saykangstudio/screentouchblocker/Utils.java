package com.saykangstudio.screentouchblocker;

import android.content.Context;
import android.content.SharedPreferences;

public class Utils {
    static String CHANNEL_ID = "stbForegroundChannel";
    static String KEY_TILE_ADDED = "TileAdded";

    static String ACTION_QUICK_TILE_SETTING = "com.saykangstudio.screentouchblocker.intent.ACTION_QUICK_TILE_SETTING";

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
    }

    public static SharedPreferences getSharedPreferences(Context context, String fileName) {
        return context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
    }
}
