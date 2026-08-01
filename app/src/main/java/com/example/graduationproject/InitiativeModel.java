package com.example.graduationproject;

import com.google.firebase.Timestamp;

public class InitiativeModel {
    private String id;
    private String title;
    private String description;
    private int progress;
    private String target;
    private String status;
    private Timestamp createdAt;

    public InitiativeModel() {}

    public InitiativeModel(String id, String title, String description, int progress, String target, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.progress = progress;
        this.target = target;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
