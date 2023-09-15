package com.scanny.scanner.main_utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.Log;

public class AdjustUtil {
    private static final String TAG = "AdjustUtil";

    public static float getConvertedValue(float f) {
        return f * 0.1f;
    }

    public static PorterDuffColorFilter setBrightness(int i) {
        if (i >= 100) {
            return new PorterDuffColorFilter(Color.argb(((i - 100) * 255) / 100, 255, 255, 255), PorterDuff.Mode.SRC_OVER);
        }
        return new PorterDuffColorFilter(Color.argb(((100 - i) * 255) / 100, 0, 0, 0), PorterDuff.Mode.SRC_ATOP);
    }

    public static Bitmap changeBitmapSaturation(float f, float f2, float f3, Bitmap bitmap) {
        try {
            Bitmap createBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
            Canvas canvas = new Canvas(createBitmap);
            Paint paint = new Paint();
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.set(new float[]{f, 0.0f, 0.0f, 0.0f, f2, 0.0f, f, 0.0f, 0.0f, f2, 0.0f, 0.0f, f, 0.0f, f2, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f});
            ColorMatrix colorMatrix2 = new ColorMatrix();
            colorMatrix2.setSaturation(f3);
            colorMatrix.postConcat(colorMatrix2);
            paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
            canvas.drawBitmap(bitmap, new Matrix(), paint);
            return createBitmap;
        } catch (Exception e) {
            Log.e(TAG, "changeBitmapSaturation: " + e);
            return null;
        }
    }

    public static Bitmap changeBitmapContrastBrightness(Bitmap bitmap, float f, float f2) {
        ColorMatrix colorMatrix = new ColorMatrix(new float[]{f, 0.0f, 0.0f, 0.0f, f2, 0.0f, f, 0.0f, 0.0f, f2, 0.0f, 0.0f, f, 0.0f, f2, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f});
        Bitmap createBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Canvas canvas = new Canvas(createBitmap);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint);
        return createBitmap;
    }
}
