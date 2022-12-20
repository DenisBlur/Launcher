package com.gibbonstudio.myapplication;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.gibbonstudio.myapplication.ObjectVariables.NOTIFICATION_LIST;
import static com.gibbonstudio.myapplication.ObjectVariables.NOTIFICATION_POST;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.gibbonstudio.myapplication.Overlays.Overlay;
import com.gibbonstudio.myapplication.Overlays.OverlayStarter;
import com.gibbonstudio.myapplication.Views.MediaControl.MediaPlayerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    public static ObjectVariables objectVariables;
    public static Intent serviceStarter;
    public static Intent serviceOverlay;

    private GestureDetector mDetector;
    private MediaPlayerView mediaPlayerView;

    //Receivers
    ChangeTimeAndDate changeTimeAndDate = new ChangeTimeAndDate();
    NotificationReceiver notificationReceiver = new NotificationReceiver();

    Runnable runnable = new Runnable() {
        public void run() {
            synchronized (this) {
                try {
                    wait(5000);
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

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        objectVariables = ObjectVariables.getInstance();

        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));

            startActivity(intent);
        } else {
            initView();
        }

        initView();
        initApps();
        initReceiversServices();
        initGestureDetector();
        changeTime();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initGestureDetector() {
        ImageView mcvGesture = findViewById(R.id.mcvGesture);
        mDetector = new GestureDetector(this, new PositionGestureDetector());
        mcvGesture.setOnTouchListener((view, motionEvent) -> {
            Log.i("RAW_X", String.valueOf(motionEvent.getRawX()));
            return mDetector.onTouchEvent(motionEvent);
        });
    }

    private class PositionGestureDetector extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDoubleTap(MotionEvent e) {

            RelativeLayout llAudioControl = findViewById(R.id.llAudioControl);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;

            ValueAnimator audioControlAnimation = ValueAnimator.ofFloat(ScreenUtils.convertDpToPx(getApplicationContext(), 16), width - ScreenUtils.convertDpToPx(getApplicationContext(), 416));
            audioControlAnimation.addUpdateListener(valueAnimator -> llAudioControl.setX((Float) valueAnimator.getAnimatedValue()));
            audioControlAnimation.setInterpolator(new FastOutSlowInInterpolator());
            audioControlAnimation.setDuration(650);

            if (e.getRawX() > (width / 2)) {
                audioControlAnimation.start();
            } else {
                audioControlAnimation.reverse();
            }
            return super.onDoubleTap(e);
        }
    }

    private void initReceiversServices() {

        serviceOverlay = new Intent(this, Overlay.class);
        serviceOverlay.setFlags(FLAG_ACTIVITY_NEW_TASK);

        serviceStarter = new Intent(this, OverlayStarter.class);
        serviceStarter.setFlags(FLAG_ACTIVITY_NEW_TASK);

        Intent notificationService = new Intent(getApplicationContext(), NLService.class);
        notificationService.putExtra("command", "get_status");
        startService(notificationService);

        //Date time Receiver
        IntentFilter dateTimeIntentFilter = new IntentFilter();
        dateTimeIntentFilter.addAction(Intent.ACTION_TIME_TICK);
        dateTimeIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        dateTimeIntentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        dateTimeIntentFilter.addAction(Intent.ACTION_DATE_CHANGED);
        registerReceiver(changeTimeAndDate, dateTimeIntentFilter);

        //Notification Receiver
        IntentFilter notificationIntentFilter = new IntentFilter();
        notificationIntentFilter.addAction(NOTIFICATION_POST);
        registerReceiver(notificationReceiver, notificationIntentFilter);

        thread.start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(changeTimeAndDate);
        unregisterReceiver(notificationReceiver);
    }

    @SuppressLint("SetTextI18n")
    private void changeTime() {
        TextView tvTime = findViewById(R.id.tvTime);
        TextView tvDate = findViewById(R.id.tvDate);

        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String formattedDate = df.format(currentTime);

        String hours;
        String minutes;

        if (currentTime.getHours() < 10) {
            hours = "0" + currentTime.getHours();
        } else {
            hours = String.valueOf(currentTime.getHours());
        }
        if (currentTime.getMinutes() < 10) {
            minutes = "0" + currentTime.getMinutes();
        } else {
            minutes = String.valueOf(currentTime.getMinutes());
        }
        tvTime.setText(hours + ":" + minutes);
        tvDate.setText(formattedDate);
    }

    private void initApps() {

    }

    private void initView() {
        mediaPlayerView = findViewById(R.id.mpvMain);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Overlay.isRunning) {
            stopService(serviceOverlay);
        }
        stopService(serviceStarter);
        mediaPlayerView.bindToTrack();
    }

    @Override
    protected void onPause() {
        super.onPause();
        startService(serviceStarter);
    }


    class NotificationReceiver extends BroadcastReceiver {
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

    class ChangeTimeAndDate extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            changeTime();
        }
    }

}