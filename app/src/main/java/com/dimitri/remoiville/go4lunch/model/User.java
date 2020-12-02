package com.dimitri.remoiville.go4lunch.model;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String mUserID;
    private String mFirstName;
    private String mLastName;
    private String mMail;
    private String mURLProfilePicture;
    private List<String> mLikesList;
    private String mRestaurantID;
    private String mRestaurantName;

    private String defaultURLPicture = "https://firebasestorage.googleapis.com/v0/b/go4lunch-63219/o/default_picture.png?alt=media&token=421a8985-e6ce-4d7c-a0cb-da1d2bc93799";

    public User() {
    }

    public User(String userID, String firstName, String lastName, String mail) {
        mUserID = userID;
        mFirstName = firstName;
        mLastName = lastName;
        mMail = mail;
        mURLProfilePicture = randomImage();
        mLikesList = new ArrayList<>();
        mRestaurantID = null;
        mRestaurantName = null;
    }


    public User(String userID, String firstName, String lastName, String mail, String URLProfilePicture, List<String> likesList, String restaurantID, String restaurantName) {
        mUserID = userID;
        mFirstName = firstName;
        mLastName = lastName;
        mMail = mail;
        mURLProfilePicture = URLProfilePicture;
        mLikesList = likesList;
        mRestaurantID = restaurantID;
        mRestaurantName = restaurantName;
    }

    public String getUserID() {
        return mUserID;
    }

    public void setUserID(String userID) {
        mUserID = userID;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String firstName) {
        mFirstName = firstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String lastName) {
        mLastName = lastName;
    }

    public String getMail() {
        return mMail;
    }

    public void setMail(String mail) {
        mMail = mail;
    }

    public String getURLProfilePicture() {
        return mURLProfilePicture;
    }

    public void setURLProfilePicture(String URLProfilePicture) {
        mURLProfilePicture = URLProfilePicture;
    }

    public List<String> getLikesList() {
        return mLikesList;
    }

    public void setLikesList(List<String> likesList) {
        mLikesList = likesList;
    }

    public String getRestaurantID() {
        return mRestaurantID;
    }

    public void setRestaurantID(String restaurantID) {
        this.mRestaurantID = restaurantID;
    }

    public String getRestaurantName() {
        return mRestaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        mRestaurantName = restaurantName;
    }

    @Override
    public String toString() {
        return "User{" +
                "mUserID='" + mUserID + '\'' +
                ", mFirstName='" + mFirstName + '\'' +
                ", mLastName='" + mLastName + '\'' +
                ", mMail='" + mMail + '\'' +
                ", mURLProfilePicture='" + mURLProfilePicture + '\'' +
                ", mLikesList=" + mLikesList +
                ", mLunchRestaurantID='" + mRestaurantID + '\'' +
                ", mRestaurantName='" + mRestaurantName + '\'' +
                '}';
    }

    private String randomImage() {
        return "https://i.pravatar.cc/300?u="+ System.currentTimeMillis();
    }
}