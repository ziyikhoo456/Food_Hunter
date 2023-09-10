package com.example.groupassignment;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Done by Chok Xin Yi
public class DataClass {
    private String dataDesc;
    private String dataImage;
    private int likes;
    private boolean likedByCurrentUser;
    private List<String> likedByUsernames;
    private String username;

    public int getLikes() {
        return likes;
    }

    public String getDataDesc() {
        return dataDesc;
    }

    public String getDataImage() {
        return dataImage;
    }

    public boolean isLikedByCurrentUser() {
        return likedByCurrentUser;
    }

    public void setLikedByCurrentUser(boolean likedByCurrentUser) {
        this.likedByCurrentUser = likedByCurrentUser;
    }

    public List<String> getLikedByUsernames() {
        return likedByUsernames;
    }

    public void setLikedByUsernames(List<String> likedByUsernames) {
        this.likedByUsernames = likedByUsernames;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public void setDataDesc(String dataDesc) {
        this.dataDesc = dataDesc;
    }

    public void setDataImage(String dataImage) {
        this.dataImage = dataImage;
    }

    public DataClass() {
        likedByUsernames = new ArrayList<>();
    }

    public DataClass(String dataDesc, String dataImage) {
        this.dataDesc = dataDesc;
        this.dataImage = dataImage;
        this.likes = 0;
    }
}
