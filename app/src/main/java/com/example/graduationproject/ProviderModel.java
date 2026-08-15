package com.example.graduationproject;

import com.google.firebase.firestore.PropertyName;
import com.google.gson.annotations.SerializedName;

public class ProviderModel {
    @SerializedName("id")
    private String id;

    @SerializedName("user_id")
    @PropertyName("user_id")
    private String userId;

    @SerializedName("business_name")
    @PropertyName("business_name")
    private String businessName;

    @SerializedName("provider_type")
    @PropertyName("provider_type")
    private String providerType; // 'truck', 'well', 'storage'

    @SerializedName("capacity")
    @PropertyName("capacity")
    private Integer capacity;

    @SerializedName("hose_length")
    @PropertyName("hose_length")
    private Integer hoseLength;

    @SerializedName("pump_type")
    @PropertyName("pump_type")
    private String pumpType;

    @SerializedName("status")
    @PropertyName("status")
    private String status; // 'active', 'busy', 'offline'

    @SerializedName("current_lat")
    @PropertyName("current_lat")
    private double currentLat;

    @SerializedName("current_lng")
    @PropertyName("current_lng")
    private double currentLng;

    @SerializedName("rating")
    @PropertyName("rating")
    private double rating;

    public ProviderModel() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @PropertyName("user_id")
    public String getUserId() { return userId; }
    @PropertyName("user_id")
    public void setUserId(String userId) { this.userId = userId; }

    @PropertyName("business_name")
    public String getBusinessName() { return businessName; }
    @PropertyName("business_name")
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    @PropertyName("provider_type")
    public String getProviderType() { return providerType; }
    @PropertyName("provider_type")
    public void setProviderType(String providerType) { this.providerType = providerType; }

    @PropertyName("capacity")
    public Integer getCapacity() { return capacity; }
    @PropertyName("capacity")
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    @PropertyName("hose_length")
    public Integer getHoseLength() { return hoseLength; }
    @PropertyName("hose_length")
    public void setHoseLength(Integer hoseLength) { this.hoseLength = hoseLength; }

    @PropertyName("pump_type")
    public String getPumpType() { return pumpType; }
    @PropertyName("pump_type")
    public void setPumpType(String pumpType) { this.pumpType = pumpType; }

    @PropertyName("status")
    public String getStatus() { return status; }
    @PropertyName("status")
    public void setStatus(String status) { this.status = status; }

    @PropertyName("current_lat")
    public double getCurrentLat() { return currentLat; }
    @PropertyName("current_lat")
    public void setCurrentLat(double currentLat) { this.currentLat = currentLat; }

    @PropertyName("current_lng")
    public double getCurrentLng() { return currentLng; }
    @PropertyName("current_lng")
    public void setCurrentLng(double currentLng) { this.currentLng = currentLng; }

    @PropertyName("rating")
    public double getRating() { return rating; }
    @PropertyName("rating")
    public void setRating(double rating) { this.rating = rating; }
}
