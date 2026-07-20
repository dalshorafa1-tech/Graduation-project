package com.example.graduationproject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ServicesActivity extends AppCompatActivity {
    private CardView cardSingleOrder, cardMonthlySubscription, cardGroupInitiative;
    private TextView tvSingleTitle, tvSingleDesc, tvMonthlyTitle, tvGroupTitle;
    private FirebaseFirestore db;
    private String providerId, providerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);

        db = FirebaseFirestore.getInstance();

        // استقبال بيانات المزود
        providerId = getIntent().getStringExtra("provider_id");
        providerName = getIntent().getStringExtra("provider_name");

        cardSingleOrder = findViewById(R.id.cardSingleOrder);
        cardMonthlySubscription = findViewById(R.id.cardMonthlySubscription);
        cardGroupInitiative = findViewById(R.id.cardGroupInitiative);

        tvSingleTitle = findViewById(R.id.tvSingleOrderTitle);
        tvSingleDesc = findViewById(R.id.tvSingleOrderDesc);
        tvMonthlyTitle = findViewById(R.id.tvMonthlySubscriptionTitle);
        tvGroupTitle = findViewById(R.id.tvGroupInitiativeTitle);

        selectCard(cardSingleOrder);
        
        loadServicesFromFirestore();
        setupBottomNavigation();

        cardSingleOrder.setOnClickListener(v -> {
            selectCard(cardSingleOrder);
            v.postDelayed(() -> {
                Intent intent = new Intent(ServicesActivity.this, Order_Checkout_Activity.class);
                intent.putExtra("service_id", "1"); 
                intent.putExtra("provider_id", providerId);
                intent.putExtra("provider_name", providerName);
                startActivity(intent);
            }, 200);
        });

        cardMonthlySubscription.setOnClickListener(v -> {
            selectCard(cardMonthlySubscription);
            v.postDelayed(() -> {
                Intent intent = new Intent(ServicesActivity.this, Monthly_Subscription_Activity.class);
                intent.putExtra("service_id", "2");
                intent.putExtra("provider_id", providerId);
                intent.putExtra("provider_name", providerName);
                startActivity(intent);
            }, 200);
        });

        cardGroupInitiative.setOnClickListener(v -> {
            selectCard(cardGroupInitiative);
            v.postDelayed(() -> {
                Intent intent = new Intent(ServicesActivity.this, Group_Order_Activity.class);
                intent.putExtra("service_id", "3");
                intent.putExtra("provider_id", providerId);
                intent.putExtra("provider_name", providerName);
                startActivity(intent);
            }, 200);
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void loadServicesFromFirestore() {
        db.collection("services").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String nameAr = document.getString("name_ar");
                    String descAr = document.getString("description_ar");
                    String docId = document.getId();
                    
                    if ("1".equals(docId)) {
                        tvSingleTitle.setText(nameAr);
                        tvSingleDesc.setText(descAr);
                    } else if ("2".equals(docId)) {
                        tvMonthlyTitle.setText(nameAr);
                    } else if ("3".equals(docId)) {
                        tvGroupTitle.setText(nameAr);
                    }
                }
            } else {
                Log.e("Firebase", "Error getting services", task.getException());
            }
        });
    }

    private void selectCard(CardView selectedCard) {
        resetCards();
        selectedCard.setCardBackgroundColor(Color.parseColor("#0D63B3"));
        selectedCard.setCardElevation(8f);
        if (selectedCard == cardSingleOrder) {
            tvSingleTitle.setTextColor(Color.WHITE);
            tvSingleDesc.setTextColor(Color.parseColor("#93C5FD"));
        } else if (selectedCard == cardMonthlySubscription) {
            tvMonthlyTitle.setTextColor(Color.WHITE);
        } else if (selectedCard == cardGroupInitiative) {
            tvGroupTitle.setTextColor(Color.WHITE);
        }
    }

    private void resetCards() {
        cardSingleOrder.setCardBackgroundColor(Color.WHITE);
        cardMonthlySubscription.setCardBackgroundColor(Color.WHITE);
        cardGroupInitiative.setCardBackgroundColor(Color.WHITE);
        cardSingleOrder.setCardElevation(2f);
        cardMonthlySubscription.setCardElevation(2f);
        cardGroupInitiative.setCardElevation(2f);
        tvSingleTitle.setTextColor(Color.parseColor("#1E293B"));
        tvSingleDesc.setTextColor(Color.parseColor("#64748B"));
        tvMonthlyTitle.setTextColor(Color.parseColor("#1E293B"));
        tvGroupTitle.setTextColor(Color.parseColor("#1E293B"));
    }

    private void setupBottomNavigation() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            Intent intent = new Intent(this, MapExplorerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
        findViewById(R.id.navWallet).setOnClickListener(v -> startActivity(new Intent(this, WalletActivity.class)));
        findViewById(R.id.navOrders).setOnClickListener(v -> startActivity(new Intent(this, My_Orders_Activity.class)));
        findViewById(R.id.navProfile).setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
    }
}
