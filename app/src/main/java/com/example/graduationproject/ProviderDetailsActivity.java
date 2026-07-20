package com.example.graduationproject;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class ProviderDetailsActivity extends AppCompatActivity {

    private MapView mapView;
    private Button btnSelectService;
    private ImageView btnBack;
    private TextView tvProviderName, tvLocation, tvPrice;
    private String providerId, providerName, address, sourceType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

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
        double lat = intent.getDoubleExtra("lat", 31.51);
        double lng = intent.getDoubleExtra("lng", 34.45);

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
            mapView.setTileSource(TileSourceFactory.MAPNIK);
            mapView.setMultiTouchControls(true);
            GeoPoint providerLocation = new GeoPoint(lat, lng); 
            mapView.getController().setZoom(16.0);
            mapView.getController().setCenter(providerLocation);
            Marker marker = new Marker(mapView);
            marker.setPosition(providerLocation);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(providerName);
            mapView.getOverlays().add(marker);
        }

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            Intent intent = new Intent(this, MapExplorerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        findViewById(R.id.navWallet).setOnClickListener(v -> {
             startActivity(new Intent(this, WalletActivity.class));
        });

        findViewById(R.id.navOrders).setOnClickListener(v -> {
             startActivity(new Intent(this, My_Orders_Activity.class));
        });

        findViewById(R.id.navProfile).setOnClickListener(v -> {
             startActivity(new Intent(this, HomeActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }
}
