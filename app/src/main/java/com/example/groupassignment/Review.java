package com.example.groupassignment;
public class Review {
    private String name;
    private String profilePicURL;
    private double rating;
    private String relativeTime;
    private String text;

    public Review(String name, String profilePicURL, double rating, String relativeTime, String text){
        this.name = name;
        this.profilePicURL = profilePicURL;
        this.rating = rating;
        this.relativeTime = relativeTime;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getRelativeTime() {
        return relativeTime;
    }

    public void setRelativeTime(String relativeTime) {
        this.relativeTime = relativeTime;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getProfilePicURL() {
        return profilePicURL;
    }

    public void setProfilePicURL(String profilePicURL) {
        this.profilePicURL = profilePicURL;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
