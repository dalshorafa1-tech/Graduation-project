package com.example.graduationproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Rate_Service_Activity extends AppCompatActivity {
    private RatingBar ratingBar;
    private EditText etNotes;
    private TextView tvStationName, tvOrderNumber, tvOrderDate, tvQuantity, tvPrice;
    private String orderId;
    private String providerId;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_service);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        ImageView btnBack = findViewById(R.id.btnBack);
        ratingBar = findViewById(R.id.ratingBar);
        etNotes = findViewById(R.id.etNotes);
        tvStationName = findViewById(R.id.tvStationName);
        tvOrderNumber = findViewById(R.id.tvOrderNumber);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvPrice = findViewById(R.id.tvPrice);

        LinearLayout btnSubmitRating = findViewById(R.id.btnSubmitRating);
        LinearLayout btnSkip = findViewById(R.id.btnSkip);

        // Get data from intent
        if (getIntent() != null) {
            orderId = getIntent().getStringExtra("order_uuid"); // Match intent key from previous code
            providerId = getIntent().getStringExtra("provider_id");

            String stationName = getIntent().getStringExtra("station_name");
            String orderNum = getIntent().getStringExtra("order_number");
            String orderDate = getIntent().getStringExtra("order_date");
            String quantity = getIntent().getStringExtra("quantity");
            String price = getIntent().getStringExtra("price");

            if (stationName != null) tvStationName.setText(stationName);
            if (orderNum != null) tvOrderNumber.setText(orderNum);
            if (orderDate != null) tvOrderDate.setText(orderDate);
            if (quantity != null) tvQuantity.setText(quantity);
            if (price != null) tvPrice.setText(price);
        }

        btnBack.setOnClickListener(v -> finish());
        
        btnSkip.setOnClickListener(v -> {
            navigateToMain();
        });

        btnSubmitRating.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            if (rating == 0) {
                Toast.makeText(this, "يرجى اختيار التقييم أولاً", Toast.LENGTH_SHORT).show();
                return;
            }
            performSubmitRating((int) rating);
        });
    }

    private void performSubmitRating(int ratingValue) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "يرجى تسجيل الدخول أولاً", Toast.LENGTH_SHORT).show();
            return;
        }

        if (orderId == null) {
            Toast.makeText(this, "خطأ: لم يتم العثور على معرّف الطلب الصحيح", Toast.LENGTH_SHORT).show();
            return;
        }

        String customerId = mAuth.getCurrentUser().getUid();
        String comment = etNotes.getText().toString().trim();

        Map<String, Object> rating = new HashMap<>();
        rating.put("order_id", orderId);
        rating.put("customer_id", customerId);
        rating.put("provider_id", providerId);
        rating.put("rating", ratingValue);
        rating.put("comment", comment);
        rating.put("created_at", FieldValue.serverTimestamp());

        db.collection("ratings").add(rating)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(Rate_Service_Activity.this, "تم إرسال تقييمك بنجاح!", Toast.LENGTH_LONG).show();
                    updateProviderRating(providerId, ratingValue);
                    navigateToMain();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase_Rating", "Error", e);
                    Toast.makeText(Rate_Service_Activity.this, "فشل إرسال التقييم", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProviderRating(String providerId, int newRating) {
        if (providerId == null) return;
        
        // This is a simplified rating update logic. 
        // In a real app, you might want to recalculate based on all ratings.
        db.collection("providers").document(providerId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Double currentRating = documentSnapshot.getDouble("rating");
                if (currentRating == null) currentRating = 4.5;
                
                double updatedRating = (currentRating + newRating) / 2.0; // Simple average for demo
                db.collection("providers").document(providerId).update("rating", updatedRating);
            }
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(Rate_Service_Activity.this, MapExplorerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
