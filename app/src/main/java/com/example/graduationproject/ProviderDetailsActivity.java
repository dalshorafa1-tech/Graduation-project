package com.example.graduationproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

public class ProviderDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private Button btnSelectService;
    private ImageView btnBack;
    private TextView tvProviderName, tvLocation, tvPrice;
    private String providerId, providerName, address, sourceType;
    private double providerLat, providerLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Mapbox
        Mapbox.getInstance(this);

        setContentView(R.layout.activity_provider_details);

        btnSelectService = findViewById(R.id.btnSelectService);
        btnBack = findViewById(R.id.btnBack);
        mapView = findViewById(R.id.mapView);
        tvProviderName = findViewById(R.id.tvProviderName);
        tvLocation = findViewById(R.id.tvLocation);
        tvPrice = findViewById(R.id.tvPrice);

        Intent intent = getIntent();
        providerId = intent.getStringExtra("provider_id");
        providerName = intent.getStringExtra("provider_name");
        address = intent.getStringExtra("address");
        sourceType = intent.getStringExtra("source_type");
        providerLat = intent.getDoubleExtra("lat", 31.51);
        providerLng = intent.getDoubleExtra("lng", 34.45);

        if (providerName != null) tvProviderName.setText(providerName);
        if (address != null) tvLocation.setText(address);
        
        if (sourceType != null && sourceType.contains("صهريج")) {
            tvPrice.setText("30 شيكل / كوب");
        } else if (sourceType != null && sourceType.contains("بئر")) {
            tvPrice.setText("15 شيكل / كوب");
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnSelectService != null) {
            btnSelectService.setOnClickListener(v -> {
                Intent sIntent = new Intent(ProviderDetailsActivity.this, ServicesActivity.class);
                sIntent.putExtra("provider_id", providerId);
                sIntent.putExtra("provider_name", providerName);
                startActivity(sIntent);
            });
        }

        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        }

        setupBottomNavigation();
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        mapboxMap.setStyle(new Style.Builder().fromUri("https://tiles.openfreemap.org/styles/bright"), style -> {
            LatLng location = new LatLng(providerLat, providerLng);
            
            // Add marker for the provider
            mapboxMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(providerName != null ? providerName : "مزود الخدمة"));

            // Move camera to provider location
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition.Builder()
                            .target(location)
                            .zoom(15.0)
                            .build()
            ));
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

        if (navWallet != null) {
            navWallet.setOnClickListener(v -> {
                 startActivity(new Intent(this, WalletActivity.class));
            });
        }

        if (navOrders != null) {
            navOrders.setOnClickListener(v -> {
                 startActivity(new Intent(this, My_Orders_Activity.class));
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                 startActivity(new Intent(this, Profile.class));
            });
        }
    }

    @Override protected void onStart() { super.onStart(); if (mapView != null) mapView.onStart(); }
    @Override protected void onResume() { super.onResume(); if (mapView != null) mapView.onResume(); }
    @Override protected void onPause() { super.onPause(); if (mapView != null) mapView.onPause(); }
    @Override protected void onStop() { super.onStop(); if (mapView != null) mapView.onStop(); }
    @Override protected void onSaveInstanceState(@NonNull Bundle outState) { super.onSaveInstanceState(outState); if (mapView != null) mapView.onSaveInstanceState(outState); }
    @Override public void onLowMemory() { super.onLowMemory(); if (mapView != null) mapView.onLowMemory(); }
    @Override protected void onDestroy() { if (mapView != null) mapView.onDestroy(); super.onDestroy(); }
}
