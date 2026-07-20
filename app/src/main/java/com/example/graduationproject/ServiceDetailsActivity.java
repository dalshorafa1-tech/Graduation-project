package com.example.graduationproject;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;

public class ServiceDetailsActivity extends AppCompatActivity {

    private TextView tvName, tvStatus, tvPrice, tvPriceCup, tvDesc, tvRejectReason;
    private ImageView btnBack;
    private MaterialCardView cardRejectReason;
    private FirebaseFirestore db;
    private String serviceId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_details);

        db = FirebaseFirestore.getInstance();
        serviceId = getIntent().getStringExtra("service_id");

        initViews();

        if (serviceId != null) {
            loadServiceDetails();
        } else {
            Toast.makeText(this, "خطأ في تحميل تفاصيل الخدمة", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void initViews() {
        tvName = findViewById(R.id.tvDetailName);
        tvStatus = findViewById(R.id.tvDetailStatus);
        tvPrice = findViewById(R.id.tvDetailPrice);
        tvPriceCup = findViewById(R.id.tvDetailPriceCup);
        tvDesc = findViewById(R.id.tvDetailDesc);
        tvRejectReason = findViewById(R.id.tvDetailRejectReason);
        cardRejectReason = findViewById(R.id.cardRejectReasonDetail);
        btnBack = findViewById(R.id.btnBack);
    }

    private void loadServiceDetails() {
        db.collection("services").document(serviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ServiceModel service = documentSnapshot.toObject(ServiceModel.class);
                        if (service != null) {
                            displayService(service);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "فشل الاتصال: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void displayService(ServiceModel service) {
        tvName.setText(service.getNameAr());
        tvDesc.setText(service.getDescriptionAr());
        tvPrice.setText(String.format("%.2f ₪", service.getPrice()));
        tvPriceCup.setText(String.format("%.2f ₪", service.getPriceCup()));

        String status = service.getStatus() != null ? service.getStatus() : "pending";
        setupStatusBadge(status);

        if ("rejected".equals(status) && !TextUtils.isEmpty(service.getRejectReason())) {
            cardRejectReason.setVisibility(View.VISIBLE);
            tvRejectReason.setText(service.getRejectReason());
        } else {
            cardRejectReason.setVisibility(View.GONE);
        }
    }

    private void setupStatusBadge(String status) {
        switch (status) {
            case "pending":
                tvStatus.setText("قيد المراجعة");
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                break;
            case "approved":
                tvStatus.setText("مقبول");
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                break;
            case "rejected":
                tvStatus.setText("مرفوض");
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                break;
        }
    }
}
