package com.scanny.scanner.scrapbook;

public class ImageXY {
    int angle;
    int x;
    int y;

    public ImageXY(int i, int i2, int i3) {
        this.x = i;
        this.y = i2;
        this.angle = i3;
    }

    public int getAngle() {
        return this.angle;
    }

    public void setAngle(int i) {
        this.angle = i;
    }

    public int getX() {
        return this.x;
    }

    public void setX(int i) {
        this.x = i;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int i) {
        this.y = i;
    }
}
