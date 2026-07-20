package com.example.graduationproject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterProviderActivity extends AppCompatActivity {

    private static final String TAG = "RegisterProvider";
    private EditText etFullName, etEmail, etPassword, etIdNumber, etPhone, etMunicipalityCode;
    private TextView tvRegion;
    private CardView cardLocalStation, cardWell, cardTruck;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String selectedRegion = "";
    private String selectedEquipment = "محطة محلية";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_provider);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etIdNumber = findViewById(R.id.etIdNumber);
        etPhone = findViewById(R.id.etPhone);
        etMunicipalityCode = findViewById(R.id.etMunicipalityCode);
        tvRegion = findViewById(R.id.tvRegion);

        cardLocalStation = findViewById(R.id.cardLocalStation);
        cardWell = findViewById(R.id.cardWell);
        cardTruck = findViewById(R.id.cardTruck);
        CardView btnSubmit = findViewById(R.id.btnSubmit);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        tvRegion.setOnClickListener(v -> showRegionDialog());

        cardLocalStation.setOnClickListener(v -> selectEquipment("محطة محلية", cardLocalStation));
        cardWell.setOnClickListener(v -> selectEquipment("بئر", cardWell));
        cardTruck.setOnClickListener(v -> selectEquipment("صهريج", cardTruck));

        selectEquipment("محطة محلية", cardLocalStation);

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> performSignUp());
        }

        TextView btnLogin = findViewById(R.id.tvSignUp);
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> {
                startActivity(new Intent(RegisterProviderActivity.this, LoginActivity.class));
                finish();
            });
        }
    }

    private void showRegionDialog() {
        String[] regions = {"غزة - الرمال", "غزة - النصر", "خانيونس", "رفح", "دير البلح", "شمال غزة"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("اختر منطقة التوزيع");
        builder.setItems(regions, (dialog, which) -> {
            selectedRegion = regions[which];
            tvRegion.setText(selectedRegion);
            tvRegion.setTextColor(Color.BLACK);
        });
        builder.show();
    }

    private void selectEquipment(String type, CardView selectedCard) {
        selectedEquipment = type;
        cardLocalStation.setCardBackgroundColor(Color.WHITE);
        cardWell.setCardBackgroundColor(Color.WHITE);
        cardTruck.setCardBackgroundColor(Color.WHITE);
        selectedCard.setCardBackgroundColor(Color.parseColor("#E0F2FE"));
    }

    private void performSignUp() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String idNumber = etIdNumber.getText().toString().trim();
        String municipalityCode = etMunicipalityCode.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || fullName.isEmpty() || idNumber.isEmpty() || selectedRegion.isEmpty()) {
            Toast.makeText(this, "يرجى ملء كافة البيانات الأساسية واختيار المنطقة", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // تم إزالة تفعيل البريد الإلكتروني بناءً على طلب المستخدم
                            saveProviderData(user.getUid(), email, fullName, phone, idNumber, municipalityCode);
                        }
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(RegisterProviderActivity.this, "فشل إنشاء الحساب: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveProviderData(String userId, String email, String fullName, String phone, String idNumber, String municipalityCode) {
        String providerTypeKey = "storage";
        if ("صهريج".equals(selectedEquipment)) providerTypeKey = "truck";
        else if ("بئر".equals(selectedEquipment)) providerTypeKey = "well";

        // تحديد الإحداثيات بناءً على المنطقة المختارة بدقة
        double lat = 31.516; // افتراضي (الرمال)
        double lng = 34.448;

        switch (selectedRegion) {
            case "غزة - الرمال":
                lat = 31.516; lng = 34.448;
                break;
            case "غزة - النصر":
                lat = 31.530; lng = 34.455;
                break;
            case "خانيونس":
                lat = 31.345; lng = 34.305;
                break;
            case "رفح":
                lat = 31.285; lng = 34.255;
                break;
            case "دير البلح":
                lat = 31.417; lng = 34.350;
                break;
            case "شمال غزة":
                lat = 31.542; lng = 34.492;
                break;
        }

        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("id", userId);
        userProfile.put("full_name", fullName);
        userProfile.put("email", email);
        userProfile.put("phone", phone);
        userProfile.put("id_number", idNumber);
        userProfile.put("role", "provider");
        userProfile.put("is_provider", true);

        Map<String, Object> providerData = new HashMap<>();
        providerData.put("user_id", userId);
        providerData.put("business_name", fullName);
        providerData.put("provider_type", providerTypeKey);
        providerData.put("municipality_code", municipalityCode);
        providerData.put("location_name", selectedRegion);
        providerData.put("current_lat", lat);
        providerData.put("current_lng", lng);
        providerData.put("email_verified", true); // تم تعيينها لتكون مفعلة تلقائياً
        providerData.put("status", "pending_verification");

        db.collection("users").document(userId).set(userProfile)
                .addOnSuccessListener(aVoid -> {
                    db.collection("providers").document(userId).set(providerData)
                            .addOnSuccessListener(aVoid2 -> showSuccessDialog())
                            .addOnFailureListener(e -> Log.e(TAG, "Error saving provider data", e));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving user profile", e);
                    Toast.makeText(RegisterProviderActivity.this, "فشل حفظ بيانات الحساب", Toast.LENGTH_SHORT).show();
                });
    }

    private void showSuccessDialog() {
        mAuth.signOut();
        new AlertDialog.Builder(RegisterProviderActivity.this)
                .setTitle("تم التسجيل بنجاح")
                .setMessage("تم إنشاء حساب مقدم الخدمة بنجاح!\n\nيمكنك الآن تسجيل الدخول مباشرة والبدء في تقديم خدماتك.")
                .setPositiveButton("حسناً", (dialog, which) -> {
                    startActivity(new Intent(RegisterProviderActivity.this, LoginActivity.class));
                    finish();
                })
                .setCancelable(false)
                .show();
    }
}
