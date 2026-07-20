package com.example.graduationproject;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class My_Orders_Activity extends AppCompatActivity {

    private RecyclerView rvOrders;
    private OrderAdapter adapter;
    private List<OrderModel> allOrders = new ArrayList<>();
    private MaterialButton btnActiveOrders, btnHistoryOrders;
    private boolean showingActive = true;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        rvOrders = findViewById(R.id.rvOrders);
        btnActiveOrders = findViewById(R.id.btnActiveOrders);
        btnHistoryOrders = findViewById(R.id.btnHistoryOrders);

        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdapter(this, new ArrayList<>());
        rvOrders.setAdapter(adapter);

        btnActiveOrders.setOnClickListener(v -> {
            showingActive = true;
            updateTabUI();
            filterOrders();
        });

        btnHistoryOrders.setOnClickListener(v -> {
            showingActive = false;
            updateTabUI();
            filterOrders();
        });

        setupBottomNavigation();
        fetchOrders();
    }

    private void setupBottomNavigation() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(this, MapExplorerActivity.class));
            finish();
        });
        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
        });
    }

    private void updateTabUI() {
        if (showingActive) {
            btnActiveOrders.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0069B4")));
            btnActiveOrders.setTextColor(Color.WHITE);
            btnHistoryOrders.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            btnHistoryOrders.setTextColor(Color.GRAY);
        } else {
            btnHistoryOrders.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0069B4")));
            btnHistoryOrders.setTextColor(Color.WHITE);
            btnActiveOrders.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            btnActiveOrders.setTextColor(Color.GRAY);
        }
    }

    private void fetchOrders() {
        if (mAuth.getCurrentUser() == null) return;
        
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("orders")
                .whereEqualTo("customer_id", userId)
                .orderBy("created_at", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        allOrders.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            OrderModel order = document.toObject(OrderModel.class);
                            order.setId(document.getId());
                            allOrders.add(order);
                        }
                        filterOrders();
                    } else {
                        Log.e("Firebase", "Error getting orders", task.getException());
                        Toast.makeText(My_Orders_Activity.this, "لا يوجد طلبات حالياً", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterOrders() {
        List<OrderModel> filtered = new ArrayList<>();
        for (OrderModel order : allOrders) {
            String status = order.getStatus();
            boolean isActive = status != null && (status.equals("pending") || status.equals("accepted") || status.equals("on_way"));
            
            if (showingActive && isActive) filtered.add(order);
            else if (!showingActive && !isActive) filtered.add(order);
        }
        adapter.updateList(filtered);
    }
}
