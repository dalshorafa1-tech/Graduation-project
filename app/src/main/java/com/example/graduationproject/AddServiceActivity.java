package com.example.graduationproject;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddServiceActivity extends AppCompatActivity {

    private EditText etServiceName, etServiceDesc, etPriceLitre, etPriceCup;
    private ImageView btnBack;
    private MaterialButton btnSave;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_service);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();

        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveService());
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etServiceName = findViewById(R.id.etServiceName);
        etServiceDesc = findViewById(R.id.etServiceDescription);
        etPriceLitre = findViewById(R.id.etPriceLitre);
        etPriceCup = findViewById(R.id.etPriceCup);
        btnSave = findViewById(R.id.btnSaveService);
    }

    private void saveService() {
        String name = etServiceName.getText().toString().trim();
        String desc = etServiceDesc.getText().toString().trim();
        String priceLitreStr = etPriceLitre.getText().toString().trim();
        String priceCupStr = etPriceCup.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etServiceName.setError("الاسم مطلوب");
            return;
        }
        if (TextUtils.isEmpty(priceLitreStr)) {
            etPriceLitre.setError("السعر مطلوب");
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("جاري الحفظ...");

        String uid = mAuth.getCurrentUser().getUid();

        ServiceModel service = new ServiceModel();
        service.setProviderId(uid);
        service.setNameAr(name);
        service.setDescriptionAr(desc);
        service.setPrice(Double.parseDouble(priceLitreStr));
        service.setPriceCup(TextUtils.isEmpty(priceCupStr) ? 0.0 : Double.parseDouble(priceCupStr));
        service.setStatus("pending");
        service.setActive(false);
        service.setCreatedAt(Timestamp.now());
        service.setProviderEmail(mAuth.getCurrentUser().getEmail());

        // جلب تفاصيل المزود لإدراجها في مستند الخدمة
        db.collection("providers").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        service.setProviderName(doc.getString("business_name"));
                        service.setProviderType(doc.getString("provider_type"));
                        service.setProviderPhone(doc.getString("phone"));
                        service.setProviderIdNumber(doc.getString("id_number"));
                        service.setMunicipalityCode(doc.getString("municipality_code"));
                        service.setRegion(doc.getString("location_name"));
                        
                        if (doc.contains("current_lat")) {
                            service.setLatitude(doc.getDouble("current_lat"));
                        }
                        if (doc.contains("current_lng")) {
                            service.setLongitude(doc.getDouble("current_lng"));
                        }
                    }
                    saveToFirestore(service);
                })
                .addOnFailureListener(e -> saveToFirestore(service));
    }

    private void saveToFirestore(ServiceModel service) {
        db.collection("services").add(service)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "تم إرسال الخدمة للأدمن للمراجعة", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    btnSave.setText("إضافة الخدمة");
                    Toast.makeText(this, "فشل الحفظ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
