package com.example.graduationproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class AdminDashboardActivity extends AppCompatActivity {
    private static final String TAG = "AdminDashboard";

    private FrameLayout flNotifications;
    private TextView tvNotificationBadge;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration notificationsListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupClickListeners();
        listenForNotifications();
    }

    private void initViews() {
        flNotifications = findViewById(R.id.flNotifications);
        tvNotificationBadge = findViewById(R.id.tvNotificationBadge);
    }

    private void listenForNotifications() {
        // للمسؤول، قد نرغب في رؤية كل الإشعارات الإدارية أو الخاصة به
        // هنا نفترض وجود إشعارات موجهة للمسؤول (مثلاً إشعارات النظام)
        if (mAuth.getCurrentUser() == null) return;

        notificationsListener = db.collection("notifications")
                .whereEqualTo("userId", mAuth.getCurrentUser().getUid())
                .whereEqualTo("read", false)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen for notifications failed.", e);
                        return;
                    }

                    if (snapshots != null) {
                        int count = snapshots.size();
                        if (count > 0) {
                            tvNotificationBadge.setText(String.valueOf(count));
                            tvNotificationBadge.setVisibility(View.VISIBLE);
                        } else {
                            tvNotificationBadge.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void setupClickListeners() {
        if (flNotifications != null) {
            flNotifications.setOnClickListener(v -> {
                // نفتح واجهة الإشعارات (المشتركة أو مخصصة للأدمن)
                startActivity(new Intent(this, UserNotificationActivity.class));
            });
        }

        // إدارة المستخدمين
        View cardUsers = findViewById(R.id.cardManageUsers);
        if (cardUsers != null) {
            cardUsers.setOnClickListener(v -> {
                startActivity(new Intent(this, AdminUsersActivity.class));
            });
        }

        // إدارة المزودين
        findViewById(R.id.cardManageProviders).setOnClickListener(v -> {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationsListener != null) {
            notificationsListener.remove();
        }
    }
}
