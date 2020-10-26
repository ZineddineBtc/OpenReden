package com.example.openreden.model;

import android.graphics.Bitmap;

public class Photo {
    private Bitmap bitmap;
    private String storageReference;

    public Photo(Bitmap bitmap, String storageReference) {
        this.bitmap = bitmap;
        this.storageReference = storageReference;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getStorageReference() {
        return storageReference;
    }

    public void setStorageReference(String storageReference) {
        this.storageReference = storageReference;
    }
}
