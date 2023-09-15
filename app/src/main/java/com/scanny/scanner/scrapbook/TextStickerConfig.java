package com.scanny.scanner.scrapbook;

import android.graphics.Paint;
import android.graphics.Typeface;

import java.util.UUID;

public class TextStickerConfig implements StickerConfigInterface {
    private final String identifierId = UUID.randomUUID().toString();
    private Paint.Align align;
    private int backgroundColor;
    private int color;
    private Typeface font;
    private String text;

    public TextStickerConfig(String str, Paint.Align align2, Typeface typeface, int i, int i2) {
        this.text = str;
        this.color = i;
        this.font = typeface;
        this.backgroundColor = i2;
        this.align = align2;
    }

    @Override
    public int getStickerId() {
        return -1;
    }

    @Override
    public StickerConfigInterface.STICKER_TYPE getType() {
        return StickerConfigInterface.STICKER_TYPE.TEXT;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String str) {
        this.text = str;
    }

    public Paint.Align getAlign() {
        return this.align;
    }

    public void setAlign(Paint.Align align2) {
        this.align = align2;
    }

    public Typeface getTypeface() {
        Typeface typeface = this.font;
        if (typeface == null) {
            return null;
        }
        return typeface;
    }

    public int getColor() {
        return this.color;
    }

    public void setColor(int i) {
        this.color = i;
    }

    public int getBackgroundColor() {
        return this.backgroundColor;
    }

    public void setBackgroundColor(int i) {
        this.backgroundColor = i;
    }

    public void setText(String str, Paint.Align align2) {
        this.text = str;
        this.align = align2;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return this.identifierId.equals(((TextStickerConfig) obj).identifierId);
    }

    @Override
    public int hashCode() {
        return this.identifierId.hashCode();
    }

    @Override
    public String toString() {
        return "TextStickerConfig{text='" + this.text + '\'' + ", Pack 1=" + this.font + ", color=" + this.color + ", backgroundColor=" + this.backgroundColor + ", align=" + this.align + ", identifierId='" + this.identifierId + '\'' + '}';
    }
}
