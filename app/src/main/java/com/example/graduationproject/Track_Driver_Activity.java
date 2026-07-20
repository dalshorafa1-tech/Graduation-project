package com.example.graduationproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class Track_Driver_Activity extends AppCompatActivity {

    private MapView mapView;
    private Marker driverMarker, destinationMarker;
    private FirebaseFirestore db;
    private ListenerRegistration driverListener;
    private String orderId, providerId, driverPhone;

    private TextView tvDriverName, tvPlateNumber, tvEstimatedTime;
    private ImageView imgDriver;
    private GeoPoint destinationLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        setContentView(R.layout.activity_track_driver);

        db = FirebaseFirestore.getInstance();
        orderId = getIntent().getStringExtra("order_id");

        // ربط العناصر الواجهة
        tvDriverName = findViewById(R.id.tvDriverName);
        tvPlateNumber = findViewById(R.id.tvPlateNumber);
        tvEstimatedTime = findViewById(R.id.tvEstimatedTime);
        imgDriver = findViewById(R.id.imgDriver);

        ImageView btnBack = findViewById(R.id.btnBackTrack);
        MaterialButton btnCall = findViewById(R.id.btnCall);
        MaterialButton btnReceived = findViewById(R.id.btnReceived);
        mapView = findViewById(R.id.mapView);

        btnBack.setOnClickListener(v -> finish());
        
        btnReceived.setOnClickListener(v -> markOrderAsDelivered());

        btnCall.setOnClickListener(v -> {
            if (driverPhone != null && !driverPhone.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + driverPhone));
                startActivity(intent);
            } else {
                Toast.makeText(this, "رقم هاتف السائق غير متوفر حالياً", Toast.LENGTH_SHORT).show();
            }
        });

        setupMap();

        if (orderId != null) {
            fetchOrderAndStartTracking();
        } else {
            Toast.makeText(this, "خطأ: لم يتم العثور على بيانات الطلب", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);

        // ماركر السائق
        driverMarker = new Marker(mapView);
        driverMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        driverMarker.setTitle("موقع السائق");
        mapView.getOverlays().add(driverMarker);

        // ماركر موقع التوصيل
        destinationMarker = new Marker(mapView);
        destinationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        destinationMarker.setTitle("موقعي (التوصيل)");
        // يمكنك تغيير أيقونة ماركر التوصيل هنا
        mapView.getOverlays().add(destinationMarker);
    }

    private void fetchOrderAndStartTracking() {
        db.collection("orders").document(orderId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                providerId = documentSnapshot.getString("provider_id");
                
                Double lat = documentSnapshot.getDouble("delivery_lat");
                Double lng = documentSnapshot.getDouble("delivery_lng");
                
                if (lat != null && lng != null) {
                    destinationLoc = new GeoPoint(lat, lng);
                    destinationMarker.setPosition(destinationLoc);
                    mapView.getController().setCenter(destinationLoc);
                }

                if (providerId != null) {
                    fetchProviderDetails();
                    startTrackingDriver();
                }
            }
        }).addOnFailureListener(e -> Log.e("TrackDriver", "Error fetching order", e));
    }

    private void fetchProviderDetails() {
        db.collection("providers").document(providerId).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String name = snapshot.getString("business_name");
                String plate = snapshot.getString("plate_number");
                driverPhone = snapshot.getString("phone");
                String eta = snapshot.getString("estimated_arrival");
                
                tvDriverName.setText(name != null ? name : "مزود مياه");
                tvPlateNumber.setText(plate != null ? "رقم اللوحة: " + plate : "رقم اللوحة: جاري التعيين");
                tvEstimatedTime.setText(eta != null ? eta : "15 دقيقة");
            }
        });
    }

    private void startTrackingDriver() {
        if (providerId == null) return;

        driverListener = db.collection("providers").document(providerId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null || !snapshot.exists()) return;

                    Double lat = snapshot.getDouble("current_lat");
                    Double lng = snapshot.getDouble("current_lng");

                    if (lat != null && lng != null) {
                        GeoPoint driverLoc = new GeoPoint(lat, lng);
                        driverMarker.setPosition(driverLoc);
                        
                        // تحديث عرض الخريطة ليشمل الموقعين إذا أردت
                        // map.invalidate();
                    }
                });
    }

    private void markOrderAsDelivered() {
        if (orderId == null) return;
        db.collection("orders").document(orderId)
                .update("status", "delivered")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "تم تأكيد استلام الطلب", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, Water_Delivered_Activity.class);
                    intent.putExtra("order_id", orderId);
                    startActivity(intent);
                    finish();
                });
    }

    @Override protected void onResume() { super.onResume(); if (mapView != null) mapView.onResume(); }
    @Override protected void onPause() { super.onPause(); if (mapView != null) mapView.onPause(); }
    @Override protected void onDestroy() { 
        super.onDestroy(); 
        if (driverListener != null) driverListener.remove(); 
        if (mapView != null) {
            mapView.onDetach();
        }
    }
}
