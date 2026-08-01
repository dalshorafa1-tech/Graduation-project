package com.example.graduationproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        setupClickListeners();
    }

    private void setupClickListeners() {
        // إدارة المستخدمين
        View cardUsers = findViewById(R.id.cardManageUsers);
        if (cardUsers != null) {
            cardUsers.setOnClickListener(v -> {
                startActivity(new Intent(this, AdminUsersActivity.class));
            });
        }

        // إدارة المزودين
        findViewById(R.id.cardManageProviders).setOnClickListener(v -> {
            // يمكننا فتح واجهة المستخدمين مع فلتر للمزودين أو واجهة مخصصة
            Intent intent = new Intent(this, AdminUsersActivity.class);
            intent.putExtra("filter_role", "provider");
            startActivity(intent);
        });

        // إدارة المبادرات
        findViewById(R.id.cardManageInitiators).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminInitiativesActivity.class));
        });

        // إدارة الطلبات
        findViewById(R.id.cardManageOrders).setOnClickListener(v -> {
            // افترضنا وجود نشاط لإدارة جميع الطلبات
            Toast.makeText(this, "واجهة إدارة الطلبات قيد التطوير", Toast.LENGTH_SHORT).show();
        });

        // مراجعة الخدمات
        findViewById(R.id.cardReviewServices).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminServicesActivity.class));
        });

        // تسجيل الخروج
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
