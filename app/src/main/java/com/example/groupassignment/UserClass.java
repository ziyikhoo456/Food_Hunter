package com.example.groupassignment;

import java.util.HashMap;
import java.util.Map;

public class UserClass {
    private String email;
    private String username;
    private String userId;

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public UserClass(){

    }

    public UserClass(String username, String email, String userId) {
        this.username = username;
        this.email = email;
        this.userId = userId;
    }

}
