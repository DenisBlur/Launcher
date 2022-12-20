package com.gibbonstudio.myapplication.Overlays;

import static com.gibbonstudio.myapplication.MainActivity.objectVariables;
import static com.gibbonstudio.myapplication.MainActivity.serviceStarter;
import static com.gibbonstudio.myapplication.ObjectVariables.NOTIFICATION_LIST;
import static com.gibbonstudio.myapplication.ObjectVariables.NOTIFICATION_POST;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.gibbonstudio.myapplication.NLService;
import com.gibbonstudio.myapplication.R;
import com.gibbonstudio.myapplication.ScreenUtils;
import com.gibbonstudio.myapplication.Views.Apps.AllAppsDrawer;
import com.gibbonstudio.myapplication.Views.MediaControl.MediaPlayerView;
import com.google.android.material.card.MaterialCardView;

public class Overlay extends Service implements View.OnClickListener {

    public static boolean isRunning;
    private RelativeLayout topView;
    private WindowManager windowManager;
    private MediaPlayerView mediaPlayerView;
    private AllAppsDrawer adDrawer;

    NotificationReceiver notificationReceiver = new NotificationReceiver();

    Runnable runnable = new Runnable() {
        public void run() {
            synchronized (this) {
                try {
                    wait(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (NLService.isWork) {
                if (objectVariables.getPlayerSbn() == null) {
                    Intent i = new Intent(NOTIFICATION_LIST);
                    i.putExtra("command", "list");
                    sendBroadcast(i);
                }
            }
        }
    };

    Thread thread = new Thread(runnable);

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
    }

    @SuppressLint({"RtlHardcoded", "InflateParams"})
    private void initViews() {

        Context context = this;
        context.setTheme(com.google.android.material.R.style.Theme_MaterialComponents_DayNight);

        topView = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.overlay_main, null);
        WindowManager.LayoutParams topParams = new WindowManager.LayoutParams(
                ScreenUtils.width,
                ScreenUtils.height,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        topParams.x = 0;
        topParams.y = 0;
        topParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
        windowManager.addView(topView, topParams);

        FrameLayout flCloseOverlay = topView.findViewById(R.id.flCloseOverlay);
        mediaPlayerView = topView.findViewById(R.id.mpvMain);
        adDrawer = topView.findViewById(R.id.adDrawer);
        mediaPlayerView.bindToTrack();
        flCloseOverlay.setVisibility(View.VISIBLE);

        flCloseOverlay.setOnClickListener(v -> {

            initAnimation(true);

        });

        initReceivers();
        initAnimation(false);
    }

    void initAnimation(boolean revers) {

        RelativeLayout llAudioControl = topView.findViewById(R.id.llAudioControl);
        llAudioControl.setY(ScreenUtils.height);

        ValueAnimator buttonAnimation = ValueAnimator.ofFloat(ScreenUtils.height, 0);
        buttonAnimation.addUpdateListener(valueAnimator -> {
            adDrawer.setY((Float) valueAnimator.getAnimatedValue());
        });
        buttonAnimation.setInterpolator(new FastOutSlowInInterpolator());
        buttonAnimation.setDuration(650);

        ValueAnimator mediaAnimation = ValueAnimator.ofFloat(ScreenUtils.height, 0);
        mediaAnimation.addUpdateListener(valueAnimator -> {
            llAudioControl.setY((Float) valueAnimator.getAnimatedValue());
        });
        mediaAnimation.setInterpolator(new FastOutSlowInInterpolator());
        mediaAnimation.setDuration(650);
        mediaAnimation.setStartDelay(250);

        if(revers) {

            mediaAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    stopSelf();
                    startService(serviceStarter);
                }
            });

            buttonAnimation.reverse();
            mediaAnimation.reverse();
        } else {
            buttonAnimation.start();
            mediaAnimation.start();
        }
    }

    void initReceivers() {

        IntentFilter notificationIntentFilter = new IntentFilter();
        notificationIntentFilter.addAction(NOTIFICATION_POST);
        registerReceiver(notificationReceiver, notificationIntentFilter);

        thread.start();
    }

    private void initScreenUtils() {
        final Display display = windowManager.getDefaultDisplay();
        int statusBarHeight = 0;
        @SuppressLint({"InternalInsetResource", "DiscouragedApi"}) int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        ScreenUtils.width = display.getWidth();
        ScreenUtils.height = display.getHeight() - statusBarHeight;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btnNotificationPrev:
                try {
                    objectVariables.getPlayerSbn().getNotification().actions[1].actionIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btnNotificationPlay:
                try {
                    objectVariables.getPlayerSbn().getNotification().actions[2].actionIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btnNotificationNext:
                try {
                    objectVariables.getPlayerSbn().getNotification().actions[3].actionIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.flStart:
                int height = ScreenUtils.height;

                break;
        }

    }

    public class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (intent.getStringExtra("player").equals("YANDEX_POST")) {
                    mediaPlayerView.bindToTrack();
                } else {
                    mediaPlayerView.showMediaContent(false);

                }
            }
        }
    }

}
