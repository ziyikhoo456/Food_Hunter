package com.example.groupassignment.Model;

//Done by Kuek Gavin
public class User {

    private String id;
    private String username;
    private String password;
    private String email;
    private String profileImg;

    public User(String id, String username, String email, String profileImg) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.profileImg = profileImg;
    }

    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }
}
