package com.gibbonstudio.myapplication.Views.MediaControl;

import static com.gibbonstudio.myapplication.MainActivity.objectVariables;

import android.animation.ValueAnimator;
import android.app.PendingIntent;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.gibbonstudio.myapplication.R;
import com.gibbonstudio.myapplication.ScreenUtils;
import com.google.android.material.card.MaterialCardView;

public class MediaPlayerView extends FrameLayout {

    Context mContext;

    TextView artistText;
    TextView trackText;

    ImageView trackImage;
    ImageView imageNotificationPlay;
    ImageView imageNotificationPrev;
    ImageView imageNotificationNext;
    ImageView imgBackground;

    MaterialCardView btnNotificationPrev;
    MaterialCardView btnNotificationPlay;
    MaterialCardView btnNotificationNext;
    MaterialCardView mcvBackdrop;

    LinearLayout playerContent;
    LinearLayout playerList;

    public MediaPlayerView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public MediaPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MediaPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(@NonNull Context context) {
        setClipChildren(false);
        setClipToPadding(false);

        inflate(context, R.layout.media_player, this);

        mContext = context;

        artistText = findViewById(R.id.artistText);
        trackText = findViewById(R.id.trackText);
        trackImage = findViewById(R.id.trackImage);
        imageNotificationPlay = findViewById(R.id.imageNotificationPlay);
        imageNotificationPrev = findViewById(R.id.imageNotificationPrev);
        imageNotificationNext = findViewById(R.id.imageNotificationNext);
        imgBackground = findViewById(R.id.imgBackground);
        btnNotificationPrev = findViewById(R.id.btnNotificationPrev);
        btnNotificationPlay = findViewById(R.id.btnNotificationPlay);
        btnNotificationNext = findViewById(R.id.btnNotificationNext);
        playerContent = findViewById(R.id.playerContent);
        playerList = findViewById(R.id.playerList);
        mcvBackdrop = findViewById(R.id.mcvBackdrop);

    }

    public void bindToTrack() {
        if (objectVariables.getPlayerSbn() != null) {

            artistText.setText(objectVariables.getPlayerSbn().getNotification().extras.getString("android.text"));
            trackText.setText(objectVariables.getPlayerSbn().getNotification().extras.getString("android.title"));
            trackImage.setImageIcon(objectVariables.getPlayerSbn().getNotification().getLargeIcon());
            imgBackground.setImageIcon(objectVariables.getPlayerSbn().getNotification().getLargeIcon());

            mcvBackdrop.setOnClickListener(v -> {
                try {
                    objectVariables.getPlayerSbn().getNotification().contentIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            });

            btnNotificationPrev.setOnClickListener(view -> {
                try {
                    objectVariables.getPlayerSbn().getNotification().actions[1].actionIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            });
            btnNotificationPlay.setOnClickListener(view -> {
                try {
                    objectVariables.getPlayerSbn().getNotification().actions[2].actionIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            });
            btnNotificationNext.setOnClickListener(view -> {
                try {
                    objectVariables.getPlayerSbn().getNotification().actions[3].actionIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            });

            btnNotificationPrev.setEnabled(!objectVariables.getPlayerSbn().getNotification().actions[1].title.equals("PreviousBlocked"));
            imageNotificationPrev.setImageResource(!objectVariables.getPlayerSbn().getNotification().actions[1].title.equals("PreviousBlocked") ? R.drawable.skip_previous : R.drawable.block);
            imageNotificationNext.setImageResource(!objectVariables.getPlayerSbn().getNotification().actions[3].title.equals("NextBlocked") ? R.drawable.skip_next : R.drawable.block);
            imageNotificationPlay.setImageResource(objectVariables.getPlayerSbn().getNotification().actions[2].title.equals("Play") ? R.drawable.play_arrow : R.drawable.stop);

            trackText.setSelected(true);
            showMediaContent(true);
        }
    }

    public void showMediaContent(boolean show) {
        if (show && playerContent.getVisibility() == View.GONE) {
            playerContent.setVisibility(View.VISIBLE);
            imgBackground.setVisibility(View.VISIBLE);
            mcvBackdrop.setVisibility(View.VISIBLE);
            playerList.setVisibility(View.GONE);

            ValueAnimator playerContentAnimation = ValueAnimator.ofFloat(-500, ScreenUtils.convertDpToPx(mContext, 16));
            playerContentAnimation.addUpdateListener(valueAnimator -> {
                playerContent.setX((Float) valueAnimator.getAnimatedValue());

            });
            playerContentAnimation.setInterpolator(new FastOutSlowInInterpolator());
            playerContentAnimation.setDuration(350);
            playerContentAnimation.start();

            ValueAnimator playerListAnimation = ValueAnimator.ofFloat(ScreenUtils.convertDpToPx(mContext, 16), 500);
            playerListAnimation.addUpdateListener(valueAnimator -> {
                playerList.setX((Float) valueAnimator.getAnimatedValue());
            });

        } else if (!show && playerContent.getVisibility() == View.VISIBLE) {
            playerContent.setVisibility(View.GONE);
            imgBackground.setVisibility(View.GONE);
            mcvBackdrop.setVisibility(View.GONE);
            playerList.setVisibility(View.VISIBLE);

            ValueAnimator playerContentAnimation = ValueAnimator.ofFloat(ScreenUtils.convertDpToPx(mContext, 16), 500);
            playerContentAnimation.addUpdateListener(valueAnimator -> {
                playerContent.setX((Float) valueAnimator.getAnimatedValue());

            });
            playerContentAnimation.setInterpolator(new FastOutSlowInInterpolator());
            playerContentAnimation.setDuration(350);
            playerContentAnimation.start();

            ValueAnimator playerListAnimation = ValueAnimator.ofFloat(500, ScreenUtils.convertDpToPx(mContext, 16));
            playerListAnimation.addUpdateListener(valueAnimator -> {
                playerList.setX((Float) valueAnimator.getAnimatedValue());
            });
            playerListAnimation.setInterpolator(new FastOutSlowInInterpolator());
            playerListAnimation.setDuration(350);
            playerListAnimation.start();
        }
    }
}
