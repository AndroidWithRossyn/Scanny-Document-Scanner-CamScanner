package com.xiaopo.flying.sticker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.MotionEventCompat;
import androidx.core.view.ViewCompat;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Sticker View
 *
 * @author wupanjie
 */
public class StickerView extends FrameLayout {

    public static final int FLIP_HORIZONTALLY = 1;
    public static final int FLIP_VERTICALLY = 2;
    public final boolean bringToFrontCurrentSticker;
    public final List<Sticker> stickers;
    private final float[] bitmapPoints;
    private final Paint borderPaint;
    private final float[] bounds;
    private final PointF currentCenterPoint;
    private final Matrix downMatrix;
    private final List<BitmapStickerIcon> icons;
    private final Matrix moveMatrix;
    private final float[] point;
    private final boolean showBorder;
    private final boolean showIcons;
    private final RectF stickerRect;
    private final float[] tmp;
    private final int touchSlop;
    private boolean constrained;
    private BitmapStickerIcon currentIcon;
    private int currentMode;
    private float downX;
    private float downY;
    private Sticker handlingSticker;
    private long lastClickTime;
    private boolean locked;
    private PointF midPoint;
    private int minClickDelayTime;
    private float oldDistance;
    private float oldRotation;
    private OnStickerOperationListener onStickerOperationListener;

    public StickerView(Context context) {
        this(context, (AttributeSet) null);
    }

