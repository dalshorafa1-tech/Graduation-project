package com.example.graduationproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsInitiatorsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsInitiators";
    private ImageView btnBack;
    private RelativeLayout btnChangePassword, btnPrivacy, btnAbout;
    private View btnLogout;
    private TextView btnLangEn, btnLangAr;
    private TextView tvUserName, tvUserEmail, tvUserPhone;
    private SwitchMaterial switchNotifications, switchDataSaver;

    private LinearLayout navDashboard, navNeedMap, navInitiators;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_settings);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupClickListeners();
        setupSwitches();
        loadUserData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnMenu);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnPrivacy = findViewById(R.id.btnPrivacy);
        btnAbout = findViewById(R.id.btnAbout);
        btnLogout = findViewById(R.id.btnLogout);

        btnLangEn = findViewById(R.id.btnLangEn);
        btnLangAr = findViewById(R.id.btnLangAr);

        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserPhone = findViewById(R.id.tvUserPhone);

        switchNotifications = findViewById(R.id.switchNotifications);
        switchDataSaver = findViewById(R.id.switchDataSaver);

        navDashboard = findViewById(R.id.navDashboard);
        navNeedMap = findViewById(R.id.navNeedMap);
        navInitiators = findViewById(R.id.navInitiators);
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            UserModel user = documentSnapshot.toObject(UserModel.class);
                            if (user != null) {
                                tvUserName.setText(user.getFullName() != null ? user.getFullName() : "بدون اسم");
                                tvUserEmail.setText(user.getEmail() != null ? user.getEmail() : currentUser.getEmail());
                                tvUserPhone.setText(user.getPhone() != null ? user.getPhone() : "لا يوجد هاتف");
                            }
                        } else {
                            tvUserName.setText(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "مستخدم إرواء");
                            tvUserEmail.setText(currentUser.getEmail());
                            tvUserPhone.setText("غير متوفر");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading user data", e);
                        Toast.makeText(this, "فشل تحميل بيانات المستخدم", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void setupClickListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                mAuth.signOut();
                Intent intent = new Intent(SettingsInitiatorsActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null && user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                    mAuth.sendPasswordResetEmail(user.getEmail())
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(this, "تم إرسال رابط إعادة تعيين كلمة المرور إلى بريدك الإلكتروني بنجاح ✅", Toast.LENGTH_LONG).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "فشل إرسال الرابط: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(this, "عذراً، هذا الحساب غير مرتبط ببريد إلكتروني صالح لإعادة التعيين.", Toast.LENGTH_LONG).show();
                }
            });
        }

        if (btnPrivacy != null) {
            btnPrivacy.setOnClickListener(v -> {
                Toast.makeText(this, "عرض سياسة الخصوصية لمشروع إرواء...", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnAbout != null) {
            btnAbout.setOnClickListener(v -> {
                Toast.makeText(this, "تطبيق إرواء - الإصدار 2.4.0", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnLangEn != null) btnLangEn.setOnClickListener(v -> setAppLocale("en"));
        if (btnLangAr != null) btnLangAr.setOnClickListener(v -> setAppLocale("ar"));

        setupBottomNavigation();
    }

    private void setAppLocale(String languageCode) {
        LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(languageCode);
        AppCompatDelegate.setApplicationLocales(appLocale);
        Toast.makeText(this, "جاري تغيير لغة التطبيق...", Toast.LENGTH_SHORT).show();
    }

    private void setupSwitches() {
        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);

        if (switchNotifications != null) {
            switchNotifications.setChecked(prefs.getBoolean("notifications", true));
            switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("notifications", isChecked).apply();
            });
        }

        if (switchDataSaver != null) {
            switchDataSaver.setChecked(prefs.getBoolean("data_saver", false));
            switchDataSaver.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("data_saver", isChecked).apply();
            });
        }
    }

    private void setupBottomNavigation() {
        if (navDashboard != null) {
            navDashboard.setOnClickListener(v -> {
                Intent intent = new Intent(this, InitiatorDashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        if (navNeedMap != null) {
            navNeedMap.setOnClickListener(v -> {
                Intent intent = new Intent(this, NeedMapActivity.class);
                startActivity(intent);
                finish();
            });
        }

        if (navInitiators != null) {
            navInitiators.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProfileInitiatorsActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }
}
