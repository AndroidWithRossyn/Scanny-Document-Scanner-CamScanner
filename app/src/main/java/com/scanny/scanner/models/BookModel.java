package com.scanny.scanner.models;

import android.graphics.Bitmap;

public class BookModel {
    private Bitmap bitmap;
    private String page_name;
    private int pos;

    public BookModel(Bitmap bitmap2, String str, int i) {
        this.bitmap = bitmap2;
        this.page_name = str;
        this.pos = i;
    }

    public Bitmap getBitmap() {
        return this.bitmap;
    }

    public void setBitmap(Bitmap bitmap2) {
        this.bitmap = bitmap2;
    }

    public String getPage_name() {
        return this.page_name;
    }

    public void setPage_name(String str) {
        this.page_name = str;
    }

    public int getPos() {
        return this.pos;
    }

    public void setPos(int i) {
        this.pos = i;
    }
}
