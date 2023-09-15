package com.scanny.scanner.scrapbook;

public interface StickerConfigInterface {

    int getStickerId();

    STICKER_TYPE getType();

    public enum STICKER_TYPE {
        IMAGE,
        TEXT,
        STICKER
    }
}
