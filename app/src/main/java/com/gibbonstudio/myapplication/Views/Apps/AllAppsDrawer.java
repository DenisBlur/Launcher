package com.gibbonstudio.myapplication.Views.Apps;

import static com.gibbonstudio.myapplication.ObjectVariables.SHOW_HIDE_APP_DRAWER;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gibbonstudio.myapplication.Adapters.AppsAdapter;
import com.gibbonstudio.myapplication.MainActivity;
import com.gibbonstudio.myapplication.Models.AppItem;
import com.gibbonstudio.myapplication.R;
import com.gibbonstudio.myapplication.Views.MediaControl.MediaPlayerView;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AllAppsDrawer extends FrameLayout {

    Context mContext;

    MaterialCardView mcvAppDrawer;
    MaterialCardView mcvStart;

    RecyclerView rvAllApps;
    RecyclerView rvFastApps;

    List<AppItem> appItems = new ArrayList<>();
    List<AppItem> fastAppItems = new ArrayList<>();

    AppDrawerReceiver appDrawerReceiver = new AppDrawerReceiver();

    boolean isShow = false;

    public AllAppsDrawer(@NonNull Context context) {
        super(context);
        init(context);
    }

    public AllAppsDrawer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AllAppsDrawer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    //Initialize all view and set settings
    private void init(@NonNull Context context) {

        setClipChildren(false);
        setClipToPadding(false);

        mContext = context;

        inflate(context, R.layout.all_apps_drawer, this);

        //Init Views
        mcvAppDrawer = findViewById(R.id.mcvAppDrawer);
        mcvStart = findViewById(R.id.mcvStart);
        rvAllApps = findViewById(R.id.rvAllApps);
        rvFastApps = findViewById(R.id.rvFastApps);

        //App Drawer Recycler View
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> appList = context.getPackageManager().queryIntentActivities(mainIntent, 0);
        for (ResolveInfo ri : appList) {
            AppItem item = new AppItem();
            item.setPackageName(ri.activityInfo.packageName);
            item.setName(ri.loadLabel(context.getPackageManager()));
            item.setIcon(ri.loadIcon(context.getPackageManager()));
            appItems.add(item);
        }
        AppsAdapter fastAppsAdapter = new AppsAdapter(context, appItems, false);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 5);
        rvAllApps.setLayoutManager(gridLayoutManager);
        rvAllApps.setAdapter(fastAppsAdapter);

        //Fast App Recycler View
        rvFastApps.setAdapter(new AppsAdapter(context, fastAppItems, true));

        //Set App Drawer Visibility
        mcvAppDrawer.setVisibility(View.GONE);

        mcvStart.setOnClickListener(v -> {
            showHideAppDrawer();
        });

        initReceiver();
    }

    private void initReceiver() {
        //AppDrawer Receiver
        IntentFilter appDrawerIntentFilter = new IntentFilter();
        appDrawerIntentFilter.addAction(SHOW_HIDE_APP_DRAWER);
        mContext.registerReceiver(appDrawerReceiver, appDrawerIntentFilter);
    }

    //Add AppItem to Fast Recycler View
    public void addApp(int position) {
        AppItem app = appItems.get(position);
        fastAppItems.add(app);
        AppsAdapter appsAdapter = new AppsAdapter(mContext, fastAppItems, true);
        rvFastApps.setAdapter(appsAdapter);
    }

    //Animation Show Hide AppDrawer
    public void showHideAppDrawer() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;

        ValueAnimator appDrawerPosAnimation = ValueAnimator.ofFloat(0, height);
        appDrawerPosAnimation.addUpdateListener(valueAnimator -> mcvAppDrawer.setY((Float) valueAnimator.getAnimatedValue()));
        appDrawerPosAnimation.setInterpolator(new AccelerateInterpolator(1.5F));
        appDrawerPosAnimation.setDuration(350);

        ValueAnimator appDrawerAlphaAnimation = ValueAnimator.ofFloat(1, 0);
        appDrawerAlphaAnimation.addUpdateListener(valueAnimator -> mcvAppDrawer.setAlpha((Float) valueAnimator.getAnimatedValue()));
        appDrawerAlphaAnimation.setInterpolator(new AccelerateInterpolator(1.5F));
        appDrawerAlphaAnimation.setDuration(350);

        if (isShow) {
            appDrawerPosAnimation.start();
            appDrawerAlphaAnimation.start();
            isShow = false;
        } else {
            mcvAppDrawer.setVisibility(View.VISIBLE);
            appDrawerPosAnimation.reverse();
            appDrawerAlphaAnimation.reverse();
            isShow = true;
        }
    }

    class AppDrawerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int value = intent.getIntExtra("add_fast_app", -1);
            if(value != -1) {
                addApp(value);
            } else {
                if(isShow) {
                    showHideAppDrawer();
                }
            }
        }
    }

}
