package com.example.graduationproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Profile extends AppCompatActivity {

    private ImageView imgProfileBig, btnNotifications;
    private TextView tvStationName, tvStationType, tvStationAddress, tvStationPhone, tvStationCapacity, tvWorkHours;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();
        loadProfileData();

        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> {
                startActivity(new Intent(this, NotificationsActivity.class));
            });
        }

        // الضغط على القلم يفتح تعديل البيانات الشخصية
        View btnEdit = findViewById(R.id.btnEditProfileImage);
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> {
                startActivity(new Intent(this, EditProfileActivity.class));
            });
        }

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // الضغط على إعدادات الحساب يفتح شاشة الإعدادات (fragment_settings / SettingsActivity)
        findViewById(R.id.btnAccountSettings).setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });
    }

    private void initViews() {
        imgProfileBig = findViewById(R.id.imgProfileBig);
        btnNotifications = findViewById(R.id.btnNotifications);
        tvStationName = findViewById(R.id.tvStationName);
        tvStationType = findViewById(R.id.tvStationType);
        tvStationAddress = findViewById(R.id.tvStationAddress);
        tvStationPhone = findViewById(R.id.tvStationPhone);
        tvStationCapacity = findViewById(R.id.tvStationCapacity);
        tvWorkHours = findViewById(R.id.tvWorkHours);
    }

    private void loadProfileData() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("providers").document(uid).addSnapshotListener((doc, e) -> {
            if (e != null || doc == null || !doc.exists()) {
                loadUserData(uid);
                return;
            }

            tvStationName.setText(doc.getString("business_name"));
            tvStationType.setText(getArabicProviderType(doc.getString("provider_type")));
            tvStationAddress.setText(doc.getString("location_name") != null ? doc.getString("location_name") : "غير محدد");
            tvStationPhone.setText(doc.getString("phone") != null ? doc.getString("phone") : "غير متوفر");
            
            Long capacity = doc.getLong("capacity");
            if (tvStationCapacity != null) tvStationCapacity.setText((capacity != null ? capacity : 0) + " لتر مكعب");

            if (tvWorkHours != null) tvWorkHours.setText(doc.getString("work_hours") != null ? doc.getString("work_hours") : "8:00 ص - 6:00 م");

            // تحميل الصورة الشخصية
            String imageUrl = doc.getString("profile_image");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this).load(imageUrl).placeholder(R.drawable.img13).into(imgProfileBig);
            } else {
                imgProfileBig.setImageResource(R.drawable.img13); // الصورة الافتراضية
            }
        });
    }

    private void loadUserData(String uid) {
        db.collection("users").document(uid).addSnapshotListener((doc, e) -> {
            if (doc != null && doc.exists()) {
                tvStationName.setText(doc.getString("full_name"));
                tvStationType.setText("حساب مستخدم");
                tvStationAddress.setText(doc.getString("address") != null ? doc.getString("address") : "غير محدد");
                tvStationPhone.setText(doc.getString("phone"));

                String imageUrl = doc.getString("profile_image");
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(this).load(imageUrl).placeholder(R.drawable.img13).into(imgProfileBig);
                } else {
                    imgProfileBig.setImageResource(R.drawable.img13);
                }
            }
        });
    }

    private String getArabicProviderType(String type) {
        if ("truck".equals(type)) return "صهريج متنقل";
        if ("well".equals(type)) return "بئر مياه";
        if ("storage".equals(type)) return "مستودع مياه";
        return "مزود خدمة";
    }
}
