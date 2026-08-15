package com.example.graduationproject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class InitiativeDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "InitiativeDetailsDebug";

    private GoogleMap mBoundedMap;
    private TextView tvShowRoute;
    private com.google.android.material.card.MaterialCardView btnContactProvider;
    private LinearLayout navDashboard, navInitiatives, navNeedMap, navWallet;

    private TextView tvInitiativeTitle, tvInitiativeLocation, tvWaterAmount, tvCost;

    private TextView tvInitiativeStatus;

    private android.widget.ImageView ivNotificationBtn;

    private FirebaseFirestore db;
    private String initiativeId = "";

    // إحداثيات افتراضية لوسط غزة (يتم تحديثها ديناميكياً بناءً على موقع المبادرة)
    private LatLng targetLocation = new LatLng(31.4485, 34.3917);
    private String locationName = "موقع المبادرة";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initiative_details);

        db = FirebaseFirestore.getInstance();

        // استقبال المعرف الفرعي للمبادرة من القائمة
        if (getIntent() != null) {
            initiativeId = getIntent().getStringExtra("initiative_id");
        }

        initViews();
        setupClickListeners();
        fetchInitiativeDetails();

        // ربط وتجهيز خريطة جوجل المدمجة بالواجهة
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

        tvInitiativeStatus = findViewById(R.id.tvInitiativeStatus);

        ivNotificationBtn = findViewById(R.id.ivNotification);

        navDashboard = findViewById(R.id.nav_dashboard);
        navInitiatives = findViewById(R.id.nav_initiatives);
        navNeedMap = findViewById(R.id.nav_need_map);
        navWallet = findViewById(R.id.nav_wallet);
    }

    private void fetchInitiativeDetails() {
        if (initiativeId == null || initiativeId.isEmpty()) {
            Toast.makeText(this, "خطأ: لم يتم العثور على معرف المبادرة", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("initiatives").document(initiativeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String title = documentSnapshot.getString("title");
                        String location = documentSnapshot.getString("location");
                        Long targetLiters = documentSnapshot.getLong("targetLiters");
                        String status = documentSnapshot.getString("status");

                        if (tvInitiativeTitle != null && title != null) tvInitiativeTitle.setText(title);
                        if (tvInitiativeLocation != null && location != null) {
                            tvInitiativeLocation.setText(location);
                            locationName = location;
                        }

                        if (tvWaterAmount != null && targetLiters != null) {
                            tvWaterAmount.setText(targetLiters + " لتر");

                            // حساب التكلفة التقريبية بناءً على السعر (0.05 شيكل للتر)
                            double totalCost = targetLiters * 0.05;
                            if (tvCost != null) {
                                tvCost.setText(String.format("%.2f ILS", totalCost));
                            }
                        }

                        //  فحص وعرض حالة المبادرة (مكتملة أم قيد التنفيذ) وتلوينها
                        if (tvInitiativeStatus != null) {
                            if (status == null || status.trim().isEmpty()) {
                                status = "قيد التنفيذ"; // حالة افتراضية إذا لم تكن محددة في فيربيس
                            }

                            tvInitiativeStatus.setText(status);

                            if (status.equals("مكتملة") || status.equalsIgnoreCase("completed")) {
                                tvInitiativeStatus.setTextColor(Color.parseColor("#4CAF50")); // لون أخضر للمكتملة
                                tvInitiativeStatus.setText("مكتملة ✅");
                            } else {
                                tvInitiativeStatus.setTextColor(Color.parseColor("#0069B4")); // لون أزرق لقيد التنفيذ
                                tvInitiativeStatus.setText("قيد التنفيذ ⚙️");
                            }
                        }

                        // محاولة مطابقة اسم الحي للحصول على إحداثيات دقيقة وعرضها على الخريطة
                        updateCoordinatesByName(location);

                    } else {
                        Toast.makeText(this, "المبادرة غير موجودة في قاعدة البيانات", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "فشل جلب البيانات: " + e.getMessage()));
    }

    // دالة لمطابقة الحي المختار بالخريطة للحصول على إحداثيات غزة التقريبية
    private void updateCoordinatesByName(String name) {
        if (name == null) return;
        String lowerName = name.toLowerCase();

        if (lowerName.contains("الرمال")) {
            targetLocation = new LatLng(31.516, 34.448);
        } else if (lowerName.contains("نصر") || lowerName.contains("النصر")) {
            targetLocation = new LatLng(31.530, 34.455);
        } else if (lowerName.contains("رضوان")) {
            targetLocation = new LatLng(31.538, 34.462);
        } else if (lowerName.contains("الهوا")) {
            targetLocation = new LatLng(31.498, 34.438);
        } else if (lowerName.contains("شجاعية")) {
            targetLocation = new LatLng(31.505, 34.482);
        } else if (lowerName.contains("زيتون")) {
            targetLocation = new LatLng(31.492, 34.465);
        } else if (lowerName.contains("جباليا")) {
            targetLocation = new LatLng(31.542, 34.492);
        } else if (lowerName.contains("خانيونس")) {
            targetLocation = new LatLng(31.345, 34.305);
        } else if (lowerName.contains("دير البلح")) {
            targetLocation = new LatLng(31.417, 34.350);
        } else if (lowerName.contains("رفح")) {
            targetLocation = new LatLng(31.285, 34.255);
        } else if (lowerName.contains("نصيرات")) {
            targetLocation = new LatLng(31.4485, 34.3917);
        }

        // تحديث الخريطة فوراً بالإحداثيات المحدثة للحي
        if (mBoundedMap != null) {
            mBoundedMap.clear();
            mBoundedMap.addMarker(new MarkerOptions().position(targetLocation).title(locationName));
            mBoundedMap.moveCamera(CameraUpdateFactory.newLatLngZoom(targetLocation, 14f));
        }
    }

    private void setupClickListeners() {
        //  عند الضغط على أيقونة الجرس، يتم الانتقال إلى شاشة التنبيهات
        if (ivNotificationBtn != null) {
            ivNotificationBtn.setOnClickListener(v -> {
                Intent intent = new Intent(InitiativeDetailsActivity.this, UserNotificationActivity.class);
                startActivity(intent);
            });
        }

        // زر عرض المسار على الخريطة
        if (tvShowRoute != null) {
            tvShowRoute.setOnClickListener(v -> {
                Toast.makeText(this, "جاري احتساب وعرض المسار إلى " + locationName + "...", Toast.LENGTH_SHORT).show();
            });
        }

        // زر مراسلة أو الاتصال بالمزود المعتمد
        if (btnContactProvider != null) {
            btnContactProvider.setOnClickListener(v -> {
                Toast.makeText(this, "فتح شاشة المحادثة مع مزود الخدمة للمبادرة...", Toast.LENGTH_SHORT).show();
            });
        }

        // --- برمجة أزرار شريط التنقل السفلي بالتنقل بين الشاشات الفعلية ---
        navDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(this, InitiatorDashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        navInitiatives.setOnClickListener(v -> {
            Toast.makeText(this, "أنت متواجد بالفعل في قسم المبادرات", Toast.LENGTH_SHORT).show();
        });

        navNeedMap.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapExplorerActivity.class);
            startActivity(intent);
        });

        navWallet.setOnClickListener(v -> {
            Toast.makeText(this, "الانتقال إلى المحفظة المخصصة للتبرعات...", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mBoundedMap = googleMap;

        // إضافة علامة (Marker) على الخريطة بالموقع الحالي
        mBoundedMap.addMarker(new MarkerOptions()
                .position(targetLocation)
                .title(locationName));

        // تحريك الكاميرا إلى الموقع وعمل تقريب مناسب
        mBoundedMap.moveCamera(CameraUpdateFactory.newLatLngZoom(targetLocation, 14f));

        // إيقاف إيماءات التمرير العشوائي لتثبيت الخريطة داخل الكارد بشكل أنيق ومريح للمستخدم
        mBoundedMap.getUiSettings().setScrollGesturesEnabled(false);
    }
}