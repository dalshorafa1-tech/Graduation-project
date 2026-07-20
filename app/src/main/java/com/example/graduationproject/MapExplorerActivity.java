package com.example.graduationproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapExplorerActivity extends AppCompatActivity {

    private MapView map = null;
    private IMapController mapController;
    private EditText etSearch;
    private CardView bottomSheetCard;
    private BottomSheetBehavior<MaterialCardView> bottomSheetBehavior;
    private TextView tvLocationTitle, tvLocationAddress, tvNearestSource, login;
    private CardView Confirm1;
    
    private MaterialCardView btnFilterWell, btnFilterTruck, btnFilterStorage;
    private String currentFilter = null;
    private FirebaseFirestore db;
    private ListenerRegistration servicesListener;
    private List<Marker> serviceMarkers = new ArrayList<>();
    private List<Marker> providerMarkers = new ArrayList<>();

    private String selectedProviderId = "";
    private String selectedProviderName = "";
    private String selectedSourceType = "";
    private String selectedAddress = "";
    private double selectedLat = 0;
    private double selectedLng = 0;
    private boolean isPickLocationMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isPickLocationMode = getIntent().getBooleanExtra("pick_location", false);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_map_explorer);

        db = FirebaseFirestore.getInstance();
        initViews();
        
        login.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                fetchProviders(currentFilter, etSearch.getText().toString().trim());
                return true;
            }
            return false;
        });

        bottomSheetBehavior = BottomSheetBehavior.from((MaterialCardView) bottomSheetCard);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        if (map != null) {
            map.setTileSource(TileSourceFactory.MAPNIK);
            map.setMultiTouchControls(true);
            mapController = map.getController();
            mapController.setZoom(15.0);
            mapController.setCenter(new GeoPoint(31.516, 34.448));
            setupMapEvents();
        }

        setupClickListeners();

        if (isPickLocationMode) {
            findViewById(R.id.btnConfirm).setVisibility(View.GONE);
            findViewById(R.id.bottomSheetCard).setVisibility(View.GONE);
        } else {
            checkAndAddSampleData();
            startListeningForActiveServices(); // إضافة الخدمات الجديدة المقبولة
        }
    }

    private void initViews() {
        map = findViewById(R.id.map);
        login = findViewById(R.id.login);
        Confirm1 = findViewById(R.id.btnConfirm);
        bottomSheetCard = findViewById(R.id.bottomSheetCard);
        tvLocationTitle = findViewById(R.id.tvLocationTitle);
        tvLocationAddress = findViewById(R.id.tvLocationAddress);
        tvNearestSource = findViewById(R.id.tvNear);
        etSearch = findViewById(R.id.etSearch);
        btnFilterWell = findViewById(R.id.btnFilterWell);
        btnFilterTruck = findViewById(R.id.btnFilterTruck);
        btnFilterStorage = findViewById(R.id.btnFilterStorage);
    }

    private void setupClickListeners() {
        btnFilterWell.setOnClickListener(v -> handleFilterClick("well", btnFilterWell));
        btnFilterTruck.setOnClickListener(v -> handleFilterClick("truck", btnFilterTruck));
        btnFilterStorage.setOnClickListener(v -> handleFilterClick("storage", btnFilterStorage));

        findViewById(R.id.btnMyLocation).setOnClickListener(v -> {
            mapController.animateTo(new GeoPoint(31.516, 34.448));
            hideBottomCard();
        });

        Confirm1.setOnClickListener(v -> {
            if (selectedProviderName.isEmpty()) return;
            Intent intent = new Intent(this, ProviderDetailsActivity.class);
            intent.putExtra("provider_id", selectedProviderId);
            intent.putExtra("provider_name", selectedProviderName);
            intent.putExtra("source_type", selectedSourceType);
            intent.putExtra("address", selectedAddress);
            intent.putExtra("lat", selectedLat);
            intent.putExtra("lng", selectedLng);
            startActivity(intent);
        });
    }

    private void checkAndAddSampleData() {
        db.collection("providers").limit(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && (task.getResult() == null || task.getResult().isEmpty())) {
                addSampleProvidersToFirestore();
            } else {
                fetchProviders(null, null);
            }
        });
    }

    private void addSampleProvidersToFirestore() {
        String[] names = {"محطة الرمال", "بئر الشيخ رضوان", "صهريج حي النصر"};
        String[] types = {"storage", "well", "truck"};
        double[] lats = {31.516, 31.538, 31.530};
        double[] lngs = {34.448, 34.462, 34.455};
        for (int i = 0; i < names.length; i++) {
            Map<String, Object> p = new HashMap<>();
            p.put("business_name", names[i]);
            p.put("provider_type", types[i]);
            p.put("current_lat", lats[i]);
            p.put("current_lng", lngs[i]);
            p.put("status", "نشط");
            p.put("location_name", "غزة");
            db.collection("providers").add(p);
        }
        fetchProviders(null, null);
    }

    private void startListeningForActiveServices() {
        if (servicesListener != null) servicesListener.remove();
        servicesListener = db.collection("services")
                .whereEqualTo("status", "approved")
                .whereEqualTo("isActive", true)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) return;
                    if (snapshots != null) {
                        clearMarkers(serviceMarkers);
                        for (QueryDocumentSnapshot doc : snapshots) {
                            ServiceModel s = doc.toObject(ServiceModel.class);
                            s.setId(doc.getId());
                            if (s.getLatitude() != 0) addServiceMarker(s);
                        }
                        map.invalidate();
                    }
                });
    }

    private void fetchProviders(String type, String searchQuery) {
        clearMarkers(providerMarkers);
        Query query = db.collection("providers");
        if (type != null) query = query.whereEqualTo("provider_type", type);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    String name = doc.getString("business_name");
                    Double lat = doc.getDouble("current_lat");
                    Double lng = doc.getDouble("current_lng");
                    String pType = doc.getString("provider_type");
                    if (name == null || lat == null || lng == null) continue;
                    if (searchQuery != null && !name.toLowerCase().contains(searchQuery.toLowerCase())) continue;

                    int icon = R.drawable.water;
                    int color = Color.parseColor("#0069B4");
                    if ("truck".equals(pType)) { icon = R.drawable.truck; color = Color.parseColor("#10B981"); }
                    else if ("storage".equals(pType)) { icon = R.drawable.barrel; color = Color.parseColor("#FF9800"); }

                    addCustomMarker(doc.getId(), new GeoPoint(lat, lng), name, doc.getString("location_name"), getArabicType(pType), icon, color);
                }
                map.invalidate();
            }
        });
    }

    private void addServiceMarker(ServiceModel s) {
        Marker m = new Marker(map);
        m.setPosition(new GeoPoint(s.getLatitude(), s.getLongitude()));
        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        m.setIcon(createCustomMarkerIcon(R.drawable.water, Color.parseColor("#0069B4")));
        m.setOnMarkerClickListener((marker, mv) -> {
            showBottomCard(s.getNameAr(), s.getProviderName(), "خدمة نشطة");
            selectedProviderId = s.getProviderId();
            selectedProviderName = s.getProviderName();
            mapController.animateTo(marker.getPosition());
            return true;
        });
        map.getOverlays().add(m);
        serviceMarkers.add(m);
    }

    private void addCustomMarker(String id, GeoPoint point, String title, String address, String type, int icon, int color) {
        Marker m = new Marker(map);
        m.setPosition(point);
        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        m.setIcon(createCustomMarkerIcon(icon, color));
        m.setOnMarkerClickListener((marker, mv) -> {
            selectedProviderId = id; selectedProviderName = title; selectedSourceType = type;
            selectedAddress = address; selectedLat = point.getLatitude(); selectedLng = point.getLongitude();
            showBottomCard(title, address, type);
            mapController.animateTo(point);
            return true;
        });
        map.getOverlays().add(m);
        providerMarkers.add(m);
    }

    private BitmapDrawable createCustomMarkerIcon(int resId, int color) {
        View v = LayoutInflater.from(this).inflate(R.layout.custom_marker_layout, null);
        ImageView img = v.findViewById(R.id.markerIcon);
        MaterialCardView card = v.findViewById(R.id.markerContainer);
        if (img != null) { img.setImageResource(resId); img.setColorFilter(color); }
        if (card != null) card.setStrokeColor(color);
        v.measure(0, 0); v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        Bitmap b = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        v.draw(new Canvas(b));
        return new BitmapDrawable(getResources(), b);
    }

    private void clearMarkers(List<Marker> list) {
        for (Marker m : list) map.getOverlays().remove(m);
        list.clear();
    }

    private void handleFilterClick(String type, MaterialCardView card) {
        if (type.equals(currentFilter)) { currentFilter = null; resetFilterUI(); fetchProviders(null, null); }
        else { currentFilter = type; updateFilterUI(card); fetchProviders(type, null); }
    }

    private void updateFilterUI(MaterialCardView card) {
        resetFilterUI();
        card.setCardBackgroundColor(Color.parseColor("#E3F2FD"));
        card.setStrokeColor(Color.parseColor("#0069B4"));
        card.setStrokeWidth(4);
    }

    private void resetFilterUI() {
        MaterialCardView[] cards = {btnFilterWell, btnFilterTruck, btnFilterStorage};
        for (MaterialCardView c : cards) { c.setCardBackgroundColor(Color.WHITE); c.setStrokeWidth(0); }
    }

    private String getArabicType(String type) {
        if ("well".equals(type)) return "بئر مياه 🚰";
        if ("truck".equals(type)) return "صهريج متنقل 🚛";
        return "مزود خدمة";
    }

    private void setupMapEvents() {
        map.getOverlays().add(0, new MapEventsOverlay(new MapEventsReceiver() {
            @Override public boolean singleTapConfirmedHelper(GeoPoint p) {
                if (isPickLocationMode) {
                    Intent r = new Intent(); r.putExtra("lat", p.getLatitude()); r.putExtra("lng", p.getLongitude());
                    setResult(RESULT_OK, r); finish(); return true;
                }
                hideBottomCard(); return true;
            }
            @Override public boolean longPressHelper(GeoPoint p) { return false; }
        }));
    }

    private void showBottomCard(String title, String address, String type) {
        tvLocationTitle.setText(title); tvLocationAddress.setText(address); tvNearestSource.setText(type);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void hideBottomCard() { bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN); }

    @Override protected void onDestroy() { super.onDestroy(); if (servicesListener != null) servicesListener.remove(); }
    @Override public void onResume() { super.onResume(); if(map != null) map.onResume(); }
    @Override public void onPause() { super.onPause(); if(map != null) map.onPause(); }
}
