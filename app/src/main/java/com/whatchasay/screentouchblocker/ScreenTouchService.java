package com.whatchasay.screentouchblocker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by seokil on 18. 9. 27.
 */

public class ScreenTouchService extends Service {

    int flag;
    WindowManager mWm;
    LayoutInflater mInflater;
    View mView;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        // The service is being created
        mWm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = mInflater.inflate(R.layout.wlayout, null);
        flag = 1;
    }

    @Override
    public void onDestroy() {
        // The service is no longer used and is being destroyed
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()

        if(flag == 1) {
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,//MATCH_PARENT
                    WindowManager.LayoutParams.WRAP_CONTENT,//MATCH_PARENT
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
            mWm.addView(mView, params);
            flag = 0;
        } else {
            mWm.removeView(mView);
            stopSelf();
        }

        return Service.START_STICKY;
    }



}
