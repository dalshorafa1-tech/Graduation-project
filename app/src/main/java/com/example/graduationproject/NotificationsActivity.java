package com.example.graduationproject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private TextView tvTitle, tvSubtitle;
    private View btnReadAll; // Changed from TextView to View to avoid ClassCastException
    private TextView filterAll, filterNewOrders, filterRatings, filterPayments;
    private View filterActiveBg;

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
        setContentView(R.layout.fragment_notifications);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        } else {
            finish();
            return;
        }

        initViews();
        loadNotifications("all");
    }

    private void initViews() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tvTitle = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        btnReadAll = findViewById(R.id.btnReadAll);

        if (btnReadAll != null) {
            btnReadAll.setOnClickListener(v -> markAllAsRead());
        }

        filterAll = findViewById(R.id.filterAll);
        filterNewOrders = findViewById(R.id.filterNewOrders);
        filterRatings = findViewById(R.id.filterRatings);
        filterPayments = findViewById(R.id.filterPayments);

        if (filterAll != null) filterAll.setOnClickListener(v -> setActiveFilter(filterAll, "all"));
        if (filterNewOrders != null) filterNewOrders.setOnClickListener(v -> setActiveFilter(filterNewOrders, "new_order"));
        if (filterRatings != null) filterRatings.setOnClickListener(v -> setActiveFilter(filterRatings, "rating"));
        if (filterPayments != null) filterPayments.setOnClickListener(v -> setActiveFilter(filterPayments, "payment"));

        filterActiveBg = filterAll;

        rvNotifications = findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(this, notificationList);
        rvNotifications.setAdapter(adapter);
    }

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

    private void loadNotifications(String filterType) {
        if (notificationsListener != null) {
            notificationsListener.remove();
        }
        notificationList.clear();
        adapter.notifyDataSetChanged();

        Query query = db.collection("notifications")
                .whereEqualTo("provider_id", userId)
                .orderBy("created_at", Query.Direction.DESCENDING);

        if (!"all".equals(filterType)) {
            query = query.whereEqualTo("type", filterType);
        }

        notificationsListener = query.addSnapshotListener((snapshots, e) -> {
            if (e != null) return;
            if (snapshots != null) {
                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                    NotificationModel notification = dc.getDocument().toObject(NotificationModel.class);
                    notification.setId(dc.getDocument().getId());

                    int index = findNotificationIndex(notification.getId());
                    switch (dc.getType()) {
                        case ADDED:
                            if (index == -1) {
                                notificationList.add(notification);
                                adapter.notifyItemInserted(notificationList.size() - 1);
                            }
                            break;
                        case REMOVED:
                            if (index != -1) {
                                notificationList.remove(index);
                                adapter.notifyItemRemoved(index);
                            }
                            break;
                        case MODIFIED:
                            if (index != -1) {
                                notificationList.set(index, notification);
                                adapter.notifyItemChanged(index);
                            }
                            break;
                    }
                }
                updateUI();
            }
        });
    }

    private int findNotificationIndex(String id) {
        for (int i = 0; i < notificationList.size(); i++) {
            if (notificationList.get(i).getId() != null && notificationList.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    private void updateUI() {
        int unread = 0;
        for (NotificationModel n : notificationList) {
            if (!n.isRead()) unread++;
        }
        if (tvTitle != null) {
            tvTitle.setText("التنبيهات" + (unread > 0 ? " (" + unread + ")" : ""));
        }
    }

    private void markAllAsRead() {
        for (NotificationModel n : notificationList) {
            if (!n.isRead() && n.getId() != null) {
                db.collection("notifications").document(n.getId()).update("is_read", true);
                n.setRead(true);
            }
        }
        adapter.notifyDataSetChanged();
        updateUI();
        Toast.makeText(this, "تم تحديد الكل كمقروء", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupBottomNavigation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationsListener != null) {
            notificationsListener.remove();
        }
    }

    private void setupBottomNavigation() {
        View navDashboard = findViewById(R.id.navHome);
        View navServices = findViewById(R.id.navServices);
        View navOrders = findViewById(R.id.navOrders);
        View navHistory = findViewById(R.id.navHistory);

        if (navDashboard != null) navDashboard.setOnClickListener(v -> {
            startActivity(new Intent(this, ProviderDashboardActivity.class));
            finish();
        });
        if (navServices != null) navServices.setOnClickListener(v -> {
            startActivity(new Intent(this, ProviderServicesActivity.class));
            finish();
        });
        if (navOrders != null) navOrders.setOnClickListener(v -> {
            startActivity(new Intent(this, OrdersManagementActivity.class));
            finish();
        });
        if (navHistory != null) navHistory.setOnClickListener(v -> {
            startActivity(new Intent(this, TripsHistoryActivity.class));
            finish();
        });
    }
}
