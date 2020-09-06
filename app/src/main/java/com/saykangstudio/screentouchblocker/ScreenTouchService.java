package com.saykangstudio.screentouchblocker;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import androidx.annotation.Nullable;

public class ScreenTouchService extends Service {

    private String TAG = "seokil::ScreenTouchService";

    ScreenTouchBlockerHandler mScreenTouchBlockerHandler;

    WindowManager mWindowManager;
    LayoutInflater mInflater;
    View mView;
    ImageView mBrightButton;
    SeekBar mSeekBar;
    ImageView mLockIconBackground;
    ImageView mLockIconGuideLine;
    View LockIconTouchOutLine;
    ImageView mLockIcon;
    RelativeLayout mSTBlockerWindow;
    Rect rect;
    boolean mIsOuting;
    Interpolator mLockIconBackgroundInterpolator;
    Interpolator mBackgroundInterpolator;
    ValueAnimator mLockIconBackgroundTransitionAnimator;
    ValueAnimator mBackgroundColorAnimator;
    int alphaValue;

    int NotificationID = 1;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "ScreenTouchService onCreate()");

        mScreenTouchBlockerHandler = new ScreenTouchBlockerHandler(this);

        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = mInflater.inflate(R.layout.wlayout, null);

        mSTBlockerWindow = mView.findViewById(R.id.STBlockerWindow);

        mBrightButton = mView.findViewById(R.id.BrightBtn);
        mBrightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSeekBar.setVisibility(View.VISIBLE);
                mBrightButton.setVisibility(View.INVISIBLE);

                mScreenTouchBlockerHandler.sendEmptyMessageDelayed(ScreenTouchBlockerHandler.MSG_SHOW_BRIGHT_BUTTON, 2000);
            }
        });

        mSeekBar = mView.findViewById(R.id.BrightSeekBar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(TAG, "onProgressChanged progress = " + progress);

                alphaValue = progress;

                String color;
                if (alphaValue >= 16) {
                    color = "#" + Integer.toHexString(alphaValue) + "000000";
                } else {
                    color = "#0" + Integer.toHexString(alphaValue) + "000000";
                }
                mSTBlockerWindow.setBackgroundColor(Color.parseColor(color));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStartTrackingTouch");
                mScreenTouchBlockerHandler.removeMessages(ScreenTouchBlockerHandler.MSG_SHOW_BRIGHT_BUTTON);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStopTrackingTouch");
                mScreenTouchBlockerHandler.sendEmptyMessageDelayed(ScreenTouchBlockerHandler.MSG_SHOW_BRIGHT_BUTTON, 2000);
            }
        });
        alphaValue = mSeekBar.getProgress();

        mLockIconBackground = mView.findViewById(R.id.LockIconBackground);
        mLockIconGuideLine = mView.findViewById(R.id.LockIconGuideLine);
        mLockIcon = mView.findViewById(R.id.LockIcon);

        initLockIconBackgroundAnimator();

        mLockIconBackground.setScaleX(0f);

        rect = new Rect();

        LockIconTouchOutLine = mView.findViewById(R.id.LockIconTouchOutLine);
        LockIconTouchOutLine.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        mScreenTouchBlockerHandler.removeMessages(ScreenTouchBlockerHandler.MSG_HIDE_LOCK_GUIDELINE);
                        mLockIconGuideLine.setVisibility(View.VISIBLE);
                        LockIconTouchOutLine.getHitRect(rect);

                        initBackgroundColorAnimator(alphaValue);

                        if (mLockIconBackgroundTransitionAnimator.getAnimatedFraction() > 0) {
                            mLockIconBackgroundTransitionAnimator.reverse();
                        } else {
                            mLockIconBackgroundTransitionAnimator.start();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mLockIconBackgroundTransitionAnimator.getAnimatedFraction() > 0) {
                            mLockIconBackgroundTransitionAnimator.reverse();
                        } else {
                            mLockIconBackgroundTransitionAnimator.start();
                        }
                        mScreenTouchBlockerHandler.sendEmptyMessageDelayed(ScreenTouchBlockerHandler.MSG_HIDE_LOCK_GUIDELINE, 3000);

                        if (!rect.contains(v.getLeft() + (int)event.getX(), v.getTop() +  (int)event.getY())) {
                            toggleService();
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!rect.contains(v.getLeft() + (int)event.getX(), v.getTop() +  (int)event.getY()) && !mIsOuting) {
                            mIsOuting = true;
                            mBackgroundColorAnimator.start();
                            mLockIconBackground.setAlpha(0.4f);
                            mLockIconGuideLine.setAlpha(0.4f);
                        } else if (rect.contains(v.getLeft() + (int)event.getX(), v.getTop() +  (int)event.getY()) && mIsOuting) {
                            mIsOuting = false;
                            mBackgroundColorAnimator.reverse();
                            mLockIconBackground.setAlpha(0.6f);
                            mLockIconGuideLine.setAlpha(1f);
                        }
                        break;
                }
                return true;
            }
        });

        Notification.Builder builder = new Notification.Builder(getApplicationContext(), Utils.CHANNEL_ID)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_content))
                .setSmallIcon(R.mipmap.sct_launcher);

        startForeground(NotificationID, builder.build());
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "ScreenTouchService onDestroy()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand intent = " + intent + " flags = " + flags + " startId = " + startId);

        if (mView.getParent() == null) {
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT);

            // support cutout (notch)
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;

            mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

            mWindowManager.addView(mView, params);
        } else {
            mWindowManager.removeView(mView);

            stopSelf();
        }

        return Service.START_STICKY;
    }

    private void hideSeekBar() {
        mSeekBar.setVisibility(View.INVISIBLE);
        mBrightButton.setVisibility(View.VISIBLE);
    }

    private void hideLockGuideline() {
        mLockIconGuideLine.setVisibility(View.INVISIBLE);
    }

    private void toggleService() {
        // toggle means hide STB
        Intent notificationIntent = new Intent(getApplicationContext(), ScreenTouchService.class);
        startService(notificationIntent);
    }

    private void initLockIconBackgroundAnimator() {
        mLockIconBackgroundInterpolator = AnimationUtils.loadInterpolator(this,
                android.R.interpolator.fast_out_slow_in);
        mLockIconBackgroundTransitionAnimator = ValueAnimator.ofFloat(0f, 1f);
        mLockIconBackgroundTransitionAnimator.setDuration(300);
        mLockIconBackgroundTransitionAnimator.setInterpolator(mLockIconBackgroundInterpolator);
        mLockIconBackgroundTransitionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float)animation.getAnimatedValue();
                mLockIconBackground.setScaleX(animatedValue);
                mLockIconBackground.setScaleY(animatedValue);
            }
        });
    }

    private void initBackgroundColorAnimator(int currentAlpha) {
        mBackgroundInterpolator = AnimationUtils.loadInterpolator(this,
                android.R.interpolator.linear);

        String currentColor;
        if (currentAlpha >= 16) {
            currentColor = "#" + Integer.toHexString(currentAlpha) + "000000";
        } else {
            currentColor = "#0" + Integer.toHexString(currentAlpha) + "000000";
        }

        int afterAlpha = currentAlpha - 128;
        String afterColor;
        if (afterAlpha <= 0) {
            afterColor = "#" + "00" + "000000";
        } else if (afterAlpha < 16){
            afterColor = "#0" + Integer.toHexString(afterAlpha) + "000000";
        } else {
            afterColor = "#" + Integer.toHexString(afterAlpha) + "000000";
        }

        mBackgroundColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), Color.parseColor(currentColor), Color.parseColor(afterColor));
        mBackgroundColorAnimator.setDuration(500);
        mBackgroundColorAnimator.setInterpolator(mBackgroundInterpolator);
        mBackgroundColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mSTBlockerWindow.setBackgroundColor((int) animator.getAnimatedValue());
            }
        });
    }

    private static class ScreenTouchBlockerHandler extends Handler {
        public static final int MSG_SHOW_BRIGHT_BUTTON = 1;
        public static final int MSG_HIDE_LOCK_GUIDELINE = 2;

        private ScreenTouchService mScreenTouchService;

        ScreenTouchBlockerHandler(ScreenTouchService screenTouchService) {
            mScreenTouchService = screenTouchService;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SHOW_BRIGHT_BUTTON:
                    mScreenTouchService.hideSeekBar();
                    break;
                case MSG_HIDE_LOCK_GUIDELINE:
                    mScreenTouchService.hideLockGuideline();
                    break;
            }
        }
    }
}