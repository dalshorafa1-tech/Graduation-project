package com.example.graduationproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TripsHistoryActivity extends AppCompatActivity {

    private TextView tvSalesAmount, tvCompletedCount, tvCancelledCount;
    private EditText etSearchHistory;
    private RecyclerView rvTripsHistory;
    private ImageView imgProfile;
    private TripsHistoryAdapter adapter;
    private List<OrderModel> allOrders = new ArrayList<>();
    private List<OrderModel> filteredList = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;
    private ListenerRegistration historyListener;
    private ListenerRegistration profileListener;

    private String statusFilter = "all"; // all, delivered, cancelled
    private boolean sortDescending = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips_history);

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
        listenToHistory();
        listenToProfile();
        setupSearch();
        setupFilters();
        setupBottomNavigation();
    }

    private void initViews() {
        tvSalesAmount = findViewById(R.id.tvSalesAmount);
        tvCompletedCount = findViewById(R.id.tvCompletedCount);
        tvCancelledCount = findViewById(R.id.tvCancelledCount);
        etSearchHistory = findViewById(R.id.etSearchHistory);
        rvTripsHistory = findViewById(R.id.rvTripsHistory);
        imgProfile = findViewById(R.id.imgProfile);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        View btnNotifications = findViewById(R.id.btnNotifications);
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> {
                startActivity(new Intent(this, NotificationsActivity.class));
            });
        }

        if (imgProfile != null) {
            imgProfile.setOnClickListener(v -> {
                startActivity(new Intent(this, Profile.class));
            });
        }
    }

    private void listenToProfile() {
        profileListener = db.collection("providers").document(userId).addSnapshotListener((doc, e) -> {
            if (e != null || doc == null || !doc.exists()) return;
            String imageUrl = doc.getString("profile_image");
            if (imgProfile != null) {
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(this).load(imageUrl).placeholder(R.drawable.user).into(imgProfile);
                } else {
                    imgProfile.setImageResource(R.drawable.user);
                }
            }
        });
    }

    private void setupRecyclerView() {
        rvTripsHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TripsHistoryAdapter(this, filteredList);
        rvTripsHistory.setAdapter(adapter);
    }

    private void listenToHistory() {
        if (userId == null) return;

        Query query = db.collection("orders")
                .whereEqualTo("provider_id", userId)
                .orderBy("created_at", sortDescending ? Query.Direction.DESCENDING : Query.Direction.ASCENDING);

        historyListener = query.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.e("TripsHistory", "Error: " + e.getMessage());
                return;
            }

            if (snapshots != null) {
                allOrders.clear();
                double totalSales = 0;
                int completed = 0;
                int cancelled = 0;

                for (QueryDocumentSnapshot doc : snapshots) {
                    OrderModel order = doc.toObject(OrderModel.class);
                    order.setId(doc.getId());
                    
                    String status = order.getStatus();
                    if ("delivered".equals(status) || "cancelled".equals(status)) {
                        allOrders.add(order);
                        if ("delivered".equals(status)) {
                            completed++;
                            if (order.getTotalPrice() != null) {
                                totalSales += order.getTotalPrice();
                            }
                        } else {
                            cancelled++;
                        }
                    }
                }

                tvSalesAmount.setText(String.format(Locale.getDefault(), "%.2f ₪", totalSales));
                tvCompletedCount.setText(String.valueOf(completed));
                tvCancelledCount.setText(String.valueOf(cancelled));

                applyFiltersAndSearch();
            }
        });
    }

    private void setupSearch() {
        etSearchHistory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFiltersAndSearch();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilters() {
        findViewById(R.id.btnFilterStatus).setOnClickListener(v -> {
            String[] items = {"الكل", "مكتملة فقط", "ملغاة فقط"};
            new AlertDialog.Builder(this)
                    .setTitle("تصفية حسب الحالة")
                    .setItems(items, (dialog, which) -> {
                        switch (which) {
                            case 0: statusFilter = "all"; break;
                            case 1: statusFilter = "delivered"; break;
                            case 2: statusFilter = "cancelled"; break;
                        }
                        applyFiltersAndSearch();
                    }).show();
        });

        findViewById(R.id.btnFilterDate).setOnClickListener(v -> {
            sortDescending = !sortDescending;
            String msg = sortDescending ? "الترتيب: من الأحدث للأقدم" : "الترتيب: من الأقدم للأحدث";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            if (historyListener != null) historyListener.remove();
            listenToHistory();
        });
    }

    private void applyFiltersAndSearch() {
        String query = etSearchHistory.getText().toString().toLowerCase().trim();
        filteredList.clear();

        for (OrderModel order : allOrders) {
            boolean matchesStatus = statusFilter.equals("all") || statusFilter.equals(order.getStatus());
            boolean matchesSearch = query.isEmpty() || 
                    (order.getId() != null && order.getId().toLowerCase().contains(query)) ||
                    (order.getAddressDetails() != null && order.getAddressDetails().toLowerCase().contains(query));
            
            if (matchesStatus && matchesSearch) {
                filteredList.add(order);
            }
        }
        adapter.updateList(filteredList);
    }

    private void setupBottomNavigation() {
        findViewById(R.id.navProviderDashboard).setOnClickListener(v -> {
            startActivity(new Intent(this, ProviderDashboardActivity.class));
            finish();
        });
        findViewById(R.id.navProviderOrders).setOnClickListener(v -> {
            startActivity(new Intent(this, OrdersManagementActivity.class));
            finish();
        });
        findViewById(R.id.navProviderServices).setOnClickListener(v -> {
            startActivity(new Intent(this, ProviderServicesActivity.class));
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (historyListener != null) historyListener.remove();
        if (profileListener != null) profileListener.remove();
    }
}
