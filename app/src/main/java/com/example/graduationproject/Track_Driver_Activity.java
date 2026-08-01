package com.example.graduationproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

public class Track_Driver_Activity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private MapboxMap mapboxMap;
    private Marker driverMarker, destinationMarker;
    private FirebaseFirestore db;
    private ListenerRegistration driverListener;
    private String orderId, providerId, driverPhone;

    private TextView tvDriverName, tvPlateNumber, tvTimeEstimate, tvDistance;
    private LatLng destinationLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize Mapbox
        Mapbox.getInstance(this);
        
        setContentView(R.layout.activity_track_driver);

        db = FirebaseFirestore.getInstance();
        orderId = getIntent().getStringExtra("order_id");

        initViews(savedInstanceState);

        if (orderId != null) {
            fetchOrderAndStartTracking();
        } else {
            Toast.makeText(this, "خطأ: لم يتم العثور على بيانات الطلب", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews(Bundle savedInstanceState) {
        tvDriverName = findViewById(R.id.tvDriverName);
        tvPlateNumber = findViewById(R.id.tvPlateNumber);
        tvTimeEstimate = findViewById(R.id.tvTimeEstimate);
        tvDistance = findViewById(R.id.tvDistance);
        mapView = findViewById(R.id.mapView);

        View btnBack = findViewById(R.id.btnBack);
        MaterialButton btnCall = findViewById(R.id.btnCallDriver);

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnCall != null) {
            btnCall.setOnClickListener(v -> {
                if (driverPhone != null && !driverPhone.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + driverPhone));
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "رقم هاتف السائق غير متوفر حالياً", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(new Style.Builder().fromUri("https://tiles.openfreemap.org/styles/bright"), style -> {
            if (destinationLoc != null) {
                updateDestinationMarker(destinationLoc);
            }
        });
    }

    private void fetchOrderAndStartTracking() {
        db.collection("orders").document(orderId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                providerId = documentSnapshot.getString("provider_id");
                
                Double lat = documentSnapshot.getDouble("delivery_lat");
                Double lng = documentSnapshot.getDouble("delivery_lng");
                
                if (lat != null && lng != null) {
                    destinationLoc = new LatLng(lat, lng);
                    if (mapboxMap != null) {
                        updateDestinationMarker(destinationLoc);
                    }
                }

                if (providerId != null) {
                    fetchProviderDetails();
                    startTrackingDriver();
                }
            }
        }).addOnFailureListener(e -> Log.e("TrackDriver", "Error fetching order", e));
    }

    private void updateDestinationMarker(LatLng loc) {
        if (mapboxMap == null) return;
        if (destinationMarker != null) mapboxMap.removeMarker(destinationMarker);
        
        destinationMarker = mapboxMap.addMarker(new MarkerOptions()
                .position(loc)
                .title("موقع التوصيل"));
        
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 14.0));
    }

    private void fetchProviderDetails() {
        db.collection("providers").document(providerId).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String name = snapshot.getString("business_name");
                if (name == null) name = snapshot.getString("name");
                String plate = snapshot.getString("plate_number");
                driverPhone = snapshot.getString("phone");
                
                tvDriverName.setText("السائق: " + (name != null ? name : "مزود مياه"));
                tvPlateNumber.setText("رقم اللوحة: " + (plate != null ? plate : "---"));
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
                        LatLng driverLoc = new LatLng(lat, lng);
                        updateDriverMarker(driverLoc);
                    }
                });
    }

    private void updateDriverMarker(LatLng loc) {
        if (mapboxMap == null) return;
        if (driverMarker != null) mapboxMap.removeMarker(driverMarker);
        
        driverMarker = mapboxMap.addMarker(new MarkerOptions()
                .position(loc)
                .icon(getIconFromVector(R.drawable.truck))
                .title("موقع السائق"));

        if (destinationLoc != null) {
            LatLngBounds bounds = new LatLngBounds.Builder()
                    .include(loc)
                    .include(destinationLoc)
                    .build();
            mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } else {
            mapboxMap.animateCamera(CameraUpdateFactory.newLatLng(loc));
        }
    }

    private Icon getIconFromVector(int resId) {
        Drawable drawable = ContextCompat.getDrawable(this, resId);
        if (drawable == null) return null;
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return IconFactory.getInstance(this).fromBitmap(bitmap);
    }

    @Override protected void onStart() { super.onStart(); if (mapView != null) mapView.onStart(); }
    @Override protected void onResume() { super.onResume(); if (mapView != null) mapView.onResume(); }
    @Override protected void onPause() { super.onPause(); if (mapView != null) mapView.onPause(); }
    @Override protected void onStop() { super.onStop(); if (mapView != null) mapView.onStop(); }
    @Override protected void onSaveInstanceState(@NonNull Bundle outState) { super.onSaveInstanceState(outState); if (mapView != null) mapView.onSaveInstanceState(outState); }
    @Override public void onLowMemory() { super.onLowMemory(); if (mapView != null) mapView.onLowMemory(); }
    @Override protected void onDestroy() { 
        if (driverListener != null) driverListener.remove(); 
        if (mapView != null) mapView.onDestroy(); 
        super.onDestroy();
    }
}
