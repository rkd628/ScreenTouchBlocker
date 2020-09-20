package com.saykangstudio.screentouchblocker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class QuickTileGuideActivity extends Activity {
    private String TAG = "seokil::QuickTileGuideActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quick_tile_guide_layout);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult requestCode = " + requestCode + " resultCode = " + resultCode);
    }
}
