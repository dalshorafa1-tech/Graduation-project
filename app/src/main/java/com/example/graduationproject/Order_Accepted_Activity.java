package com.example.graduationproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class Order_Accepted_Activity extends AppCompatActivity {

    private String orderId;
    private FirebaseFirestore db;
    private TextView tvAcceptanceMainDesc, tvServiceTypeName, tvServiceQuantityValue, tvPaymentStatusLabel, tvAcceptedOrderPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_accepted);

        db = FirebaseFirestore.getInstance();
        orderId = getIntent().getStringExtra("order_id");

        initViews();

        if (orderId != null) {
            loadOrderData();
        } else {
            Toast.makeText(this, "خطأ في تحميل بيانات الطلب", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBack);
        Button btnNavigateToPayment = findViewById(R.id.btnNavigateToPayment);
        Button btnCancelCurrentOrder = findViewById(R.id.btnCancelCurrentOrder);
        
        tvAcceptanceMainDesc = findViewById(R.id.tvAcceptanceMainDesc);
        tvServiceTypeName = findViewById(R.id.tvServiceTypeName);
        tvServiceQuantityValue = findViewById(R.id.tvServiceQuantityValue);
        tvPaymentStatusLabel = findViewById(R.id.tvPaymentStatusLabel);
        tvAcceptedOrderPrice = findViewById(R.id.tvAcceptedOrderPrice);

        btnBack.setOnClickListener(v -> finish());

        btnNavigateToPayment.setOnClickListener(v -> {
            Intent intent = new Intent(Order_Accepted_Activity.this, Payment_Method_Activity.class);
            intent.putExtra("order_id", orderId);
            startActivity(intent);
        });

        btnCancelCurrentOrder.setOnClickListener(v -> {
            if (orderId != null) {
                cancelOrder();
            } else {
                finish();
            }
        });

        setupBottomNavigation();
    }

    private void loadOrderData() {
        db.collection("orders").document(orderId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String providerName = documentSnapshot.getString("provider_name");
                String serviceId = documentSnapshot.getString("service_id");
                String notes = documentSnapshot.getString("notes");
                Long quantity = documentSnapshot.getLong("quantity");
                String unit = documentSnapshot.getString("unit");
                String scheduledTime = documentSnapshot.getString("scheduled_time");
                Double totalPrice = documentSnapshot.getDouble("total_price");

                // تحديث الوصف الرئيسي
                if (providerName != null) {
                    tvAcceptanceMainDesc.setText("قام المزود (" + providerName + ") بالموافقة على طلبك. يرجى إتمام عملية الدفع لتأكيد الحجز وبدء التوصيل.");
                }

                // عرض السعر
                if (tvAcceptedOrderPrice != null && totalPrice != null) {
                    tvAcceptedOrderPrice.setText(String.format(Locale.getDefault(), "%.2f ₪", totalPrice));
                }

                // التعامل مع بيانات الاشتراك الشهري
                if ("subscription_monthly".equals(serviceId)) {
                    tvServiceTypeName.setText("اشتراك مياه شهري");
                    
                    // استخراج اسم الباقة من الملاحظات
                    if (notes != null && notes.contains("الباقة")) {
                        String planName = notes.split("-")[0].replace("اشتراك شهري: ", "").trim();
                        tvServiceQuantityValue.setText(planName);
                    } else {
                        tvServiceQuantityValue.setText(quantity + " لتر (اشتراك)");
                    }
                    
                    if (scheduledTime != null) {
                        tvPaymentStatusLabel.setText("حالة الطلب: مقبول | موعدك: " + scheduledTime);
                    } else {
                        tvPaymentStatusLabel.setText("حالة الطلب: مقبول | بانتظار الدفع");
                    }
                } else {
                    tvServiceTypeName.setText("تزويد مياه");
                    tvServiceQuantityValue.setText((quantity != null ? quantity : "---") + " " + (unit != null ? unit : "لتر"));
                    tvPaymentStatusLabel.setText("حالة الطلب: بانتظار الدفع");
                }
            }
        }).addOnFailureListener(e -> {
            Log.e("OrderAccepted", "Error loading order data", e);
            Toast.makeText(this, "فشل في تحميل تفاصيل الطلب", Toast.LENGTH_SHORT).show();
        });
    }

    private void cancelOrder() {
        db.collection("orders").document(orderId)
                .update("status", "cancelled")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "تم إلغاء الطلب", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "فشل في إلغاء الطلب", Toast.LENGTH_SHORT).show();
                });
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
}
