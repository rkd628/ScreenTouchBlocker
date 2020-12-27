package com.saykangstudio.screentouchblocker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimatedImageDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ScrollView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class QuickTileGuideActivity extends Activity {
    private String TAG = "seokil::QuickTileGuideActivity";

    private BroadcastReceiver mTileServiceBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive intent = " + intent.getStringExtra("message"));

            String message = intent.getStringExtra("message");
            if (message.equals("ADDED")) {
                QuickTileGuideActivity.this.setResult(Activity.RESULT_OK);
                QuickTileGuideActivity.this.finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quick_tile_guide_layout);

        final ImageView imageView = findViewById(R.id.GuideImage);
        AnimatedImageDrawable drawable = (AnimatedImageDrawable) imageView.getDrawable();
        drawable.start();

        final ImageView imageView1 = findViewById(R.id.GuideImage1);
        AnimatedImageDrawable drawable1 = (AnimatedImageDrawable) imageView1.getDrawable();
        drawable1.start();

        final ImageView imageView2 = findViewById(R.id.GuideImage2);
        AnimatedImageDrawable drawable2 = (AnimatedImageDrawable) imageView2.getDrawable();
        drawable2.start();

        final HorizontalScrollView scrollView = findViewById(R.id.QuickTileGuideHorizontalScrollView);
        scrollView.post(new Runnable() {
            @Override public void run() {

                scrollView.fullScroll(ScrollView.FOCUS_RIGHT);
//                scrollView.smoothScrollTo(500, 100);
            }
        });

        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//                Log.d(TAG, "onScrollChange v = " + v + ", scrollX = " + scrollX
//                        + ", scrollY = " + scrollY + ", oldScrollX = "
//                        + oldScrollX + ", oldScrollY = " + oldScrollY);
            }
        });

        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d(TAG, "ACTION_DOWN = " + v.getScrollX());
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d(TAG, "ACTION_UP = " + v.getScrollX());
                        break;
                }
                return false;
            }
        });



        LocalBroadcastManager.getInstance(this).registerReceiver(
                mTileServiceBroadcastReceiver, new IntentFilter(Utils.ACTION_QUICK_TILE_SETTING));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult requestCode = " + requestCode + " resultCode = " + resultCode);
    }
}
