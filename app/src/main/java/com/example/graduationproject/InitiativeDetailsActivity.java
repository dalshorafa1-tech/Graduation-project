package com.example.graduationproject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
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

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class InitiativeDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "InitiativeDetails";

    private MapView mapView;
    private MapboxMap mBoundedMap;
    private TextView tvShowRoute;
    private com.google.android.material.card.MaterialCardView btnContactProvider;
    private LinearLayout navDashboard, navInitiatives, navNeedMap;

    private TextView tvInitiativeTitle, tvInitiativeLocation, tvWaterAmount, tvCost, tvInitiativeDesc;
    private TextView tvInitiativeStatus, tvInitiativeId, tvFundingType, tvProviderName;
    private TextView tvTotalFunded, tvProgressPercent, tvRemainingCost;
    private TextView tvStartDate, tvEndDate, tvWorkingHours;
    private LinearProgressIndicator progressIndicator;

    private android.widget.ImageView ivNotificationBtn;

    private FirebaseFirestore db;
    private String initiativeId = "";

    private LatLng targetLocation = new LatLng(31.5017, 34.4668); // Gaza Default
    private String locationName = "موقع المبادرة";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize Mapbox before layout inflation
        Mapbox.getInstance(this);
        
        setContentView(R.layout.activity_initiative_details);

        db = FirebaseFirestore.getInstance();

        if (getIntent() != null) {
            initiativeId = getIntent().getStringExtra("initiative_id");
        }

        initViews();
        setupClickListeners();
        fetchInitiativeDetails();

        mapView = findViewById(R.id.mapview_initiative_details);
        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        }
    }

    private void initViews() {
        tvShowRoute = findViewById(R.id.tvShowRoute);
        btnContactProvider = findViewById(R.id.btnContactProvider);

        tvInitiativeTitle = findViewById(R.id.tvInitiativeTitle);
        tvInitiativeLocation = findViewById(R.id.tvInitiativeLocation);
        tvWaterAmount = findViewById(R.id.tvWaterAmount);
        tvCost = findViewById(R.id.tvCost);
        tvInitiativeDesc = findViewById(R.id.tvInitiativeDesc);
        tvInitiativeId = findViewById(R.id.tvInitiativeId);
        tvInitiativeStatus = findViewById(R.id.tvInitiativeStatus);
        tvFundingType = findViewById(R.id.tvFundingType);
        tvProviderName = findViewById(R.id.tvProviderName);
        
        tvTotalFunded = findViewById(R.id.tv_total_funded);
        tvProgressPercent = findViewById(R.id.tvProgressPercent);
        tvRemainingCost = findViewById(R.id.tvRemainingCost);
        
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        tvWorkingHours = findViewById(R.id.tvWorkingHours);
        
        progressIndicator = findViewById(R.id.progressIndicator);

        ivNotificationBtn = findViewById(R.id.ivNotification);

        navDashboard = findViewById(R.id.nav_dashboard);
        navInitiatives = findViewById(R.id.nav_initiatives);
        navNeedMap = findViewById(R.id.nav_need_map);
    }

    private void fetchInitiativeDetails() {
        if (initiativeId == null || initiativeId.isEmpty()) {
            Toast.makeText(this, "خطأ: لم يتم العثور على معرف المبادرة", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("initiatives").document(initiativeId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        InitiativeModel initiative = doc.toObject(InitiativeModel.class);
                        if (initiative != null) {
                            displayData(initiative);
                        }
                    } else {
                        Toast.makeText(this, "المبادرة غير موجودة", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching details", e));
    }

    private void displayData(InitiativeModel initiative) {
        tvInitiativeTitle.setText(initiative.getTitle());
        tvInitiativeLocation.setText("📍 نقطة التوزيع: " + initiative.getLocation());
        
        String rawId = initiative.getId();
        String displayId = "---";
        if (rawId != null) {
            if (rawId.length() >= 5) {
                displayId = rawId.substring(0, 5).toUpperCase();
            } else {
                displayId = rawId.toUpperCase();
            }
        }
        tvInitiativeId.setText("رقم المبادرة: #" + displayId);

        tvWaterAmount.setText(String.format(Locale.getDefault(), "%, d لتر", initiative.getTargetLiters()));
        tvFundingType.setText(initiative.getFundingType());
        tvProviderName.setText(initiative.getProviderName() != null ? initiative.getProviderName() : "لم يحدد بعد");
        
        tvStartDate.setText(initiative.getStartDate() != null ? initiative.getStartDate() : "--");
        tvEndDate.setText(initiative.getEndDate() != null ? initiative.getEndDate() : "--");
        tvWorkingHours.setText(initiative.getWorkingHours() != null ? initiative.getWorkingHours() : "--");
        
        locationName = initiative.getLocation();
        
        // Status Styling
        String status = initiative.getStatus();
        tvInitiativeStatus.setText(status);
        if ("نشط".equals(status)) {
            tvInitiativeStatus.setTextColor(Color.parseColor("#0069B4"));
        } else if ("مكتمل".equals(status)) {
            tvInitiativeStatus.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            tvInitiativeStatus.setTextColor(Color.parseColor("#E74C3C"));
        }

        // Financials & Progress
        double costPerLiter = 0.05; // Example cost
        double totalCost = initiative.getTargetLiters() * costPerLiter;
        double currentFunded = initiative.getCurrentLiters() * costPerLiter;
        double remaining = totalCost - currentFunded;

        tvCost.setText(String.format(Locale.getDefault(), "من أصل %,.0f ₪", totalCost));
        tvTotalFunded.setText(String.format(Locale.getDefault(), "%,.0f ₪", currentFunded));
        tvRemainingCost.setText(String.format(Locale.getDefault(), "%,.0f ₪", remaining > 0 ? remaining : 0));
        
        int progress = initiative.getProgressPercentage();
        tvProgressPercent.setText(progress + "% مكتمل");
        progressIndicator.setProgress(progress);

        // Update Map Location
        if (initiative.getLatitude() != 0 && initiative.getLongitude() != 0) {
            targetLocation = new LatLng(initiative.getLatitude(), initiative.getLongitude());
            updateMap();
        }
    }

    private void updateMap() {
        if (mBoundedMap != null && mBoundedMap.getStyle() != null) {
            mBoundedMap.clear();
            mBoundedMap.addMarker(new MarkerOptions().position(targetLocation).title(locationName));
            mBoundedMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition.Builder()
                            .target(targetLocation)
                            .zoom(14.0)
                            .build()
            ));
        }
    }

    private void setupClickListeners() {
        if (ivNotificationBtn != null) {
            ivNotificationBtn.setOnClickListener(v -> startActivity(new Intent(this, UserNotificationActivity.class)));
        }

        if (tvShowRoute != null) {
            tvShowRoute.setOnClickListener(v -> Toast.makeText(this, "جاري فتح الخرائط...", Toast.LENGTH_SHORT).show());
        }

        if (btnContactProvider != null) {
            btnContactProvider.setOnClickListener(v -> Toast.makeText(this, "ميزة الدردشة ستتوفر قريباً", Toast.LENGTH_SHORT).show());
        }

        navDashboard.setOnClickListener(v -> {
            startActivity(new Intent(this, InitiatorDashboardActivity.class));
            finish();
        });

        navInitiatives.setOnClickListener(v -> finish());

        navNeedMap.setOnClickListener(v -> startActivity(new Intent(this, MapExplorerActivity.class)));
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        mBoundedMap = mapboxMap;
        mapboxMap.setStyle(new Style.Builder().fromUri("https://tiles.openfreemap.org/styles/bright"), style -> {
            updateMap();
            mBoundedMap.getUiSettings().setScrollGesturesEnabled(false);
            mBoundedMap.getUiSettings().setAllGesturesEnabled(false);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mapView != null) mapView.onStart();
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

    @Override
    protected void onStop() {
        super.onStop();
        if (mapView != null) mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) mapView.onDestroy();
    }
}
