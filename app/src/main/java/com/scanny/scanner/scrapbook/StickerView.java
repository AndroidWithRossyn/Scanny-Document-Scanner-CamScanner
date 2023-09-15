package com.scanny.scanner.scrapbook;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

import com.edmodo.cropper.util.ImageViewUtil;
import com.scanny.scanner.R;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class StickerView extends ImageView {
    private static final int CACHE_THRESHOLD = 65536;
    private static final String TAG = "StickerView";
    public static Bitmap delBitmap;
    public static Bitmap resizeBitmap;
    public final StickerHolderView holder;
    private final StickerConfigInterface config;
    private final Matrix drawStickerMatrix = new Matrix();
    private final Matrix stickerMatrix = new Matrix();
    public long cachePixelSize = -1;
    public float memoryScaleDown = 1.0f;
    public boolean requestRedraw = false;
    Bitmap bitmap2;
    int flag;
    Bitmap imageBitmap;
    ArrayList<ImageXY> imageXYArrayList = new ArrayList<>();
    boolean isShow = false;
    int numberOfImage;
    private Path boxPath = new Path();
    private boolean cacheIsLoading = false;
    private long cacheNewPixelSize = -1;
    private float currentRotation = 0.0f;
    private float currentScale = 1.0f;
    private float currentX = 0.0f;
    private float currentY = 0.0f;
    private int delBitmapHeight;
    private int delBitmapWidth;
    private RectF del_resize;
    private DisplayMetrics dm;
    private RectF dst_resize;
    private Bitmap editBitmap;
    private int editBitmapHeight;
    private int editBitmapWidth;
    private RectF edit_resize;
    private View holderView;
    private float imageScale = 1.0f;
    private boolean isHorizonMirrored = false;
    private boolean isInEdit = true;
    private boolean isStickerImageInitialized = false;
    private Paint localPaint;
    private float maxScale = 1.2f;
    private int maxTextWidth = 1;
    private float minScale = 0.5f;
    private Paint paint;
    private boolean reinitializedAspect = true;
    private int resizeBitmapHeight;
    private int resizeBitmapWidth;
    private int screenHeight;
    private int screenWidth;
    private int stickerCacheHeight = -1;
    private int stickerCacheWidth = -1;
    private Bitmap stickerPictureCache;
    private float translationX = 0.0f;
    private float translationY = 0.0f;
    private Paint uiPaint;

    public StickerView(Context context, StickerConfigInterface stickerConfigInterface, StickerHolderView stickerHolderView, int i) {
        super(context);
        this.config = stickerConfigInterface;
        this.holder = stickerHolderView;
        this.numberOfImage = i;
        setLayerType(LAYER_TYPE_SOFTWARE, (Paint) null);
        setWillNotDraw(false);
        init();
    }

    protected static Picture drawTextToPicture(TextStickerConfig textStickerConfig) {
        String text = textStickerConfig.getText();
        Rect rect = new Rect();
        Paint paint2 = new Paint();
        Picture picture = new Picture();
        TextPaint textPaint = new TextPaint(1);
        textPaint.setColor(textStickerConfig.getColor());
        textPaint.setTextSize(714.2857f);
        textPaint.setTypeface(textStickerConfig.getTypeface());
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(textStickerConfig.getAlign());
        textPaint.getTextBounds(text, 0, text.length(), rect);
        textPaint.setSubpixelText(true);
        textPaint.setHinting(1);
        paint2.setColor(textStickerConfig.getBackgroundColor());
        int width = (int) (((float) rect.width()) + 0.0f + Math.abs(0.0f) + 100.0f);
        int height = (int) (((float) rect.height()) + 0.0f + Math.abs(0.0f) + 100.0f);
        Rect rect2 = new Rect(0, 0, width, height);
        Canvas beginRecording = picture.beginRecording(width, height);
        beginRecording.drawRect(rect2, paint2);
        beginRecording.save();
        beginRecording.drawText(text, (0.0f - ((float) rect.left)) + 50.0f, (0.0f - ((float) rect.top)) + 50.0f, textPaint);
        beginRecording.restore();
        picture.endRecording();
        return picture;
    }

    public void refresh() {
        this.cachePixelSize = -1;
        this.cacheIsLoading = false;
        this.reinitializedAspect = true;
        loadBitmapCache();
    }

    private void init() {
        this.dm = getResources().getDisplayMetrics();
        this.paint = new Paint();
        this.uiPaint = new Paint();
        this.uiPaint.setAlpha(255);
        this.dst_resize = new RectF();
        this.del_resize = new RectF();
        this.edit_resize = new RectF();
        this.localPaint = new Paint();
        this.localPaint.setColor(-1);
        this.localPaint.setStyle(Paint.Style.STROKE);
        this.localPaint.setAntiAlias(true);
        this.localPaint.setStrokeWidth(4.0f);
        this.localPaint.setPathEffect(new DashPathEffect(new float[]{1.0f, 2.0f, 4.0f, 8.0f}, 1.0f));
        this.screenWidth = this.dm.widthPixels;
        this.screenHeight = this.dm.heightPixels;
        this.maxTextWidth = Math.max(this.screenWidth, this.screenHeight) * 2;
        loadBitmapCache();
        initButtonBitmaps();
    }

    public boolean hasStickerSize() {
        return this.stickerCacheHeight > 0 && this.stickerCacheWidth > 0;
    }

    public void invalidate() {
        super.invalidate();
        View view = this.holderView;
        if (view != null) {
            view.invalidate();
        }
    }

    public void postInvalidate() {
        super.postInvalidate();
        View view = this.holderView;
        if (view != null) {
            view.postInvalidate();
        }
    }

    public void onAttachedToWindow() {
        this.holderView = (View) getParent();
        super.onAttachedToWindow();
    }

    public void onDetachedFromWindow() {
        this.holderView = null;
        super.onDetachedFromWindow();
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (hasStickerSize()) {
            drawToCanvas(canvas, this.imageScale, this.translationX, this.translationY);
        }
    }

    public void drawStickerToCanvas(Canvas canvas, int i, int i2) {
        Rect bitmapRectCenterInside = ImageViewUtil.getBitmapRectCenterInside(canvas.getWidth(), canvas.getHeight(), getWidth(), getHeight());
        float min = Math.min(((float) canvas.getWidth()) / ((float) bitmapRectCenterInside.width()), ((float) canvas.getHeight()) / ((float) bitmapRectCenterInside.height()));
        int i3 = i - bitmapRectCenterInside.left;
        int i4 = i2 - bitmapRectCenterInside.top;
        this.maxTextWidth = Math.max(canvas.getWidth(), canvas.getHeight()) * 2;
        this.isInEdit = false;
        double d = (double) min;
        loadBitmapCache(Math.round(((double) this.cachePixelSize) * d * d), true);
        drawToCanvas(canvas, min, (float) i3, (float) i4);
    }

    public boolean isOnEditButton(ScaledMotionEventWrapper scaledMotionEventWrapper) {
        float f = this.edit_resize.top - 0.21875f;
        float f2 = this.edit_resize.right + 20.0f;
        float f3 = this.edit_resize.bottom + 20.0f;
        if (scaledMotionEventWrapper.getX(0) < this.edit_resize.left - 0.21875f || scaledMotionEventWrapper.getX(0) > f2 || scaledMotionEventWrapper.getY(0) < f || scaledMotionEventWrapper.getY(0) > f3) {
            return false;
        }
        return true;
    }

    public StickerConfigInterface getConfig() {
        return this.config;
    }

    public StickerConfigInterface.STICKER_TYPE getType() {
        if (this.config == null) {
            return null;
        }
        return getConfig().getType();
    }

    public void setScale(float f) {
        this.imageScale = f;
        postInvalidate();
    }

    public void setTranslationX(float f) {
        this.translationX = f;
        postInvalidate();
    }

    public void setTranslationY(float f) {
        this.translationY = f;
        postInvalidate();
    }

    public void rescaleCache(float f) {
        boolean z = (((double) Math.abs(f - this.memoryScaleDown)) > 0.2d && this.memoryScaleDown != 1.0f) || this.memoryScaleDown == 0.0f;
        this.memoryScaleDown = f;
        if (z) {
            Bitmap bitmap = this.stickerPictureCache;
            if (!(bitmap == null || f == 1.0f)) {
                bitmap.recycle();
                this.stickerPictureCache = null;
            }
            System.gc();
            if (!this.requestRedraw && this.memoryScaleDown != 0.0f) {
                this.requestRedraw = true;
                post(new Runnable() {
                    public void run() {
                        if (((float) (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory())) > ((float) cachePixelSize) * memoryScaleDown * 4.0f * 2.0f) {
                            requestRedraw = false;
                            loadBitmapCache();
                            return;
                        }
                        post(this);
                    }
                });
            }
        }
    }

    public boolean isInEdit() {
        return this.isInEdit;
    }

    public void setInEdit(boolean z) {
        this.isInEdit = z;
        invalidate();
    }

    private synchronized Matrix getStickerMatrix() {
        synchronized (this.stickerMatrix) {
            float f = ((float) this.stickerCacheWidth) * this.currentScale;
            float f2 = ((float) this.stickerCacheHeight) * this.currentScale;
            float f3 = this.currentX - (f / 2.0f);
            float f4 = this.currentY - (f2 / 2.0f);
            this.stickerMatrix.reset();
            this.stickerMatrix.postTranslate(f3, f4);
            if (this.isHorizonMirrored) {
                this.stickerMatrix.postScale(-1.0f, 1.0f, this.currentX, this.currentY);
            }
            this.stickerMatrix.postRotate(this.currentRotation, this.currentX, this.currentY);
            this.stickerMatrix.preScale(this.currentScale, this.currentScale);
        }
        return this.stickerMatrix;
    }

    public float[] getCurrentTransformState() {
        return new float[]{this.currentX, this.currentY, this.currentScale, this.currentRotation};
    }

    public void setTransformation(float f, float f2, float f3, float f4) {
        this.currentX = f;
        this.currentY = f2;
        this.currentScale = f3;
        this.currentRotation = f4;
        invalidate();
    }

    public void drawToCanvas(Canvas canvas, float f, float f2, float f3) {
        Canvas canvas2 = canvas;
        float f4 = f;
        float f5 = f2;
        float f6 = f3;
        if (this.isStickerImageInitialized) {
            Matrix stickerMatrix2 = getStickerMatrix();
            float[] fArr = new float[9];
            stickerMatrix2.getValues(fArr);
            float f7 = (fArr[0] * 0.0f) + (fArr[1] * 0.0f) + fArr[2];
            float f8 = (fArr[3] * 0.0f) + (fArr[4] * 0.0f) + fArr[5];
            float f9 = fArr[0];
            int i = this.stickerCacheWidth;
            float f10 = (f9 * ((float) i)) + (fArr[1] * 0.0f) + fArr[2];
            float f11 = (fArr[3] * ((float) i)) + (fArr[4] * 0.0f) + fArr[5];
            float f12 = fArr[1];
            int i2 = this.stickerCacheHeight;
            float f13 = (fArr[0] * 0.0f) + (f12 * ((float) i2)) + fArr[2];
            float f14 = (fArr[3] * 0.0f) + (fArr[4] * ((float) i2)) + fArr[5];
            float f15 = (fArr[0] * ((float) i)) + (fArr[1] * ((float) i2)) + fArr[2];
            float f16 = (fArr[3] * ((float) i)) + (fArr[4] * ((float) i2)) + fArr[5];
            Bitmap bitmap = this.stickerPictureCache;
            if (bitmap != null && !bitmap.isRecycled()) {
                float width = ((float) this.stickerCacheWidth) / ((float) this.stickerPictureCache.getWidth());
                this.drawStickerMatrix.set(stickerMatrix2);
                this.drawStickerMatrix.preScale(width, width);
                this.drawStickerMatrix.postTranslate(f5, f6);
                this.drawStickerMatrix.postScale(f4, f4);
                this.paint.setAntiAlias(true);
                this.paint.setFilterBitmap(true);
                canvas.save();
                canvas2.setMatrix(this.drawStickerMatrix);
                canvas2.drawBitmap(this.stickerPictureCache, 0.0f, 0.0f, this.paint);
                canvas.restore();
                if (this.isInEdit) {
                    this.localPaint.setStrokeWidth(this.dm.density / f4);
                    float f17 = this.imageScale;
                    int i3 = (int) (((float) this.resizeBitmapHeight) / f17);
                    RectF rectF = this.dst_resize;
                    float f18 = (float) (((int) (((float) this.resizeBitmapWidth) / f17)) / 2);
                    rectF.left = (float) ((int) (f15 - f18));
                    rectF.right = (float) ((int) (f18 + f15));
                    float f19 = (float) (i3 / 2);
                    rectF.top = (float) ((int) (f16 - f19));
                    rectF.bottom = (float) ((int) (f19 + f16));
                    canvas.save();
                    canvas2.scale(f4, f4);
                    canvas2.translate(f5, f6);
                    this.boxPath.reset();
                    this.boxPath.moveTo(f7, f8);
                    this.boxPath.lineTo(f10, f11);
                    this.boxPath.lineTo(f15, f16);
                    this.boxPath.lineTo(f13, f14);
                    this.boxPath.lineTo(f7, f8);
                    canvas2.drawPath(this.boxPath, this.localPaint);
                    canvas2.drawBitmap(resizeBitmap, (Rect) null, this.dst_resize, this.uiPaint);
                    float f20 = this.imageScale;
                    int i4 = (int) (((float) this.delBitmapHeight) / f20);
                    RectF rectF2 = this.del_resize;
                    float f21 = (float) (((int) (((float) this.delBitmapWidth) / f20)) / 2);
                    rectF2.left = (float) ((int) (f7 - f21));
                    rectF2.right = (float) ((int) (f21 + f7));
                    float f22 = (float) (i4 / 2);
                    rectF2.top = (float) ((int) (f8 - f22));
                    rectF2.bottom = (float) ((int) (f22 + f8));
                    canvas2.drawBitmap(delBitmap, (Rect) null, rectF2, this.uiPaint);
                    float[] fArr2 = new float[9];
                    getStickerMatrix().getValues(fArr2);
                    float f23 = fArr2[0];
                    float f24 = (float) this.stickerCacheWidth;
                    float f25 = (f23 * f24) + (fArr2[1] * 0.0f) + fArr2[2];
                    float f26 = (fArr2[3] * f24) + (fArr2[4] * 0.0f) + fArr2[5];
                    float f27 = this.imageScale;
                    int i5 = (int) (((float) this.editBitmapHeight) / f27);
                    RectF rectF3 = this.edit_resize;
                    float f28 = (float) (((int) (((float) this.editBitmapWidth) / f27)) / 2);
                    rectF3.left = (float) ((int) (f25 - f28));
                    rectF3.right = (float) ((int) (f28 + f25));
                    float f29 = (float) (i5 / 2);
                    rectF3.top = (float) ((int) (f26 - f29));
                    rectF3.bottom = (float) ((int) (f29 + f26));
                    if (this.config.getType() == StickerConfigInterface.STICKER_TYPE.TEXT) {
                        canvas2.drawBitmap(this.editBitmap, (Rect) null, this.edit_resize, this.uiPaint);
                    }
                    canvas.restore();
                }
                double d = (double) (f7 - f10);
                double d2 = (double) (f8 - f11);
                double d3 = (double) (f10 - f15);
                double d4 = (double) (f11 - f16);
                double d5 = (double) f4;
                this.cacheNewPixelSize = Math.round(((double) ((float) ((int) Math.sqrt((d * d) + (d2 * d2))))) * d5 * ((double) ((float) ((int) Math.sqrt((d3 * d3) + (d4 * d4))))) * d5);
                loadBitmapCache();
            }
        }
    }

    public boolean calculateOnScreenFlip() {
        float rotationX = getRotationX();
        float rotationY = getRotationY();
        View rootView = getRootView();
        float f = rotationY;
        float f2 = rotationX;
        View view = null;
        View view2 = (View) getParent();
        float f3 = f;
        while (view2 != null && !view2.equals(rootView) && !view2.equals(view)) {
            f2 += view2.getRotationX();
            f3 += view2.getRotationY();
            View view3 = view2;
            view2 = (View) view2.getParent();
            view = view3;
        }
        boolean z = Math.round(f2 / 180.0f) == 1;
        boolean z2 = Math.round(f3 / 180.0f) == 1;
        if ((!z || z2) && (!z2 || z)) {
            return false;
        }
        return true;
    }

    public float calculateOnScreenRotation() {
        float rotation = getRotation();
        View rootView = getRootView();
        float f = rotation;
        View view = null;
        View view2 = (View) getParent();
        while (view2 != null && !view2.equals(rootView) && !view2.equals(view)) {
            f += view2.getRotation();
            View view3 = view2;
            view2 = (View) view2.getParent();
            view = view3;
        }
        return f % 360.0f;
    }

    public synchronized void loadBitmapCache() {
        if (!this.cacheIsLoading) {
            int width = getWidth() / 10;
            int height = getHeight() / 10;
            if (this.cacheNewPixelSize <= 0) {
                this.cacheNewPixelSize = (long) Math.max(width * height, 65536);
            }
            loadBitmapCache(this.cacheNewPixelSize, false);
        }
    }

    public synchronized void loadBitmapCache(long j, boolean z) {
        if (z) {
            this.cachePixelSize = j;
            LoadPictureCacheTask loadPictureCacheTask = new LoadPictureCacheTask(this.config, true);
            try {
                loadPictureCacheTask.onPostExecute((Bitmap) loadPictureCacheTask.execute(new Void[0]).get());
            } catch (InterruptedException | ExecutionException exception) {
            }
        } else {
            if (j < PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH) {
                j = 65536;
            }
            if (j > ((long) (this.screenHeight * this.screenWidth))) {
                j = (long) (this.screenHeight * this.screenWidth);
            }
            if (!this.cacheIsLoading) {
                if (this.stickerPictureCache == null || this.reinitializedAspect || this.stickerPictureCache.isRecycled() || Math.abs((((float) j) * this.memoryScaleDown) - ((float) (this.stickerPictureCache.getWidth() * this.stickerPictureCache.getHeight()))) >= 65536.0f) {
                    this.cacheIsLoading = true;
                    this.cachePixelSize = j;
                    new LoadPictureCacheTask(this.config, false).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, new Void[0]);
                }
            }
        }
    }

    public synchronized void setStickerPictureCache(Bitmap bitmap) {
        this.cacheIsLoading = false;
        if (bitmap != null) {
            this.stickerPictureCache = bitmap;
            setImageDimensions(bitmap.getWidth(), bitmap.getHeight());
            postInvalidate();
        }
    }

    public synchronized void setImageDimensions(int i, int i2) {
        float f = ((float) this.stickerCacheWidth) / ((float) this.stickerCacheHeight);
        float f2 = ((float) i) / ((float) i2);
        if (!this.isStickerImageInitialized) {
            this.reinitializedAspect = false;
            this.stickerCacheWidth = i;
            this.stickerCacheHeight = i2;
            if (this.stickerCacheWidth >= this.stickerCacheHeight) {
                float f3 = (float) (this.screenWidth / 8);
                if (((float) this.stickerCacheWidth) < f3) {
                    this.minScale = 1.0f;
                } else {
                    this.minScale = (f3 * 1.0f) / ((float) this.stickerCacheWidth);
                }
                if (this.stickerCacheWidth > this.screenWidth) {
                    this.maxScale = 1.0f;
                } else {
                    this.maxScale = (((float) this.screenWidth) * 1.0f) / ((float) this.stickerCacheWidth);
                }
            } else {
                float f4 = (float) (this.screenWidth / 8);
                if (((float) this.stickerCacheHeight) < f4) {
                    this.minScale = 1.0f;
                } else {
                    this.minScale = (f4 * 1.0f) / ((float) this.stickerCacheHeight);
                }
                if (this.stickerCacheHeight > this.screenWidth) {
                    this.maxScale = 1.0f;
                } else {
                    this.maxScale = (((float) this.screenWidth) * 1.0f) / ((float) this.stickerCacheHeight);
                }
            }
            float f5 = (this.minScale + this.maxScale) / 4.0f;
            imagePositionArrayList();
            this.flag = this.numberOfImage;
            if (this.flag >= 20) {
                this.flag %= 10;
            }
            for (int i3 = 0; i3 < this.imageXYArrayList.size(); i3++) {
                if (i3 == this.flag) {
                    this.currentX = (float) this.imageXYArrayList.get(i3).getX();
                    this.currentY = (float) this.imageXYArrayList.get(i3).getY();
                    this.currentRotation = (float) this.imageXYArrayList.get(i3).getAngle();
                }
            }
            this.currentScale = f5 / this.imageScale;
            if (calculateOnScreenFlip()) {
                flip(false);
            }
            this.isStickerImageInitialized = true;
            postInvalidate();
        } else if (this.reinitializedAspect && f != f2) {
            this.reinitializedAspect = false;
            this.stickerCacheWidth = (int) (((double) i) * (((double) this.stickerCacheHeight) / ((double) i2)));
            postInvalidate();
        }
    }

    public void flip(boolean z) {
        if (z) {
            this.currentRotation = (this.currentRotation + 180.0f) % 360.0f;
        }
        this.isHorizonMirrored = !this.isHorizonMirrored;
        postInvalidate();
    }

    private void initButtonBitmaps() {
        resizeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sticker_rotate);
        this.resizeBitmapWidth = resizeBitmap.getWidth();
        this.resizeBitmapHeight = resizeBitmap.getHeight();
        delBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sticker_delete1);
        this.delBitmapWidth = delBitmap.getWidth();
        this.delBitmapHeight = delBitmap.getHeight();
        this.editBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sticker_flip);
        this.editBitmapWidth = this.editBitmap.getWidth();
        this.editBitmapHeight = this.editBitmap.getHeight();
    }

    public boolean isEnabled() {
        View view = (View) getParent();
        return view != null && view.isEnabled() && super.isEnabled();
    }

    public boolean isInBitmap(ScaledMotionEventWrapper scaledMotionEventWrapper) {
        ScaledMotionEventWrapper scaledMotionEventWrapper2 = scaledMotionEventWrapper;
        float[] fArr = new float[9];
        getStickerMatrix().getValues(fArr);
        float f = (fArr[3] * 0.0f) + (fArr[4] * 0.0f) + fArr[5];
        float f2 = fArr[0];
        int i = this.stickerCacheWidth;
        float f3 = (f2 * ((float) i)) + (fArr[1] * 0.0f) + fArr[2];
        float f4 = (fArr[3] * ((float) i)) + (fArr[4] * 0.0f) + fArr[5];
        float f5 = fArr[1];
        int i2 = this.stickerCacheHeight;
        float f6 = (fArr[0] * 0.0f) + (f5 * ((float) i2)) + fArr[2];
        float f7 = (fArr[3] * 0.0f) + (fArr[4] * ((float) i2)) + fArr[5];
        float f8 = (fArr[0] * ((float) i)) + (fArr[1] * ((float) i2)) + fArr[2];
        float f9 = (fArr[3] * ((float) i)) + (fArr[4] * ((float) i2)) + fArr[5];
        return pointInRect(new float[]{(fArr[0] * 0.0f) + (fArr[1] * 0.0f) + fArr[2], f3, f8, f6}, new float[]{f, f4, f9, f7}, scaledMotionEventWrapper2.getX(0), scaledMotionEventWrapper2.getY(0));
    }

    private boolean pointInRect(float[] fArr, float[] fArr2, float f, float f2) {
        double hypot = Math.hypot((double) (fArr[0] - fArr[1]), (double) (fArr2[0] - fArr2[1]));
        double hypot2 = Math.hypot((double) (fArr[1] - fArr[2]), (double) (fArr2[1] - fArr2[2]));
        double hypot3 = Math.hypot((double) (fArr[3] - fArr[2]), (double) (fArr2[3] - fArr2[2]));
        double hypot4 = Math.hypot((double) (fArr[0] - fArr[3]), (double) (fArr2[0] - fArr2[3]));
        double hypot5 = Math.hypot((double) (f - fArr[0]), (double) (f2 - fArr2[0]));
        double d = hypot;
        double hypot6 = Math.hypot((double) (f - fArr[1]), (double) (f2 - fArr2[1]));
        double hypot7 = Math.hypot((double) (f - fArr[2]), (double) (f2 - fArr2[2]));
        double hypot8 = Math.hypot((double) (f - fArr[3]), (double) (f2 - fArr2[3]));
        double d2 = ((d + hypot5) + hypot6) / 2.0d;
        double d3 = ((hypot2 + hypot6) + hypot7) / 2.0d;
        double d4 = ((hypot3 + hypot7) + hypot8) / 2.0d;
        double d5 = ((hypot4 + hypot8) + hypot5) / 2.0d;
        return Math.abs((d * hypot2) - (((Math.sqrt((((d2 - d) * d2) * (d2 - hypot5)) * (d2 - hypot6)) + Math.sqrt((((d3 - hypot2) * d3) * (d3 - hypot6)) * (d3 - hypot7))) + Math.sqrt((((d4 - hypot3) * d4) * (d4 - hypot7)) * (d4 - hypot8))) + Math.sqrt((((d5 - hypot4) * d5) * (d5 - hypot8)) * (d5 - hypot5)))) < 0.5d;
    }

    public boolean isOnResizeButton(ScaledMotionEventWrapper scaledMotionEventWrapper) {
        float f = this.dst_resize.top - 0.21875f;
        float f2 = this.dst_resize.right + 20.0f;
        float f3 = this.dst_resize.bottom + 20.0f;
        if (scaledMotionEventWrapper.getX(0) < this.dst_resize.left - 0.21875f || scaledMotionEventWrapper.getX(0) > f2 || scaledMotionEventWrapper.getY(0) < f || scaledMotionEventWrapper.getY(0) > f3) {
            return false;
        }
        return true;
    }

    public boolean isOnDelButton(ScaledMotionEventWrapper scaledMotionEventWrapper) {
        float f = this.del_resize.top - 0.21875f;
        float f2 = this.del_resize.right + 20.0f;
        float f3 = this.del_resize.bottom + 20.0f;
        if (scaledMotionEventWrapper.getX(0) < this.del_resize.left - 0.21875f || scaledMotionEventWrapper.getX(0) > f2 || scaledMotionEventWrapper.getY(0) < f || scaledMotionEventWrapper.getY(0) > f3) {
            return false;
        }
        return true;
    }

    public int getAllocatedByteCount() {
        Bitmap bitmap = this.stickerPictureCache;
        if (bitmap == null) {
            return 0;
        }
        return bitmap.getByteCount();
    }

    public long getRequestedByteCount() {
        return this.cachePixelSize * 4;
    }

    public void setEditedImage(Bitmap bitmap, boolean isShow) {
        this.imageBitmap = bitmap;
        this.isShow = isShow;
    }

    public void flipHorizontal(boolean isFlipHorizontal) {
        if (isFlipHorizontal) {
            this.currentRotation = (this.currentRotation + 360.0f) % 360.0f;
        }
        this.isHorizonMirrored = !this.isHorizonMirrored;
        postInvalidate();
    }

    public void roteteLeft(boolean isRotateLeft) {
        if (isRotateLeft) {
            this.currentRotation += 90.0f;
        }
        postInvalidate();
    }

    public void roteteRight(boolean isRotateRight) {
        if (isRotateRight) {
            this.currentRotation -= 90.0f;
        }
        postInvalidate();
    }

    public int calculatePercentagePixelWidth(int i) {
        return (this.screenWidth * i) / 100;
    }

    public int calculatePercentagePixelHieght(int i) {
        return (this.screenHeight * i) / 100;
    }

    public void imagePositionArrayList() {
        this.imageXYArrayList.add(new ImageXY(calculatePercentagePixelWidth(50), calculatePercentagePixelHieght(20), 0));
        this.imageXYArrayList.add(new ImageXY(calculatePercentagePixelWidth(50), calculatePercentagePixelHieght(55), 0));
        this.imageXYArrayList.add(new ImageXY(calculatePercentagePixelWidth(25), calculatePercentagePixelHieght(16), 16));
        this.imageXYArrayList.add(new ImageXY(calculatePercentagePixelWidth(25), calculatePercentagePixelHieght(40), 350));
        this.imageXYArrayList.add(new ImageXY(calculatePercentagePixelWidth(79), calculatePercentagePixelHieght(40), 345));
        this.imageXYArrayList.add(new ImageXY(calculatePercentagePixelWidth(79), calculatePercentagePixelHieght(30), 14));
        this.imageXYArrayList.add(new ImageXY(calculatePercentagePixelWidth(30), calculatePercentagePixelHieght(30), 350));
        this.imageXYArrayList.add(new ImageXY(calculatePercentagePixelWidth(50), calculatePercentagePixelHieght(14), 350));
        this.imageXYArrayList.add(new ImageXY(calculatePercentagePixelWidth(50), calculatePercentagePixelHieght(42), 13));
        this.imageXYArrayList.add(new ImageXY(calculatePercentagePixelWidth(65), calculatePercentagePixelHieght(12), 10));
        this.imageXYArrayList.add(new ImageXY(calculatePercentagePixelWidth(25), calculatePercentagePixelHieght(45), 10));
        this.imageXYArrayList.add(new ImageXY(calculatePercentagePixelWidth(65), calculatePercentagePixelHieght(43), 12));
        this.imageXYArrayList.add(new ImageXY(calculatePercentagePixelWidth(91), calculatePercentagePixelHieght(28), 360));
        this.imageXYArrayList.add(new ImageXY(calculatePercentagePixelWidth(21), calculatePercentagePixelHieght(28), 360));
        this.imageXYArrayList.add(new ImageXY(calculatePercentagePixelWidth(21), calculatePercentagePixelHieght(58), 360));
        this.imageXYArrayList.add(new ImageXY(calculatePercentagePixelWidth(91), calculatePercentagePixelHieght(58), 360));
        this.imageXYArrayList.add(new ImageXY(calculatePercentagePixelWidth(83), calculatePercentagePixelHieght(25), 360));
        this.imageXYArrayList.add(new ImageXY(calculatePercentagePixelWidth(29), calculatePercentagePixelHieght(24), 360));
        this.imageXYArrayList.add(new ImageXY(calculatePercentagePixelWidth(29), calculatePercentagePixelHieght(63), 360));
        this.imageXYArrayList.add(new ImageXY(calculatePercentagePixelWidth(84), calculatePercentagePixelHieght(63), 360));
    }

    private class LoadPictureCacheTask extends AsyncTask<Void, Integer, Bitmap> {
        final Context context;
        final ImageStickerConfig imageConfig;
        final boolean isText;
        final StickerHolderView parent;
        final TextStickerConfig textConfig;
        final Picture textPicture;

        private LoadPictureCacheTask(StickerConfigInterface stickerConfigInterface, boolean z) {
            float f;
            this.context = getContext();
            this.isText = stickerConfigInterface.getType() == StickerConfigInterface.STICKER_TYPE.TEXT;
            if (z) {
                f = 1.0f;
            } else {
                f = holder.takeStickerMemory(StickerView.this);
            }
            memoryScaleDown = f;
            if (this.isText) {
                TextStickerConfig textStickerConfig = (TextStickerConfig) stickerConfigInterface;
                this.textConfig = textStickerConfig;
                this.imageConfig = null;
                this.textPicture = StickerView.drawTextToPicture(textStickerConfig);
            } else {
                this.imageConfig = (ImageStickerConfig) stickerConfigInterface;
                this.textConfig = null;
                this.textPicture = null;
            }
            if (getParent() instanceof StickerHolderView) {
                this.parent = (StickerHolderView) getParent();
            } else {
                this.parent = null;
            }
        }


        public Bitmap doInBackground(Void... voidArr) {
            Bitmap bitmap;
            if (memoryScaleDown == 0.0f || holder == null) {
                return null;
            }
            Thread.currentThread().setPriority(1);
            long round = (long) Math.round(((float) cachePixelSize) * memoryScaleDown);
            long round2 = (long) Math.round(((float) cachePixelSize) * memoryScaleDown);
            if (this.isText) {
                double width = ((double) this.textPicture.getWidth()) / ((double) this.textPicture.getHeight());
                double d = (double) round2;
                int sqrt = (int) Math.sqrt(d * width);
                int sqrt2 = (int) Math.sqrt(d * (1.0d / width));
                Bitmap createBitmap = Bitmap.createBitmap(sqrt, sqrt2, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(createBitmap);
                Matrix matrix = new Matrix();
                matrix.setScale(((float) sqrt) / ((float) this.textPicture.getWidth()), ((float) sqrt2) / ((float) this.textPicture.getHeight()));
                canvas.setMatrix(matrix);
                this.textPicture.draw(canvas);
                if (((float) (createBitmap.getWidth() * createBitmap.getHeight())) <= ((float) round2) * 1.01f || sqrt <= 0 || sqrt2 <= 0) {
                    return createBitmap;
                }
                Bitmap createScaledBitmap = Bitmap.createScaledBitmap(createBitmap, sqrt, sqrt2, true);
                createBitmap.recycle();
                return createScaledBitmap;
            }
            float[] decodeSize = BitmapFactoryUtils.decodeSize(this.context.getResources(), this.imageConfig.getStickerId());
            float[] decodeSize2 = BitmapFactoryUtils.decodeSize(this.context.getResources(), this.imageConfig.getStickerId());
            double d2 = ((double) decodeSize2[0]) / ((double) decodeSize2[1]);
            double d3 = (double) round;
            long j = round;
            int sqrt3 = (int) Math.sqrt(d3 * d2);
            double d4 = ((double) decodeSize[0]) / ((double) decodeSize[1]);
            double d5 = (double) round2;
            Math.sqrt(d5 * d4);
            Math.sqrt(d5 * (1.0d / d4));
            int sqrt4 = (int) Math.sqrt(d3 * (1.0d / d2));
            if (isShow) {
                bitmap = imageBitmap;
            } else {
                bitmap = this.imageConfig.getBitmapImage();
            }
            if (bitmap != null) {
                if (((float) (bitmap.getWidth() * bitmap.getHeight())) <= ((float) j) * 1.01f || sqrt3 <= 0 || sqrt4 <= 0) {
                    bitmap2 = bitmap;
                } else {
                    bitmap2 = Bitmap.createScaledBitmap(bitmap, sqrt3, sqrt4, true);
                    bitmap.recycle();
                }
            }
            return bitmap2;
        }


        public void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                setStickerPictureCache(bitmap);
            }
            if (bitmap == null || ((float) bitmap.getByteCount()) > ((float) cachePixelSize) * memoryScaleDown * 3.9f) {
                loadBitmapCache();
            }
        }
    }
}
