package com.scanny.scanner.scrapbook;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;

public class BitmapFactoryUtils {
    private BitmapFactoryUtils() {
    }

    public static boolean checkIsSvgResource(int i) {
        return false;
    }

    public static Bitmap decodeFile(String str, int i, boolean z) {
        return decodeFile(str, i, z, true);
    }

    public static Bitmap decodeFile(String str, int i, boolean z, boolean z2) {
        int imageRotation = getImageRotation(str);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(str, options);
        int max = Math.max(options.outWidth, options.outHeight);
        if (max <= i || i <= 0) {
            options.inSampleSize = 1;
        } else {
            options.inSampleSize = max / i;
        }
        Bitmap decodeFile = decodeFile(str, options.inSampleSize);
        if (decodeFile == null) {
            return null;
        }
        if (imageRotation != 0 && z2) {
            Matrix matrix = new Matrix();
            matrix.postRotate((float) imageRotation);
            decodeFile = Bitmap.createBitmap(decodeFile, 0, 0, decodeFile.getWidth(), decodeFile.getHeight(), matrix, false);
        }
        if (!z || decodeFile.getWidth() == decodeFile.getHeight()) {
            return decodeFile;
        }
        if (decodeFile.getWidth() > decodeFile.getHeight()) {
            return Bitmap.createBitmap(decodeFile, (decodeFile.getWidth() - decodeFile.getHeight()) / 2, 0, decodeFile.getHeight(), decodeFile.getHeight());
        }
        return decodeFile.getWidth() < decodeFile.getHeight() ? Bitmap.createBitmap(decodeFile, 0, (decodeFile.getHeight() - decodeFile.getWidth()) / 2, decodeFile.getWidth(), decodeFile.getWidth()) : decodeFile;
    }

    public static int getImageRotation(String str) {
        return ExifUtils.getAngle(str);
    }

    public static Bitmap decodeResource(Resources resources, int i, int i2) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, i, options);
        int max = Math.max(options.outWidth, options.outHeight);
        if (max <= i2 || i2 <= 0) {
            options.inSampleSize = 1;
        } else {
            options.inSampleSize = max / i2;
        }
        limitMemoryUsage(options);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(resources, i, options);
    }

    public static float[] decodeSize(Resources resources, int i) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, i, options);
        return new float[]{(float) options.outWidth, (float) options.outHeight};
    }

    public static float[] decodeSize(String str) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(str, options);
        return new float[]{(float) options.outWidth, (float) options.outHeight};
    }

    private static void limitMemoryUsage(BitmapFactory.Options options) {
        if (options.inSampleSize < 1) {
            options.inSampleSize = 1;
        }
        if (((float) freeMemory()) < ((float) (((options.outWidth * options.outHeight) * 4) / (options.inSampleSize * options.inSampleSize))) * 1.5f) {
            System.gc();
            System.gc();
        }
        while (((float) freeMemory()) < ((float) (((options.outWidth * options.outHeight) * 4) / (options.inSampleSize * options.inSampleSize))) * 2.0f) {
            options.inSampleSize++;
        }
    }

    private static long freeMemory() {
        return Runtime.getRuntime().maxMemory() - (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
    }

    public static Bitmap decodeResource(Resources resources, int i) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, i, options);
        limitMemoryUsage(options);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeResource(resources, i, options);
    }

    public static Bitmap drawResource(Resources resources, int i, int i2, int i3) {
        Bitmap createBitmap = Bitmap.createBitmap(i2, i3, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        Drawable drawable = resources.getDrawable(i);
        if (drawable != null) {
            drawable.setBounds(0, 0, i2, i3);
            drawable.draw(canvas);
        }
        return createBitmap;
    }

    private static Bitmap decodeFile(String str, int i) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(str, options);
        limitMemoryUsage(options);
        options.inJustDecodeBounds = false;
        options.inSampleSize = i;
        options.inDither = false;
        options.inMutable = true;
        return BitmapFactory.decodeFile(str, options);
    }
}
