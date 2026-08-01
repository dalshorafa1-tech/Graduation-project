package com.example.graduationproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.HashMap;
import java.util.Map;

public class Review_Order_Activity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private MapboxMap mapboxMap;
    private LatLng deliveryLoc;

    private int quantity;
    private String unit, address, notes, scheduledTime;
    private String providerId, providerName, serviceId;
    private double totalPrice;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private TextView tvServiceName, tvLocationMain, tvOrderNotes, tvWaterPrice, tvTotalPriceMain, tvFooterPriceText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Mapbox
        Mapbox.getInstance(this);

        setContentView(R.layout.activity_review_order);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews(savedInstanceState);

        Intent data = getIntent();
        serviceId = data.getStringExtra("service_id");
        if (serviceId == null && data.hasExtra("service_id")) {
            serviceId = String.valueOf(data.getIntExtra("service_id", 0));
        }

        providerId = data.getStringExtra("provider_id");
        providerName = data.getStringExtra("provider_name");
        quantity = data.getIntExtra("quantity", 500);
        unit = data.getStringExtra("unit");
        address = data.getStringExtra("address");
        notes = data.getStringExtra("notes");
        scheduledTime = data.getStringExtra("scheduledTime");

        double lat = data.getDoubleExtra("lat", 31.516);
        double lng = data.getDoubleExtra("lng", 34.448);
        deliveryLoc = new LatLng(lat, lng);

        if (data.hasExtra("total_price_from_plan")) {
            totalPrice = data.getDoubleExtra("total_price_from_plan", 0.0);
            updateUI("اشتراك مياه شهري");
        } else if (serviceId != null && !serviceId.isEmpty() && !serviceId.equals("0")) {
            fetchRealDataAndCalculate();
        } else {
            Toast.makeText(this, "بيانات الخدمة غير مكتملة", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews(Bundle savedInstanceState) {
        tvServiceName = findViewById(R.id.tvServiceName);
        tvLocationMain = findViewById(R.id.tvLocationMain);
        tvOrderNotes = findViewById(R.id.tvOrderNotes);
        tvWaterPrice = findViewById(R.id.tvWaterPrice);
        tvTotalPriceMain = findViewById(R.id.tvTotalPriceMain);
        tvFooterPriceText = findViewById(R.id.tvFooterPriceText);
        mapView = findViewById(R.id.mapViewReview);

        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnConfirmAndSend).setOnClickListener(v -> saveOrderToFirebase());
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(new Style.Builder().fromUri("https://tiles.openfreemap.org/styles/bright"), style -> {
            if (deliveryLoc != null) {
                updateMapMarker(deliveryLoc);
            }
        });
    }

    private void updateMapMarker(LatLng point) {
        if (mapboxMap == null) return;
        mapboxMap.clear();
        mapboxMap.addMarker(new MarkerOptions()
                .position(point)
                .title("موقع التوصيل"));
        
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(point)
                        .zoom(15.0)
                        .build()
        ));
    }

    private void fetchRealDataAndCalculate() {
        db.collection("services").document(serviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ServiceModel service = documentSnapshot.toObject(ServiceModel.class);
                        if (service != null) {
                            double pricePerUnit = (unit != null && unit.equals("لتر")) ? service.getPrice() : service.getPriceCup();
                            double deliveryFee = 10.0;
                            totalPrice = (quantity * pricePerUnit) + deliveryFee;
                            updateUI(service.getNameAr());
                        }
                    } else {
                        totalPrice = (unit != null && unit.equals("لتر")) ? (quantity * 0.05) + 10.0 : 30.0;
                        updateUI(providerName != null ? providerName : "طلب مياه");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "خطأ في جلب بيانات السعر", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUI(String serviceNameDisplay) {
        tvServiceName.setText(serviceNameDisplay);
        tvLocationMain.setText(address != null ? address : "الموقع المختار");
        tvOrderNotes.setText(notes != null && !notes.isEmpty() ? notes : "لا توجد ملاحظات");
        
        tvWaterPrice.setText(String.format("%.2f ₪", Math.max(0, totalPrice - 10.0)));
        tvTotalPriceMain.setText(String.format("%.2f ₪", totalPrice));
        tvFooterPriceText.setText(String.format("%.2f ₪", totalPrice));
    }

    private void saveOrderToFirebase() {
        if (mAuth.getCurrentUser() == null) return;

        Map<String, Object> order = new HashMap<>();
        order.put("customer_id", mAuth.getUid());
        order.put("provider_id", providerId);
        order.put("service_id", serviceId);
        order.put("provider_name", providerName);
        order.put("quantity", quantity);
        order.put("unit", unit);
        order.put("address_details", address);
        order.put("notes", notes);
        order.put("scheduled_time", scheduledTime);
        order.put("total_price", totalPrice);
        order.put("status", "pending");
        order.put("delivery_lat", deliveryLoc.getLatitude());
        order.put("delivery_lng", deliveryLoc.getLongitude());
        order.put("created_at", com.google.firebase.Timestamp.now());

        db.collection("orders").add(order)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, "تم إرسال الطلب بنجاح!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, Order_Status_Activity.class);
                    intent.putExtra("order_id", ref.getId());
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "فشل في إرسال الطلب", Toast.LENGTH_SHORT).show();
                });
    }

    @Override protected void onStart() { super.onStart(); if (mapView != null) mapView.onStart(); }
    @Override protected void onResume() { super.onResume(); if (mapView != null) mapView.onResume(); }
    @Override protected void onPause() { super.onPause(); if (mapView != null) mapView.onPause(); }
    @Override protected void onStop() { super.onStop(); if (mapView != null) mapView.onStop(); }
    @Override protected void onSaveInstanceState(@NonNull Bundle outState) { super.onSaveInstanceState(outState); if (mapView != null) mapView.onSaveInstanceState(outState); }
    @Override public void onLowMemory() { super.onLowMemory(); if (mapView != null) mapView.onLowMemory(); }
    @Override protected void onDestroy() { if (mapView != null) mapView.onDestroy(); super.onDestroy(); }
}
