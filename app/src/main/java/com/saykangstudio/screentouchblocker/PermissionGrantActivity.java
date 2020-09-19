package com.saykangstudio.screentouchblocker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class PermissionGrantActivity extends Activity {

    private String TAG = "seokil::PermissionGrantActivity";

    Button mAllowButton;

    final int MANAGE_OVERLAY_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.permission_grant_layout);

        mAllowButton = findViewById(R.id.AllowButton);
        mAllowButton.setOnClickListener(new View.OnClickListener() {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult requestCode = " + requestCode + " resultCode = " + resultCode);

        switch(requestCode) {
            case MANAGE_OVERLAY_PERMISSION:
                if (Settings.canDrawOverlays(this)) {
                    finish();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Please allow permission!")
                            .setMessage("If you not allow permission, you can't use Screen-Touch-Blocker. We don't misuse this permissions. Please allow permission!")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Do nothing
                                }})
                            .show();
                }
                break;
        }
    }
}
