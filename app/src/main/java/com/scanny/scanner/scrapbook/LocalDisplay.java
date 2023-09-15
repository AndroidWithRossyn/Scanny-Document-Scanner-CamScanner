package com.scanny.scanner.scrapbook;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

public class LocalDisplay {
    public static float SCREEN_DENSITY;
    public static int SCREEN_HEIGHT_DP;
    public static int SCREEN_HEIGHT_PIXELS;
    public static int SCREEN_WIDTH_DP;
    public static int SCREEN_WIDTH_PIXELS;
    private static boolean sInitialed;

    public static void init(Context context) {
        if (!sInitialed && context != null) {
            sInitialed = true;
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
            SCREEN_WIDTH_PIXELS = displayMetrics.widthPixels;
            SCREEN_HEIGHT_PIXELS = displayMetrics.heightPixels;
            SCREEN_DENSITY = displayMetrics.density;
            SCREEN_WIDTH_DP = (int) (((float) SCREEN_WIDTH_PIXELS) / displayMetrics.density);
            SCREEN_HEIGHT_DP = (int) (((float) SCREEN_HEIGHT_PIXELS) / displayMetrics.density);
        }
    }

    public static int dp2px(float f) {
        return (int) ((f * SCREEN_DENSITY) + 0.5f);
    }

    public static int designedDP2px(float f) {
        int i = SCREEN_WIDTH_DP;
        if (i != 320) {
            f = (f * ((float) i)) / 320.0f;
        }
        return dp2px(f);
    }

    public static void setPadding(View view, float f, float f2, float f3, float f4) {
        view.setPadding(designedDP2px(f), dp2px(f2), designedDP2px(f3), dp2px(f4));
    }

    public static int sp2px(float f) {
        return (int) ((f * SCREEN_DENSITY) + 0.5f);
    }
}
