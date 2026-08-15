package com.example.graduationproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileInitiatorsActivity extends AppCompatActivity {
    private static final String TAG = "ProfileInitiatorsDebug";

    private MaterialCardView btnBack, btnEditPhoto;
    private ImageView btnHeaderSettings;
    private TextView tvUserName, tvProviderId;
    private MaterialCardView optionWallet, optionInitiatives, optionSettings, optionLogout;
    private LinearLayout navDashboard, navInitiatives, navMap, navWallet;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_provider_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupClickListeners();
        loadUserDataFromFirebase();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnHeaderSettings = findViewById(R.id.btnHeaderSettings);
        btnEditPhoto = findViewById(R.id.btnEditPhoto);
        tvUserName = findViewById(R.id.tvUserName);
        tvProviderId = findViewById(R.id.tvProviderId);

        optionWallet = findViewById(R.id.optionWallet);
        optionInitiatives = findViewById(R.id.optionInitiatives);
        optionSettings = findViewById(R.id.optionSettings);
        optionLogout = findViewById(R.id.optionLogout);

        navDashboard = findViewById(R.id.nav_dashboard);
        navInitiatives = findViewById(R.id.nav_initiatives);
        navMap = findViewById(R.id.nav_map);
        navWallet = findViewById(R.id.nav_wallet);
    }

    //  جلب ومراقبة بيانات المستخدم بشكل حي (Realtime Listener) مع دعم كافة الكولكشنز المحتملة
    private void loadUserDataFromFirebase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "الرجاء تسجيل الدخول أولاً", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        // استخدام addSnapshotListener لضمان تحديث الاسم تلقائياً بدون الحاجة لإعادة تشغيل التطبيق
        db.collection("users").document(userId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "فشل جلب البيانات: " + error.getMessage());
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        // محاولة قراءة الاسم بكافة المفاتيح المحتملة في الفايربيس
                        String name = documentSnapshot.getString("name");
                        if (name == null) name = documentSnapshot.getString("userName");
                        if (name == null) name = documentSnapshot.getString("username");
                        if (name == null) name = documentSnapshot.getString("fullName");
                        if (name == null) name = currentUser.getDisplayName();

                        String phone = documentSnapshot.getString("phone");
                        if (phone == null) phone = documentSnapshot.getString("phoneNumber");

                        // تعيين النصوص للواجهة بأمان
                        if (tvUserName != null) {
                            if (name != null && !name.trim().isEmpty()) {
                                tvUserName.setText(name);
                            } else {
                                tvUserName.setText("مستخدم مبادر");
                            }
                        }

                        if (tvProviderId != null) {
                            if (phone != null && !phone.trim().isEmpty()) {
                                tvProviderId.setText("ID: " + phone);
                            } else {
                                tvProviderId.setText("ID: " + userId.substring(0, 8));
                            }
                        }
                    } else {
                        // محاولة البحث البديل في كولكشن "initiators" في حال كان حساب المستخدم مبوب بشكل منفصل
                        db.collection("initiators").document(userId)
                                .get()
                                .addOnSuccessListener(snapshot -> {
                                    if (snapshot.exists()) {
                                        String name = snapshot.getString("name");
                                        String phone = snapshot.getString("phone");
                                        if (tvUserName != null && name != null) tvUserName.setText(name);
                                        if (tvProviderId != null && phone != null) tvProviderId.setText("ID: " + phone);
                                    }
                                });
                    }
                });
    }

    private void setupClickListeners() {
        // زر الرجوع في الهيدر العلوي
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        //  أيقونة الإعدادات السريعة العلوية تنتقل الآن لواجهة SettingsInitiatorsActivity المعتمدة لمنع الـ Crash
        if (btnHeaderSettings != null) {
            btnHeaderSettings.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileInitiatorsActivity.this, SettingsInitiatorsActivity.class);
                startActivity(intent);
            });
        }

        // زر التعديل (القلم) ينتقل إلى واجهة تعديل الملف الشخصي
        if (btnEditPhoto != null) {
            btnEditPhoto.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileInitiatorsActivity.this, EditProfileActivity.class);
                startActivity(intent);
            });
        }

        // خيار الانتقال إلى شاشة المحفظة
        if (optionWallet != null) {
            optionWallet.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileInitiatorsActivity.this, WalletActivity.class);
                startActivity(intent);
            });
        }

        // خيار الانتقال لسجل وقائمة المبادرات
        if (optionInitiatives != null) {
            optionInitiatives.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileInitiatorsActivity.this, InitiativesListActivity.class);
                startActivity(intent);
            });
        }

        // 🌟 خيار فتح الإعدادات العامة يوجه لصفحة SettingsInitiatorsActivity لحل مشكلة كراش الانتقال
        if (optionSettings != null) {
            optionSettings.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileInitiatorsActivity.this, SettingsInitiatorsActivity.class);
                startActivity(intent);
            });
        }

        // خيار تسجيل الخروج
        if (optionLogout != null) {
            optionLogout.setOnClickListener(v -> {
                mAuth.signOut();
                Intent intent = new Intent(ProfileInitiatorsActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        // --- مستمعات شريط الملاحة السفلي (Bottom Navigation) الانتقال الفعلي ---
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
            });
        }

        if (navMap != null) {
            navMap.setOnClickListener(v -> {
                Intent intent = new Intent(this, NeedMapActivity.class);
                startActivity(intent);
            });
        }

        if (navWallet != null) {
            navWallet.setOnClickListener(v -> {
                Intent intent = new Intent(this, WalletActivity.class);
                startActivity(intent);
            });
        }
    }
}