package com.example.graduationproject;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddServiceDialog extends Dialog {

    private EditText etName, etDescription, etPrice, etPriceCup;
    private MaterialButton btnSave, btnCancel;
    private OnServiceAddedListener listener;
    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public interface OnServiceAddedListener {
        void onServiceAdded(ServiceModel service);
    }

    public AddServiceDialog(@NonNull Context context, OnServiceAddedListener listener) {
        super(context);
        this.listener = listener;
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // استخدام شاشة الإضافة الموجودة بالفعل
        setContentView(R.layout.activity_add_service);
        setCancelable(true);

        etName = findViewById(R.id.etServiceName);
        etDescription = findViewById(R.id.etServiceDescription);
        etPrice = findViewById(R.id.etPriceLitre); 
        etPriceCup = findViewById(R.id.etPriceCup); 
        
        btnSave = findViewById(R.id.btnSaveService);
        btnCancel = findViewById(R.id.btnBack);

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                String name = etName.getText().toString().trim();
                String priceStr = etPrice.getText().toString().trim();
                String priceCupStr = etPriceCup != null ? etPriceCup.getText().toString().trim() : "";

                if (TextUtils.isEmpty(name)) {
                    etName.setError("الاسم مطلوب");
                    return;
                }
                if (TextUtils.isEmpty(priceStr)) {
                    etPrice.setError("السعر مطلوب");
                    return;
                }

                btnSave.setEnabled(false);
                btnSave.setText("جاري التجهيز...");

                // إنشاء كائن الخدمة وتعبئة البيانات الأساسية
                ServiceModel service = new ServiceModel();
                service.setNameAr(name);
                service.setDescriptionAr(etDescription.getText().toString().trim());
                service.setPrice(Double.parseDouble(priceStr));
                service.setPriceCup(TextUtils.isEmpty(priceCupStr) ? 0.0 : Double.parseDouble(priceCupStr));
                
                service.setProviderId(mAuth.getUid());
                if (mAuth.getCurrentUser() != null) {
                    service.setProviderEmail(mAuth.getCurrentUser().getEmail());
                }
                
                service.setStatus("pending");
                service.setActive(false);
                service.setCreatedAt(Timestamp.now());

                // جلب بيانات المزود لضمان ظهورها كاملة عند الأدمن
                db.collection("providers").document(mAuth.getUid()).get()
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
                            
                            if (listener != null) {
                                listener.onServiceAdded(service);
                            }
                            dismiss();
                        })
                        .addOnFailureListener(e -> {
                            // في حال فشل الجلب، نمرر ما لدينا لضمان عدم توقف العملية
                            if (listener != null) {
                                listener.onServiceAdded(service);
                            }
                            dismiss();
                        });
            });
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dismiss());
        }
    }
}
