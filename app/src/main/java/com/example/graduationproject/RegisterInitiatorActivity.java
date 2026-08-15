package com.example.graduationproject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class RegisterInitiatorActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword, etPhone, etIdNumber;
    private TextView tvRegion;
    private LinearLayout layoutRegion;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CardView btnRegister;

    private String selectedRegion = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_initiator);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        etIdNumber = findViewById(R.id.etIdNumber);
        tvRegion = findViewById(R.id.tvRegion);
        layoutRegion = findViewById(R.id.layoutRegion);
        btnRegister = findViewById(R.id.btnRegister);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        if (layoutRegion != null) {
            layoutRegion.setOnClickListener(v -> showRegionDialog());
        }

        btnRegister.setOnClickListener(v -> performSignUp());
    }

    private void showRegionDialog() {
        String[] regions = {"غزة - الرمال", "غزة - النصر", "خانيونس", "رفح", "دير البلح", "شمال غزة"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("اختر منطقة العمل الرئيسية");
        builder.setItems(regions, (dialog, which) -> {
            selectedRegion = regions[which];
            tvRegion.setText(selectedRegion);
            tvRegion.setTextColor(Color.BLACK);
        });
        builder.show();
    }

    private void performSignUp() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String idNumber = etIdNumber.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || fullName.isEmpty() || idNumber.isEmpty() || selectedRegion.isEmpty()) {
            Toast.makeText(this, "يرجى ملء كافة البيانات واختيار المنطقة", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setAlpha(0.5f);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveInitiatorProfile(user.getUid(), fullName, email, phone, idNumber);
                        }
                    } else {
                        btnRegister.setEnabled(true);
                        btnRegister.setAlpha(1.0f);
                        Log.w("RegisterInitiator", "createUserWithEmail:failure", task.getException());
                        Toast.makeText(RegisterInitiatorActivity.this, "فشل إنشاء الحساب: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveInitiatorProfile(String userId, String fullName, String email, String phone, String idNumber) {
        WriteBatch batch = db.batch();

        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("id", userId);
        userProfile.put("full_name", fullName);
        userProfile.put("email", email);
        userProfile.put("phone", phone);
        userProfile.put("id_number", idNumber);
        userProfile.put("region", selectedRegion);
        userProfile.put("role", "initiator");
        userProfile.put("is_initiator", true);
        userProfile.put("created_at", com.google.firebase.Timestamp.now());

        batch.set(db.collection("users").document(userId), userProfile);

        Map<String, Object> initiatorData = new HashMap<>();
        initiatorData.put("user_id", userId);
        initiatorData.put("name", fullName);
        initiatorData.put("region", selectedRegion);
        initiatorData.put("status", "active");
        
        batch.set(db.collection("initiators").document(userId), initiatorData);

        batch.commit()
                .addOnSuccessListener(aVoid -> showSuccessDialog())
                .addOnFailureListener(e -> {
                    btnRegister.setEnabled(true);
                    btnRegister.setAlpha(1.0f);
                    Toast.makeText(RegisterInitiatorActivity.this, "فشل حفظ بيانات الحساب", Toast.LENGTH_SHORT).show();
                });
    }

    private void showSuccessDialog() {
        if (isFinishing()) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("تم التسجيل بنجاح");
        builder.setMessage("تم إنشاء حساب المبادر الخاص بك بنجاح! يمكنك الآن تسجيل الدخول.");
        builder.setPositiveButton("تسجيل الدخول", (dialog, which) -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
        builder.setCancelable(false);
        builder.show();
    }
}
