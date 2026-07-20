package com.example.graduationproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etPhoneNumber, etEmail, etAddress;
    private ShapeableImageView ivUserAvatar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private String userId;
    private Uri imageUri;

    private final ActivityResultLauncher<String> pickImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    ivUserAvatar.setImageURI(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        
        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        } else {
            finish();
            return;
        }

        initViews();
        loadCurrentData();
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etEmail = findViewById(R.id.etEmail);
        etAddress = findViewById(R.id.etAddress);
        ivUserAvatar = findViewById(R.id.ivUserAvatar);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
        
        findViewById(R.id.btnChangePhoto).setOnClickListener(v -> pickImage.launch("image/*"));
        
        findViewById(R.id.btnSaveChanges).setOnClickListener(v -> {
            if (imageUri != null) {
                uploadImageAndSave();
            } else {
                saveChanges(null);
            }
        });
    }

    private void loadCurrentData() {
        db.collection("providers").document(userId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                etFullName.setText(doc.getString("business_name"));
                etPhoneNumber.setText(doc.getString("phone"));
                etEmail.setText(mAuth.getCurrentUser().getEmail());
                etAddress.setText(doc.getString("location_name"));
                
                String img = doc.getString("profile_image");
                if (img != null && !img.isEmpty()) {
                    Glide.with(this).load(img).placeholder(R.drawable.user).into(ivUserAvatar);
                }
            }
        });
    }

    private void uploadImageAndSave() {
        StorageReference ref = storage.getReference().child("profile_images/" + userId + ".jpg");
        Toast.makeText(this, "جاري رفع الصورة...", Toast.LENGTH_SHORT).show();
        
        ref.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            ref.getDownloadUrl().addOnSuccessListener(uri -> {
                saveChanges(uri.toString());
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "فشل رفع الصورة", Toast.LENGTH_SHORT).show();
            saveChanges(null);
        });
    }

    private void saveChanges(String imageUrl) {
        String name = etFullName.getText().toString().trim();
        String phone = etPhoneNumber.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etFullName.setError("الاسم مطلوب");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("business_name", name);
        updates.put("phone", phone);
        updates.put("location_name", address);
        if (imageUrl != null) {
            updates.put("profile_image", imageUrl);
        }

        db.collection("providers").document(userId).update(updates)
            .addOnSuccessListener(aVoid -> {
                // مزامنة مع جدول المستخدمين أيضاً
                Map<String, Object> userUpdates = new HashMap<>();
                userUpdates.put("full_name", name);
                userUpdates.put("phone", phone);
                userUpdates.put("address", address);
                if (imageUrl != null) userUpdates.put("profile_image", imageUrl);
                db.collection("users").document(userId).update(userUpdates);

                Toast.makeText(this, "تم حفظ التعديلات بنجاح", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> Toast.makeText(this, "فشل الحفظ", Toast.LENGTH_SHORT).show());
    }
}
