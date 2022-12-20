package com.gibbonstudio.myapplication.Views.Apps;

import static android.content.Context.WINDOW_SERVICE;
import static com.gibbonstudio.myapplication.ObjectVariables.SHOW_HIDE_APP_DRAWER;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gibbonstudio.myapplication.Adapters.AppsAdapter;
import com.gibbonstudio.myapplication.Models.AppItem;
import com.gibbonstudio.myapplication.R;
import com.gibbonstudio.myapplication.ScreenUtils;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class AllAppsDrawer extends FrameLayout {

    Context mContext;
    FrameLayout mcvStart;

    RecyclerView rvAllApps;
    RecyclerView rvFastApps;

    WindowManager windowManager;

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

        windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);

        //Init Views
        mcvStart = findViewById(R.id.flStart);
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
        rvAllApps.setVisibility(View.GONE);

        mcvStart.setOnClickListener(v -> {
            showHideAppDrawer();
        });

        initReceiver();
        initScreenUtils();
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
        int height = ScreenUtils.height;

        ValueAnimator appDrawerPosAnimation = ValueAnimator.ofFloat(0, height);
        appDrawerPosAnimation.addUpdateListener(valueAnimator -> rvAllApps.setY((Float) valueAnimator.getAnimatedValue()));
        appDrawerPosAnimation.setInterpolator(new AccelerateInterpolator(1.5F));
        appDrawerPosAnimation.setDuration(350);

        ValueAnimator appDrawerAlphaAnimation = ValueAnimator.ofFloat(1, 0);
        appDrawerAlphaAnimation.addUpdateListener(valueAnimator -> rvAllApps.setAlpha((Float) valueAnimator.getAnimatedValue()));
        appDrawerAlphaAnimation.setInterpolator(new AccelerateInterpolator(1.5F));
        appDrawerAlphaAnimation.setDuration(350);

        if (isShow) {
            appDrawerPosAnimation.start();
            appDrawerAlphaAnimation.start();
            isShow = false;
        } else {
            rvAllApps.setVisibility(View.VISIBLE);
            appDrawerPosAnimation.reverse();
            appDrawerAlphaAnimation.reverse();
            isShow = true;
        }
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
