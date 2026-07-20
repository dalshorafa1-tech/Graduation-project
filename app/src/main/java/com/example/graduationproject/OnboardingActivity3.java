package com.example.graduationproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;



public class OnboardingActivity3 extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView tvLocationTitle, tvLocationAddress;
    private String currentSelectedType = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding3);

        // ربط نصوص البطاقة السفلية
        tvLocationTitle = findViewById(R.id.tvLocationTitle);
        tvLocationAddress = findViewById(R.id.tvLocationAddress);

        // تهيئة الخريطة
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        setupClickListeners();
    }

    private void setupClickListeners() {
        // برمجة أيقونات الفلترة الجانبية
        findViewById(R.id.btnLayerWater).setOnClickListener(v -> toggleFilter("well"));
        findViewById(R.id.btnLayerTruck).setOnClickListener(v -> toggleFilter("truck"));
        findViewById(R.id.btnLayerStorage).setOnClickListener(v -> toggleFilter("storage"));

        // زر التأكيد
        findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });

        // القائمة
        findViewById(R.id.ivMenu).setOnClickListener(v -> {
            Toast.makeText(this, "القائمة الرئيسية", Toast.LENGTH_SHORT).show();
        });
    }

    private void toggleFilter(String type) {
        if (type.equals(currentSelectedType)) {
            currentSelectedType = null; // إلغاء الفلتر إذا ضغط مرتين
            fetchProvidersByType(null);
            Toast.makeText(this, "عرض جميع المزودين", Toast.LENGTH_SHORT).show();
        } else {
            currentSelectedType = type;
            fetchProvidersByType(type);
            Toast.makeText(this, "عرض " + getArabicName(type) + " فقط", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        
        // إعدادات الخريطة
        LatLng gaza = new LatLng(31.5017, 34.4578);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gaza, 13));

        // مستمع للنقر على الدبابيس لتحديث البطاقة السفلية
        mMap.setOnMarkerClickListener(marker -> {
            tvLocationTitle.setText(marker.getTitle());
            tvLocationAddress.setText(marker.getSnippet());
            marker.showInfoWindow();
            return false;
        });

        // جلب البيانات الأولية
        fetchProvidersByType(null);
    }

    private void fetchProvidersByType(String type) {
        if (mMap == null) return;
        mMap.clear();

        /*
        SupabaseApi api = SupbaseClient.getClient(this).create(SupabaseApi.class);
        String filter = (type == null) ? null : "eq." + type;

        api.getProviders("*", filter).enqueue(new Callback<List<ProviderModel>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProviderModel>> call, @NonNull Response<List<ProviderModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (ProviderModel provider : response.body()) {
                        LatLng position = new LatLng(provider.getCurrentLat(), provider.getCurrentLng());
                        
                        int iconRes = R.drawable.water;
                        if ("truck".equals(provider.getProviderType())) iconRes = R.drawable.truck;
                        else if ("storage".equals(provider.getProviderType())) iconRes = R.drawable.barrel;

                        mMap.addMarker(new MarkerOptions()
                                .position(position)
                                .title(provider.getBusinessName())
                                .snippet("حالة المزود: " + provider.getStatus())
                                .icon(bitmapDescriptorFromVector(iconRes)));
                    }
                }
            }



            @Override
            public void onFailure(@NonNull Call<List<ProviderModel>> call, @NonNull Throwable t) {
                Log.e("MapError", "Failed", t);
            }
        });
*/
    }

    // تحويل الأيقونة لشكل متوافق مع خرائط جوجل
    private BitmapDescriptor bitmapDescriptorFromVector(int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(this, vectorResId);
        if (vectorDrawable == null) return BitmapDescriptorFactory.defaultMarker();
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private String getArabicName(String type) {
        if ("well".equals(type)) return "الآبار";
        if ("truck".equals(type)) return "الصهاريج";
        if ("storage".equals(type)) return "المستودعات";
        return "";
    }
}
