package com.gibbonstudio.myapplication.Overlays;

import static android.widget.Toast.LENGTH_SHORT;

import static com.gibbonstudio.myapplication.MainActivity.objectVariables;
import static com.gibbonstudio.myapplication.ObjectVariables.NOTIFICATION_LIST;
import static com.gibbonstudio.myapplication.ObjectVariables.NOTIFICATION_POST;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.gibbonstudio.myapplication.MainActivity;
import com.gibbonstudio.myapplication.NLService;
import com.gibbonstudio.myapplication.ObjectVariables;
import com.gibbonstudio.myapplication.R;
import com.gibbonstudio.myapplication.ScreenUtils;
import com.google.android.material.card.MaterialCardView;

public class Overlay extends Service implements View.OnClickListener {

    public static boolean isRunning;
    private WindowManager.LayoutParams topParams;
    private RelativeLayout topView;
    private WindowManager windowManager;
    private View edge;

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
        if (edge != null) windowManager.removeView(edge);
    }

    @SuppressLint("RtlHardcoded")
    private void initViews() {

        Context context = this;
        context.setTheme(com.google.android.material.R.style.Theme_MaterialComponents_DayNight);

        topView = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.overlay_main, null);
        topParams = new WindowManager.LayoutParams(
                ScreenUtils.width,
                ScreenUtils.height,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        topParams.x = 0;
        topParams.y = 0;
        topParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
        windowManager.addView(topView, topParams);

        edge = new View(getApplicationContext());
        WindowManager.LayoutParams edgeParams = new WindowManager.LayoutParams(
                ScreenUtils.width / 20,
                ScreenUtils.height,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        windowManager.addView(edge, edgeParams);
        animFirstPreview();
        initMediaPlayer();
    }

    void initReceivers() {

        IntentFilter notificationIntentFilter = new IntentFilter();
        notificationIntentFilter.addAction(NOTIFICATION_POST);
        registerReceiver(notificationReceiver, notificationIntentFilter);

        thread.start();
    }

    void initMediaContent(boolean show) {

        Log.e("SHOW_HIDE_LOG", "MEDIA_CONTENT");

        LinearLayout playerContent = topView.findViewById(R.id.playerContent);
        LinearLayout playerList = topView.findViewById(R.id.playerList);

        ImageView imgBackground = topView.findViewById(R.id.imgBackground);

        MaterialCardView mcvBackdrop = topView.findViewById(R.id.mcvBackdrop);

        if (show && playerContent.getVisibility() == View.GONE) {
            playerContent.setVisibility(View.VISIBLE);
            imgBackground.setVisibility(View.VISIBLE);
            mcvBackdrop.setVisibility(View.VISIBLE);
            playerList.setVisibility(View.GONE);

            ValueAnimator playerContentAnimation = ValueAnimator.ofFloat(-500, ScreenUtils.convertDpToPx(this, 16));
            playerContentAnimation.addUpdateListener(valueAnimator -> {
                playerContent.setX((Float) valueAnimator.getAnimatedValue());

            });
            playerContentAnimation.setInterpolator(new FastOutSlowInInterpolator());
            playerContentAnimation.setDuration(350);
            playerContentAnimation.start();

            ValueAnimator playerListAnimation = ValueAnimator.ofFloat(ScreenUtils.convertDpToPx(this, 16), 500);
            playerListAnimation.addUpdateListener(valueAnimator -> {
                playerList.setX((Float) valueAnimator.getAnimatedValue());
            });

        } else if(!show && playerContent.getVisibility() == View.VISIBLE) {
            playerContent.setVisibility(View.GONE);
            imgBackground.setVisibility(View.GONE);
            mcvBackdrop.setVisibility(View.GONE);
            playerList.setVisibility(View.VISIBLE);

            ValueAnimator playerContentAnimation = ValueAnimator.ofFloat(ScreenUtils.convertDpToPx(this, 16), 500);
            playerContentAnimation.addUpdateListener(valueAnimator -> {
                playerContent.setX((Float) valueAnimator.getAnimatedValue());

            });
            playerContentAnimation.setInterpolator(new FastOutSlowInInterpolator());
            playerContentAnimation.setDuration(350);
            playerContentAnimation.start();

            ValueAnimator playerListAnimation = ValueAnimator.ofFloat(500, ScreenUtils.convertDpToPx(this, 16));
            playerListAnimation.addUpdateListener(valueAnimator -> {
                playerList.setX((Float) valueAnimator.getAnimatedValue());
            });
            playerListAnimation.setInterpolator(new FastOutSlowInInterpolator());
            playerListAnimation.setDuration(350);
            playerListAnimation.start();
        }
    }

    private void initMediaPlayer() {
        if (ObjectVariables.getInstance().getPlayerSbn() != null) {

            TextView artistText = topView.findViewById(R.id.artistText);
            TextView trackText = topView.findViewById(R.id.trackText);

            ImageView trackImage = topView.findViewById(R.id.trackImage);
            ImageView imageNotificationPlay = topView.findViewById(R.id.imageNotificationPlay);
            ImageView imageNotificationPrev = topView.findViewById(R.id.imageNotificationPrev);
            ImageView imgBackground = topView.findViewById(R.id.imgBackground);

            MaterialCardView btnNotificationPrev = topView.findViewById(R.id.btnNotificationPrev);
            MaterialCardView btnNotificationPlay = topView.findViewById(R.id.btnNotificationPlay);
            MaterialCardView btnNotificationNext = topView.findViewById(R.id.btnNotificationNext);

            artistText.setText(objectVariables.getPlayerSbn().getNotification().extras.getString("android.text"));
            trackText.setText(objectVariables.getPlayerSbn().getNotification().extras.getString("android.title"));
            trackImage.setImageIcon(objectVariables.getPlayerSbn().getNotification().getLargeIcon());
            imgBackground.setImageIcon(objectVariables.getPlayerSbn().getNotification().getLargeIcon());

            btnNotificationPrev.setOnClickListener(this);
            btnNotificationPlay.setOnClickListener(this);
            btnNotificationNext.setOnClickListener(this);

            btnNotificationPrev.setEnabled(!objectVariables.getPlayerSbn().getNotification().actions[1].title.equals("PreviousBlocked"));
            imageNotificationPrev.setImageResource(!objectVariables.getPlayerSbn().getNotification().actions[1].title.equals("PreviousBlocked") ? R.drawable.skip_previous : R.drawable.block);
            imageNotificationPlay.setImageResource(objectVariables.getPlayerSbn().getNotification().actions[2].title.equals("Play") ? R.drawable.play_arrow : R.drawable.stop);

            initMediaContent(true);
            thread.interrupt();

        } else {
            Toast.makeText(this, "Empty", LENGTH_SHORT).show();
        }
    }

    private void animFirstPreview() {

        MaterialCardView mcvAppBar = topView.findViewById(R.id.mcvAppBar);
        MaterialCardView mcvMusicPlayer = topView.findViewById(R.id.musicPlayer);

        ValueAnimator showAppBar = ValueAnimator.ofFloat(ScreenUtils.height + 250, ScreenUtils.height - ScreenUtils.convertDpToPx(this, ObjectVariables.posHeightAppBar));
        showAppBar.addUpdateListener(valueAnimator -> mcvAppBar.setY((Float) valueAnimator.getAnimatedValue()));
        showAppBar.setInterpolator(new FastOutSlowInInterpolator());
        showAppBar.setDuration(650);
        showAppBar.start();

        ValueAnimator showMusicPlayer = ValueAnimator.ofFloat(ScreenUtils.height + 800, ScreenUtils.height - ScreenUtils.convertDpToPx(this, ObjectVariables.posHeightMediaPlayer));
        showMusicPlayer.addUpdateListener(valueAnimator -> mcvMusicPlayer.setY((Float) valueAnimator.getAnimatedValue()));
        showMusicPlayer.setInterpolator(new FastOutSlowInInterpolator());
        showMusicPlayer.setDuration(650);
        showMusicPlayer.start();

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

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {

        MaterialCardView appDrawer = topView.findViewById(R.id.appDrawer);

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
            case R.id.mcvStart:
                int height = ScreenUtils.height;

                if (appDrawer.getVisibility() == View.VISIBLE) {
                    appDrawer.setVisibility(View.GONE);
                    ValueAnimator appDrawerAnimation = ValueAnimator.ofFloat(0, height);
                    appDrawerAnimation.addUpdateListener(valueAnimator -> appDrawer.setY((Float) valueAnimator.getAnimatedValue()));
                    appDrawerAnimation.setInterpolator(new FastOutSlowInInterpolator());
                    appDrawerAnimation.setDuration(650);
                    appDrawerAnimation.start();

                } else {
                    appDrawer.setVisibility(View.VISIBLE);
                    ValueAnimator appDrawerAnimation = ValueAnimator.ofFloat(height, 0);
                    appDrawerAnimation.addUpdateListener(valueAnimator -> appDrawer.setY((Float) valueAnimator.getAnimatedValue()));
                    appDrawerAnimation.setInterpolator(new FastOutSlowInInterpolator());
                    appDrawerAnimation.setDuration(650);
                    appDrawerAnimation.start();
                }
                break;
        }

    }

    public class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (intent.getStringExtra("player").equals("YANDEX_POST")) {
                    initMediaPlayer();
                } else {
                    initMediaContent(false);
                }
            }
        }
    }

}
