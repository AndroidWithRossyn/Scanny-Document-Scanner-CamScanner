package com.scanny.scanner.models;

public class PhotoSizeModel {
    private int photo_dpi;
    private double photo_height;
    private String photo_unit;
    private double photo_width;

    public PhotoSizeModel(double d, double d2, String str, int i) {
        this.photo_width = d;
        this.photo_height = d2;
        this.photo_unit = str;
        this.photo_dpi = i;
    }

    public double getPhoto_width() {
        return this.photo_width;
    }

    public void setPhoto_width(double d) {
        this.photo_width = d;
    }

    public double getPhoto_height() {
        return this.photo_height;
    }

    public void setPhoto_height(double d) {
        this.photo_height = d;
    }

    public String getPhoto_unit() {
        return this.photo_unit;
    }

    public void setPhoto_unit(String str) {
        this.photo_unit = str;
    }

    public int getPhoto_dpi() {
        return this.photo_dpi;
    }

    public void setPhoto_dpi(int i) {
        this.photo_dpi = i;
    }
}
