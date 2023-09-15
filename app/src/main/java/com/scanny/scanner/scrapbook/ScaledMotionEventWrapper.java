package com.scanny.scanner.scrapbook;

import android.graphics.RectF;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

public class ScaledMotionEventWrapper {
    private static final int MAX_CLICK_DISTANCE = 15;
    private static final int MAX_CLICK_DURATION = 200;
    private static boolean isClicked = false;
    private static long pressStartTime;
    private static float pressedX;
    private static float pressedY;
    private static TransformState startTransformState;
    private final MotionEvent event;
    private final float offsetX;
    private final float offsetY;
    private final float scale;
    public Point fixedCenterPoint = null;
    MotionEvent lastEvent;
    private boolean isCheckpoint = false;

    public ScaledMotionEventWrapper(MotionEvent motionEvent, float mScale, float mOffsetX, float mOffsetY) {
        this.event = motionEvent;
        this.scale = mScale;
        this.offsetX = mOffsetX;
        this.offsetY = mOffsetY;
        int actionMasked = getActionMasked();
        if (actionMasked == 0) {
            saveTransformState();
            if (lastEvent == null || !lastEvent.equals(motionEvent)) {
                this.lastEvent = motionEvent;
                pressStartTime = System.currentTimeMillis();
                pressedX = getX(0);
                pressedY = getY(0);
                isClicked = false;
            }
        } else if (actionMasked == 1 && System.currentTimeMillis() - pressStartTime < 200 && distance(pressedX, pressedY, getX(0), getY(0)) < 15.0f) {
            isClicked = true;
        }
        if (getPointerCount() != 1) {
            pressStartTime = 0;
        }
        if (startTransformState != null && startTransformState.getPointCount() != getPointerCount()) {
            saveTransformState();
        }
    }

    private static float distance(float f, float f2, float f3, float f4) {
        float f5 = f - f3;
        float f6 = f2 - f4;
        return pxToDp((float) Math.sqrt((double) ((f5 * f5) + (f6 * f6))));
    }

    private static float pxToDp(float f) {
        return f / LocalDisplay.SCREEN_DENSITY;
    }

    public void setFixedCenterPoint(float f, float f2) {
        this.fixedCenterPoint = new Point(f, f2);
        if (isCheckpoint()) {
            saveTransformState();
        }
    }

    public boolean hasFixedCenterPoint() {
        return this.fixedCenterPoint != null;
    }

    public boolean hasClicked() {
        return isClicked;
    }

    private void saveTransformState() {
        startTransformState = new TransformState(this);
        this.isCheckpoint = true;
    }

    public boolean isCheckpoint() {
        return this.isCheckpoint;
    }

    public float getX(int i) {
        return (this.event.getX(i) / this.scale) - this.offsetX;
    }

    public float getY(int i) {
        return (this.event.getY(i) / this.scale) - this.offsetY;
    }

    public int getPointerCount() {
        return this.event.getPointerCount();
    }

    public int getActionMasked() {
        return this.event.getAction() & 255;
    }

    public TransformDiff getTransformDifference() {
        if (startTransformState == null) {
            startTransformState = new TransformState(this);
        }
        return startTransformState.calculateDiff(this);
    }

    private static class TransformState {
        private final boolean hasFixedCenterPoint;
        private final List<Point> points = new ArrayList();

        public TransformState(ScaledMotionEventWrapper scaledMotionEventWrapper) {
            int pointerCount = scaledMotionEventWrapper.getPointerCount();
            int i = 0;
            while (i < pointerCount) {
                Point point = this.points.size() > i ? this.points.get(i) : null;
                if (point == null) {
                    point = new Point();
                    this.points.add(point);
                }
                point.x = scaledMotionEventWrapper.getX(i);
                point.y = scaledMotionEventWrapper.getY(i);
                i++;
            }
            this.hasFixedCenterPoint = scaledMotionEventWrapper.hasFixedCenterPoint();
            if (this.hasFixedCenterPoint) {
                this.points.add(1, scaledMotionEventWrapper.fixedCenterPoint);
            }
        }

        public int getPointCount() {
            if (this.hasFixedCenterPoint) {
                return 1;
            }
            return this.points.size();
        }

        public float getDistance() {
            if (this.points.size() != 2) {
                return 1.0f;
            }
            Point point = this.points.get(0);
            Point point2 = this.points.get(1);
            return Math.max((float) Math.sqrt((double) (((point.x - point2.x) * (point.x - point2.x)) + ((point.y - point2.y) * (point.y - point2.y)))), 1.0f);
        }

        public float getAngle() {
            if (this.points.size() != 2) {
                return 0.0f;
            }
            Point point = this.points.get(0);
            Point point2 = this.points.get(1);
            float degrees = (float) Math.toDegrees(Math.atan2((double) (point.y - point2.y), (double) (point.x - point2.x)));
            return degrees < 0.0f ? 360.0f + degrees : degrees;
        }

        public Point getCenterPoint() {
            if (this.hasFixedCenterPoint) {
                return this.points.get(1);
            }
            if (this.points.size() == 2) {
                Point point = this.points.get(0);
                Point point2 = this.points.get(1);
                RectF rectF = new RectF(point.x, point.y, point2.x, point2.y);
                return new Point(rectF.centerX(), rectF.centerY());
            }
            Point point3 = this.points.get(0);
            return new Point(point3.x, point3.y);
        }

        public TransformDiff calculateDiff(ScaledMotionEventWrapper scaledMotionEventWrapper) {
            TransformState transformState = new TransformState(scaledMotionEventWrapper);
            Point centerPoint = getCenterPoint();
            Point centerPoint2 = transformState.getCenterPoint();
            return new TransformDiff(transformState.getDistance() - getDistance(), transformState.getAngle() - getAngle(), centerPoint2.x - centerPoint.x, centerPoint2.y - centerPoint.y, transformState.getDistance() / getDistance());
        }
    }

    public static class TransformDiff {
        public final float angleDiff;
        public final float distanceDiff;
        public final float scale;
        public final float xDiff;
        public final float yDiff;

        public TransformDiff(float distanceDiff, float angleDiff, float xDiff, float yDiff, float scale) {
            this.distanceDiff = distanceDiff;
            this.angleDiff = angleDiff;
            this.xDiff = xDiff;
            this.yDiff = yDiff;
            this.scale = scale;
        }
    }

    private static class Point {
        public float x = 0.0f;
        public float y = 0.0f;

        public Point() {
        }

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
