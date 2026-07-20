package com.example.graduationproject;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

public class OrderModel {
    @Exclude
    private String id;

    private String customer_id;
    private String provider_id;
    private String provider_name;
    private String service_id;
    private String status;
    private String order_type;
    private int quantity;
    private String unit;
    private Double total_price;
    private double delivery_lat;
    private double delivery_lng;
    private String address_details;
    private String notes;
    private String scheduled_time;
    private Object created_at;

    public OrderModel() {}

    @Exclude
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @PropertyName("customer_id")
    public String getCustomerId() { return customer_id; }
    @PropertyName("customer_id")
    public void setCustomerId(String customerId) { this.customer_id = customerId; }

    @PropertyName("provider_id")
    public String getProviderId() { return provider_id; }
    @PropertyName("provider_id")
    public void setProviderId(String providerId) { this.provider_id = providerId; }

    @PropertyName("provider_name")
    public String getProviderName() { return provider_name; }
    @PropertyName("provider_name")
    public void setProviderName(String providerName) { this.provider_name = providerName; }

    @PropertyName("service_id")
    public String getServiceId() { return service_id; }
    @PropertyName("service_id")
    public void setServiceId(String serviceId) { this.service_id = serviceId; }

    @PropertyName("status")
    public String getStatus() { return status; }
    @PropertyName("status")
    public void setStatus(String status) { this.status = status; }

    @PropertyName("order_type")
    public String getOrderType() { return order_type; }
    @PropertyName("order_type")
    public void setOrderType(String orderType) { this.order_type = orderType; }

    @PropertyName("total_price")
    public Double getTotalPrice() { return total_price; }
    @PropertyName("total_price")
    public void setTotalPrice(Double totalPrice) { this.total_price = totalPrice; }

    @PropertyName("delivery_lat")
    public double getDeliveryLat() { return delivery_lat; }
    @PropertyName("delivery_lat")
    public void setDeliveryLat(double deliveryLat) { this.delivery_lat = deliveryLat; }

    @PropertyName("delivery_lng")
    public double getDeliveryLng() { return delivery_lng; }
    @PropertyName("delivery_lng")
    public void setDeliveryLng(double deliveryLng) { this.delivery_lng = deliveryLng; }

    @PropertyName("address_details")
    public String getAddressDetails() { return address_details; }
    @PropertyName("address_details")
    public void setAddressDetails(String addressDetails) { this.address_details = addressDetails; }

    @PropertyName("quantity")
    public int getQuantity() { return quantity; }
    @PropertyName("quantity")
    public void setQuantity(int quantity) { this.quantity = quantity; }

    @PropertyName("unit")
    public String getUnit() { return unit; }
    @PropertyName("unit")
    public void setUnit(String unit) { this.unit = unit; }

    @PropertyName("notes")
    public String getNotes() { return notes; }
    @PropertyName("notes")
    public void setNotes(String notes) { this.notes = notes; }

    @PropertyName("scheduled_time")
    public String getScheduledTime() { return scheduled_time; }
    @PropertyName("scheduled_time")
    public void setScheduledTime(String scheduledTime) { this.scheduled_time = scheduledTime; }

    @PropertyName("created_at")
    public Object getCreatedAt() { return created_at; }
    @PropertyName("created_at")
    public void setCreatedAt(Object createdAt) { this.created_at = createdAt; }
}
