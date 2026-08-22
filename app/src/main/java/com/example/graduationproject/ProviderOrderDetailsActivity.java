package com.example.graduationproject;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.Locale;

public class ProviderOrderDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TextView tvOrderNumber, tvOrderStatus, tvCustomerName, tvCustomerAddress, tvWaterType, tvWaterQty, tvPriceTotal;
    private ImageView imgCustomer, ivOrderReceipt;
    private CardView cardReceipt;
    private MaterialButton btnConfirmArrival, btnCompleteTask;
    private View btnCallCustomer;
    private MapView map;
    private MapboxMap mapboxMap;
    private FirebaseFirestore db;
    private String orderId, customerPhone;
    private ListenerRegistration orderListener;
    private LatLng deliveryLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize Mapbox
        Mapbox.getInstance(this);
        
        setContentView(R.layout.activity_provider_order_details);

        db = FirebaseFirestore.getInstance();
        orderId = getIntent().getStringExtra("order_id");

        initViews(savedInstanceState);
        
        if (orderId != null) {
            fetchOrderDetails();
        } else {
            Toast.makeText(this, "خطأ: لم يتم العثور على معرف الطلب", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews(Bundle savedInstanceState) {
        tvOrderNumber = findViewById(R.id.tvOrderNumber);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvCustomerName = findViewById(R.id.tvCustomerName);
        tvCustomerAddress = findViewById(R.id.tvCustomerAddress);
        tvWaterType = findViewById(R.id.tvWaterType);
        tvWaterQty = findViewById(R.id.tvWaterQty);
        tvPriceTotal = findViewById(R.id.tvPriceTotal);
        imgCustomer = findViewById(R.id.imgCustomer);
        ivOrderReceipt = findViewById(R.id.ivOrderReceipt);
        cardReceipt = findViewById(R.id.cardReceipt);
        btnConfirmArrival = findViewById(R.id.btnConfirmArrival);
        btnCompleteTask = findViewById(R.id.btnCompleteTask);
        btnCallCustomer = findViewById(R.id.btnCallCustomer);
        map = findViewById(R.id.mapView);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        findViewById(R.id.btnNotification).setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationsActivity.class));
        });

        btnCallCustomer.setOnClickListener(v -> {
            if (customerPhone != null && !customerPhone.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + customerPhone));
                startActivity(intent);
            } else {
                Toast.makeText(this, "رقم هاتف الزبون غير متوفر", Toast.LENGTH_SHORT).show();
            }
        });

        btnConfirmArrival.setOnClickListener(v -> updateStatus("on_way"));
        btnCompleteTask.setOnClickListener(v -> updateStatus("delivered"));

        if (map != null) {
            map.onCreate(savedInstanceState);
            map.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(new Style.Builder().fromUri("https://tiles.openfreemap.org/styles/bright"), style -> {
            if (deliveryLocation != null) {
                updateMapMarker(deliveryLocation);
            }
        });
    }

    private void fetchOrderDetails() {
        // استخدام SnapshotListener لضمان تحديث البيانات لحظياً من الفايربيز
        orderListener = db.collection("orders").document(orderId).addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.e("Details", "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                OrderModel order = snapshot.toObject(OrderModel.class);
                if (order == null) return;

                // ربط البيانات بالواجهة
                tvOrderNumber.setText("#" + orderId.substring(0, Math.min(orderId.length(), 6)).toUpperCase());
                tvCustomerAddress.setText(order.getAddressDetails() != null ? "📍 " + order.getAddressDetails() : "📍 العنوان غير محدد");
                tvWaterType.setText(order.getOrderType() != null ? order.getOrderType() : "تزويد مياه");
                tvWaterQty.setText(order.getQuantity() + " " + (order.getUnit() != null ? order.getUnit() : "لتر"));
                tvPriceTotal.setText(String.format(Locale.getDefault(), "%.2f ₪", order.getTotalPrice() != null ? order.getTotalPrice() : 0.0));

                updateStatusUI(order.getStatus());
                
                // عرض إشعار الدفع إذا وجد
                if (order.getReceiptUrl() != null && !order.getReceiptUrl().isEmpty()) {
                    cardReceipt.setVisibility(View.VISIBLE);
                    Glide.with(this).load(order.getReceiptUrl()).into(ivOrderReceipt);
                } else {
                    cardReceipt.setVisibility(View.GONE);
                }

                // تحديث موقع الزبون على الخريطة
                if (order.getDeliveryLat() != 0) {
                    deliveryLocation = new LatLng(order.getDeliveryLat(), order.getDeliveryLng());
                    if (mapboxMap != null && mapboxMap.getStyle() != null) {
                        updateMapMarker(deliveryLocation);
                    }
                }

                // جلب بيانات الزبون الإضافية (الاسم، الهاتف، الصورة)
                if (order.getCustomerId() != null) {
                    fetchCustomerInfo(order.getCustomerId());
                }
            }
        });
    }

    private void updateMapMarker(LatLng point) {
        if (mapboxMap == null) return;
        mapboxMap.clear();
        mapboxMap.addMarker(new MarkerOptions()
                .position(point)
                .title("موقع الزبون"));
        
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(point)
                        .zoom(15.0)
                        .build()
        ));
    }

    private void fetchCustomerInfo(String customerId) {
        db.collection("users").document(customerId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                tvCustomerName.setText(doc.getString("full_name"));
                customerPhone = doc.getString("phone");
                String photoUrl = doc.getString("profile_image");
                
                if (photoUrl != null && !photoUrl.isEmpty()) {
                    Glide.with(this).load(photoUrl).placeholder(R.drawable.user).into(imgCustomer);
                }
            }
        });
    }

    private void updateStatusUI(String status) {
        if (status == null) return;
        switch (status) {
            case "accepted":
                tvOrderStatus.setText("● تم القبول");
                tvOrderStatus.setTextColor(Color.parseColor("#3B82F6"));
                btnConfirmArrival.setVisibility(View.VISIBLE);
                btnCompleteTask.setVisibility(View.GONE);
                break;
            case "on_way":
                tvOrderStatus.setText("● في الطريق");
                tvOrderStatus.setTextColor(Color.parseColor("#10B981"));
                btnConfirmArrival.setVisibility(View.GONE);
                btnCompleteTask.setVisibility(View.VISIBLE);
                break;
            case "delivered":
                tvOrderStatus.setText("● مكتمل");
                tvOrderStatus.setTextColor(Color.parseColor("#15803D"));
                btnConfirmArrival.setVisibility(View.GONE);
                btnCompleteTask.setVisibility(View.GONE);
                break;
            case "cancelled":
                tvOrderStatus.setText("● ملغي");
                tvOrderStatus.setTextColor(Color.parseColor("#EF4444"));
                btnConfirmArrival.setVisibility(View.GONE);
                btnCompleteTask.setVisibility(View.GONE);
                break;
        }
    }

    private void updateStatus(String status) {
        db.collection("orders").document(orderId).update("status", status)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "تم تحديث حالة الطلب", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "فشل التحديث", Toast.LENGTH_SHORT).show());
    }

    @Override protected void onStart() { super.onStart(); if (map != null) map.onStart(); }
    @Override protected void onResume() { super.onResume(); if (map != null) map.onResume(); }
    @Override protected void onPause() { super.onPause(); if (map != null) map.onPause(); }
    @Override protected void onStop() { super.onStop(); if (map != null) map.onStop(); }
    @Override protected void onSaveInstanceState(@NonNull Bundle outState) { super.onSaveInstanceState(outState); if (map != null) map.onSaveInstanceState(outState); }
    @Override public void onLowMemory() { super.onLowMemory(); if (map != null) map.onLowMemory(); }
    @Override protected void onDestroy() { 
        if (orderListener != null) orderListener.remove();
        if (map != null) map.onDestroy(); 
        super.onDestroy();
    }
}
