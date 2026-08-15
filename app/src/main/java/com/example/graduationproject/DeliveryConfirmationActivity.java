package com.example.graduationproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class DeliveryConfirmationActivity extends AppCompatActivity {

    private ImageView btnMenu, btnNotifications;
    private MaterialButton btnConfirmDelivery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_confirmation);

        // 1. ربط العناصر
        btnMenu = findViewById(R.id.btnMenuDelivered);
        btnNotifications = findViewById(R.id.btnNotificationsDelivered);
        btnConfirmDelivery = findViewById(R.id.btnConfirmDelivery);

        // 2. إعداد المستمعات والأحداث
        setupClickListeners();
    }

    private void setupClickListeners() {
        // قائمة الهيدر الجانبية
        btnMenu.setOnClickListener(v ->
                Toast.makeText(this, "فتح القائمة الرئيسية", Toast.LENGTH_SHORT).show()
        );

        // مركز التنبيهات
        btnNotifications.setOnClickListener(v ->
                Toast.makeText(this, "الانتقال لمركز الإشعارات", Toast.LENGTH_SHORT).show()
        );

        // حدث الضغط على زر تأكيد استلام المياه
        btnConfirmDelivery.setOnClickListener(v -> showConfirmationDialog());
    }

    private void showConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("تأكيد الاستلام")
                .setMessage("هل أنت متأكد من تفريغ كمية الشحنة (1000 لتر) بالكامل في خزاناتك؟")
                .setPositiveButton("نعم، استلمت", (dialog, which) -> {
                    // هنا يتم استدعاء الـ API الخاص بتحديث حالة الطلب إلى (تم التسليم بنجاح)
                    Toast.makeText(this, "✅ تم تأكيد الاستلام، شكراً لثقتك بإرواء!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(DeliveryConfirmationActivity.this, Monthly_Subscription_Activity.class);
                    startActivity(intent);
                    finish(); // العودة للشاشة السابقة أو لوحة التحكم
                })
                .setNegativeButton("إلغاء", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }
}
