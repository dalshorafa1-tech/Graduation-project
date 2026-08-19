package com.example.graduationproject;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CreateInitiativeActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "CreateInitiative";

    private EditText etTitle, etWaterAmount, etSearchProvider, etLocation;
    private TextView tvEstimatedCost;
    private MaterialButton btnSubmitInitiative;

    private MaterialButton btn5k, btn10k, btn25k;

    private RadioGroup radioGroupFunding;
    private RadioButton rbInternalFunding, rbCrowdFunding;
    private View layoutInternalFunding, layoutCrowdFunding;
    private String selectedFundingType = "تمويل داخلي / مؤسساتي";

    private RecyclerView rvProviders;
    private ProvidersSelectableAdapter providersAdapter;
    private List<ProviderModel> providerList = new ArrayList<>();
    private String selectedProvider = "";

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private final double PRICE_PER_LITER = 0.05;
    private double currentEstimatedCost = 0.0;

    private MapView mapView;
    private MapboxMap mapboxMap;
    private Marker selectionMarker;
    private LatLng selectedLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize Mapbox before layout inflation
        Mapbox.getInstance(this);
        
        setContentView(R.layout.activity_create_initiative);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // ربط العناصر
        etTitle = findViewById(R.id.etInitiativeName);
        etLocation = findViewById(R.id.spinner_target_district);
        etWaterAmount = findViewById(R.id.etWaterAmount);
        tvEstimatedCost = findViewById(R.id.tvEstimatedCost);
        btnSubmitInitiative = findViewById(R.id.btnPublishInitiative);
        etSearchProvider = findViewById(R.id.etSearchProvider);

        btn5k = findViewById(R.id.btn5k);
        btn10k = findViewById(R.id.btn10k);
        btn25k = findViewById(R.id.btn25k);

        radioGroupFunding = findViewById(R.id.radioGroupFunding);
        rbInternalFunding = findViewById(R.id.rbInternalFunding);
        rbCrowdFunding = findViewById(R.id.rbCrowdFunding);
        layoutInternalFunding = findViewById(R.id.layoutInternalFunding);
        layoutCrowdFunding = findViewById(R.id.layoutCrowdFunding);

        rvProviders = findViewById(R.id.rvProviders);

        // إعداد الخريطة
        mapView = findViewById(R.id.mapview);
        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        }

        // إعداد المنطق
        setupWaterAmountInput();
        setupQuickAmountButtons();
        setupFundingSelection();
        setupProvidersList();
        fetchProvidersFromFirestore();
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        if (btnSubmitInitiative != null) {
            btnSubmitInitiative.setOnClickListener(v -> saveInitiativeToFirebase());
        }
    }

    private void setupProvidersList() {
        providersAdapter = new ProvidersSelectableAdapter(providerList, provider -> {
            selectedProvider = provider.getBusinessName();
            Toast.makeText(this, "تم اختيار: " + selectedProvider, Toast.LENGTH_SHORT).show();
        });
        rvProviders.setLayoutManager(new LinearLayoutManager(this));
        rvProviders.setAdapter(providersAdapter);
    }

    private void fetchProvidersFromFirestore() {
        db.collection("providers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    providerList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ProviderModel provider = document.toObject(ProviderModel.class);
                        providerList.add(provider);
                    }
                    providersAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching providers", e);
                    Toast.makeText(this, "فشل في تحميل قائمة المزودين", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(new Style.Builder().fromUri("https://tiles.openfreemap.org/styles/bright"), style -> {
            // إحداثيات غزة الافتراضية
            LatLng gaza = new LatLng(31.5017, 34.4668);
            mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition.Builder()
                            .target(gaza)
                            .zoom(12.0)
                            .build()
            ));

            // إضافة ماركر للتحريك
            selectionMarker = mapboxMap.addMarker(new MarkerOptions()
                    .position(gaza)
                    .title("موقع التوزيع"));

            selectedLatLng = gaza;
            updateLocationName(gaza);

            // المستمع عند النقر على الخريطة
            mapboxMap.addOnMapClickListener(latLng -> {
                selectedLatLng = latLng;
                if (selectionMarker != null) {
                    selectionMarker.setPosition(latLng);
                }
                updateLocationName(latLng);
                return true;
            });

            // المستمع عند تحريك الكاميرا
            mapboxMap.addOnCameraIdleListener(() -> {
                LatLng center = mapboxMap.getCameraPosition().target;
                selectedLatLng = center;
                if (selectionMarker != null) {
                    selectionMarker.setPosition(center);
                }
                updateLocationName(center);
            });
        });
    }

    private void updateLocationName(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, new Locale("ar"));
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.getLatitude(), latLng.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String subLocality = address.getSubLocality(); // الحي
                
                String locationText = "";
                if (subLocality != null) locationText += subLocality;
                else if (address.getFeatureName() != null) locationText += address.getFeatureName();
                
                if (locationText.isEmpty()) {
                    locationText = "موقع مخصص";
                }
                
                if (etLocation != null) {
                    etLocation.setText(locationText);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoder error: " + e.getMessage());
        }
    }

    private void setupWaterAmountInput() {
        etWaterAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { calculateCost(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupQuickAmountButtons() {
        btn5k.setOnClickListener(v -> etWaterAmount.setText("5000"));
        btn10k.setOnClickListener(v -> etWaterAmount.setText("10000"));
        btn25k.setOnClickListener(v -> etWaterAmount.setText("25000"));
    }

    private void setupFundingSelection() {
        layoutInternalFunding.setOnClickListener(v -> {
            selectedFundingType = "تمويل داخلي / مؤسساتي";
            rbInternalFunding.setChecked(true);
        });
        layoutCrowdFunding.setOnClickListener(v -> {
            selectedFundingType = "تمويل جماعي / تبرعات";
            rbCrowdFunding.setChecked(true);
        });
    }

    private void calculateCost() {
        String amountStr = etWaterAmount.getText().toString().trim();
        if (TextUtils.isEmpty(amountStr)) {
            currentEstimatedCost = 0.0;
        } else {
            try {
                int liters = Integer.parseInt(amountStr);
                currentEstimatedCost = liters * PRICE_PER_LITER;
            } catch (NumberFormatException e) {
                currentEstimatedCost = 0.0;
            }
        }
        tvEstimatedCost.setText(String.format("%.2f ILS", currentEstimatedCost));
    }

    private void saveInitiativeToFirebase() {
        String title = etTitle.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String waterStr = etWaterAmount.getText().toString().trim();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "🔴 يجب تسجيل الدخول أولاً", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(location) || TextUtils.isEmpty(waterStr)) {
            Toast.makeText(this, "🔴 يرجى إكمال جميع البيانات", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(selectedProvider)) {
            Toast.makeText(this, "🔴 يرجى اختيار مزود خدمة", Toast.LENGTH_SHORT).show();
            return;
        }

        int targetLiters = Integer.parseInt(waterStr);
        if (targetLiters <= 0) {
            Toast.makeText(this, "🔴 الكمية يجب أن تكون أكبر من صفر", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmitInitiative.setEnabled(false);

        double lat = (selectedLatLng != null) ? selectedLatLng.getLatitude() : 0;
        double lng = (selectedLatLng != null) ? selectedLatLng.getLongitude() : 0;

        InitiativeModel initiative = new InitiativeModel(
                null, 
                currentUser.getUid(),
                title,
                location,
                lat,
                lng,
                targetLiters,
                0,
                selectedFundingType,
                selectedProvider,
                currentEstimatedCost,
                "قيد المراجعة" // تعيين الحالة إلى "قيد المراجعة" بدلاً من "نشط"
        );

        db.collection("initiatives")
                .add(initiative)
                .addOnSuccessListener(documentReference -> {
                    String docId = documentReference.getId();
                    db.collection("initiatives").document(docId).update("id", docId);
                    
                    Toast.makeText(this, "✅ تم إرسال المبادرة للمراجعة من قبل الإدارة", Toast.LENGTH_LONG).show();
                    
                    Intent intent = new Intent(this, InitiativesListActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSubmitInitiative.setEnabled(true);
                    Toast.makeText(this, "❌ حدث خطأ أثناء الحفظ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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