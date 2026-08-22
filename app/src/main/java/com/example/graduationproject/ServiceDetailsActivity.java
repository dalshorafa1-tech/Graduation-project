package com.example.graduationproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class ServiceDetailsActivity extends AppCompatActivity {

    private TextView tvName, tvStatus, tvPrice, tvPriceCup, tvDesc, tvRejectReason;
    private TextView tvServiceType, tvProviderName, tvRegion, tvPhone;
    private ImageView btnBack;
    private MaterialButton btnRequestService;
    private MaterialCardView cardRejectReason;
    private FirebaseFirestore db;
    private String serviceId;
    private ServiceModel currentService;

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

        if (btnRequestService != null) {
            btnRequestService.setOnClickListener(v -> {
                if (currentService != null) {
                    Intent intent = new Intent(ServiceDetailsActivity.this, Order_Checkout_Activity.class);
                    intent.putExtra("service_id", currentService.getId());
                    intent.putExtra("provider_id", currentService.getProviderId());
                    intent.putExtra("provider_name", currentService.getProviderName());
                    startActivity(intent);
                }
            });
        }
    }

    private void initViews() {
        tvName = findViewById(R.id.tvDetailName);
        tvStatus = findViewById(R.id.tvDetailStatus);
        tvPrice = findViewById(R.id.tvDetailPrice);
        tvPriceCup = findViewById(R.id.tvDetailPriceCup);
        tvDesc = findViewById(R.id.tvDetailDesc);
        tvRejectReason = findViewById(R.id.tvDetailRejectReason);
        
        tvServiceType = findViewById(R.id.tvDetailServiceType);
        tvProviderName = findViewById(R.id.tvDetailProviderName);
        tvRegion = findViewById(R.id.tvDetailRegion);
        tvPhone = findViewById(R.id.tvDetailPhone);

        cardRejectReason = findViewById(R.id.cardRejectReasonDetail);
        btnBack = findViewById(R.id.btnBack);
        btnRequestService = findViewById(R.id.btnRequestService);
    }

    private void loadServiceDetails() {
        db.collection("services").document(serviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentService = documentSnapshot.toObject(ServiceModel.class);
                        if (currentService != null) {
                            currentService.setId(documentSnapshot.getId());
                            displayService(currentService);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "فشل الاتصال: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void displayService(ServiceModel service) {
        tvName.setText(service.getNameAr());
        tvDesc.setText(TextUtils.isEmpty(service.getDescriptionAr()) ? "لا يوجد وصف متاح لهذه الخدمة." : service.getDescriptionAr());
        
        tvPrice.setText(String.format(Locale.getDefault(), "%.2f ₪", service.getPrice()));
        tvPriceCup.setText(String.format(Locale.getDefault(), "%.2f ₪", service.getPriceCup()));

        tvServiceType.setText("نوع الخدمة: " + (service.getServiceType() != null ? service.getServiceType() : "غير محدد"));
        tvProviderName.setText(service.getProviderName() != null ? service.getProviderName() : "غير متوفر");
        tvRegion.setText("المنطقة: " + (service.getRegion() != null ? service.getRegion() : "غير محددة"));
        tvPhone.setText("رقم التواصل: " + (service.getProviderPhone() != null ? service.getProviderPhone() : "غير متوفر"));

        String status = service.getStatus() != null ? service.getStatus() : "pending";
        setupStatusBadge(status);

        // إظهار زر الطلب فقط إذا كانت الخدمة مقبولة
        if ("approved".equals(status)) {
            if (btnRequestService != null) btnRequestService.setVisibility(View.VISIBLE);
        } else {
            if (btnRequestService != null) btnRequestService.setVisibility(View.GONE);
        }

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
                tvStatus.setBackgroundResource(R.drawable.bg_alert_blue); // Assuming this is available
                break;
            case "approved":
                tvStatus.setText("مقبول");
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                break;
            case "rejected":
                tvStatus.setText("مرفوض");
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                break;
            default:
                tvStatus.setText(status);
                break;
        }
    }
}
