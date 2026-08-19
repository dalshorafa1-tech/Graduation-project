package com.example.graduationproject;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

public class NotificationModel {

    @Exclude
    private String id;

    private String userId; // معرف المستخدم العام (للمسؤول والمبادر)
    private String provider_id; // معرف المزود (للتوافق مع لوحة تحكم المزود)
    private String title;
    private String message;
    private String type;
    private String order_id;
    private boolean read;
    private Timestamp created_at;

    public NotificationModel() {}

    @Exclude
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProvider_id() { return provider_id; }
    public void setProvider_id(String provider_id) { this.provider_id = provider_id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getOrder_id() { return order_id; }
    public void setOrder_id(String order_id) { this.order_id = order_id; }

    @PropertyName("read")
    public boolean isRead() { return read; }
    
    @PropertyName("read")
    public void setRead(boolean read) { this.read = read; }

    public Timestamp getCreated_at() { return created_at; }
    public void setCreated_at(Timestamp created_at) { this.created_at = created_at; }
}
