package com.scanny.scanner.qrcode_reader;

import android.graphics.Rect;

public interface IViewFinder {
    Rect getFramingRect();

    int getHeight();

    int getWidth();

    void setBorderAlpha(float borderAlpha);

    void setBorderColor(int i);

    void setBorderCornerRadius(int i);

    void setBorderCornerRounded(boolean isCornerRounded);

    void setBorderLineLength(int i);

    void setBorderStrokeWidth(int i);

    void setLaserColor(int i);

    void setLaserEnabled(boolean isLaserEnabled);

    void setMaskColor(int i);

    void setSquareViewFinder(boolean isViewFinder);

    void setViewFinderOffset(int i);

    void setupViewFinder();
}
