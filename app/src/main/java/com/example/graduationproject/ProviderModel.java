package com.example.graduationproject;

import com.google.gson.annotations.SerializedName;

public class ProviderModel {
    @SerializedName("id")
    private int id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("business_name")
    private String businessName;

    @SerializedName("provider_type")
    private String providerType; // 'truck', 'well', 'storage'

    @SerializedName("capacity")
    private Integer capacity;

    @SerializedName("hose_length")
    private Integer hoseLength;

    @SerializedName("pump_type")
    private String pumpType;

    @SerializedName("status")
    private String status; // 'active', 'busy', 'offline'

    @SerializedName("current_lat")
    private double currentLat;

    @SerializedName("current_lng")
    private double currentLng;

    @SerializedName("rating")
    private double rating;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    public String getProviderType() { return providerType; }
    public void setProviderType(String providerType) { this.providerType = providerType; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public Integer getHoseLength() { return hoseLength; }
    public void setHoseLength(Integer hoseLength) { this.hoseLength = hoseLength; }
    public String getPumpType() { return pumpType; }
    public void setPumpType(String pumpType) { this.pumpType = pumpType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getCurrentLat() { return currentLat; }
    public void setCurrentLat(double currentLat) { this.currentLat = currentLat; }
    public double getCurrentLng() { return currentLng; }
    public void setCurrentLng(double currentLng) { this.currentLng = currentLng; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
}
