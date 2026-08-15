package com.example.graduationproject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserNotificationActivity extends AppCompatActivity {
    private static final String TAG = "NotificationDebug";

    private ImageView btnBack, btnSettings;
    private MaterialCardView btnReadAll;
    private View unreadIndicator;

    private TextView filterAll, filterNewOrders, filterRatings, filterPayments;
    private View filterActiveBg;

    private LinearLayout navDashboard, navInitiatives, navMap, navWallet;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;
    private List<NotificationModel> notificationList = new ArrayList<>();
    private NotificationAdapter adapter;
    private ListenerRegistration notificationsListener;

    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_customer_notifications);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "الرجاء تسجيل الدخول أولاً", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupClickListeners();
        setupBottomNavigation();
        loadNotifications("all");
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSettings = findViewById(R.id.btnSettings);
        btnReadAll = findViewById(R.id.btnReadAll);
        unreadIndicator = findViewById(R.id.unreadIndicator);

        filterAll = findViewById(R.id.filterAll);
        filterNewOrders = findViewById(R.id.filterNewOrders);
        filterRatings = findViewById(R.id.filterRatings);
        filterPayments = findViewById(R.id.filterPayments);

        if (filterAll != null) filterAll.setOnClickListener(v -> setActiveFilter(filterAll, "all"));
        if (filterNewOrders != null) filterNewOrders.setOnClickListener(v -> setActiveFilter(filterNewOrders, "new_order"));
        if (filterRatings != null) filterRatings.setOnClickListener(v -> setActiveFilter(filterRatings, "rating"));
        if (filterPayments != null) filterPayments.setOnClickListener(v -> setActiveFilter(filterPayments, "payment"));

        filterActiveBg = filterAll;

        navDashboard = findViewById(R.id.nav_dashboard);
        navInitiatives = findViewById(R.id.nav_initiatives);
        navMap = findViewById(R.id.nav_map);
        navWallet = findViewById(R.id.nav_wallet);
    }

    private void setupClickListeners() {
        // زر العودة
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // زر الإعدادات العلوي لفتح صفحة الإعدادات المناسبة
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                Intent intent = new Intent(UserNotificationActivity.this, SettingsInitiatorsActivity.class);
                startActivity(intent);
            });
        }

        // تعيين كافة الإشعارات كمقروءة
        if (btnReadAll != null) {
            btnReadAll.setOnClickListener(v -> markAllNotificationsAsRead());
        }
    }

    // إدارة تبديل الفلاتر العلوية بشكل مرئي ومحاكاة تصفية البيانات
    private void setActiveFilter(TextView selected, String filterType) {
        if (filterActiveBg != null) {
            filterActiveBg.setBackgroundResource(R.drawable.bg_filter_inactive);
            ((TextView) filterActiveBg).setTextColor(Color.parseColor("#475569"));
        }
        selected.setBackgroundResource(R.drawable.bg_filter_active);
        selected.setTextColor(Color.WHITE);
        filterActiveBg = selected;
        currentFilter = filterType;
        loadNotifications(filterType);
    }

    //  جلب ومراقبة الإشعارات الحية بشكل تلقائي وتصفيتها ديناميكياً
    private void loadNotifications(String filterType) {
        if (notificationsListener != null) {
            notificationsListener.remove();
        }
        notificationList.clear();

        Query query = db.collection("notifications")
                .whereEqualTo("userId", userId);

        if (!"all".equals(filterType)) {
            query = query.whereEqualTo("type", filterType);
        }

        notificationsListener = query.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.e(TAG, "خطأ بالاستماع للإشعارات: " + e.getMessage());
                return;
            }

            if (snapshots != null) {
                boolean hasUnread = false;
                notificationList.clear();

                for (QueryDocumentSnapshot doc : snapshots) {
                    try {
                        NotificationModel notification = doc.toObject(NotificationModel.class);
                        notification.setId(doc.getId());
                        notificationList.add(notification);

                        if (!notification.isRead()) {
                            hasUnread = true;
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "خطأ في تحويل الإشعار: " + ex.getMessage());
                    }
                }

                // فرز محلي للأحدث أولاً
                Collections.sort(notificationList, (n1, n2) -> {
                    if (n1.getCreated_at() == null || n2.getCreated_at() == null) return 0;
                    return n2.getCreated_at().compareTo(n1.getCreated_at());
                });

                // تحديث شارة الإشعارات الزرقاء
                if (unreadIndicator != null) {
                    unreadIndicator.setVisibility(hasUnread ? View.VISIBLE : View.GONE);
                }

                RecyclerView rv = findViewById(R.id.rvNotifications);
                if (rv != null) {
                    if (rv.getAdapter() == null) {
                        rv.setLayoutManager(new LinearLayoutManager(this));
                        adapter = new NotificationAdapter(this, notificationList);
                        rv.setAdapter(adapter);
                    } else {
                        rv.getAdapter().notifyDataSetChanged();
                    }
                }
            }
        });
    }

    // تحديث حالة جميع الإشعارات إلى "مقروءة" في الفايرستور دفعة واحدة
    private void markAllNotificationsAsRead() {
        db.collection("notifications")
                .whereEqualTo("userId", userId)
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "لا توجد إشعارات جديدة غير مقروءة", Toast.LENGTH_SHORT).show();
                        if (unreadIndicator != null) unreadIndicator.setVisibility(View.GONE);
                        return;
                    }

                    WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.update(doc.getReference(), "read", true);
                    }

                    batch.commit().addOnSuccessListener(aVoid -> {
                        if (unreadIndicator != null) {
                            unreadIndicator.setVisibility(View.GONE);
                        }
                        Toast.makeText(this, "تم تحديث كافة الإشعارات كمقروءة بنجاح ✅", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "فشل تحديث الإشعارات: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                });
    }

    public void onUserFilterClicked(View view) {
        if (!(view instanceof TextView)) return;
        TextView clickedFilter = (TextView) view;
        String filterName = clickedFilter.getText().toString();
        Toast.makeText(this, "عرض إشعارات: " + filterName, Toast.LENGTH_SHORT).show();
    }

    public void onTrackDriverClicked(View view) {
        Intent intent = new Intent(this, DeliveryConfirmationActivity.class);
        startActivity(intent);
        Toast.makeText(this, "جاري فتح الخريطة لتتبع مسار صهريج المياه... 📍", Toast.LENGTH_SHORT).show();
    }

    public void onViewOrderDetailsClicked(View view) {
        Toast.makeText(this, "جاري تحميل تفاصيل الطلب من الخادم...", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationsListener != null) {
            notificationsListener.remove();
        }
    }

    // شريط التنقل السفلي الموحد للتنقل التفاعلي والحقيقي بين شاشاتك
    private void setupBottomNavigation() {
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
                finish();
            });
        }

        if (navMap != null) {
            navMap.setOnClickListener(v -> {
                Intent intent = new Intent(this, NeedMapActivity.class);
                startActivity(intent);
                finish();
            });
        }

        if (navWallet != null) {
            navWallet.setOnClickListener(v -> {
                Intent intent = new Intent(this, WalletActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }
}