package com.gibbonstudio.myapplication.Views.MediaControl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gibbonstudio.myapplication.R;
import com.google.android.material.card.MaterialCardView;

public class MediaControlView extends FrameLayout {

    MaterialCardView btnVolumeDown;
    MaterialCardView btnVolumeUp;
    ProgressBar pbVolumeProgress;
    AudioManager audioManager;

    public MediaControlView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public MediaControlView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MediaControlView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(@NonNull Context context) {

        setClipChildren(false);
        setClipToPadding(false);

        inflate(context, R.layout.media_control, this);

        btnVolumeDown = findViewById(R.id.btnVolumeDown);
        btnVolumeUp = findViewById(R.id.btnVolumeUp);
        pbVolumeProgress = findViewById(R.id.pbVolumeProgress);

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        btnVolumeDown.setOnClickListener(view -> {
            audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
        });

        btnVolumeUp.setOnClickListener(view -> {
            audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
        });

        pbVolumeProgress.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        pbVolumeProgress.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        pbVolumeProgress.setMin(audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC));

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.media.VOLUME_CHANGED_ACTION");
        VolumeChanged volumeChanged = new VolumeChanged();
        context.registerReceiver(volumeChanged, filter);
    }

    class VolumeChanged extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            pbVolumeProgress.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        }
    }

}
