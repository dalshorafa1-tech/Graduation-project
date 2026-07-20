package com.example.graduationproject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Payment_Method_Activity extends AppCompatActivity {

    private MaterialCardView cardWallet, cardCash, cardPhone, cardBank;
    private RadioButton rbWallet, rbCash, rbPhone, rbBank;
    private CardView btnConfirmPayment;
    private String selectedMethod = "wallet";
    private String orderId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_method);

        db = FirebaseFirestore.getInstance();
        orderId = getIntent().getStringExtra("order_id");

        initViews();
        setupClickListeners();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnConfirmPayment.setOnClickListener(v -> {
            if (orderId != null) {
                confirmPaymentInFirebase();
            } else {
                // Fallback for testing
                navigateToTracking();
            }
        });
    }

    private void confirmPaymentInFirebase() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("payment_method", selectedMethod);
        updates.put("is_paid", !selectedMethod.equals("cash"));
        
        db.collection("orders").document(orderId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "تم تأكيد وسيلة الدفع: " + getArabicMethodName(), Toast.LENGTH_SHORT).show();
                    navigateToTracking();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "فشل في تحديث بيانات الدفع", Toast.LENGTH_SHORT).show());
    }

    private void navigateToTracking() {
        Intent intent = new Intent(Payment_Method_Activity.this, Track_Driver_Activity.class);
        intent.putExtra("order_id", orderId);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void initViews() {
        cardWallet = findViewById(R.id.cardWallet);
        cardCash = findViewById(R.id.cardCash);
        cardPhone = findViewById(R.id.cardPhone);
        cardBank = findViewById(R.id.cardBank);
        rbWallet = findViewById(R.id.rbWallet);
        rbCash = findViewById(R.id.rbCash);
        rbPhone = findViewById(R.id.rbPhone);
        rbBank = findViewById(R.id.rbBank);
        btnConfirmPayment = findViewById(R.id.btnConfirmPaymentAction);
    }

    private void setupClickListeners() {
        cardWallet.setOnClickListener(v -> updateSelection("wallet"));
        cardCash.setOnClickListener(v -> updateSelection("cash"));
        cardPhone.setOnClickListener(v -> updateSelection("phone"));
        cardBank.setOnClickListener(v -> updateSelection("bank"));
    }

    private void updateSelection(String method) {
        selectedMethod = method;
        resetCardStyles();
        switch (method) {
            case "wallet": highlightCard(cardWallet, rbWallet); break;
            case "cash": highlightCard(cardCash, rbCash); break;
            case "phone": highlightCard(cardPhone, rbPhone); break;
            case "bank": highlightCard(cardBank, rbBank); break;
        }
    }

    private void highlightCard(MaterialCardView card, RadioButton rb) {
        card.setStrokeColor(Color.parseColor("#0069B4"));
        card.setStrokeWidth(5);
        rb.setChecked(true);
    }

    private void resetCardStyles() {
        MaterialCardView[] cards = {cardWallet, cardCash, cardPhone, cardBank};
        RadioButton[] rbs = {rbWallet, rbCash, rbPhone, rbBank};
        for (int i = 0; i < cards.length; i++) {
            cards[i].setStrokeColor(Color.parseColor("#E2E8F0"));
            cards[i].setStrokeWidth(2);
            rbs[i].setChecked(false);
        }
    }

    private String getArabicMethodName() {
        switch (selectedMethod) {
            case "wallet": return "محفظة إرواء";
            case "cash": return "الدفع نقداً";
            case "phone": return "رصيد الهاتف";
            case "bank": return "باي بال";
            default: return "";
        }
    }
}
