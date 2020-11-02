package com.example.openreden.model;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class User {
    private String id, username, name, bio, city;
    private Bitmap photoBitmap;
    private byte[] bytes;
    private ArrayList<String> galleryReferences;

    public User(){}
    public User(String id, String username, String name) {
        this.id = id;
        this.username = username;
        this.name = name;
    }

    public User(String id, String username, String name, String bio, String city) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.bio = bio;
        this.city = city;
    }

    public byte[] getPhotoBytes() {
        return bytes;
    }

    public void setPhotoBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public Bitmap getPhotoBitmap() {
        return photoBitmap;
    }

    public void setPhotoBitmap(Bitmap photoBitmap) {
        this.photoBitmap = photoBitmap;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public ArrayList<String> getGalleryReferences() {
        return galleryReferences;
    }

    public void setGalleryReferences(ArrayList<String> galleryReferences) {
        this.galleryReferences = galleryReferences;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
