package com.example.graduationproject;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditServiceActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvServiceName, tvServiceId;
    private EditText etPriceLitre, etPriceCup;
    private SwitchMaterial switchAvailability;
    private MaterialButton btnSaveEdits;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String serviceId;
    private ServiceModel currentService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_service);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        serviceId = getIntent().getStringExtra("service_id");
        if (serviceId == null) {
            finish();
            return;
        }

        initViews();
        loadServiceData();

        btnBack.setOnClickListener(v -> finish());
        btnSaveEdits.setOnClickListener(v -> saveChanges());
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvServiceName = findViewById(R.id.tvServiceName);
        tvServiceId = findViewById(R.id.tvServiceId);
        etPriceLitre = findViewById(R.id.etPriceLitre);
        etPriceCup = findViewById(R.id.etPriceCup);
        switchAvailability = findViewById(R.id.switchAvailability);
        btnSaveEdits = findViewById(R.id.btnSaveEdits);
    }

    private void loadServiceData() {
        db.collection("services").document(serviceId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                currentService = doc.toObject(ServiceModel.class);
                if (currentService != null) {
                    tvServiceName.setText(currentService.getNameAr());
                    tvServiceId.setText("معرف الخدمة: #" + serviceId.substring(0, 6));
                    etPriceLitre.setText(String.valueOf(currentService.getPrice()));
                    if (currentService.getPriceCup() > 0) {
                        etPriceCup.setText(String.valueOf(currentService.getPriceCup()));
                    }
                    switchAvailability.setChecked(currentService.isActive());
                }
            }
        });
    }

    private void saveChanges() {
        String priceStr = etPriceLitre.getText().toString().trim();
        String priceCupStr = etPriceCup.getText().toString().trim();
        
        if (TextUtils.isEmpty(priceStr)) {
            etPriceLitre.setError("السعر مطلوب");
            return;
        }

        btnSaveEdits.setEnabled(false);
        btnSaveEdits.setText("جاري إرسال التعديلات...");

        Map<String, Object> updates = new HashMap<>();
        updates.put("price", Double.parseDouble(priceStr));
        updates.put("priceCup", TextUtils.isEmpty(priceCupStr) ? 0.0 : Double.parseDouble(priceCupStr));
        
        // عند التعديل، نغير الحالة لتنتظر موافقة الأدمن مرة أخرى
        updates.put("status", "pending");
        updates.put("isActive", false); 

        applyUpdatesToFirestore(updates);
    }

    private void applyUpdatesToFirestore(Map<String, Object> updates) {
        db.collection("services").document(serviceId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "تم إرسال التعديلات للأدمن للمراجعة", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSaveEdits.setEnabled(true);
                    btnSaveEdits.setText("حفظ التعديلات");
                    Toast.makeText(this, "فشل الحفظ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
