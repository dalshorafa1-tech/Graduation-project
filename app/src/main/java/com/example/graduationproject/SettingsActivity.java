package com.example.graduationproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private SwitchMaterial switchNotifications, switchDataSaver;
    private TextView btnLangEn, btnLangAr;
    private RelativeLayout btnAbout, btnPrivacy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_settings);

        mAuth = FirebaseAuth.getInstance();

        // ربط العناصر
        ImageView btnBack = findViewById(R.id.btnMenu);
        MaterialCardView btnLogout = findViewById(R.id.btnLogout);
        RelativeLayout btnChangePassword = findViewById(R.id.btnChangePassword);
        switchNotifications = findViewById(R.id.switchNotifications);
        switchDataSaver = findViewById(R.id.switchDataSaver);
        btnLangEn = findViewById(R.id.btnLangEn);
        btnLangAr = findViewById(R.id.btnLangAr);
        btnAbout = findViewById(R.id.btnAbout);
        btnPrivacy = findViewById(R.id.btnPrivacy);

        // العودة
        btnBack.setOnClickListener(v -> finish());

        // الانتقال لشاشة عن إرواء
        btnAbout.setOnClickListener(v -> {

            startActivity(new Intent(SettingsActivity.this, AboutErwaaActivity.class));
        });

      // الانتقال لشاشة سياسة الخصوصية
        btnPrivacy.setOnClickListener(v -> {

            startActivity(new Intent(SettingsActivity.this, PrivacyActivity.class));
        });
        // تسجيل الخروج
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // تغيير كلمة المرور - إرسال رابط إعادة التعيين للبريد
        btnChangePassword.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getEmail() != null) {
                mAuth.sendPasswordResetEmail(mAuth.getCurrentUser().getEmail())
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "تم إرسال رابط إعادة تعيين كلمة المرور إلى بريدك الإلكتروني", Toast.LENGTH_LONG).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "خطأ: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "لم يتم العثور على بريد إلكتروني مرتبط", Toast.LENGTH_SHORT).show();
            }
        });

        // تغيير اللغة
        btnLangEn.setOnClickListener(v -> setAppLocale("en"));
        btnLangAr.setOnClickListener(v -> setAppLocale("ar"));

        setupSwitches();
        setupBottomNavigation();
    }

    private void setAppLocale(String languageCode) {
        LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(languageCode);
        AppCompatDelegate.setApplicationLocales(appLocale);
        Toast.makeText(this, "جاري تغيير اللغة...", Toast.LENGTH_SHORT).show();
    }

    private void setupSwitches() {
        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);

        switchNotifications.setChecked(prefs.getBoolean("notifications", true));
        switchDataSaver.setChecked(prefs.getBoolean("data_saver", false));

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("notifications", isChecked).apply();
        });

        switchDataSaver.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("data_saver", isChecked).apply();
        });
    }

    private void setupBottomNavigation() {
        // زر الرئيسية يفتح شاشة الملف الشخصي (fragment_profile)
        findViewById(R.id.navHome).setOnClickListener(v -> {
            Intent intent = new Intent(this, Profile.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        // زر المحفظة يفتح شاشة المحفظة
        findViewById(R.id.navWallet).setOnClickListener(v -> startActivity(new Intent(this, WalletActivity.class)));

        // زر الطلبات يفتح شاشة طلباتي
        findViewById(R.id.navOrders).setOnClickListener(v -> startActivity(new Intent(this, My_Orders_Activity.class)));

        // زر حسابي (أنت الآن في شاشة الإعدادات التابعة له)
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            // حالياً في شاشة الإعدادات
        });
    }
}