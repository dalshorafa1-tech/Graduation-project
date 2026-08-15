package com.example.graduationproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class NeedMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "NeedMapDebug";

    private GoogleMap mMap;
    private BottomSheetBehavior<MaterialCardView> bottomSheetBehavior;

    private MaterialCardView cardMapLegend;
    private MaterialCardView btnZoomIn, btnZoomOut, btnMyLocation;
    private MaterialCardView bottomSheetDetails;
    private MaterialButton btnCoordinateInitiative;

    private TextView tvLocationName, tvDeficitPercentage, tvWaterVolumeNeeded;

    private LinearLayout bottomNavProvider;
    private LinearLayout navDashboard, navInitiatives, navNeedMap;

    private FirebaseFirestore db;
    private Map<String, Map<String, Object>> markerDataMap = new HashMap<>();
    private LatLng defaultLocation = new LatLng(31.5234, 34.4485); // موقع افتراضي (حي الرمال)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_need_map);

        db = FirebaseFirestore.getInstance();

        initViews();
        setupBottomSheet();

        // استدعاء وتهيئة خريطة جوجل
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        setupClickListeners();
    }

    private void initViews() {
        cardMapLegend = findViewById(R.id.cardMapLegend);
        btnZoomIn = findViewById(R.id.btnZoomIn);
        btnZoomOut = findViewById(R.id.btnZoomOut);
        btnMyLocation = findViewById(R.id.btnMyLocation);
        bottomSheetDetails = findViewById(R.id.bottomSheetDetails);
        btnCoordinateInitiative = findViewById(R.id.btnCoordinateInitiative);
        bottomNavProvider = findViewById(R.id.bottomNavProvider);

        tvLocationName = findViewById(R.id.tvLocationName);
        tvDeficitPercentage = findViewById(R.id.tvDeficitPercentage);
        tvWaterVolumeNeeded = findViewById(R.id.tvWaterVolumeNeeded);

        if (bottomNavProvider != null) {
            navDashboard = (LinearLayout) bottomNavProvider.getChildAt(0);
            navInitiatives = (LinearLayout) bottomNavProvider.getChildAt(1);
            navNeedMap = (LinearLayout) bottomNavProvider.getChildAt(2);
        }
    }

    private void setupBottomSheet() {
        if (bottomSheetDetails == null) return;
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetDetails);

        // تعيين الارتفاع الافتراضي للظهور
        bottomSheetBehavior.setPeekHeight((int) (320 * getResources().getDisplayMetrics().density));

        // إخفاء الـ Bottom Sheet في البداية حتى يضغط المستخدم على ماركر معيّن
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // التحكم المستقبلي بالحالة إذا لزم الأمر
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // إيقاف أزرار التحكم الافتراضية
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);

        // تعيين وتركيز الكاميرا على غزة كبداية افتراضية
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f));

        // جلب البيانات الحية من الفايربيس وعرض الماركرز
        getNeedsLocationsFromFirestore();

        // عند الضغط على أي علامة (Marker) في الخريطة، نقوم بتحديث وعرض الـ Bottom Sheet
        mMap.setOnMarkerClickListener(marker -> {
            String markerId = marker.getId();
            if (markerDataMap.containsKey(markerId)) {
                Map<String, Object> data = markerDataMap.get(markerId);
                if (data != null) {
                    updateBottomSheetDetails(data);
                }
            }
            return false;
        });
    }

    //  جلب نقاط الاحتياج ديناميكياً وتلقائياً من Firestore
    private void getNeedsLocationsFromFirestore() {
        db.collection("needs_map")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "فشل جلب نقاط الاحتياج: " + error.getMessage());
                        return;
                    }

                    if (value != null && mMap != null) {
                        mMap.clear(); // مسح العلامات القديمة لإعادة الرسم النظيف
                        markerDataMap.clear();

                        for (QueryDocumentSnapshot doc : value) {
                            Double lat = doc.getDouble("latitude");
                            Double lng = doc.getDouble("longitude");
                            String title = doc.getString("title");

                            if (lat != null && lng != null) {
                                LatLng position = new LatLng(lat, lng);
                                Marker marker = mMap.addMarker(new MarkerOptions()
                                        .position(position)
                                        .title(title != null ? title : "منطقة بحاجة للمياه"));

                                if (marker != null) {
                                    // تخزين تفاصيل المستند لربطها بالـ MarkerID الخاص بجوجل عند الضغط عليه
                                    markerDataMap.put(marker.getId(), doc.getData());
                                }
                            }
                        }
                    }
                });
    }

    //  تحديث بيانات الـ Bottom Sheet عند تحديد موقع معيّن على الخريطة
    private void updateBottomSheetDetails(Map<String, Object> data) {
        String title = (String) data.get("title");
        String deficit = (String) data.get("deficitPercentage");
        String volume = (String) data.get("waterVolumeNeeded");

        if (tvLocationName != null && title != null) tvLocationName.setText(title);
        if (tvDeficitPercentage != null && deficit != null) tvDeficitPercentage.setText("نسبة العجز: " + deficit);
        if (tvWaterVolumeNeeded != null && volume != null) tvWaterVolumeNeeded.setText("الكمية المطلوبة: " + volume + " لتر");

        // إظهار الـ Bottom Sheet فوراً وبشكل سلس
        if (bottomSheetBehavior != null) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    private void setupClickListeners() {
        // زر تكبير الخريطة (+)
        if (btnZoomIn != null) {
            btnZoomIn.setOnClickListener(v -> {
                if (mMap != null) mMap.animateCamera(CameraUpdateFactory.zoomIn());
            });
        }

        // زر تصغير الخريطة (-)
        if (btnZoomOut != null) {
            btnZoomOut.setOnClickListener(v -> {
                if (mMap != null) mMap.animateCamera(CameraUpdateFactory.zoomOut());
            });
        }

        // زر إعادة تركيز الكاميرا للموقع الافتراضي
        if (btnMyLocation != null) {
            btnMyLocation.setOnClickListener(v -> {
                if (mMap != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 14f));
                    Toast.makeText(this, "تمت إعادة توجيه الخريطة للمركز", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // زر تنسيق مبادرة جديدة وينتقل ديناميكياً لواجهة الإنشاء
        if (btnCoordinateInitiative != null) {
            btnCoordinateInitiative.setOnClickListener(v -> {
                Intent intent = new Intent(NeedMapActivity.this, CreateInitiativeActivity.class);
                startActivity(intent);
            });
        }

        // --- أزرار شريط التنقل السفلي الموحد والانتقال التفاعلي الفعلي ---
        if (navDashboard != null) {
            navDashboard.setOnClickListener(v -> {
                Intent intent = new Intent(this, InitiatorDashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        if (navInitiatives != null) {
            navInitiatives.setOnClickListener(v -> {
                Intent intent = new Intent(this, InitiativesListActivity.class);
                startActivity(intent);
                finish();
            });
        }

        if (navNeedMap != null) {
            navNeedMap.setOnClickListener(v -> {
                if (bottomSheetBehavior != null) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                Toast.makeText(NeedMapActivity.this, "أنت متواجد بالفعل في خريطة الاحتياج", Toast.LENGTH_SHORT).show();
            });
        }
    }
}