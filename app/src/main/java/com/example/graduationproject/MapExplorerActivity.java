package com.example.graduationproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapExplorerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapExplorerActivity";
    private MapView mapView;
    private MapboxMap mapboxMap;
    private EditText etSearch;
    private MaterialCardView bottomSheetCard; // تم تغيير النوع لـ MaterialCardView ليتطابق مع الـ XML
    private BottomSheetBehavior<MaterialCardView> bottomSheetBehavior;
    private TextView tvLocationTitle, tvLocationAddress, tvNearestSource;
    private CardView Confirm1;
    private CardView btnLoginMap;

    private View btnLayerWater, btnLayerTruck, btnLayerStorage, btnLayerInitiatives;

    private String currentFilter = null;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration servicesListener;
    private ListenerRegistration initiativesListener;

    private List<Marker> serviceMarkers = new ArrayList<>();
    private List<Marker> initiativeMarkers = new ArrayList<>();
    private Map<Marker, String> markerToServiceId = new HashMap<>();
    private Map<Marker, String> markerToInitiativeId = new HashMap<>();

    private String selectedId = "";
    private boolean isInitiativeSelected = false;

    private static final LatLng GAZA_CITY_CENTER = new LatLng(31.5126, 34.4426);
    private static final double DETAILED_ZOOM = 14.5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_map_explorer);

        db = FirebaseFirestore.getInstance();
        initViews();

        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        }

        if (etSearch != null) {
            etSearch.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                    String query = etSearch.getText().toString().trim();
                    fetchServices(currentFilter, query);
                    fetchInitiatives(query);
                    return true;
                }
                return false;
            });
        }

        setupBottomSheet();
        setupClickListeners();
        setupBottomNavigation();
        checkLoginStatus();
    }

    private void initViews() {
        mapView = findViewById(R.id.mapview);
        bottomSheetCard = findViewById(R.id.bottomSheetCard);
        tvLocationTitle = findViewById(R.id.tvLocationTitle);
        tvLocationAddress = findViewById(R.id.tvLocationAddress);
        tvNearestSource = findViewById(R.id.tvNear);
        etSearch = findViewById(R.id.etSearch);
        Confirm1 = findViewById(R.id.btnConfirm);
        btnLoginMap = findViewById(R.id.btnLoginMap);

        btnLayerWater = findViewById(R.id.btnFilterWell);
        btnLayerTruck = findViewById(R.id.btnFilterTruck);
        btnLayerStorage = findViewById(R.id.btnFilterStorage);
        btnLayerInitiatives = findViewById(R.id.btnFilterInitiatives);
    }

    private void setupBottomSheet() {
        if (bottomSheetCard != null) {
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetCard);
            // البدء بحالة مخفية تماماً
            bottomSheetBehavior.setHideable(true);
            bottomSheetBehavior.setPeekHeight(0);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

            // جعل البطاقة بالكامل قابلة للنقر للانتقال للتفاصيل
            bottomSheetCard.setOnClickListener(v -> navigateToDetails());
        }
    }

    private void setupClickListeners() {
        if (btnLayerWater != null) btnLayerWater.setOnClickListener(v -> handleFilterClick("well"));
        if (btnLayerTruck != null) btnLayerTruck.setOnClickListener(v -> handleFilterClick("truck"));
        if (btnLayerStorage != null) btnLayerStorage.setOnClickListener(v -> handleFilterClick("storage"));
        if (btnLayerInitiatives != null) btnLayerInitiatives.setOnClickListener(v -> handleFilterClick("initiatives"));

        // زر التأكيد داخل البطاقة يقوم بنفس وظيفة النقر على البطاقة
        if (Confirm1 != null) {
            Confirm1.setOnClickListener(v -> navigateToDetails());
        }
    }

    private void navigateToDetails() {
        if (selectedId == null || selectedId.isEmpty()) return;

        Intent intent;
        if (isInitiativeSelected) {
            intent = new Intent(this, InitiativeDetailsActivity.class);
            intent.putExtra("initiative_id", selectedId);
        } else {
            intent = new Intent(this, ServiceDetailsActivity.class);
            intent.putExtra("service_id", selectedId);
        }
        startActivity(intent);
    }

    private void checkLoginStatus() {
        if (btnLoginMap != null) {
            if (mAuth.getCurrentUser() == null) {
                btnLoginMap.setVisibility(View.VISIBLE);
                btnLoginMap.setOnClickListener(v -> startActivity(new Intent(MapExplorerActivity.this, LoginActivity.class)));
            } else {
                btnLoginMap.setVisibility(View.GONE);
            }
        }
    }

    private void setupBottomNavigation() {
        View navHome = findViewById(R.id.navHome);
        View navOrders = findViewById(R.id.navOrders);
        View navProfile = findViewById(R.id.navProfile);

        if (navHome != null) navHome.setOnClickListener(v -> {});
        if (navOrders != null) navOrders.setOnClickListener(v -> startActivity(new Intent(this, My_Orders_Activity.class)));
        if (navProfile != null) navProfile.setOnClickListener(v -> startActivity(new Intent(this, Profile.class)));
    }

    private void handleFilterClick(String filterType) {
        currentFilter = filterType.equals(currentFilter) ? null : filterType;
        String query = etSearch != null ? etSearch.getText().toString().trim() : "";
        fetchServices(currentFilter, query);
        fetchInitiatives(query);
    }

    private void fetchServices(String filterType, String searchQuery) {
        if (mapboxMap == null) return;
        if (servicesListener != null) servicesListener.remove();

        Query query = db.collection("services").whereEqualTo("status", "approved");

        servicesListener = query.addSnapshotListener((value, error) -> {
            if (error != null) return;
            if (value != null) {
                for (Marker m : serviceMarkers) mapboxMap.removeMarker(m);
                serviceMarkers.clear();
                markerToServiceId.clear();

                for (DocumentSnapshot doc : value.getDocuments()) {
                    String name = doc.getString("name_ar");
                    String typeStr = doc.getString("service_type");
                    Double lat = doc.getDouble("latitude");
                    Double lng = doc.getDouble("longitude");

                    if (lat != null && lng != null) {
                        String mappedType = mapTypeToKey(typeStr);
                        if (filterType != null && !filterType.equals("initiatives") && !filterType.equalsIgnoreCase(mappedType)) continue;
                        if (filterType != null && filterType.equals("initiatives")) continue;
                        if (searchQuery != null && !searchQuery.isEmpty() && name != null && !name.toLowerCase().contains(searchQuery.toLowerCase())) continue;

                        Marker marker = mapboxMap.addMarker(new MarkerOptions()
                                .position(new LatLng(lat, lng))
                                .icon(getIconByMappedType(mappedType))
                                .title(name));
                        serviceMarkers.add(marker);
                        markerToServiceId.put(marker, doc.getId());
                    }
                }
            }
        });
    }

    private String mapTypeToKey(String typeAr) {
        if (typeAr == null) return "";
        if (typeAr.contains("آبار") || typeAr.contains("بئر")) return "well";
        if (typeAr.contains("صهريج") || typeAr.contains("شاحنة")) return "truck";
        if (typeAr.contains("خزانات") || typeAr.contains("خزان")) return "storage";
        return "";
    }

    private void fetchInitiatives(String searchQuery) {
        if (mapboxMap == null) return;
        if (initiativesListener != null) initiativesListener.remove();

        if (currentFilter != null && !currentFilter.equals("initiatives")) {
            for (Marker m : initiativeMarkers) mapboxMap.removeMarker(m);
            initiativeMarkers.clear();
            markerToInitiativeId.clear();
            return;
        }

        initiativesListener = db.collection("initiatives")
                .whereEqualTo("status", "نشط")
                .addSnapshotListener((value, error) -> {
            if (error != null) return;
            if (value != null) {
                for (Marker m : initiativeMarkers) mapboxMap.removeMarker(m);
                initiativeMarkers.clear();
                markerToInitiativeId.clear();

                for (DocumentSnapshot doc : value.getDocuments()) {
                    String title = doc.getString("title");
                    Double lat = doc.getDouble("latitude");
                    Double lng = doc.getDouble("longitude");

                    if (lat != null && lng != null) {
                        if (searchQuery != null && !searchQuery.isEmpty() && title != null && !title.toLowerCase().contains(searchQuery.toLowerCase())) continue;

                        Marker marker = mapboxMap.addMarker(new MarkerOptions()
                                .position(new LatLng(lat, lng))
                                .icon(getIconFromVector(R.drawable.ic_water_drop))
                                .title(title));
                        initiativeMarkers.add(marker);
                        markerToInitiativeId.put(marker, doc.getId());
                    }
                }
            }
        });
    }

    private Icon getIconByMappedType(String type) {
        int resId = R.drawable.ic_location_pin;
        if ("well".equals(type)) resId = R.drawable.ic_pin_well;
        else if ("truck".equals(type)) resId = R.drawable.ic_pin_truck;
        else if ("storage".equals(type)) resId = R.drawable.ic_pin_storage;
        return getIconFromVector(resId);
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

    private void showServiceDetails(String serviceId) {
        isInitiativeSelected = false;
        db.collection("services").document(serviceId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                selectedId = doc.getId();
                tvLocationTitle.setText(doc.getString("name_ar"));
                String region = doc.getString("region");
                tvLocationAddress.setText(region != null ? region : "المنطقة غير محددة");
                tvNearestSource.setText("عرض تفاصيل الخدمة والطلب");
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
    }

    private void showInitiativeDetails(String initiativeId) {
        isInitiativeSelected = true;
        db.collection("initiatives").document(initiativeId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                selectedId = doc.getId();
                tvLocationTitle.setText(doc.getString("title"));
                tvLocationAddress.setText(doc.getString("location"));
                tvNearestSource.setText("مبادرة سقاية - عرض التفاصيل");
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
    }

    @Override public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setOnMarkerClickListener(marker -> {
            if (markerToServiceId.containsKey(marker)) showServiceDetails(markerToServiceId.get(marker));
            else if (markerToInitiativeId.containsKey(marker)) showInitiativeDetails(markerToInitiativeId.get(marker));
            return true;
        });
        mapboxMap.addOnMapClickListener(point -> {
            if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
            return true;
        });
        mapboxMap.setStyle(new Style.Builder().fromUri("https://tiles.openfreemap.org/styles/bright"), style -> {
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(GAZA_CITY_CENTER).zoom(DETAILED_ZOOM).build()));
            fetchServices(currentFilter, "");
            fetchInitiatives("");
        });
    }

    @Override protected void onStart() { super.onStart(); if (mapView != null) mapView.onStart(); }
    @Override protected void onResume() { super.onResume(); if (mapView != null) mapView.onResume(); }
    @Override protected void onPause() { super.onPause(); if (mapView != null) mapView.onPause(); }
    @Override protected void onStop() { super.onStop(); if (mapView != null) mapView.onStop(); }
    @Override protected void onSaveInstanceState(@NonNull Bundle outState) { super.onSaveInstanceState(outState); if (mapView != null) mapView.onSaveInstanceState(outState); }
    @Override public void onLowMemory() { super.onLowMemory(); if (mapView != null) mapView.onLowMemory(); }
    @Override protected void onDestroy() { if (servicesListener != null) servicesListener.remove(); if (initiativesListener != null) initiativesListener.remove(); if (mapView != null) mapView.onDestroy(); super.onDestroy(); }
}