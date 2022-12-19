package com.gibbonstudio.myapplication;

import android.content.Context;
import android.util.DisplayMetrics;

public class ScreenUtils {
    public static int height;
    public static int width;

    public static int convertDpToPx(Context context, int dp) {
        return Math.round(dp * (context.getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
