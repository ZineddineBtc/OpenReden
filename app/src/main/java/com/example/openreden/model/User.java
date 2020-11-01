package com.example.openreden.model;

import java.util.ArrayList;

public class User {
    private String id, username, name, bio, city;
    private ArrayList<String> galleryReferences;

    public User(){}
    public User(String id, String username, String name) {
        this.id = id;
        this.username = username;
        this.name = name;
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
