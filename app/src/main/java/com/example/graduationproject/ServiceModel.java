package com.example.graduationproject;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;

public class ServiceModel {
    private String id;
    @PropertyName("provider_id")
    private String providerId;
    @PropertyName("provider_name")
    private String providerName;
    @PropertyName("provider_type")
    private String providerType;
    @PropertyName("service_type")
    private String serviceType;
    @PropertyName("provider_phone")
    private String providerPhone;
    @PropertyName("provider_email")
    private String providerEmail;
    @PropertyName("provider_id_number")
    private String providerIdNumber;
    @PropertyName("municipality_code")
    private String municipalityCode;
    private String region;
    @PropertyName("name_ar")
    private String nameAr;
    @PropertyName("description_ar")
    private String descriptionAr;
    private double price;
    @PropertyName("priceCup")
    private double priceCup;
    private String status; // pending, approved, rejected
    @PropertyName("isActive")
    private boolean active;
    private String imageUrl;
    private String rejectReason;
    private double latitude;
    private double longitude;
    @PropertyName("created_at")
    private Timestamp createdAt;

    public ServiceModel() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    @PropertyName("provider_id")
    public String getProviderId() { return providerId; }
    @PropertyName("provider_id")
    public void setProviderId(String providerId) { this.providerId = providerId; }
    
    @PropertyName("provider_name")
    public String getProviderName() { return providerName; }
    @PropertyName("provider_name")
    public void setProviderName(String providerName) { this.providerName = providerName; }
    
    @PropertyName("provider_type")
    public String getProviderType() { return providerType; }
    @PropertyName("provider_type")
    public void setProviderType(String providerType) { this.providerType = providerType; }

    @PropertyName("service_type")
    public String getServiceType() { return serviceType; }
    @PropertyName("service_type")
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    
    @PropertyName("provider_phone")
    public String getProviderPhone() { return providerPhone; }
    @PropertyName("provider_phone")
    public void setProviderPhone(String providerPhone) { this.providerPhone = providerPhone; }

    @PropertyName("provider_email")
    public String getProviderEmail() { return providerEmail; }
    @PropertyName("provider_email")
    public void setProviderEmail(String providerEmail) { this.providerEmail = providerEmail; }

    @PropertyName("provider_id_number")
    public String getProviderIdNumber() { return providerIdNumber; }
    @PropertyName("provider_id_number")
    public void setProviderIdNumber(String providerIdNumber) { this.providerIdNumber = providerIdNumber; }

    @PropertyName("municipality_code")
    public String getMunicipalityCode() { return municipalityCode; }
    @PropertyName("municipality_code")
    public void setMunicipalityCode(String municipalityCode) { this.municipalityCode = municipalityCode; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    
    @PropertyName("name_ar")
    public String getNameAr() { return nameAr; }
    @PropertyName("name_ar")
    public void setNameAr(String nameAr) { this.nameAr = nameAr; }
    
    @PropertyName("description_ar")
    public String getDescriptionAr() { return descriptionAr; }
    @PropertyName("description_ar")
    public void setDescriptionAr(String descriptionAr) { this.descriptionAr = descriptionAr; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    @PropertyName("priceCup")
    public double getPriceCup() { return priceCup; }
    @PropertyName("priceCup")
    public void setPriceCup(double priceCup) { this.priceCup = priceCup; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @PropertyName("isActive")
    public boolean isActive() { return active; }
    @PropertyName("isActive")
    public void setActive(boolean active) { this.active = active; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    
    @PropertyName("created_at")
    public Timestamp getCreatedAt() { return createdAt; }
    @PropertyName("created_at")
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
