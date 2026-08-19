package com.example.graduationproject;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class InitiativeModel {
    private String id;
    private String initiatorId;
    private String title;
    private String location;
    private double latitude;
    private double longitude;
    private int targetLiters;
    private int currentLiters;
    private String fundingType;
    private String providerName;
    private String providerId; // تم إضافة معرف المزود
    private double estimatedCost;
    private String status; // مثلاً: "نشط"، "مكتمل"، "قيد المراجعة"

    @ServerTimestamp
    private Date createdAt;

    public InitiativeModel() {}

    public InitiativeModel(String id, String initiatorId, String title, String location, 
                           double latitude, double longitude, int targetLiters, 
                           int currentLiters, String fundingType, String providerName, 
                           String providerId, double estimatedCost, String status) {
        this.id = id;
        this.initiatorId = initiatorId;
        this.title = title;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.targetLiters = targetLiters;
        this.currentLiters = currentLiters;
        this.fundingType = fundingType;
        this.providerName = providerName;
        this.providerId = providerId;
        this.estimatedCost = estimatedCost;
        this.status = status;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getInitiatorId() { return initiatorId; }
    public void setInitiatorId(String initiatorId) { this.initiatorId = initiatorId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public int getTargetLiters() { return targetLiters; }
    public void setTargetLiters(int targetLiters) { this.targetLiters = targetLiters; }
    public int getCurrentLiters() { return currentLiters; }
    public void setCurrentLiters(int currentLiters) { this.currentLiters = currentLiters; }
    public String getFundingType() { return fundingType; }
    public void setFundingType(String fundingType) { this.fundingType = fundingType; }
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
    public double getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(double estimatedCost) { this.estimatedCost = estimatedCost; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public int getProgressPercentage() {
        if (targetLiters <= 0) return 0;
        return (int) (((float) currentLiters / targetLiters) * 100);
    }
}
