package com.example.graduationproject;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.HashMap;
import java.util.Map;

public class Review_Order_Activity extends AppCompatActivity {

    private MapView mapView;
    private GeoPoint deliveryLoc;

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

        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        setContentView(R.layout.activity_review_order);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();

        Intent data = getIntent();
        // محاولة جلب الـ ID كنص، وإذا فشل نجلب كـ int ونحوله (لزيادة الأمان)
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
        deliveryLoc = new GeoPoint(lat, lng);

        // إذا كان السعر مرسلاً مسبقاً (كما في الاشتراكات) نستخدمه مباشرة
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

    private void initViews() {
        tvServiceName = findViewById(R.id.tvServiceName);
        tvLocationMain = findViewById(R.id.tvLocationMain);
        tvOrderNotes = findViewById(R.id.tvOrderNotes);
        tvWaterPrice = findViewById(R.id.tvWaterPrice);
        tvTotalPriceMain = findViewById(R.id.tvTotalPriceMain);
        tvFooterPriceText = findViewById(R.id.tvFooterPriceText);
        mapView = findViewById(R.id.mapViewReview);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnConfirmAndSend).setOnClickListener(v -> saveOrderToFirebase());
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
                        // في حال لم يتم العثور على الخدمة، نستخدم سعر افتراضي بدلاً من الانهيار
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

        if (mapView != null) {
            mapView.setTileSource(TileSourceFactory.MAPNIK);
            mapView.getController().setZoom(16.0);
            mapView.getController().setCenter(deliveryLoc);
            Marker marker = new Marker(mapView);
            marker.setPosition(deliveryLoc);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle("موقعك");
            mapView.getOverlays().clear();
            mapView.getOverlays().add(marker);
            mapView.invalidate();
        }
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

    @Override protected void onResume() { super.onResume(); if(mapView!=null) mapView.onResume(); }
    @Override protected void onPause() { super.onPause(); if(mapView!=null) mapView.onPause(); }
}
