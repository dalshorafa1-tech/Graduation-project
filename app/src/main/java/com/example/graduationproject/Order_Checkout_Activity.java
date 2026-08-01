package com.example.graduationproject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

public class Order_Checkout_Activity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private MapboxMap mapboxMap;
    private Marker deliveryMarker;
    private int quantity = 500;
    private String unit = "لتر";
    private String selectedTime = "الآن";
    private LatLng selectedLocation = new LatLng(31.516, 34.448);

    private TextView tvQuantityCount, tvUnit;
    private CardView btnByLiter, btnByTank;
    private EditText etAddressDetails, etNotes;
    
    private String providerId, providerName, serviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize Mapbox
        Mapbox.getInstance(this);
        
        setContentView(R.layout.activity_order_checkout);

        // استقبال بيانات المزود والخدمة
        providerId = getIntent().getStringExtra("provider_id");
        providerName = getIntent().getStringExtra("provider_name");
        serviceId = getIntent().getStringExtra("service_id");

        // Initialize Views
        ImageView btnBack = findViewById(R.id.btnBack);
        btnByLiter = findViewById(R.id.btnByLiter);
        btnByTank = findViewById(R.id.btnByTank);
        CardView btnMinus = findViewById(R.id.btnMinus);
        CardView btnPlus = findViewById(R.id.btnPlus);
        tvQuantityCount = findViewById(R.id.tvQuantityCount);
        tvUnit = findViewById(R.id.tvUnit);

        mapView = findViewById(R.id.mapView);
        CardView btnLocateMe = findViewById(R.id.btnLocateMe);
        etAddressDetails = findViewById(R.id.etAddressDetails);
        etNotes = findViewById(R.id.etNotes);
        CardView btnSubmitOrder = findViewById(R.id.btnSubmitOrder);

        btnBack.setOnClickListener(v -> finish());
        btnByLiter.setOnClickListener(v -> selectQuantityType(true));
        btnByTank.setOnClickListener(v -> selectQuantityType(false));

        btnMinus.setOnClickListener(v -> {
            if (quantity > 0) {
                int step = (unit.equals("لتر") ? 50 : 1);
                quantity -= step;
                if (quantity < 0) quantity = 0;
                updateQuantityText();
            }
        });

        btnPlus.setOnClickListener(v -> {
            int step = (unit.equals("لتر") ? 50 : 1);
            quantity += step;
            updateQuantityText();
        });

        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        }

        btnLocateMe.setOnClickListener(v -> {
            if (mapboxMap != null) {
                mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 15.0));
            }
        });

        btnSubmitOrder.setOnClickListener(v -> {
            String address = etAddressDetails.getText().toString().trim();
            if (address.isEmpty()) {
                Toast.makeText(this, "يرجى إدخال تفاصيل الموقع", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Intent intent = new Intent(Order_Checkout_Activity.this, Review_Order_Activity.class);
            intent.putExtra("provider_id", providerId);
            intent.putExtra("provider_name", providerName);
            intent.putExtra("service_id", serviceId);
            intent.putExtra("quantity", quantity);
            intent.putExtra("unit", unit);
            intent.putExtra("address", address);
            intent.putExtra("notes", etNotes.getText().toString().trim());
            intent.putExtra("scheduledTime", selectedTime);
            intent.putExtra("lat", selectedLocation.getLatitude());
            intent.putExtra("lng", selectedLocation.getLongitude());
            startActivity(intent);
        });

        setupBottomNavigation();
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(new Style.Builder().fromUri("https://tiles.openfreemap.org/styles/bright"), style -> {
            
            deliveryMarker = mapboxMap.addMarker(new MarkerOptions()
                    .position(selectedLocation)
                    .title("موقع التوصيل"));
            
            mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition.Builder()
                            .target(selectedLocation)
                            .zoom(15.0)
                            .build()
            ));

            mapboxMap.addOnMapClickListener(point -> {
                selectedLocation = point;
                deliveryMarker.setPosition(point);
                return true;
            });
        });
    }

    private void setupBottomNavigation() {
        View navHome = findViewById(R.id.navHome);
        View navWallet = findViewById(R.id.navWallet);
        View navOrders = findViewById(R.id.navOrders);
        View navProfile = findViewById(R.id.navProfile);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, MapExplorerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });
        }
        if (navWallet != null) navWallet.setOnClickListener(v -> startActivity(new Intent(this, WalletActivity.class)));
        if (navOrders != null) navOrders.setOnClickListener(v -> startActivity(new Intent(this, My_Orders_Activity.class)));
        if (navProfile != null) navProfile.setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
    }

    private void updateQuantityText() {
        tvQuantityCount.setText(String.valueOf(quantity));
    }

    private void selectQuantityType(boolean isLiter) {
        if (isLiter) {
            unit = "لتر"; quantity = 500;
            btnByLiter.setCardBackgroundColor(Color.parseColor("#BAE6FD"));
            btnByTank.setCardBackgroundColor(Color.WHITE);
        } else {
            unit = "خزان"; quantity = 1;
            btnByLiter.setCardBackgroundColor(Color.WHITE);
            btnByTank.setCardBackgroundColor(Color.parseColor("#BAE6FD"));
        }
        tvUnit.setText(unit);
        updateQuantityText();
    }

    @Override protected void onStart() { super.onStart(); if (mapView != null) mapView.onStart(); }
    @Override protected void onResume() { super.onResume(); if (mapView != null) mapView.onResume(); }
    @Override protected void onPause() { super.onPause(); if (mapView != null) mapView.onPause(); }
    @Override protected void onStop() { super.onStop(); if (mapView != null) mapView.onStop(); }
    @Override protected void onSaveInstanceState(@NonNull Bundle outState) { super.onSaveInstanceState(outState); if (mapView != null) mapView.onSaveInstanceState(outState); }
    @Override public void onLowMemory() { super.onLowMemory(); if (mapView != null) mapView.onLowMemory(); }
    @Override protected void onDestroy() { if (mapView != null) mapView.onDestroy(); super.onDestroy(); }
}
