package com.example.graduationproject;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProviderDashboardActivity extends AppCompatActivity {

    private SwitchCompat switchWorkStatus;
    private TextView tvActiveOrdersCount, tvTodayEarnings, tvAverageRating, tvWorkStatus, tvNotificationBadge;
    private ImageView imgProfile;
    private RecyclerView rvUrgentOrders;
    private UrgentOrderAdapter urgentAdapter;
    private List<OrderModel> urgentOrders = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;

    private ListenerRegistration ordersListener;
    private ListenerRegistration statsListener;
    private ListenerRegistration urgentOrdersListener;
    private ListenerRegistration profileListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_dashboard);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupBottomNavigation();
        loadDashboardStats();
        listenForUrgentOrders();
        listenToProfile();
    }

    private void initViews() {
        switchWorkStatus = findViewById(R.id.switchWorkStatus);
        tvActiveOrdersCount = findViewById(R.id.tvActiveOrdersCount);
        tvTodayEarnings = findViewById(R.id.tvTodayEarnings);
        tvAverageRating = findViewById(R.id.tvAverageRating);
        tvWorkStatus = findViewById(R.id.tvWorkStatus);
        rvUrgentOrders = findViewById(R.id.rvUrgentOrders);
        tvNotificationBadge = findViewById(R.id.tvNotificationBadge);
        imgProfile = findViewById(R.id.imgProfile);

        rvUrgentOrders.setLayoutManager(new LinearLayoutManager(this));
        urgentAdapter = new UrgentOrderAdapter(this, urgentOrders, userId);
        rvUrgentOrders.setAdapter(urgentAdapter);

        if (imgProfile != null) {
            imgProfile.setOnClickListener(v -> startActivity(new Intent(this, Profile.class)));
        }

        switchWorkStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateStatusUI(isChecked);
            updateStatusInFirestore(isChecked);
        });

        findViewById(R.id.btnCustomOffer).setOnClickListener(v -> showCustomOfferDialog());
        findViewById(R.id.btnNavNotification).setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationsActivity.class));
        });
    }

    private void listenToProfile() {
        profileListener = db.collection("providers").document(userId).addSnapshotListener((doc, e) -> {
            if (e != null || doc == null || !doc.exists()) return;

            String status = doc.getString("status");
            boolean isActive = "active".equals(status);
            switchWorkStatus.setChecked(isActive);
            updateStatusUI(isActive);

            // تحميل الصورة الشخصية
            String imageUrl = doc.getString("profile_image");
            if (imgProfile != null) {
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(this).load(imageUrl).placeholder(R.drawable.user).circleCrop().into(imgProfile);
                } else {
                    imgProfile.setImageResource(R.drawable.user);
                }
            }

            Double rating = doc.getDouble("rating");
            tvAverageRating.setText(String.format(Locale.getDefault(), "%.1f", rating != null ? rating : 0.0));
            
            Double earnings = doc.getDouble("today_earnings");
            tvTodayEarnings.setText(String.format(Locale.getDefault(), "%.0f NIS", earnings != null ? earnings : 0.0));
        });
    }

    private void updateStatusUI(boolean isActive) {
        if (isActive) {
            tvWorkStatus.setText("متاح للعمل");
            tvWorkStatus.setTextColor(Color.parseColor("#0069B4"));
            switchWorkStatus.setThumbTintList(ColorStateList.valueOf(Color.parseColor("#0069B4")));
            switchWorkStatus.setTrackTintList(ColorStateList.valueOf(Color.parseColor("#B3E5FC")));
        } else {
            tvWorkStatus.setText("غير متاح حالياً");
            tvWorkStatus.setTextColor(Color.parseColor("#94A3B8"));
            switchWorkStatus.setThumbTintList(ColorStateList.valueOf(Color.parseColor("#94A3B8")));
            switchWorkStatus.setTrackTintList(ColorStateList.valueOf(Color.parseColor("#E2E8F0")));
        }
    }

    private void updateStatusInFirestore(boolean isActive) {
        if (userId == null) return;
        String status = isActive ? "active" : "offline";
        db.collection("providers").document(userId).update("status", status)
                .addOnFailureListener(e -> Toast.makeText(this, "فشل تحديث الحالة", Toast.LENGTH_SHORT).show());
    }

    private void loadDashboardStats() {
        if (userId == null) return;

        statsListener = db.collection("orders")
                .whereEqualTo("provider_id", userId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) return;
                    int activeCount = 0;
                    if (snapshots != null) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots) {
                            String status = doc.getString("status");
                            if ("accepted".equals(status) || "on_way".equals(status)) {
                                activeCount++;
                            }
                        }
                    }
                    tvActiveOrdersCount.setText(String.valueOf(activeCount));
                });
    }

    private void listenForUrgentOrders() {
        urgentOrdersListener = db.collection("orders")
                .whereEqualTo("status", "pending")
                .orderBy("created_at", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("Dashboard", "Error listening to orders", e);
                        return;
                    }
                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            OrderModel order = dc.getDocument().toObject(OrderModel.class);
                            order.setId(dc.getDocument().getId());
                            if (order.getProviderId() != null && !order.getProviderId().isEmpty()) continue;

                            switch (dc.getType()) {
                                case ADDED:
                                    urgentOrders.add(0, order);
                                    urgentAdapter.notifyItemInserted(0);
                                    break;
                                case REMOVED:
                                    removeOrderFromList(order.getId());
                                    break;
                                case MODIFIED:
                                    updateOrderInList(order);
                                    break;
                            }
                        }
                        updateBadge();
                    }
                });
    }

    private void removeOrderFromList(String id) {
        for (int i = 0; i < urgentOrders.size(); i++) {
            if (urgentOrders.get(i).getId().equals(id)) {
                urgentOrders.remove(i);
                urgentAdapter.notifyItemRemoved(i);
                break;
            }
        }
    }

    private void updateOrderInList(OrderModel order) {
        for (int i = 0; i < urgentOrders.size(); i++) {
            if (urgentOrders.get(i).getId().equals(order.getId())) {
                urgentOrders.set(i, order);
                urgentAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    private void updateBadge() {
        if (tvNotificationBadge == null) return;
        int count = urgentOrders.size();
        if (count > 0) {
            tvNotificationBadge.setVisibility(View.VISIBLE);
            tvNotificationBadge.setText(String.valueOf(count));
        } else {
            tvNotificationBadge.setVisibility(View.GONE);
        }
    }

    private void showCustomOfferDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("إضافة عرض خاص");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 30, 40, 30);
        EditText etTitle = new EditText(this);
        etTitle.setHint("عنوان العرض");
        layout.addView(etTitle);
        EditText etPrice = new EditText(this);
        etPrice.setHint("السعر الجديد");
        etPrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etPrice);
        builder.setView(layout);
        builder.setPositiveButton("نشر", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String price = etPrice.getText().toString().trim();
            if (!title.isEmpty() && !price.isEmpty()) {
                Map<String, Object> offer = new HashMap<>();
                offer.put("provider_id", userId);
                offer.put("title", title);
                offer.put("price", Double.parseDouble(price));
                offer.put("created_at", com.google.firebase.Timestamp.now());
                db.collection("special_offers").add(offer)
                        .addOnSuccessListener(ref -> Toast.makeText(this, "تم نشر العرض", Toast.LENGTH_SHORT).show());
            }
        });
        builder.setNegativeButton("إلغاء", null);
        builder.show();
    }

    private void setupBottomNavigation() {
        findViewById(R.id.navProviderDashboard).setOnClickListener(v -> {});
        findViewById(R.id.navProviderOrders).setOnClickListener(v -> startActivity(new Intent(this, OrdersManagementActivity.class)));
        findViewById(R.id.navProviderServices).setOnClickListener(v -> startActivity(new Intent(this, ProviderServicesActivity.class)));
        findViewById(R.id.navProviderHistory).setOnClickListener(v -> startActivity(new Intent(this, TripsHistoryActivity.class)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (statsListener != null) statsListener.remove();
        if (urgentOrdersListener != null) urgentOrdersListener.remove();
        if (profileListener != null) profileListener.remove();
    }
}
