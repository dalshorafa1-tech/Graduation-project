package com.example.graduationproject;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

public class AddServiceActivity extends AppCompatActivity implements OnMapReadyCallback {

    private EditText etServiceName, etServiceDesc, etPriceLitre, etPriceCup;
    private AutoCompleteTextView spinnerServiceType;
    private ImageView btnBack;
    private MaterialButton btnSave;
    
    private MapView mapView;
    private MapboxMap mapboxMap;
    private LatLng selectedLocation = new LatLng(31.5017, 34.4668); // موقع افتراضي (غزة)

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String[] serviceTypes = {"صهريج", "آبار", "خزانات"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize Mapbox before layout inflation
        Mapbox.getInstance(this);
        
        setContentView(R.layout.activity_add_service);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews(savedInstanceState);
        setupServiceTypeSpinner();

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveService());
    }

    private void initViews(Bundle savedInstanceState) {
        btnBack = findViewById(R.id.btnBack);
        etServiceName = findViewById(R.id.etServiceName);
        etServiceDesc = findViewById(R.id.etServiceDescription);
        spinnerServiceType = findViewById(R.id.spinnerServiceType);
        etPriceLitre = findViewById(R.id.etPriceLitre);
        etPriceCup = findViewById(R.id.etPriceCup);
        btnSave = findViewById(R.id.btnSaveService);

        mapView = findViewById(R.id.mapview);
        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        }
    }

    private void setupServiceTypeSpinner() {
        // استخدام تصميم مخصص للعناصر لضمان ظهور النص بوضوح
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_dropdown, serviceTypes);
        spinnerServiceType.setAdapter(adapter);
        
        // منع الفلترة وإظهار القائمة كاملة عند النقر
        spinnerServiceType.setThreshold(100); 
        
        spinnerServiceType.setOnClickListener(v -> {
            spinnerServiceType.showDropDown();
        });

        // التأكد من أن القائمة تظهر عند التركيز أيضاً
        spinnerServiceType.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                spinnerServiceType.showDropDown();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(new Style.Builder().fromUri("https://tiles.openfreemap.org/styles/bright"), style -> {
            // الخريطة جاهزة
            mapboxMap.setCameraPosition(new CameraPosition.Builder()
                    .target(selectedLocation)
                    .zoom(12)
                    .build());
            
            // الاستماع لتغيير الكاميرا للحصول على الإحداثيات في المنتصف
            mapboxMap.addOnCameraIdleListener(() -> {
                selectedLocation = mapboxMap.getCameraPosition().target;
                Log.d("AddService", "New location selected: " + selectedLocation.getLatitude() + ", " + selectedLocation.getLongitude());
            });
        });
    }

    private void saveService() {
        String name = etServiceName.getText().toString().trim();
        String desc = etServiceDesc.getText().toString().trim();
        String serviceType = spinnerServiceType.getText().toString().trim();
        String priceLitreStr = etPriceLitre.getText().toString().trim();
        String priceCupStr = etPriceCup.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etServiceName.setError("الاسم مطلوب");
            return;
        }
        if (TextUtils.isEmpty(serviceType)) {
            spinnerServiceType.setError("يرجى اختيار نوع الخدمة");
            Toast.makeText(this, "يرجى اختيار نوع الخدمة من القائمة", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(priceLitreStr)) {
            etPriceLitre.setError("السعر مطلوب");
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("جاري الحفظ...");

        String uid = mAuth.getCurrentUser().getUid();

        ServiceModel service = new ServiceModel();
        service.setProviderId(uid);
        service.setNameAr(name);
        service.setDescriptionAr(desc);
        service.setServiceType(serviceType);
        service.setPrice(Double.parseDouble(priceLitreStr));
        service.setPriceCup(TextUtils.isEmpty(priceCupStr) ? 0.0 : Double.parseDouble(priceCupStr));
        service.setStatus("pending");
        service.setActive(false);
        service.setCreatedAt(Timestamp.now());
        service.setProviderEmail(mAuth.getCurrentUser().getEmail());
        
        // حفظ إحداثيات الموقع المختار من الخريطة
        service.setLatitude(selectedLocation.getLatitude());
        service.setLongitude(selectedLocation.getLongitude());

        // جلب تفاصيل المزود لإدراجها في مستند الخدمة
        db.collection("providers").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        service.setProviderName(doc.getString("business_name"));
                        service.setProviderType(doc.getString("provider_type"));
                        service.setProviderPhone(doc.getString("phone"));
                        service.setProviderIdNumber(doc.getString("id_number"));
                        service.setMunicipalityCode(doc.getString("municipality_code"));
                        service.setRegion(doc.getString("location_name"));
                    }
                    saveToFirestore(service);
                })
                .addOnFailureListener(e -> saveToFirestore(service));
    }

    private void saveToFirestore(ServiceModel service) {
        db.collection("services").add(service)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "تم إرسال الخدمة والموقع للأدمن للمراجعة ✅", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    btnSave.setText("إضافة الخدمة");
                    Toast.makeText(this, "فشل الحفظ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Lifecycle methods for MapView
    @Override
    protected void onStart() { super.onStart(); if (mapView != null) mapView.onStart(); }
    @Override
    protected void onResume() { super.onResume(); if (mapView != null) mapView.onResume(); }
    @Override
    protected void onPause() { super.onPause(); if (mapView != null) mapView.onPause(); }
    @Override
    protected void onStop() { super.onStop(); if (mapView != null) mapView.onStop(); }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) { super.onSaveInstanceState(outState); if (mapView != null) mapView.onSaveInstanceState(outState); }
    @Override
    public void onLowMemory() { super.onLowMemory(); if (mapView != null) mapView.onLowMemory(); }
    @Override
    protected void onDestroy() { super.onDestroy(); if (mapView != null) mapView.onDestroy(); }
}
