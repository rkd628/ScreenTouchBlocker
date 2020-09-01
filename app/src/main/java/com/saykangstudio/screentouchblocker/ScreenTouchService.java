package com.saykangstudio.screentouchblocker;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
    ImageView mLockIconOutLine;
    View LockIconTouchOutLine;
    ImageView mLockIcon;
    RelativeLayout mSTBlockerWindow;

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

                String color;
                if (progress >= 16) {
                    color = "#" + Integer.toHexString(progress) + "000000";
                } else {
                    color = "#0" + Integer.toHexString(progress) + "000000";
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

        mLockIconBackground = mView.findViewById(R.id.LockIconBackground);
        mLockIconBackground.setColorFilter(Color.parseColor("#ffffff"));
        mLockIconBackground.setAlpha((float)0.8);

        mLockIconOutLine = mView.findViewById(R.id.LockIconOutLine);
        final Animation anim = AnimationUtils.loadAnimation(this, R.anim.scale_up);

        mLockIcon = mView.findViewById(R.id.LockIcon);

        LockIconTouchOutLine = mView.findViewById(R.id.LockIconTouchOutLine);
        LockIconTouchOutLine.setOnTouchListener(new OnSwipeTouchListener(this,
                new OnSwipeTouchListener.OnSwipeListener() {
                    @Override
                    public void onShowPress() {
                        mLockIconBackground.setVisibility(View.VISIBLE);
                        mLockIconBackground.startAnimation(anim);
                        mLockIconOutLine.setVisibility(View.VISIBLE);
                        mLockIcon.setColorFilter(Color.parseColor("#00ffaa"));

                        mScreenTouchBlockerHandler.removeMessages(ScreenTouchBlockerHandler.MSG_SHOW_LOCK_ICON_BACKGROUND);
                        mScreenTouchBlockerHandler.sendEmptyMessageDelayed(ScreenTouchBlockerHandler.MSG_SHOW_LOCK_ICON_BACKGROUND, 2000);
                    }

                    @Override
                    public void onSwipeRight() {
                        toggleService();
                    }

                    @Override
                    public void onSwipeLeft() {
                        toggleService();
                    }

                    @Override
                    public void onSwipeTop() {
                        toggleService();
                    }

                    @Override
                    public void onSwipeBottom() {
                        toggleService();
                    }
        }));

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

    private void hideLockIconBackground() {
        mLockIconBackground.setVisibility(View.INVISIBLE);
        mLockIconOutLine.setVisibility(View.INVISIBLE);
        mLockIcon.setColorFilter(Color.parseColor("#c1c1c1"));
    }

    private void toggleService() {
        // toggle means hide STB
        Intent notificationIntent = new Intent(getApplicationContext(), ScreenTouchService.class);
        startService(notificationIntent);
    }

    private static class ScreenTouchBlockerHandler extends Handler {
        public static final int MSG_SHOW_BRIGHT_BUTTON = 1;
        public static final int MSG_SHOW_LOCK_ICON_BACKGROUND = 2;

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
                case MSG_SHOW_LOCK_ICON_BACKGROUND:
                    mScreenTouchService.hideLockIconBackground();
                    break;
            }
        }
    }

    public static class OnSwipeTouchListener implements View.OnTouchListener {

        private final GestureDetector mGestureDetector;
        private OnSwipeListener mOnSwipeListener;

        public OnSwipeTouchListener(Context context, OnSwipeListener onSwipeListener) {
            mGestureDetector = new GestureDetector(context, new GestureListener());
            mOnSwipeListener = onSwipeListener;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return mGestureDetector.onTouchEvent(event);
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public void onShowPress(MotionEvent e) {
                mOnSwipeListener.onShowPress();
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                Log.d("seokil","seokil onSingleTapConfirmed");
                return super.onSingleTapConfirmed(e);
            }

            @Override
            public void onLongPress(MotionEvent e) {
                Log.d("seokil","seokil onLongPress");
                super.onLongPress(e);
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                mOnSwipeListener.onSwipeRight();
                            } else {
                                mOnSwipeListener.onSwipeLeft();

                            }
                        }
                        result = true;
                    } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            mOnSwipeListener.onSwipeBottom();
                        } else {
                            mOnSwipeListener.onSwipeTop();
                        }
                        result = true;
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return result;
            }
        }

        public interface OnSwipeListener {
            void onShowPress();
            void onSwipeRight();
            void onSwipeLeft();
            void onSwipeTop();
            void onSwipeBottom();
        }
    }
}