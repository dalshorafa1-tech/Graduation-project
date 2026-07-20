package com.example.graduationproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Locale;

public class Order_Details_Activity extends AppCompatActivity {

    private TextView tvOrderId, tvStatusBadge, tvProviderName, tvAddress, tvUnit, tvQuantity, tvTotalPrice;
    private MaterialButton btnCancelOrder, btnTrackOrder;
    private FirebaseFirestore db;
    private String orderId;
    private ListenerRegistration orderListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        db = FirebaseFirestore.getInstance();

        initViews();

        orderId = getIntent().getStringExtra("order_id");

        if (orderId != null) {
            fetchOrderDetails();
        } else {
            Toast.makeText(this, "معرف الطلب غير موجود", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnCancelOrder.setOnClickListener(v -> cancelOrder());

        btnTrackOrder.setOnClickListener(v -> {
            Intent trackIntent = new Intent(this, Track_Driver_Activity.class);
            trackIntent.putExtra("order_id", orderId);
            startActivity(trackIntent);
        });
    }

    private void initViews() {
        tvOrderId = findViewById(R.id.tvOrderId);
        tvStatusBadge = findViewById(R.id.tvStatusBadge);
        tvProviderName = findViewById(R.id.tvCustomerName); // مستخدم لعرض اسم المزود في واجهة الزبون
        tvAddress = findViewById(R.id.tvAddress);
        tvUnit = findViewById(R.id.tvUnit);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        
        // الأزرار في XML لها أسماء مختلفة، سنقوم بربطها حسب وظيفتها الجديدة
        btnCancelOrder = findViewById(R.id.btnConfirmArrival); // مستخدم كزر إلغاء
        btnTrackOrder = findViewById(R.id.btnCompleteTask);   // مستخدم كزر تتبع
        
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void fetchOrderDetails() {
        orderListener = db.collection("orders").document(orderId).addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w("OrderDetails", "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                OrderModel order = snapshot.toObject(OrderModel.class);
                if (order == null) return;

                // تحديث واجهة المستخدم بالبيانات من Firebase
                tvOrderId.setText("#" + (orderId.length() > 8 ? orderId.substring(0, 8).toUpperCase() : orderId.toUpperCase()));
                tvProviderName.setText(order.getProviderName() != null ? order.getProviderName() : "مزود الخدمة");
                tvAddress.setText(order.getAddressDetails() != null ? "📍 " + order.getAddressDetails() : "📍 العنوان غير محدد");
                tvUnit.setText(order.getUnit() != null ? order.getUnit() : "لتر");
                tvQuantity.setText(String.valueOf(order.getQuantity()));
                tvTotalPrice.setText(String.format(Locale.getDefault(), "%.2f ₪", order.getTotalPrice() != null ? order.getTotalPrice() : 0.0));

                updateStatusUI(order.getStatus());
            }
        });
    }

    private void cancelOrder() {
        if (orderId == null) return;
        
        btnCancelOrder.setEnabled(false);
        db.collection("orders").document(orderId)
                .update("status", "cancelled")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "تم إلغاء الطلب بنجاح", Toast.LENGTH_SHORT).show();
                    btnCancelOrder.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "فشل في إلغاء الطلب: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnCancelOrder.setEnabled(true);
                });
    }

    private void updateStatusUI(String status) {
        if (status == null) return;
        
        switch (status) {
            case "pending":
                tvStatusBadge.setText("● قيد الانتظار");
                tvStatusBadge.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                btnTrackOrder.setVisibility(View.GONE);
                btnCancelOrder.setVisibility(View.VISIBLE);
                break;
            case "accepted":
                tvStatusBadge.setText("● تم القبول");
                tvStatusBadge.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                btnTrackOrder.setVisibility(View.VISIBLE);
                btnCancelOrder.setVisibility(View.VISIBLE);
                break;
            case "on_way":
                tvStatusBadge.setText("● في الطريق");
                tvStatusBadge.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                btnTrackOrder.setVisibility(View.VISIBLE);
                btnCancelOrder.setVisibility(View.GONE); // لا يمكن الإلغاء إذا أصبح في الطريق
                break;
            case "delivered":
                tvStatusBadge.setText("● تم التوصيل");
                tvStatusBadge.setTextColor(getResources().getColor(android.R.color.darker_gray));
                btnTrackOrder.setVisibility(View.GONE);
                btnCancelOrder.setVisibility(View.GONE);
                break;
            case "cancelled":
                tvStatusBadge.setText("● ملغي");
                tvStatusBadge.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                btnTrackOrder.setVisibility(View.GONE);
                btnCancelOrder.setVisibility(View.GONE);
                break;
            default:
                tvStatusBadge.setText("● " + status);
                btnTrackOrder.setVisibility(View.GONE);
                btnCancelOrder.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orderListener != null) {
            orderListener.remove();
        }
    }
}
