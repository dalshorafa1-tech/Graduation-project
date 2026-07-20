package com.example.graduationproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class Order_Status_Activity extends AppCompatActivity {

    private TextView tvStatusDescription, tvOrderSerialNumber, tvOrderQuantity, tvOrderPriceStatus, tvOrderLocationText;
    private Button btnCancelOrder;
    private String orderId;
    private FirebaseFirestore db;
    private ListenerRegistration orderListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        db = FirebaseFirestore.getInstance();
        orderId = getIntent().getStringExtra("order_id");

        ImageView btnBack = findViewById(R.id.btnBack);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);
        tvStatusDescription = findViewById(R.id.tvStatusDescription);
        tvOrderSerialNumber = findViewById(R.id.tvOrderSerialNumber);
        tvOrderQuantity = findViewById(R.id.tvOrderQuantity);
        tvOrderPriceStatus = findViewById(R.id.tvOrderPriceStatus);
        tvOrderLocationText = findViewById(R.id.tvOrderLocationText);

        if (orderId != null) {
            tvOrderSerialNumber.setText("#" + (orderId.length() > 8 ? orderId.substring(0, 8) : orderId));
            listenToOrderUpdates();
        } else {
            Toast.makeText(this, "خطأ في تحميل بيانات الطلب", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnBack.setOnClickListener(v -> finish());

        btnCancelOrder.setOnClickListener(v -> cancelOrder());
        
        setupBottomNavigation();
    }

    private void listenToOrderUpdates() {
        DocumentReference docRef = db.collection("orders").document(orderId);
        orderListener = docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w("OrderStatus", "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                String status = snapshot.getString("status");
                String providerName = snapshot.getString("provider_name");
                Long quantity = snapshot.getLong("quantity");
                String unit = snapshot.getString("unit");
                Double price = snapshot.getDouble("total_price");
                String address = snapshot.getString("address_details");
                String serviceId = snapshot.getString("service_id");
                String notes = snapshot.getString("notes");
                String scheduledTime = snapshot.getString("scheduled_time");

                // التعامل مع بيانات الاشتراك الشهري
                if ("subscription_monthly".equals(serviceId)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("طلب اشتراك شهري قيد المراجعة.\n");
                    if (notes != null) sb.append(notes).append("\n");
                    if (scheduledTime != null) sb.append("الموعد المجدول: ").append(scheduledTime);
                    tvStatusDescription.setText(sb.toString());
                } else {
                    if (providerName != null) {
                        tvStatusDescription.setText("يقوم المزود (" + providerName + ") بمراجعة طلبك، ستتلقى إشعاراً فور القبول.");
                    }
                }
                
                if (quantity != null) tvOrderQuantity.setText(quantity + " " + (unit != null ? unit : ""));
                if (price != null) tvOrderPriceStatus.setText(String.format("%.2f ₪", price));
                if (address != null) tvOrderLocationText.setText(address);

                if ("accepted".equals(status)) {
                    Intent intent = new Intent(Order_Status_Activity.this, Order_Accepted_Activity.class);
                    intent.putExtra("order_id", orderId);
                    startActivity(intent);
                    finish();
                } else if ("cancelled".equals(status)) {
                    Toast.makeText(this, "تم إلغاء الطلب", Toast.LENGTH_SHORT).show();
                    finish();
                } else if ("on_way".equals(status) || "delivered".equals(status)) {
                    Intent intent = new Intent(Order_Status_Activity.this, Order_Details_Activity.class);
                    intent.putExtra("order_id", orderId);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private void cancelOrder() {
        if (orderId == null) return;
        db.collection("orders").document(orderId)
                .update("status", "cancelled")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(Order_Status_Activity.this, "تم إلغاء الطلب", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(Order_Status_Activity.this, "فشل إلغاء الطلب", Toast.LENGTH_SHORT).show());
    }

    private void setupBottomNavigation() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            Intent intent = new Intent(this, MapExplorerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
        findViewById(R.id.navWallet).setOnClickListener(v -> startActivity(new Intent(this, WalletActivity.class)));
        findViewById(R.id.navOrders).setOnClickListener(v -> startActivity(new Intent(this, My_Orders_Activity.class)));
        findViewById(R.id.navProfile).setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orderListener != null) {
            orderListener.remove();
        }
    }
}
