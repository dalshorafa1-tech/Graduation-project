package com.example.graduationproject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class OrdersManagementActivity extends AppCompatActivity {

    private RecyclerView rvProviderOrders;
    private ProviderOrderAdapter adapter;
    private List<OrderModel> allOrders = new ArrayList<>();
    private List<OrderModel> filteredOrders = new ArrayList<>();
    private String userId;

    private TextView tvTabNew, tvTabInProgress, tvTabCompleted;
    private View indicatorNew, indicatorInProgress, indicatorCompleted;
    private TextView tvEmptyState;
    private ImageView imgUserAvatar;
    private String currentTab = "new";
    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration ordersListener;
    private ListenerRegistration profileListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_management);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        } else {
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        setupBottomNavigation();
        listenToOrders();
        listenToProfile();
    }

    private void initViews() {
        rvProviderOrders = findViewById(R.id.rvProviderOrders);
        tvTabNew = findViewById(R.id.tvTabNew);
        tvTabInProgress = findViewById(R.id.tvTabInProgress);
        tvTabCompleted = findViewById(R.id.tvTabCompleted);
        indicatorNew = findViewById(R.id.indicatorNew);
        indicatorInProgress = findViewById(R.id.indicatorInProgress);
        indicatorCompleted = findViewById(R.id.indicatorCompleted);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        imgUserAvatar = findViewById(R.id.imgUserAvatar);

        findViewById(R.id.tabNewOrders).setOnClickListener(v -> switchTab("new"));
        findViewById(R.id.tabInProgressOrders).setOnClickListener(v -> switchTab("in_progress"));
        findViewById(R.id.tabCompletedOrders).setOnClickListener(v -> switchTab("completed"));

        // ربط الإشعارات
        View btnNotif = findViewById(R.id.btnNotificationWrapper);
        if (btnNotif != null) {
            btnNotif.setOnClickListener(v -> 
                    startActivity(new Intent(this, NotificationsActivity.class)));
        }
        
        // ربط الصورة الشخصية للملف الشخصي
        if (imgUserAvatar != null) {
            imgUserAvatar.setOnClickListener(v -> {
                startActivity(new Intent(this, Profile.class));
            });
        }
    }

    private void listenToProfile() {
        profileListener = db.collection("providers").document(userId).addSnapshotListener((doc, e) -> {
            if (e != null || doc == null || !doc.exists()) return;
            String imageUrl = doc.getString("profile_image");
            if (imgUserAvatar != null) {
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(this).load(imageUrl).placeholder(R.drawable.user).into(imgUserAvatar);
                } else {
                    imgUserAvatar.setImageResource(R.drawable.user);
                }
            }
        });
    }

    private void setupRecyclerView() {
        rvProviderOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProviderOrderAdapter(this, filteredOrders, currentTab);
        rvProviderOrders.setAdapter(adapter);
    }

    private void listenToOrders() {
        if (userId == null) return;

        ordersListener = db.collection("orders")
                .whereEqualTo("provider_id", userId)
                .orderBy("created_at", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("OrdersMgmt", "Error: " + e.getMessage());
                        return;
                    }
                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            OrderModel order = dc.getDocument().toObject(OrderModel.class);
                            order.setId(dc.getDocument().getId());

                            int index = findOrderIndex(order.getId());
                            switch (dc.getType()) {
                                case ADDED:
                                    if (index == -1) allOrders.add(order);
                                    break;
                                case MODIFIED:
                                    if (index != -1) allOrders.set(index, order);
                                    break;
                                case REMOVED:
                                    if (index != -1) allOrders.remove(index);
                                    break;
                            }
                        }
                        updateTabCounts();
                        applyFilter();
                    }
                });
    }

    private int findOrderIndex(String id) {
        for (int i = 0; i < allOrders.size(); i++) {
            if (allOrders.get(i).getId().equals(id)) return i;
        }
        return -1;
    }

    private void updateTabCounts() {
        int cNew = 0, cProgress = 0, cDone = 0;
        for (OrderModel o : allOrders) {
            String s = o.getStatus();
            if ("pending".equals(s)) cNew++;
            else if ("accepted".equals(s) || "on_way".equals(s)) cProgress++;
            else if ("delivered".equals(s) || "cancelled".equals(s)) cDone++;
        }
        tvTabNew.setText("جديدة" + (cNew > 0 ? " (" + cNew + ")" : ""));
        tvTabInProgress.setText("قيد التنفيذ" + (cProgress > 0 ? " (" + cProgress + ")" : ""));
        tvTabCompleted.setText("مكتملة" + (cDone > 0 ? " (" + cDone + ")" : ""));
    }

    private void applyFilter() {
        filteredOrders.clear();
        for (OrderModel o : allOrders) {
            String s = o.getStatus();
            if ("new".equals(currentTab) && "pending".equals(s)) filteredOrders.add(o);
            else if ("in_progress".equals(currentTab) && ("accepted".equals(s) || "on_way".equals(s))) filteredOrders.add(o);
            else if ("completed".equals(currentTab) && ("delivered".equals(s) || "cancelled".equals(s))) filteredOrders.add(o);
        }
        
        adapter.setCurrentTab(currentTab);
        adapter.notifyDataSetChanged();
        
        tvEmptyState.setVisibility(filteredOrders.isEmpty() ? View.VISIBLE : View.GONE);
        rvProviderOrders.setVisibility(filteredOrders.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void switchTab(String tab) {
        currentTab = tab;
        resetTabsUI();
        if ("new".equals(tab)) {
            tvTabNew.setTextColor(Color.parseColor("#0069B4"));
            indicatorNew.setBackgroundColor(Color.parseColor("#0069B4"));
            indicatorNew.getLayoutParams().height = (int) (3 * getResources().getDisplayMetrics().density);
        } else if ("in_progress".equals(tab)) {
            tvTabInProgress.setTextColor(Color.parseColor("#0069B4"));
            indicatorInProgress.setBackgroundColor(Color.parseColor("#0069B4"));
            indicatorInProgress.getLayoutParams().height = (int) (3 * getResources().getDisplayMetrics().density);
        } else {
            tvTabCompleted.setTextColor(Color.parseColor("#0069B4"));
            indicatorCompleted.setBackgroundColor(Color.parseColor("#0069B4"));
            indicatorCompleted.getLayoutParams().height = (int) (3 * getResources().getDisplayMetrics().density);
        }
        indicatorNew.requestLayout();
        indicatorInProgress.requestLayout();
        indicatorCompleted.requestLayout();
        applyFilter();
    }

    private void resetTabsUI() {
        tvTabNew.setTextColor(Color.parseColor("#64748B"));
        tvTabInProgress.setTextColor(Color.parseColor("#64748B"));
        tvTabCompleted.setTextColor(Color.parseColor("#64748B"));
        indicatorNew.setBackgroundColor(Color.parseColor("#E2E8F0"));
        indicatorInProgress.setBackgroundColor(Color.parseColor("#E2E8F0"));
        indicatorCompleted.setBackgroundColor(Color.parseColor("#E2E8F0"));
        indicatorNew.getLayoutParams().height = (int) (1 * getResources().getDisplayMetrics().density);
        indicatorInProgress.getLayoutParams().height = (int) (1 * getResources().getDisplayMetrics().density);
        indicatorCompleted.getLayoutParams().height = (int) (1 * getResources().getDisplayMetrics().density);
    }

    private void setupBottomNavigation() {
        findViewById(R.id.navProviderDashboard).setOnClickListener(v -> {
            startActivity(new Intent(this, ProviderDashboardActivity.class));
            finish();
        });
        findViewById(R.id.navProviderServices).setOnClickListener(v -> {
            startActivity(new Intent(this, ProviderServicesActivity.class));
            finish();
        });
        findViewById(R.id.navProviderHistory).setOnClickListener(v -> {
            startActivity(new Intent(this, TripsHistoryActivity.class));
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ordersListener != null) ordersListener.remove();
        if (profileListener != null) profileListener.remove();
    }
}
