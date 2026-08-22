package com.example.graduationproject;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
    private RadioGroup rgServiceType;
    private RadioButton rbTruck, rbWell, rbStorage;
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

        // جلب معرف الخدمة من الـ Intent
        serviceId = getIntent().getStringExtra("service_id");
        if (serviceId == null) {
            Toast.makeText(this, "خطأ: لم يتم العثور على بيانات الخدمة", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadServiceData();

        btnBack.setOnClickListener(v -> finish());
        btnSaveEdits.setOnClickListener(v -> validateAndSave());
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvServiceName = findViewById(R.id.tvServiceName);
        tvServiceId = findViewById(R.id.tvServiceId);
        etPriceLitre = findViewById(R.id.etPriceLitre);
        etPriceCup = findViewById(R.id.etPriceCup);
        rgServiceType = findViewById(R.id.rgEditServiceType);
        rbTruck = findViewById(R.id.rbEditTruck);
        rbWell = findViewById(R.id.rbEditWell);
        rbStorage = findViewById(R.id.rbEditStorage);
        switchAvailability = findViewById(R.id.switchAvailability);
        btnSaveEdits = findViewById(R.id.btnSaveEdits);
    }

    private void loadServiceData() {
        db.collection("services").document(serviceId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                currentService = doc.toObject(ServiceModel.class);
                if (currentService != null) {
                    tvServiceName.setText(currentService.getNameAr());
                    tvServiceId.setText("معرف الخدمة: #" + serviceId.substring(0, Math.min(serviceId.length(), 6)));
                    etPriceLitre.setText(String.valueOf(currentService.getPrice()));
                    etPriceCup.setText(String.valueOf(currentService.getPriceCup()));
                    switchAvailability.setChecked(currentService.isActive());
                    
                    // تحديد نوع الخدمة الحالي في الـ RadioGroup
                    String type = currentService.getServiceType();
                    if ("صهريج".equals(type)) rbTruck.setChecked(true);
                    else if ("آبار".equals(type)) rbWell.setChecked(true);
                    else if ("خزانات".equals(type)) rbStorage.setChecked(true);
                }
            } else {
                Toast.makeText(this, "الخدمة غير موجودة", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "فشل تحميل البيانات", Toast.LENGTH_SHORT).show();
        });
    }

    private void validateAndSave() {
        String priceStr = etPriceLitre.getText().toString().trim();
        String priceCupStr = etPriceCup.getText().toString().trim();

        // الحصول على النوع المختار
        int selectedId = rgServiceType.getCheckedRadioButtonId();
        String selectedType = "";
        if (selectedId != -1) {
            RadioButton rb = findViewById(selectedId);
            selectedType = rb.getText().toString();
        }

        // التحقق من المدخلات
        if (TextUtils.isEmpty(priceStr)) {
            etPriceLitre.setError("يرجى إدخال سعر اللتر");
            return;
        }

        if (TextUtils.isEmpty(selectedType)) {
            Toast.makeText(this, "يرجى اختيار نوع الخدمة", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            double priceCup = TextUtils.isEmpty(priceCupStr) ? 0.0 : Double.parseDouble(priceCupStr);

            saveChanges(price, priceCup, selectedType);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "يرجى إدخال أرقام صحيحة للأسعار", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveChanges(double price, double priceCup, String serviceType) {
        btnSaveEdits.setEnabled(false);
        btnSaveEdits.setText("جاري إرسال التعديلات...");

        Map<String, Object> updates = new HashMap<>();
        updates.put("price", price);
        updates.put("priceCup", priceCup);
        updates.put("service_type", serviceType);
        
        // عند التعديل، تعود الخدمة للمراجعة وتتوقف مؤقتاً
        updates.put("status", "pending");
        updates.put("isActive", false); 
        updates.put("updatedAt", com.google.firebase.Timestamp.now());
        updates.put("isEdited", true); // علامة للأدمن أن هذه تعديلات وليست خدمة جديدة

        db.collection("services").document(serviceId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // إضافة إشعار للأدمن في قاعدة البيانات
                    sendAdminNotification();
                    
                    Toast.makeText(this, "تم إرسال التعديلات للأدمن للمراجعة ✅ سيتم إشعارك عند القبول", Toast.LENGTH_LONG).show();
                    finish(); 
                })
                .addOnFailureListener(e -> {
                    btnSaveEdits.setEnabled(true);
                    btnSaveEdits.setText("حفظ التعديلات");
                    Toast.makeText(this, "فشل الإرسال: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void sendAdminNotification() {
        Map<String, Object> notification = new HashMap<>();
        notification.put("title", "طلب تعديل خدمة");
        notification.put("message", "قام المزود " + (currentService != null ? currentService.getProviderName() : "") + " بتعديل بيانات الخدمة: " + (currentService != null ? currentService.getNameAr() : ""));
        notification.put("type", "service_edit");
        notification.put("serviceId", serviceId);
        notification.put("timestamp", com.google.firebase.Timestamp.now());
        notification.put("read", false);

        db.collection("admin_notifications").add(notification);
    }
}
