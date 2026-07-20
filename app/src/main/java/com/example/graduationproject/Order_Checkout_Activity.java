package com.example.graduationproject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class Order_Checkout_Activity extends AppCompatActivity {

    private MapView mapView;
    private int quantity = 500;
    private String unit = "لتر";
    private String selectedTime = "الآن";
    private GeoPoint selectedLocation = new GeoPoint(31.516, 34.448);

    private TextView tvQuantityCount, tvUnit;
    private CardView btnByLiter, btnByTank;
    private EditText etAddressDetails, etNotes;
    
    private String providerId, providerName, serviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
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

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);
        mapControllerSetCenter();

        Marker startMarker = new Marker(mapView);
        startMarker.setPosition(selectedLocation);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setTitle("موقع التوصيل");
        startMarker.setDraggable(true);
        startMarker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
            @Override public void onMarkerDrag(Marker marker) {}
            @Override public void onMarkerDragEnd(Marker marker) {
                selectedLocation = (GeoPoint) marker.getPosition();
            }
            @Override public void onMarkerDragStart(Marker marker) {}
        });
        mapView.getOverlays().add(startMarker);

        btnLocateMe.setOnClickListener(v -> {
            mapView.getController().animateTo(selectedLocation);
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

    private void mapControllerSetCenter() {
        if (mapView != null) mapView.getController().setCenter(selectedLocation);
    }

    private void setupBottomNavigation() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            Intent intent = new Intent(this, MapExplorerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
        findViewById(R.id.navWallet).setOnClickListener(v -> startActivity(new Intent(this, WalletActivity.class)));
        findViewById(R.id.navOrders).setOnClickListener(v -> startActivity(new Intent(this, My_Orders_Activity.class)));
        findViewById(R.id.navProfile).setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
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

    @Override protected void onResume() { super.onResume(); if (mapView != null) mapView.onResume(); }
    @Override protected void onPause() { super.onPause(); if (mapView != null) mapView.onPause(); }
}