    public StickerView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public StickerView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.stickers = new ArrayList();
        this.icons = new ArrayList(4);
        Paint paint = new Paint();
        this.borderPaint = paint;
        this.stickerRect = new RectF();
        new Matrix();
        this.downMatrix = new Matrix();
        this.moveMatrix = new Matrix();
        this.bitmapPoints = new float[8];
        this.bounds = new float[8];
        this.point = new float[2];
        this.currentCenterPoint = new PointF();
        this.tmp = new float[2];
        this.midPoint = new PointF();
        this.oldDistance = 0.0f;
        this.oldRotation = 0.0f;
        this.currentMode = 0;
        this.lastClickTime = 0;
        this.minClickDelayTime = 200;
        this.touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        TypedArray typedArray = null;
        try {
            typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.StickerView);
            this.showIcons = typedArray.getBoolean(R.styleable.StickerView_showIcons, false);
            this.showBorder = typedArray.getBoolean(R.styleable.StickerView_showBorder, false);
            this.bringToFrontCurrentSticker = typedArray.getBoolean(R.styleable.StickerView_bringToFrontCurrentSticker, false);
            paint.setAntiAlias(true);
            paint.setColor(typedArray.getColor(R.styleable.StickerView_borderColor, ViewCompat.MEASURED_STATE_MASK));
            configDefaultIcons();
        } finally {
            if (typedArray != null) {
                typedArray.recycle();
            }
        }
    }

    public void configDefaultIcons() {
        BitmapStickerIcon bitmapStickerIcon = new BitmapStickerIcon(ContextCompat.getDrawable(getContext(), R.drawable.sticker_ic_close_white_18dp), 0);
        bitmapStickerIcon.setIconEvent(new DeleteIconEvent());
        BitmapStickerIcon bitmapStickerIcon2 = new BitmapStickerIcon(ContextCompat.getDrawable(getContext(), R.drawable.sticker_ic_scale_white_18dp), 3);
        bitmapStickerIcon2.setIconEvent(new ZoomIconEvent());
        BitmapStickerIcon bitmapStickerIcon3 = new BitmapStickerIcon(ContextCompat.getDrawable(getContext(), R.drawable.sticker_ic_flip_white_18dp), 1);
        bitmapStickerIcon3.setIconEvent(new FlipHorizontallyEvent());
        this.icons.clear();
        this.icons.add(bitmapStickerIcon);
        this.icons.add(bitmapStickerIcon2);
        this.icons.add(bitmapStickerIcon3);
    }

    public void swapLayers(int i, int i2) {
        if (this.stickers.size() >= i && this.stickers.size() >= i2) {
            Collections.swap(this.stickers, i, i2);
            invalidate();
        }
    }

    public void sendToLayer(int i, int i2) {
        if (this.stickers.size() >= i && this.stickers.size() >= i2) {
            this.stickers.remove(i);
            this.stickers.add(i2, this.stickers.get(i));
            invalidate();
        }
    }

    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        if (z) {
            RectF rectF = this.stickerRect;
            rectF.left = (float) i;
            rectF.top = (float) i2;
            rectF.right = (float) i3;
            rectF.bottom = (float) i4;
        }
    }

    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        drawStickers(canvas);
    }

    public void drawStickers(Canvas canvas) {
        float f;
        float f2;
        float f3;
        float f4;
        Canvas canvas2 = canvas;
        int i = 0;
        for (int i2 = 0; i2 < this.stickers.size(); i2++) {
            Sticker sticker = this.stickers.get(i2);
            if (sticker != null) {
                sticker.draw(canvas2);
            }
        }
        Sticker sticker2 = this.handlingSticker;
        if (sticker2 != null && !this.locked) {
            if (this.showBorder || this.showIcons) {
                getStickerPoints(sticker2, this.bitmapPoints);
                float[] fArr = this.bitmapPoints;
                float f5 = fArr[0];
                int i3 = 1;
                float f6 = fArr[1];
                float f7 = fArr[2];
                float f8 = fArr[3];
                float f9 = fArr[4];
                float f10 = fArr[5];
                float f11 = fArr[6];
                float f12 = fArr[7];
                if (this.showBorder) {
                    Canvas canvas3 = canvas;
                    float f13 = f5;
                    f4 = f12;
                    float f14 = f6;
                    f3 = f11;
                    f2 = f10;
                    f = f9;
                    canvas3.drawLine(f13, f14, f7, f8, this.borderPaint);
                    canvas3.drawLine(f13, f14, f, f2, this.borderPaint);
                    canvas3.drawLine(f7, f8, f3, f4, this.borderPaint);
                    canvas3.drawLine(f3, f4, f, f2, this.borderPaint);
                } else {
                    f4 = f12;
                    f3 = f11;
                    f2 = f10;
                    f = f9;
                }
                if (this.showIcons) {
                    float f15 = f4;
                    float f16 = f3;
                    float f17 = f2;
                    float f18 = f;
                    float calculateRotation = calculateRotation(f16, f15, f18, f17);
                    while (i < this.icons.size()) {
                        BitmapStickerIcon bitmapStickerIcon = this.icons.get(i);
                        int position = bitmapStickerIcon.getPosition();
                        if (position == 0) {
                            configIconMatrix(bitmapStickerIcon, f5, f6, calculateRotation);
                        } else if (position == i3) {
                            configIconMatrix(bitmapStickerIcon, f7, f8, calculateRotation);
                        } else if (position == 2) {
                            configIconMatrix(bitmapStickerIcon, f18, f17, calculateRotation);
                        } else if (position == 3) {
                            configIconMatrix(bitmapStickerIcon, f16, f15, calculateRotation);
                        }
                        bitmapStickerIcon.draw(canvas2, this.borderPaint);
                        i++;
                        i3 = 1;
                    }
                }
            }
        }
    }

    public void configIconMatrix(@NonNull BitmapStickerIcon bitmapStickerIcon, float f, float f2, float f3) {
        bitmapStickerIcon.setX(f);
        bitmapStickerIcon.setY(f2);
        bitmapStickerIcon.getMatrix().reset();
        bitmapStickerIcon.getMatrix().postRotate(f3, (float) (bitmapStickerIcon.getWidth() / 2), (float) (bitmapStickerIcon.getHeight() / 2));
        bitmapStickerIcon.getMatrix().postTranslate(f - ((float) (bitmapStickerIcon.getWidth() / 2)), f2 - ((float) (bitmapStickerIcon.getHeight() / 2)));
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (this.locked) {
            return super.onInterceptTouchEvent(motionEvent);
        }
        if (motionEvent.getAction() != 0) {
            return super.onInterceptTouchEvent(motionEvent);
        }
        this.downX = motionEvent.getX();
        this.downY = motionEvent.getY();
        return (findCurrentIconTouched() == null && findHandlingSticker() == null) ? false : true;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        Sticker sticker;
        OnStickerOperationListener onStickerOperationListener2;
        if (this.locked) {
            return super.onTouchEvent(motionEvent);
        }
        int actionMasked = MotionEventCompat.getActionMasked(motionEvent);
        if (actionMasked != 0) {
            if (actionMasked == 1) {
                onTouchUp(motionEvent);
            } else if (actionMasked == 2) {
                handleCurrentMode(motionEvent);
                invalidate();
            } else if (actionMasked == 5) {
                this.oldDistance = calculateDistance(motionEvent);
                this.oldRotation = calculateRotation(motionEvent);
                this.midPoint = calculateMidPoint(motionEvent);
                Sticker sticker2 = this.handlingSticker;
                if (sticker2 != null && isInStickerArea(sticker2, motionEvent.getX(1), motionEvent.getY(1)) && findCurrentIconTouched() == null) {
                    this.currentMode = 2;
                }
            } else if (actionMasked == 6) {
                if (!(this.currentMode != 2 || (sticker = this.handlingSticker) == null || (onStickerOperationListener2 = this.onStickerOperationListener) == null)) {
                    onStickerOperationListener2.onStickerZoomFinished(sticker);
                }
                this.currentMode = 0;
            }
        } else if (!onTouchDown(motionEvent)) {
            return false;
        }
        return true;
    }

    public boolean onTouchDown(@NonNull MotionEvent motionEvent) {
        this.currentMode = 1;
        this.downX = motionEvent.getX();
        this.downY = motionEvent.getY();
        PointF calculateMidPoint = calculateMidPoint();
        this.midPoint = calculateMidPoint;
        this.oldDistance = calculateDistance(calculateMidPoint.x, calculateMidPoint.y, this.downX, this.downY);
        PointF pointF = this.midPoint;
        this.oldRotation = calculateRotation(pointF.x, pointF.y, this.downX, this.downY);
        BitmapStickerIcon findCurrentIconTouched = findCurrentIconTouched();
        this.currentIcon = findCurrentIconTouched;
        if (findCurrentIconTouched != null) {
            this.currentMode = 3;
            findCurrentIconTouched.onActionDown(this, this.handlingSticker, motionEvent);
        } else {
            this.handlingSticker = findHandlingSticker();
        }
        Sticker sticker = this.handlingSticker;
        if (sticker != null) {
            this.downMatrix.set(sticker.getMatrix());
            if (this.bringToFrontCurrentSticker) {
                this.stickers.remove(this.handlingSticker);
                this.stickers.add(this.handlingSticker);
            }
            OnStickerOperationListener onStickerOperationListener2 = this.onStickerOperationListener;
            if (onStickerOperationListener2 != null) {
                onStickerOperationListener2.onStickerTouchedDown(this.handlingSticker);
            }
        }
        if (this.currentIcon == null && this.handlingSticker == null) {
            return false;
        }
        invalidate();
        return true;
    }

    public void onTouchUp(@NonNull MotionEvent motionEvent) {
        Sticker sticker;
        OnStickerOperationListener onStickerOperationListener2;
        Sticker sticker2;
        OnStickerOperationListener onStickerOperationListener3;
        BitmapStickerIcon bitmapStickerIcon;
        long uptimeMillis = SystemClock.uptimeMillis();
        if (!(this.currentMode != 3 || (bitmapStickerIcon = this.currentIcon) == null || this.handlingSticker == null)) {
            bitmapStickerIcon.onActionUp(this, motionEvent);
        }
        if (this.currentMode == 1 && Math.abs(motionEvent.getX() - this.downX) < ((float) this.touchSlop) && Math.abs(motionEvent.getY() - this.downY) < ((float) this.touchSlop) && (sticker2 = this.handlingSticker) != null) {
            this.currentMode = 4;
            OnStickerOperationListener onStickerOperationListener4 = this.onStickerOperationListener;
            if (onStickerOperationListener4 != null) {
                onStickerOperationListener4.onStickerClicked(sticker2);
            }
            if (uptimeMillis - this.lastClickTime < ((long) this.minClickDelayTime) && (onStickerOperationListener3 = this.onStickerOperationListener) != null) {
                onStickerOperationListener3.onStickerDoubleTapped(this.handlingSticker);
            }
        }
        if (!(this.currentMode != 1 || (sticker = this.handlingSticker) == null || (onStickerOperationListener2 = this.onStickerOperationListener) == null)) {
            onStickerOperationListener2.onStickerDragFinished(sticker);
        }
        this.currentMode = 0;
        this.lastClickTime = uptimeMillis;
    }

    public void handleCurrentMode(@NonNull MotionEvent motionEvent) {
        BitmapStickerIcon bitmapStickerIcon;
        int i = this.currentMode;
        if (i != 1) {
            if (i != 2) {
                if (i == 3 && this.handlingSticker != null && (bitmapStickerIcon = this.currentIcon) != null) {
                    bitmapStickerIcon.onActionMove(this, motionEvent);
                }
            } else if (this.handlingSticker != null) {
                float calculateDistance = calculateDistance(motionEvent);
                float calculateRotation = calculateRotation(motionEvent);
                this.moveMatrix.set(this.downMatrix);
                Matrix matrix = this.moveMatrix;
                float f = this.oldDistance;
                float f2 = calculateDistance / f;
                float f3 = calculateDistance / f;
                PointF pointF = this.midPoint;
                matrix.postScale(f2, f3, pointF.x, pointF.y);
                Matrix matrix2 = this.moveMatrix;
                float f4 = calculateRotation - this.oldRotation;
                PointF pointF2 = this.midPoint;
                matrix2.postRotate(f4, pointF2.x, pointF2.y);
                this.handlingSticker.setMatrix(this.moveMatrix);
            }
        } else if (this.handlingSticker != null) {
            this.moveMatrix.set(this.downMatrix);
            this.moveMatrix.postTranslate(motionEvent.getX() - this.downX, motionEvent.getY() - this.downY);
            this.handlingSticker.setMatrix(this.moveMatrix);
            if (this.constrained) {
                constrainSticker(this.handlingSticker);
            }
        }
    }

    public void zoomAndRotateCurrentSticker(@NonNull MotionEvent motionEvent) {
        zoomAndRotateSticker(this.handlingSticker, motionEvent);
    }

    public void zoomAndRotateSticker(@Nullable Sticker sticker, @NonNull MotionEvent motionEvent) {
        if (sticker != null) {
            PointF pointF = this.midPoint;
            float calculateDistance = calculateDistance(pointF.x, pointF.y, motionEvent.getX(), motionEvent.getY());
            PointF pointF2 = this.midPoint;
            float calculateRotation = calculateRotation(pointF2.x, pointF2.y, motionEvent.getX(), motionEvent.getY());
            this.moveMatrix.set(this.downMatrix);
            Matrix matrix = this.moveMatrix;
            float f = this.oldDistance;
            float f2 = calculateDistance / f;
            float f3 = calculateDistance / f;
            PointF pointF3 = this.midPoint;
            matrix.postScale(f2, f3, pointF3.x, pointF3.y);
            Matrix matrix2 = this.moveMatrix;
            float f4 = calculateRotation - this.oldRotation;
            PointF pointF4 = this.midPoint;
            matrix2.postRotate(f4, pointF4.x, pointF4.y);
            this.handlingSticker.setMatrix(this.moveMatrix);
        }
    }

    public void constrainSticker(@NonNull Sticker sticker) {
        int width = getWidth();
        int height = getHeight();
        sticker.getMappedCenterPoint(this.currentCenterPoint, this.point, this.tmp);
        PointF pointF = this.currentCenterPoint;
        float f = pointF.x;
        float f2 = 0.0f;
        float f3 = f < 0.0f ? -f : 0.0f;
        float f4 = (float) width;
        if (f > f4) {
            f3 = f4 - f;
        }
        float f5 = pointF.y;
        if (f5 < 0.0f) {
            f2 = -f5;
        }
        float f6 = (float) height;
        if (f5 > f6) {
            f2 = f6 - f5;
        }
        sticker.getMatrix().postTranslate(f3, f2);
    }

    @Nullable
    public BitmapStickerIcon findCurrentIconTouched() {
        for (BitmapStickerIcon next : this.icons) {
            float x = next.getX() - this.downX;
            float y = next.getY() - this.downY;
            if (((double) ((x * x) + (y * y))) <= Math.pow((double) (next.getIconRadius() + next.getIconRadius()), 2.0d)) {
                return next;
            }
        }
        return null;
    }

    @Nullable
    public Sticker findHandlingSticker() {
        for (int size = this.stickers.size() - 1; size >= 0; size--) {
            if (isInStickerArea(this.stickers.get(size), this.downX, this.downY)) {
                return this.stickers.get(size);
            }
        }
        return null;
    }

    public boolean isInStickerArea(@NonNull Sticker sticker, float f, float f2) {
        float[] fArr = this.tmp;
        fArr[0] = f;
        fArr[1] = f2;
        return sticker.contains(fArr);
    }

    @NonNull
    public PointF calculateMidPoint(@Nullable MotionEvent motionEvent) {
        if (motionEvent == null || motionEvent.getPointerCount() < 2) {
            this.midPoint.set(0.0f, 0.0f);
            return this.midPoint;
        }
        this.midPoint.set((motionEvent.getX(0) + motionEvent.getX(1)) / 2.0f, (motionEvent.getY(0) + motionEvent.getY(1)) / 2.0f);
        return this.midPoint;
    }

    @NonNull
    public PointF calculateMidPoint() {
        Sticker sticker = this.handlingSticker;
        if (sticker == null) {
            this.midPoint.set(0.0f, 0.0f);
            return this.midPoint;
        }
        sticker.getMappedCenterPoint(this.midPoint, this.point, this.tmp);
        return this.midPoint;
    }

    public float calculateRotation(@Nullable MotionEvent motionEvent) {
        if (motionEvent == null || motionEvent.getPointerCount() < 2) {
            return 0.0f;
        }
        return calculateRotation(motionEvent.getX(0), motionEvent.getY(0), motionEvent.getX(1), motionEvent.getY(1));
    }

    public float calculateRotation(float f, float f2, float f3, float f4) {
        return (float) Math.toDegrees(Math.atan2((double) (f2 - f4), (double) (f - f3)));
    }

    public float calculateDistance(@Nullable MotionEvent motionEvent) {
        if (motionEvent == null || motionEvent.getPointerCount() < 2) {
            return 0.0f;
        }
        return calculateDistance(motionEvent.getX(0), motionEvent.getY(0), motionEvent.getX(1), motionEvent.getY(1));
    }

    public float calculateDistance(float f, float f2, float f3, float f4) {
        double d = (double) (f - f3);
        double d2 = (double) (f2 - f4);
        return (float) Math.sqrt((d * d) + (d2 * d2));
    }

    public void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
    }

    public void flipCurrentSticker(int i) {
        flip(this.handlingSticker, i);
    }

    public void flip(@Nullable Sticker sticker, int i) {
        if (sticker != null) {
            sticker.getCenterPoint(this.midPoint);
            if ((i & 1) > 0) {
                Matrix matrix = sticker.getMatrix();
                PointF pointF = this.midPoint;
                matrix.preScale(-1.0f, 1.0f, pointF.x, pointF.y);
                sticker.setFlippedHorizontally(!sticker.isFlippedHorizontally());
            }
            if ((i & 2) > 0) {
                Matrix matrix2 = sticker.getMatrix();
                PointF pointF2 = this.midPoint;
                matrix2.preScale(1.0f, -1.0f, pointF2.x, pointF2.y);
                sticker.setFlippedVertically(!sticker.isFlippedVertically());
            }
            OnStickerOperationListener onStickerOperationListener2 = this.onStickerOperationListener;
            if (onStickerOperationListener2 != null) {
                onStickerOperationListener2.onStickerFlipped(sticker);
            }
            invalidate();
        }
    }

    public boolean replace(@Nullable Sticker sticker) {
        return replace(sticker, true);
    }

    public boolean replace(@Nullable Sticker sticker, boolean z) {
        float f;
        if (this.handlingSticker == null || sticker == null) {
            return false;
        }
        float width = (float) getWidth();
        float height = (float) getHeight();
        if (z) {
            sticker.setMatrix(this.handlingSticker.getMatrix());
            sticker.setFlippedVertically(this.handlingSticker.isFlippedVertically());
            sticker.setFlippedHorizontally(this.handlingSticker.isFlippedHorizontally());
        } else {
            this.handlingSticker.getMatrix().reset();
            sticker.getMatrix().postTranslate((width - ((float) this.handlingSticker.getWidth())) / 2.0f, (height - ((float) this.handlingSticker.getHeight())) / 2.0f);
            if (width < height) {
                f = width / ((float) this.handlingSticker.getDrawable().getIntrinsicWidth());
            } else {
                f = height / ((float) this.handlingSticker.getDrawable().getIntrinsicHeight());
            }
            float f2 = f / 2.0f;
            sticker.getMatrix().postScale(f2, f2, width / 2.0f, height / 2.0f);
        }
        this.stickers.set(this.stickers.indexOf(this.handlingSticker), sticker);
        this.handlingSticker = sticker;
        invalidate();
        return true;
    }

    public boolean remove(@Nullable Sticker sticker) {
        if (this.stickers.contains(sticker)) {
            this.stickers.remove(sticker);
            OnStickerOperationListener onStickerOperationListener2 = this.onStickerOperationListener;
            if (onStickerOperationListener2 != null) {
                onStickerOperationListener2.onStickerDeleted(sticker);
            }
            if (this.handlingSticker == sticker) {
                this.handlingSticker = null;
            }
            invalidate();
            return true;
        }
        Log.d("StickerView", "remove: the sticker is not in this StickerView");
        return false;
    }

    public boolean removeCurrentSticker() {
        return remove(this.handlingSticker);
    }

    public void removeAllStickers() {
        this.stickers.clear();
        Sticker sticker = this.handlingSticker;
        if (sticker != null) {
            sticker.release();
            this.handlingSticker = null;
        }
        invalidate();
    }

    @NonNull
    public StickerView addSticker(@NonNull Sticker sticker) {
        return addSticker(sticker, 1);
    }

    public StickerView addSticker(@NonNull final Sticker sticker, final int i) {
        if (ViewCompat.isLaidOut(this)) {
            addStickerImmediately(sticker, i);
        } else {
            post(new Runnable() {
                public void run() {
                    StickerView.this.addStickerImmediately(sticker, i);
                }
            });
        }
        return this;
    }

    public void addStickerImmediately(@NonNull Sticker sticker, int i) {
        setStickerPosition(sticker, i);
        float width = ((float) getWidth()) / ((float) sticker.getDrawable().getIntrinsicWidth());
        float height = ((float) getHeight()) / ((float) sticker.getDrawable().getIntrinsicHeight());
        if (width > height) {
            width = height;
        }
        float f = width / 2.0f;
        sticker.getMatrix().postScale(f, f, (float) (getWidth() / 2), (float) (getHeight() / 2));
        this.handlingSticker = sticker;
        this.stickers.add(sticker);
        OnStickerOperationListener onStickerOperationListener2 = this.onStickerOperationListener;
        if (onStickerOperationListener2 != null) {
            onStickerOperationListener2.onStickerAdded(sticker);
        }
        invalidate();
    }

    public void setStickerPosition(@NonNull Sticker sticker, int i) {
        float width = ((float) getWidth()) - ((float) sticker.getWidth());
        float height = ((float) getHeight()) - ((float) sticker.getHeight());
        sticker.getMatrix().postTranslate((i & 4) > 0 ? width / 4.0f : (i & 8) > 0 ? width * 0.75f : width / 2.0f, (i & 2) > 0 ? height / 4.0f : (i & 16) > 0 ? height * 0.75f : height / 2.0f);
    }

    @NonNull
    public float[] getStickerPoints(@Nullable Sticker sticker) {
        float[] fArr = new float[8];
        getStickerPoints(sticker, fArr);
        return fArr;
    }

    public void getStickerPoints(@Nullable Sticker sticker, @NonNull float[] fArr) {
        if (sticker == null) {
            Arrays.fill(fArr, 0.0f);
            return;
        }
        sticker.getBoundPoints(this.bounds);
        sticker.getMappedPoints(fArr, this.bounds);
    }

    public void save(@NonNull File file) {
        try {
            StickerUtils.saveImageToGallery(file, createBitmap());
        } catch (IllegalArgumentException | IllegalStateException exception) {
        }
    }

    @NonNull
    public Bitmap createBitmap() throws OutOfMemoryError {
        this.handlingSticker = null;
        Bitmap createBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        draw(new Canvas(createBitmap));
        return createBitmap;
    }

    public int getStickerCount() {
        return this.stickers.size();
    }

    public boolean isNoneSticker() {
        return getStickerCount() == 0;
    }

    public boolean isLocked() {
        return this.locked;
    }

    @NonNull
    public StickerView setLocked(boolean z) {
        this.locked = z;
        invalidate();
        return this;
    }

    public int getMinClickDelayTime() {
        return this.minClickDelayTime;
    }

    @NonNull
    public StickerView setMinClickDelayTime(int i) {
        this.minClickDelayTime = i;
        return this;
    }

    public boolean isConstrained() {
        return this.constrained;
    }

    @NonNull
    public StickerView setConstrained(boolean z) {
        this.constrained = z;
        postInvalidate();
        return this;
    }

    @Nullable
    public OnStickerOperationListener getOnStickerOperationListener() {
        return this.onStickerOperationListener;
    }

    @NonNull
    public StickerView setOnStickerOperationListener(@Nullable OnStickerOperationListener onStickerOperationListener2) {
        this.onStickerOperationListener = onStickerOperationListener2;
        return this;
    }

    @Nullable
    public Sticker getCurrentSticker() {
        return this.handlingSticker;
    }

    @NonNull
    public List<BitmapStickerIcon> getIcons() {
        return this.icons;
    }

    public void setIcons(@NonNull List<BitmapStickerIcon> list) {
        this.icons.clear();
        this.icons.addAll(list);
        invalidate();
    }

    @Retention(RetentionPolicy.SOURCE)
    protected @interface ActionMode {
        public static final int CLICK = 4;
        public static final int DRAG = 1;
        public static final int ICON = 3;
        public static final int NONE = 0;
        public static final int ZOOM_WITH_TWO_FINGER = 2;
    }

    @Retention(RetentionPolicy.SOURCE)
    protected @interface Flip {
    }

    public interface OnStickerOperationListener {
        void onStickerAdded(@NonNull Sticker sticker);

        void onStickerClicked(@NonNull Sticker sticker);

        void onStickerDeleted(@NonNull Sticker sticker);

        void onStickerDoubleTapped(@NonNull Sticker sticker);

        void onStickerDragFinished(@NonNull Sticker sticker);

        void onStickerEditClicked(@NonNull Sticker sticker);

        void onStickerFlipped(@NonNull Sticker sticker);

        void onStickerTouchedDown(@NonNull Sticker sticker);

        void onStickerZoomFinished(@NonNull Sticker sticker);
    }
}