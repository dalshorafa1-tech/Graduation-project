package com.example.graduationproject;

import com.google.firebase.firestore.Exclude;

public class NotificationModel {

    @Exclude
    private String id;

    private String provider_id;
    private String title;
    private String message;
    private String type;
    private String order_id;
    private boolean is_read;
    private Object created_at;

    public NotificationModel() {}

    @Exclude
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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

    public boolean isRead() { return is_read; }
    public void setRead(boolean is_read) { this.is_read = is_read; }

    public Object getCreated_at() { return created_at; }
    public void setCreated_at(Object created_at) { this.created_at = created_at; }
}
