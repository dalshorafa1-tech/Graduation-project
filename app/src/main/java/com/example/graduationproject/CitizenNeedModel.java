package com.example.graduationproject;

import com.google.firebase.Timestamp;

public class CitizenNeedModel {
    private String id;
    private String region;
    private String needType;
    private String description;
    private String status;
    private Timestamp createdAt;

    public CitizenNeedModel() {}

    public CitizenNeedModel(String id, String region, String needType, String description, String status) {
        this.id = id;
        this.region = region;
        this.needType = needType;
        this.description = description;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getNeedType() { return needType; }
    public void setNeedType(String needType) { this.needType = needType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
