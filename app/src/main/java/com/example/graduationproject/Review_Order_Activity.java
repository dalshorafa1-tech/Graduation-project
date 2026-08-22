package com.example.graduationproject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Review_Order_Activity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "ReviewOrder";
    private MapView mapView;
    private MapboxMap mapboxMap;
    private LatLng deliveryLoc;

    private int quantity;
    private String unit, address, notes, scheduledTime;
    private String providerId, providerName, serviceId;
    private double totalPrice;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private TextView tvServiceName, tvLocationMain, tvOrderNotes, tvWaterPrice, tvTotalPriceMain, tvFooterPriceText;
    private ImageView ivPaymentReceipt;
    private Uri filePath;

    // مُشغل اختيار الصورة من المعرض
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    filePath = uri;
                    // عرض الصورة المختارة في الواجهة
                    ivPaymentReceipt.setImageURI(uri);
                    ivPaymentReceipt.setPadding(0, 0, 0, 0);
                    ivPaymentReceipt.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    ivPaymentReceipt.setImageTintList(null); // إزالة اللون الرمادي الافتراضي
                    
                    TextView tvUploadText = findViewById(R.id.tvUploadText);
                    if (tvUploadText != null) tvUploadText.setVisibility(View.GONE);
                    
                    // إتاحة تكبير الصورة عند النقر عليها
                    ivPaymentReceipt.setOnClickListener(v -> showFullImage(uri));
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this);
        setContentView(R.layout.activity_review_order);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        
        // ربط التخزين بالدلو (Bucket) الصحيح لتجنب خطأ "Object does not exist"
        try {
            storage = FirebaseStorage.getInstance("gs://erwaa-app-c4e5c.firebasestorage.app");
        } catch (Exception e) {
            storage = FirebaseStorage.getInstance();
        }
        storageReference = storage.getReference();

        initViews(savedInstanceState);
        loadIntentData();
    }

    private void initViews(Bundle savedInstanceState) {
        tvServiceName = findViewById(R.id.tvServiceName);
        tvLocationMain = findViewById(R.id.tvLocationMain);
        tvOrderNotes = findViewById(R.id.tvOrderNotes);
        tvWaterPrice = findViewById(R.id.tvWaterPrice);
        tvTotalPriceMain = findViewById(R.id.tvTotalPriceMain);
        tvFooterPriceText = findViewById(R.id.tvFooterPriceText);
        ivPaymentReceipt = findViewById(R.id.ivPaymentReceipt);
        mapView = findViewById(R.id.mapViewReview);

        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnConfirmAndSend).setOnClickListener(v -> uploadImageAndSaveOrder());
        findViewById(R.id.btnUploadReceipt).setOnClickListener(v -> pickImageLauncher.launch("image/*"));
    }

    private void loadIntentData() {
        Intent data = getIntent();
        serviceId = data.getStringExtra("service_id");
        providerId = data.getStringExtra("provider_id");
        providerName = data.getStringExtra("provider_name");
        quantity = data.getIntExtra("quantity", 500);
        unit = data.getStringExtra("unit");
        address = data.getStringExtra("address");
        notes = data.getStringExtra("notes");
        scheduledTime = data.getStringExtra("scheduledTime");

        double lat = data.getDoubleExtra("lat", 31.516);
        double lng = data.getDoubleExtra("lng", 34.448);
        deliveryLoc = new LatLng(lat, lng);

        if (data.hasExtra("total_price_from_plan")) {
            totalPrice = data.getDoubleExtra("total_price_from_plan", 0.0);
            updateUI("اشتراك مياه شهري");
        } else if (serviceId != null && !serviceId.isEmpty()) {
            fetchRealDataAndCalculate();
        }
    }

    private void showFullImage(Uri uri) {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_full_image);
        
        ImageView imageView = dialog.findViewById(R.id.ivFullImage);
        imageView.setImageURI(uri);
        
        dialog.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void uploadImageAndSaveOrder() {
        if (filePath == null) {
            Toast.makeText(this, "يرجى رفع صورة إشعار الدفع أولاً", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("جاري إرسال الطلب...");
        progressDialog.setMessage("يرجى رفع الصورة...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String fileName = "receipts/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference ref = storageReference.child(fileName);
        
        ref.putFile(filePath)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    progressDialog.dismiss();
                    // هنا يتم تخزين الرابط فقط في الداتابيز
                    saveOrderToFirebase(uri.toString());
                }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e(TAG, "Upload error: " + e.getMessage());
                    Toast.makeText(this, "فشل رفع الصورة، يرجى التحقق من الاتصال", Toast.LENGTH_LONG).show();
                });
    }

    private void saveOrderToFirebase(String receiptUrl) {
        if (mAuth.getCurrentUser() == null) return;

        Map<String, Object> order = new HashMap<>();
        order.put("customer_id", mAuth.getUid());
        order.put("provider_id", providerId);
        order.put("service_id", serviceId);
        order.put("provider_name", providerName);
        order.put("quantity", quantity);
        order.put("unit", unit);
        order.put("address_details", address);
        order.put("notes", notes);
        order.put("scheduled_time", scheduledTime);
        order.put("total_price", totalPrice);
        order.put("status", "pending");
        order.put("receipt_url", receiptUrl); // الرابط المخزن في Firestore
        order.put("delivery_lat", deliveryLoc.getLatitude());
        order.put("delivery_lng", deliveryLoc.getLongitude());
        order.put("created_at", com.google.firebase.Timestamp.now());

        db.collection("orders").add(order)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, "تم إرسال الطلب بنجاح!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "خطأ في حفظ البيانات", Toast.LENGTH_SHORT).show());
    }

    @Override public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(new Style.Builder().fromUri("https://tiles.openfreemap.org/styles/bright"), style -> {
            if (deliveryLoc != null) {
                mapboxMap.addMarker(new MarkerOptions().position(deliveryLoc).title("موقع التوصيل"));
                mapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(deliveryLoc, 14));
            }
        });
    }

    private void fetchRealDataAndCalculate() {
        db.collection("services").document(serviceId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String name = doc.getString("nameAr");
                double price = doc.contains("price") ? doc.getDouble("price") : 0.05;
                totalPrice = (quantity * price) + 10.0;
                updateUI(name);
            }
        });
    }

    private void updateUI(String serviceName) {
        tvServiceName.setText(serviceName);
        tvLocationMain.setText(address != null ? address : "الموقع المختار");
        tvOrderNotes.setText(notes != null && !notes.isEmpty() ? notes : "لا توجد ملاحظات");
        tvWaterPrice.setText(String.format("%.2f ₪", Math.max(0, totalPrice - 10)));
        tvTotalPriceMain.setText(String.format("%.2f ₪", totalPrice));
        tvFooterPriceText.setText(String.format("%.2f ₪", totalPrice));
    }

    @Override protected void onStart() { super.onStart(); if (mapView != null) mapView.onStart(); }
    @Override protected void onResume() { super.onResume(); if (mapView != null) mapView.onResume(); }
    @Override protected void onPause() { super.onPause(); if (mapView != null) mapView.onPause(); }
    @Override protected void onStop() { super.onStop(); if (mapView != null) mapView.onStop(); }
    @Override protected void onDestroy() { if (mapView != null) mapView.onDestroy(); super.onDestroy(); }
    @Override protected void onSaveInstanceState(@NonNull Bundle outState) { super.onSaveInstanceState(outState); if (mapView != null) mapView.onSaveInstanceState(outState); }
}
