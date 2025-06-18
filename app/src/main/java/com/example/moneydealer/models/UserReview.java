package com.example.moneydealer.models;

public class UserReview {
    public String uid;
    public int stars;
    public String comment;
    public long timestamp;

    public UserReview() {}

    public UserReview(String uid, int stars, String comment, long timestamp) {
        this.uid = uid;
        this.stars = stars;
        this.comment = comment;
        this.timestamp = timestamp;
    }
} 