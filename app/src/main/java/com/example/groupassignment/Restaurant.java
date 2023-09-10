package com.example.groupassignment;

//Done by Khoo Zi Yi
public class Restaurant {
    private String name, placeID, photoReference;
    private boolean opening_now, addedWishlist=false;
    private double rating, distance;
    private int priceLevel, userRatingTotal;

    public Restaurant(String name, double rating, String placeID, String photoReference, boolean opening_now, double distance, int priceLevel, int userRatingTotal) {
        this.name = name;
        this.rating = rating;
        this.placeID = placeID;
        this.photoReference = photoReference;
        this.opening_now = opening_now;
        this.distance = distance;
        this.priceLevel = priceLevel;
        this.userRatingTotal = userRatingTotal;
    }

    public String getName() {
        return name;
    }

    public double getRating() {
        return rating;
    }

    public String getPlaceID() {
        return placeID;
    }
    public String getPhotoReference() {
        return photoReference;
    }

    public boolean isOpening_now() {
        return opening_now;
    }

    public void setOpening_now(boolean opening_now) {
        this.opening_now = opening_now;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getPriceLevel() {
        return priceLevel;
    }

    public void setPriceLevel(int priceLevel) {
        this.priceLevel = priceLevel;
    }

    public int getUserRatingTotal() {
        return userRatingTotal;
    }

    public void setUserRatingTotal(int userRatingTotal) {
        this.userRatingTotal = userRatingTotal;
    }

    public boolean isAddedWishlist() {
        return addedWishlist;
    }

    public void setAddedWishlist(boolean addedWishlist) {
        this.addedWishlist = addedWishlist;
    }
}
