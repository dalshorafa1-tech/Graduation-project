package com.example.graduationproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminLoginActivity extends AppCompatActivity {

    private static final String DEFAULT_ADMIN_EMAIL = "admin@erwaa.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private View btnBack; // تم تغييره إلى View ليتوافق مع ImageView في الـ XML
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etAdminEmail);
        etPassword = findViewById(R.id.etAdminPassword);
        btnLogin = findViewById(R.id.btnAdminLogin);
        btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> loginAdmin());
        }

        // إنشاء أدمن افتراضي إذا لم يكن موجوداً
        createDefaultAdminIfNeeded();
    }

    private void createDefaultAdminIfNeeded() {
        db.collection("admins").limit(1).get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        mAuth.createUserWithEmailAndPassword(DEFAULT_ADMIN_EMAIL, DEFAULT_ADMIN_PASSWORD)
                                .addOnSuccessListener(authResult -> {
                                    Map<String, Object> adminData = new HashMap<>();
                                    adminData.put("role", "admin");
                                    adminData.put("email", DEFAULT_ADMIN_EMAIL);
                                    db.collection("admins").document(DEFAULT_ADMIN_EMAIL)
                                            .set(adminData);
                                })
                                .addOnFailureListener(e -> {
                                    Map<String, Object> adminData = new HashMap<>();
                                    adminData.put("role", "admin");
                                    adminData.put("email", DEFAULT_ADMIN_EMAIL);
                                    db.collection("admins").document(DEFAULT_ADMIN_EMAIL)
                                            .set(adminData);
                                });
                    }
                });
    }

    private void loginAdmin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("البريد الإلكتروني مطلوب");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("كلمة المرور مطلوبة");
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("جاري تسجيل الدخول...");

        db.collection("admins").document(email.toLowerCase()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        mAuth.signInWithEmailAndPassword(email, password)
                                .addOnSuccessListener(authResult -> {
                                    Toast.makeText(this, "مرحباً أيها الأدمن", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, AdminServicesActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    btnLogin.setEnabled(true);
                                    btnLogin.setText("تسجيل الدخول");
                                    Toast.makeText(this, "خطأ في البريد أو كلمة المرور", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("تسجيل الدخول");
                        Toast.makeText(this, "ليس لديك صلاحية الأدمن", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("تسجيل الدخول");
                    Toast.makeText(this, "فشل الاتصال", Toast.LENGTH_SHORT).show();
                });
    }
}
