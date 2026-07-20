package com.example.graduationproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Water_Delivered_Activity extends AppCompatActivity {

    private String orderId;
    private FirebaseFirestore db;
    private TextView tvDriverName, tvOrderNumber, tvOrderDate, tvOrderQuantity, tvTotalPrice;
    private ImageView imgDriverAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_delivered);

        db = FirebaseFirestore.getInstance();
        orderId = getIntent().getStringExtra("order_id");

        tvDriverName = findViewById(R.id.tvDriverName);
        tvOrderNumber = findViewById(R.id.tvOrderNumber);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvOrderQuantity = findViewById(R.id.tvOrderQuantity);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        imgDriverAvatar = findViewById(R.id.imgDriverAvatar);

        ImageView btnMenu = findViewById(R.id.btnMenuDelivered);
        ImageView btnNotifications = findViewById(R.id.btnNotificationsDelivered);
        MaterialButton btnConfirmDelivery = findViewById(R.id.btnConfirmDelivery);

        if (orderId != null) {
            loadOrderData();
        } else {
            Toast.makeText(this, "خطأ في معرف الطلب", Toast.LENGTH_SHORT).show();
            finish();
        }

        // إغلاق الواجهة والعودة عند الضغط على القائمة
        btnMenu.setOnClickListener(v -> finish());

        btnNotifications.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationsActivity.class));
        });

        btnConfirmDelivery.setOnClickListener(v -> {
            confirmDeliveryAndRate();
        });
    }

    private void loadOrderData() {
        db.collection("orders").document(orderId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // 1. عرض بيانات الطلب الأساسية
                String orderNo = "#" + (orderId.length() > 6 ? orderId.substring(0, 6).toUpperCase() : orderId.toUpperCase());
                tvOrderNumber.setText(orderNo);

                String serviceId = documentSnapshot.getString("service_id");
                String notes = documentSnapshot.getString("notes");
                Long quantity = documentSnapshot.getLong("quantity");
                String unit = documentSnapshot.getString("unit");
                
                // في حال كان اشتراكاً شهرياً، نستخلص اسم الباقة للعرض بشكل أنيق
                if ("subscription_monthly".equals(serviceId) && notes != null) {
                    if (notes.contains("الباقة")) {
                        // استخراج "الباقة الأساسية" مثلاً من النص الكامل
                        String planName = notes.split("-")[0].replace("اشتراك شهري: ", "").trim();
                        tvOrderQuantity.setText(planName);
                    } else {
                        tvOrderQuantity.setText(notes);
                    }
                } else {
                    tvOrderQuantity.setText((quantity != null ? quantity : "---") + " " + (unit != null ? unit : "لتر"));
                }

                Double price = documentSnapshot.getDouble("total_price");
                if (tvTotalPrice != null) {
                    tvTotalPrice.setText(String.format(Locale.getDefault(), "%.2f ₪", price != null ? price : 0.0));
                }

                String scheduledTime = documentSnapshot.getString("scheduled_time");
                Timestamp createdAt = documentSnapshot.getTimestamp("created_at");
                
                // عرض الموعد المجدول للاشتراكات أو تاريخ الطلب للطلبات العادية
                if ("subscription_monthly".equals(serviceId) && scheduledTime != null) {
                    tvOrderDate.setText("الموعد: " + scheduledTime);
                } else if (createdAt != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM، hh:mm a", new Locale("ar"));
                    tvOrderDate.setText(sdf.format(createdAt.toDate()));
                }

                // 2. عرض بيانات المزود / السائق
                String providerName = documentSnapshot.getString("provider_name");
                tvDriverName.setText(providerName != null ? providerName : "مزود الخدمة");

                String providerId = documentSnapshot.getString("provider_id");
                if (providerId != null) {
                    db.collection("providers").document(providerId).get().addOnSuccessListener(providerDoc -> {
                        if (providerDoc.exists()) {
                            String imageUrl = providerDoc.getString("profile_image");
                            if (imageUrl != null && !imageUrl.isEmpty() && imgDriverAvatar != null) {
                                Glide.with(this)
                                        .load(imageUrl)
                                        .circleCrop()
                                        .placeholder(R.drawable.user)
                                        .into(imgDriverAvatar);
                            }
                        }
                    });
                }
            }
        }).addOnFailureListener(e -> {
            Log.e("WaterDelivered", "Error loading order", e);
            Toast.makeText(this, "فشل تحميل بيانات الطلب", Toast.LENGTH_SHORT).show();
        });
    }

    private void confirmDeliveryAndRate() {
        db.collection("orders").document(orderId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // تحديث حالة الطلب إلى مكتمل (Delivered)
                db.collection("orders").document(orderId).update("status", "delivered");

                Intent intent = new Intent(Water_Delivered_Activity.this, Rate_Service_Activity.class);
                intent.putExtra("order_uuid", orderId);
                intent.putExtra("provider_id", documentSnapshot.getString("provider_id"));
                intent.putExtra("station_name", documentSnapshot.getString("provider_name"));
                intent.putExtra("order_number", "#" + (orderId.length() > 8 ? orderId.substring(0, 8).toUpperCase() : orderId.toUpperCase()));
                
                String serviceId = documentSnapshot.getString("service_id");
                String notes = documentSnapshot.getString("notes");
                Long quantity = documentSnapshot.getLong("quantity");
                String unit = documentSnapshot.getString("unit");
                Double price = documentSnapshot.getDouble("total_price");
                
                String quantityDisplay;
                if ("subscription_monthly".equals(serviceId) && notes != null) {
                    if (notes.contains("الباقة")) {
                        quantityDisplay = notes.split("-")[0].replace("اشتراك شهري: ", "").trim();
                    } else {
                        quantityDisplay = notes;
                    }
                } else {
                    quantityDisplay = (quantity != null ? quantity : "---") + " " + (unit != null ? unit : "");
                }
                
                intent.putExtra("quantity", quantityDisplay);
                intent.putExtra("price", String.format(Locale.getDefault(), "%.2f ₪", price != null ? price : 0.0));
                intent.putExtra("order_date", "تم التوصيل بنجاح");

                startActivity(intent);
                finish();
            }
        });
    }
}
