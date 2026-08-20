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
    private CardView bottomSheetCard;
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

    private List<Marker> providerMarkers = new ArrayList<>();
    private List<Marker> initiativeMarkers = new ArrayList<>();
    private Map<Marker, String> markerToProviderId = new HashMap<>();
    private Map<Marker, String> markerToInitiativeId = new HashMap<>();

    private String selectedProviderId = "";
    private String selectedProviderName = "";
    private String selectedSourceType = "";
    private String selectedAddress = "";
    private double selectedLat = 0;
    private double selectedLng = 0;
    private boolean isPickLocationMode = false;
    private boolean isInitiativeSelected = false;

    private static final LatLng GAZA_CITY_CENTER = new LatLng(31.5126, 34.4426);
    private static final double DETAILED_ZOOM = 14.5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this);
        mAuth = FirebaseAuth.getInstance();
        isPickLocationMode = getIntent().getBooleanExtra("pick_location", false);
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
                    fetchProviders(currentFilter, query);
                    fetchInitiatives(query);
                    return true;
                }
                return false;
            });
        }

        if (bottomSheetCard != null) {
            bottomSheetBehavior = BottomSheetBehavior.from((MaterialCardView) bottomSheetCard);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }

        setupClickListeners();
        setupBottomNavigation();
        checkLoginStatus();

        if (isPickLocationMode) {
            if (findViewById(R.id.btnConfirm) != null) findViewById(R.id.btnConfirm).setVisibility(View.GONE);
            if (findViewById(R.id.bottomSheetCard) != null) findViewById(R.id.bottomSheetCard).setVisibility(View.GONE);
        }
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

    private void checkLoginStatus() {
        if (btnLoginMap != null) {
            if (mAuth.getCurrentUser() == null) {
                btnLoginMap.setVisibility(View.VISIBLE);
                btnLoginMap.setOnClickListener(v -> {
                    startActivity(new Intent(MapExplorerActivity.this, LoginActivity.class));
                });
            } else {
                btnLoginMap.setVisibility(View.GONE);
            }
        }
    }

    private void setupClickListeners() {
        if (btnLayerWater != null) btnLayerWater.setOnClickListener(v -> handleFilterClick("well"));
        if (btnLayerTruck != null) btnLayerTruck.setOnClickListener(v -> handleFilterClick("truck"));
        if (btnLayerStorage != null) btnLayerStorage.setOnClickListener(v -> handleFilterClick("storage"));
        if (btnLayerInitiatives != null) btnLayerInitiatives.setOnClickListener(v -> handleFilterClick("initiatives"));

        if (Confirm1 != null) {
            Confirm1.setOnClickListener(v -> {
                if (isInitiativeSelected) {
                    Intent intent = new Intent(this, InitiativeDetailsActivity.class);
                    intent.putExtra("initiative_id", selectedProviderId);
                    startActivity(intent);
                } else {
                    if (selectedProviderId == null || selectedProviderId.isEmpty()) return;
                    Intent intent = new Intent(this, ProviderDetailsActivity.class);
                    intent.putExtra("provider_id", selectedProviderId);
                    intent.putExtra("provider_name", selectedProviderName);
                    intent.putExtra("source_type", selectedSourceType);
                    intent.putExtra("address", selectedAddress);
                    intent.putExtra("lat", selectedLat);
                    intent.putExtra("lng", selectedLng);
                    startActivity(intent);
                }
            });
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
        if (filterType.equals(currentFilter)) {
            currentFilter = null;
            Toast.makeText(this, "عرض الجميع", Toast.LENGTH_SHORT).show();
        } else {
            currentFilter = filterType;
            String filterName = filterType;
            if ("well".equals(filterType)) filterName = "آبار";
            else if ("truck".equals(filterType)) filterName = "صهاريج";
            else if ("storage".equals(filterType)) filterName = "خزانات";
            else if ("initiatives".equals(filterType)) filterName = "مبادرات";
            
            Toast.makeText(this, "تصفية: " + filterName, Toast.LENGTH_SHORT).show();
        }
        
        String query = etSearch != null ? etSearch.getText().toString().trim() : "";
        fetchProviders(currentFilter, query);
        fetchInitiatives(query);
    }

    private void fetchProviders(String filterType, String searchQuery) {
        if (mapboxMap == null) return;
        if (servicesListener != null) servicesListener.remove();

        servicesListener = db.collection("providers").addSnapshotListener((value, error) -> {
            if (error != null) return;
            if (value != null) {
                for (Marker m : providerMarkers) mapboxMap.removeMarker(m);
                providerMarkers.clear();
                markerToProviderId.clear();

                for (DocumentSnapshot doc : value.getDocuments()) {
                    String name = doc.getString("business_name");
                    String type = doc.getString("provider_type");
                    Double lat = doc.getDouble("current_lat");
                    Double lng = doc.getDouble("current_lng");

                    if (lat != null && lng != null) {
                        // إذا كان الفلتر نشطاً وهو ليس "مبادرات"، نتحقق من النوع
                        if (filterType != null) {
                            if (filterType.equals("initiatives")) continue; // إخفاء المزودين عند تصفية المبادرات
                            if (!filterType.equalsIgnoreCase(type)) continue;
                        }
                        
                        if (searchQuery != null && !searchQuery.isEmpty() && name != null && !name.toLowerCase().contains(searchQuery.toLowerCase())) continue;

                        Marker marker = mapboxMap.addMarker(new MarkerOptions()
                                .position(new LatLng(lat, lng))
                                .icon(getIconByType(type))
                                .title(name));
                        providerMarkers.add(marker);
                        markerToProviderId.put(marker, doc.getId());
                    }
                }
            }
        });
    }

    private void fetchInitiatives(String searchQuery) {
        if (mapboxMap == null) return;
        if (initiativesListener != null) initiativesListener.remove();

        // إذا كان الفلتر نشطاً وهو ليس "مبادرات"، نخفي المبادرات
        if (currentFilter != null && !currentFilter.equals("initiatives")) {
            for (Marker m : initiativeMarkers) mapboxMap.removeMarker(m);
            initiativeMarkers.clear();
            markerToInitiativeId.clear();
            return;
        }

        // جلب المبادرات المقبولة فقط (الحالة نشط)
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

    private Icon getIconByType(String type) {
        int resId = R.drawable.ic_location_pin;
        if ("well".equalsIgnoreCase(type)) resId = R.drawable.ic_pin_well;
        else if ("truck".equalsIgnoreCase(type)) resId = R.drawable.ic_pin_truck;
        else if ("storage".equalsIgnoreCase(type)) resId = R.drawable.ic_pin_storage;
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

    private void showProviderDetails(String providerId) {
        isInitiativeSelected = false;
        db.collection("providers").document(providerId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                selectedProviderId = doc.getId();
                selectedProviderName = doc.getString("business_name");
                selectedSourceType = doc.getString("provider_type");
                selectedAddress = doc.getString("location_name");
                tvLocationTitle.setText(selectedProviderName);
                tvLocationAddress.setText(selectedAddress);
                tvNearestSource.setText("عرض التفاصيل والطلب");
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
    }

    private void showInitiativeDetails(String initiativeId) {
        isInitiativeSelected = true;
        db.collection("initiatives").document(initiativeId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                selectedProviderId = doc.getId();
                selectedProviderName = doc.getString("title");
                selectedAddress = doc.getString("location");
                tvLocationTitle.setText(selectedProviderName);
                tvLocationAddress.setText(selectedAddress);
                tvNearestSource.setText("مبادرة سقاية - عرض التفاصيل");
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
    }

    @Override public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setOnMarkerClickListener(marker -> {
            if (markerToProviderId.containsKey(marker)) showProviderDetails(markerToProviderId.get(marker));
            else if (markerToInitiativeId.containsKey(marker)) showInitiativeDetails(markerToInitiativeId.get(marker));
            return true;
        });
        mapboxMap.setStyle(new Style.Builder().fromUri("https://tiles.openfreemap.org/styles/bright"), style -> {
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(GAZA_CITY_CENTER).zoom(DETAILED_ZOOM).build()));
            fetchProviders(currentFilter, "");
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