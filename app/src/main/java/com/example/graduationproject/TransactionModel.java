package com.example.graduationproject;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

public class TransactionModel {
    @Exclude
    private String id;
    private String type; // 'recharge' or 'payment'
    private Double amount;
    private String description;
    private Timestamp timestamp;

    public TransactionModel() {
        // Required for Firestore
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
