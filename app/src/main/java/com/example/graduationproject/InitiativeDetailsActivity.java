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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class InitiativeDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "InitiativeDetails";

    private GoogleMap mBoundedMap;
    private TextView tvShowRoute;
    private com.google.android.material.card.MaterialCardView btnContactProvider;
    private LinearLayout navDashboard, navInitiatives, navNeedMap;

    private TextView tvInitiativeTitle, tvInitiativeLocation, tvWaterAmount, tvCost, tvInitiativeDesc;
    private TextView tvInitiativeStatus, tvInitiativeId, tvFundingType, tvProviderName;
    private TextView tvTotalFunded, tvProgressPercent, tvRemainingCost;
    private LinearProgressIndicator progressIndicator;

    private android.widget.ImageView ivNotificationBtn;

    private FirebaseFirestore db;
    private String initiativeId = "";

    private LatLng targetLocation = new LatLng(31.5017, 34.4668); // Gaza Default
    private String locationName = "موقع المبادرة";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initiative_details);

        db = FirebaseFirestore.getInstance();

        if (getIntent() != null) {
            initiativeId = getIntent().getStringExtra("initiative_id");
        }

        initViews();
        setupClickListeners();
        fetchInitiativeDetails();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_initiative_details);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
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
        tvInitiativeId.setText("رقم المبادرة: #" + (initiative.getId() != null ? initiative.getId().substring(0, 5).toUpperCase() : "---"));
        tvWaterAmount.setText(String.format(Locale.getDefault(), "%, d لتر", initiative.getTargetLiters()));
        tvFundingType.setText(initiative.getFundingType());
        tvProviderName.setText(initiative.getProviderName() != null ? initiative.getProviderName() : "لم يحدد بعد");
        
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
        if (mBoundedMap != null) {
            mBoundedMap.clear();
            mBoundedMap.addMarker(new MarkerOptions().position(targetLocation).title(locationName));
            mBoundedMap.moveCamera(CameraUpdateFactory.newLatLngZoom(targetLocation, 14f));
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
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mBoundedMap = googleMap;
        updateMap();
        mBoundedMap.getUiSettings().setScrollGesturesEnabled(false);
    }
}