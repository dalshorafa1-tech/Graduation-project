package com.example.graduationproject;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

public class AddServiceActivity extends AppCompatActivity implements OnMapReadyCallback {

    private EditText etServiceName, etServiceDesc, etPriceLitre, etPriceCup;
    private RadioGroup rgServiceType;
    private ImageView btnBack, imgCenterPin;
    private MaterialButton btnSave;
    
    private MapView mapView;
    private MapboxMap mapboxMap;
    private Marker serviceMarker; // الدبوس الذي سيتم تحريكه
    private LatLng selectedLocation = new LatLng(31.5017, 34.4668); // موقع افتراضي (غزة)

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize Mapbox before layout inflation
        Mapbox.getInstance(this);
        
        setContentView(R.layout.activity_add_service);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews(savedInstanceState);

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveService());
    }

    private void initViews(Bundle savedInstanceState) {
        btnBack = findViewById(R.id.btnBack);
        etServiceName = findViewById(R.id.etServiceName);
        etServiceDesc = findViewById(R.id.etServiceDescription);
        rgServiceType = findViewById(R.id.rgServiceType);
        etPriceLitre = findViewById(R.id.etPriceLitre);
        etPriceCup = findViewById(R.id.etPriceCup);
        btnSave = findViewById(R.id.btnSaveService);
        imgCenterPin = findViewById(R.id.imgCenterPin);

        mapView = findViewById(R.id.mapview);
        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(new Style.Builder().fromUri("https://tiles.openfreemap.org/styles/bright"), style -> {
            // إخفاء الدبوس الثابت الموجود في تخطيط XML لأننا سنستخدم Marker تفاعلي
            if (imgCenterPin != null) {
                imgCenterPin.setVisibility(View.GONE);
            }

            // إضافة الدبوس (Marker) في الموقع الافتراضي
            serviceMarker = mapboxMap.addMarker(new MarkerOptions()
                    .position(selectedLocation)
                    .icon(getIconFromVector(R.drawable.ic_location_pin)));

            // ضبط الكاميرا على الموقع الافتراضي
            mapboxMap.setCameraPosition(new CameraPosition.Builder()
                    .target(selectedLocation)
                    .zoom(14)
                    .build());
            
            // الاستماع للنقر على الخريطة لتحريك الدبوس إلى مكان النقرة
            mapboxMap.addOnMapClickListener(latLng -> {
                selectedLocation = latLng;
                if (serviceMarker != null) {
                    serviceMarker.setPosition(latLng);
                }
                Log.d("AddService", "Marker moved to: " + latLng.getLatitude() + ", " + latLng.getLongitude());
                return true;
            });
        });
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

    private void saveService() {
        String name = etServiceName.getText().toString().trim();
        String desc = etServiceDesc.getText().toString().trim();
        String priceLitreStr = etPriceLitre.getText().toString().trim();
        String priceCupStr = etPriceCup.getText().toString().trim();

        // الحصول على نوع الخدمة المختار من الـ RadioGroup
        int selectedId = rgServiceType.getCheckedRadioButtonId();
        String serviceType = "";
        if (selectedId != -1) {
            RadioButton rb = findViewById(selectedId);
            serviceType = rb.getText().toString();
        }

        if (TextUtils.isEmpty(name)) {
            etServiceName.setError("الاسم مطلوب");
            return;
        }
        if (TextUtils.isEmpty(serviceType)) {
            Toast.makeText(this, "يرجى اختيار نوع الخدمة", Toast.LENGTH_SHORT).show();
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
        
        // حفظ إحداثيات الموقع المختار من الخريطة (موقع الماركر)
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
