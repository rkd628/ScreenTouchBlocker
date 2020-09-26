package com.saykangstudio.screentouchblocker;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimatedImageDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

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
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mTileServiceBroadcastReceiver, new IntentFilter(Utils.ACTION_QUICK_TILE_SETTING));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult requestCode = " + requestCode + " resultCode = " + resultCode);
    }
}
