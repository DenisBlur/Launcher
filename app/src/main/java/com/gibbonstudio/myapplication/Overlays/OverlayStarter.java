package com.gibbonstudio.myapplication.Overlays;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import static com.gibbonstudio.myapplication.MainActivity.serviceOverlay;
import static com.gibbonstudio.myapplication.ObjectVariables.posHeightAppBar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.gibbonstudio.myapplication.R;
import com.gibbonstudio.myapplication.ScreenUtils;
import com.google.android.material.card.MaterialCardView;

public class OverlayStarter extends Service {

    public static boolean isRunning;
    private WindowManager.LayoutParams topParams;
    private RelativeLayout topView;
    private WindowManager windowManager;
    private View edge;

    int mathHeight = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        isRunning = true;

        initScreenUtils();
        initViews();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        if (topView != null) windowManager.removeView(topView);
        if (edge != null) windowManager.removeView(edge);
    }

    @SuppressLint({"RtlHardcoded", "InflateParams", "ClickableViewAccessibility"})
    private void initViews() {

        Context context = this;
        context.setTheme(com.google.android.material.R.style.Theme_MaterialComponents_DayNight);

        topView = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.overlay_starter, null);
        topParams = new WindowManager.LayoutParams(
                ScreenUtils.width,
                ScreenUtils.convertDpToPx(context, 16),
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        topParams.x = 0;
        topParams.y = 0;
        topParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
        windowManager.addView(topView, topParams);

        MaterialCardView mcvStartOverlay = topView.findViewById(R.id.mcvStartOverlay);

        ValueAnimator buttonAnimation = ValueAnimator.ofFloat(0, 1);
        buttonAnimation.addUpdateListener(valueAnimator -> {
            mcvStartOverlay.setScaleY((Float) valueAnimator.getAnimatedValue());
        });
        buttonAnimation.setInterpolator(new FastOutSlowInInterpolator());
        buttonAnimation.setDuration(650);
        buttonAnimation.start();
        mcvStartOverlay.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    return true;
                case MotionEvent.ACTION_MOVE:
                    mathHeight = (int) ((ScreenUtils.height - event.getRawY()));
                    if (mathHeight >= ScreenUtils.convertDpToPx(context, posHeightAppBar)) {
                        mcvStartOverlay.setOnTouchListener(null);
                        buttonAnimation.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                stopSelf();
                                startService(serviceOverlay);
                            }
                        });
                        buttonAnimation.reverse();
                        return true;
                    } else {
                        topParams.height = (int) Math.max(mathHeight, ScreenUtils.convertDpToPx(context, 16));
                        windowManager.updateViewLayout(topView, topParams);
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    if (mathHeight < ScreenUtils.convertDpToPx(context, posHeightAppBar)) {
                        topParams.height = ScreenUtils.convertDpToPx(context, 16);
                        windowManager.updateViewLayout(topView, topParams);
                        return true;
                    }
                    return true;
            }
            return true;
        });
        mcvStartOverlay.setOnClickListener(
                view -> {

                }
        );
    }

    private void initScreenUtils() {
        final Display display = windowManager.getDefaultDisplay();
        int statusBarHeight = 0;
        @SuppressLint("InternalInsetResource") int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        ScreenUtils.width = display.getWidth();
        ScreenUtils.height = display.getHeight() - statusBarHeight;
    }

    public static class ChangeNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }

}
