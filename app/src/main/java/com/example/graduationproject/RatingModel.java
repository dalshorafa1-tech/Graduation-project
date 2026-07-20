package com.example.graduationproject;

import com.google.gson.annotations.SerializedName;

public class RatingModel {
    @SerializedName("order_id")
    private String orderId;

    @SerializedName("customer_id")
    private String customerId;

    @SerializedName("provider_id")
    private Integer providerId;

    @SerializedName("rating")
    private int rating;

    @SerializedName("comment")
    private String comment;

    public RatingModel(String orderId, String customerId, Integer providerId, int rating, String comment) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.providerId = providerId;
        this.rating = rating;
        this.comment = comment;
    }

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public Integer getProviderId() { return providerId; }
    public void setProviderId(Integer providerId) { this.providerId = providerId; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
