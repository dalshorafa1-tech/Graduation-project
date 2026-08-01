package com.example.graduationproject;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;

public class UserModel {
    private String id;
    private String full_name;
    private String email;
    private String phone;
    private String id_number;
    private String region;
    private String role;
    private boolean is_provider;
    private Timestamp created_at;

    public UserModel() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @PropertyName("full_name")
    public String getFullName() { return full_name; }
    @PropertyName("full_name")
    public void setFullName(String fullName) { this.full_name = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    @PropertyName("id_number")
    public String getIdNumber() { return id_number; }
    @PropertyName("id_number")
    public void setIdNumber(String idNumber) { this.id_number = idNumber; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @PropertyName("is_provider")
    public boolean isProvider() { return is_provider; }
    @PropertyName("is_provider")
    public void setProvider(boolean provider) { is_provider = provider; }

    @PropertyName("created_at")
    public Timestamp getCreatedAt() { return created_at; }
    @PropertyName("created_at")
    public void setCreatedAt(Timestamp createdAt) { this.created_at = createdAt; }
}
