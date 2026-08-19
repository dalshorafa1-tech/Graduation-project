package com.example.graduationproject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

        if (cardSingleOrder != null) {
            selectCard(cardSingleOrder);
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
        }

        if (cardMonthlySubscription != null) {
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
        }

        if (cardGroupInitiative != null) {
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
        }

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        loadServicesFromFirestore();
        setupBottomNavigation();
    }

    private void loadServicesFromFirestore() {
        db.collection("services").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String nameAr = document.getString("name_ar");
                    String descAr = document.getString("description_ar");
                    String docId = document.getId();
                    
                    if ("1".equals(docId)) {
                        if (tvSingleTitle != null) tvSingleTitle.setText(nameAr);
                        if (tvSingleDesc != null) tvSingleDesc.setText(descAr);
                    } else if ("2".equals(docId)) {
                        if (tvMonthlyTitle != null) tvMonthlyTitle.setText(nameAr);
                    } else if ("3".equals(docId)) {
                        if (tvGroupTitle != null) tvGroupTitle.setText(nameAr);
                    }
                }
            } else {
                Log.e("Firebase", "Error getting services", task.getException());
            }
        });
    }

    private void selectCard(CardView selectedCard) {
        resetCards();
        if (selectedCard != null) {
            selectedCard.setCardBackgroundColor(Color.parseColor("#0D63B3"));
            selectedCard.setCardElevation(8f);
            if (selectedCard == cardSingleOrder) {
                if (tvSingleTitle != null) tvSingleTitle.setTextColor(Color.WHITE);
                if (tvSingleDesc != null) tvSingleDesc.setTextColor(Color.parseColor("#93C5FD"));
            } else if (selectedCard == cardMonthlySubscription) {
                if (tvMonthlyTitle != null) tvMonthlyTitle.setTextColor(Color.WHITE);
            } else if (selectedCard == cardGroupInitiative) {
                if (tvGroupTitle != null) tvGroupTitle.setTextColor(Color.WHITE);
            }
        }
    }

    private void resetCards() {
        if (cardSingleOrder != null) {
            cardSingleOrder.setCardBackgroundColor(Color.WHITE);
            cardSingleOrder.setCardElevation(2f);
        }
        if (cardMonthlySubscription != null) {
            cardMonthlySubscription.setCardBackgroundColor(Color.WHITE);
            cardMonthlySubscription.setCardElevation(2f);
        }
        if (cardGroupInitiative != null) {
            cardGroupInitiative.setCardBackgroundColor(Color.WHITE);
            cardGroupInitiative.setCardElevation(2f);
        }
        
        if (tvSingleTitle != null) tvSingleTitle.setTextColor(Color.parseColor("#1E293B"));
        if (tvSingleDesc != null) tvSingleDesc.setTextColor(Color.parseColor("#64748B"));
        if (tvMonthlyTitle != null) tvMonthlyTitle.setTextColor(Color.parseColor("#1E293B"));
        if (tvGroupTitle != null) tvGroupTitle.setTextColor(Color.parseColor("#1E293B"));
    }

    private void setupBottomNavigation() {
        View navHome = findViewById(R.id.navHome);
        View navOrders = findViewById(R.id.navOrders);
        View navProfile = findViewById(R.id.navProfile);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, MapExplorerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            });
        }
        if (navOrders != null) {
            navOrders.setOnClickListener(v -> startActivity(new Intent(this, My_Orders_Activity.class)));
        }
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> startActivity(new Intent(this, Profile.class)));
        }
    }
}