package com.scanny.scanner.scrapbook;

import android.graphics.Bitmap;

public class ImageStickerConfig implements StickerConfigInterface {
    Bitmap bitmap;
    int stickerId;
    StickerConfigInterface.STICKER_TYPE stickerType;

    public ImageStickerConfig(Bitmap bitmap2, StickerConfigInterface.STICKER_TYPE sticker_type) {
        this.bitmap = bitmap2;
        this.stickerType = sticker_type;
    }

    @Override
    public int getStickerId() {
        return this.stickerId;
    }

    @Override
    public StickerConfigInterface.STICKER_TYPE getType() {
        return StickerConfigInterface.STICKER_TYPE.IMAGE;
    }

    public Bitmap getBitmapImage() {
        return this.bitmap;
    }
}
