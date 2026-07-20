package com.example.graduationproject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class Monthly_Subscription_Activity extends AppCompatActivity {

    private CardView planBasic, planStandard, planFamily;
    private CardView freqWeekly, freqBiWeekly;
    private CardView[] dayCards;
    private TextView[] dayTexts;
    private TextView tvSummaryPrice, tvBottomPrice, tvSelectedTime;
    
    private int selectedPlanPrice = 45;
    private String selectedPlanName = "الباقة الأساسية";
    private int selectedQuantity = 1000; 
    private String selectedFrequency = "كل أسبوع";
    private String selectedDay = "السبت";
    
    private String providerId, providerName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_subscription);

        // استقبال بيانات المزود
        providerId = getIntent().getStringExtra("provider_id");
        providerName = getIntent().getStringExtra("provider_name");

        ImageView btnBack = findViewById(R.id.btnBack);
        planBasic = findViewById(R.id.planBasic);
        planStandard = findViewById(R.id.planStandard);
        planFamily = findViewById(R.id.planFamily);
        
        freqWeekly = findViewById(R.id.freqWeekly);
        freqBiWeekly = findViewById(R.id.freqBiWeekly);
        
        tvSummaryPrice = findViewById(R.id.tvSummaryPrice);
        tvBottomPrice = findViewById(R.id.tvBottomPrice);
        tvSelectedTime = findViewById(R.id.tvSelectedTime);
        
        CardView btnConfirmSubscription = findViewById(R.id.btnConfirmSubscription);
        CardView btnSelectTime = findViewById(R.id.btnSelectTime);

        dayCards = new CardView[]{
                findViewById(R.id.daySat), findViewById(R.id.daySun), findViewById(R.id.dayMon),
                findViewById(R.id.dayTue), findViewById(R.id.dayWed), findViewById(R.id.dayThu)
        };
        dayTexts = new TextView[]{
                findViewById(R.id.tvSat), findViewById(R.id.tvSun), findViewById(R.id.tvMon),
                findViewById(R.id.tvTue), findViewById(R.id.tvWed), findViewById(R.id.tvThu)
        };

        btnBack.setOnClickListener(v -> finish());

        planBasic.setOnClickListener(v -> selectPlan(45, "الباقة الأساسية", 1000, planBasic));
        planStandard.setOnClickListener(v -> selectPlan(90, "الباقة القياسية", 2500, planStandard));
        planFamily.setOnClickListener(v -> selectPlan(160, "الباقة العائلية", 5000, planFamily));

        freqWeekly.setOnClickListener(v -> selectFrequency("كل أسبوع", freqWeekly, freqBiWeekly));
        freqBiWeekly.setOnClickListener(v -> selectFrequency("كل أسبوعين", freqBiWeekly, freqWeekly));

        for (int i = 0; i < dayCards.length; i++) {
            final int index = i;
            dayCards[i].setOnClickListener(v -> selectDay(index));
        }

        btnSelectTime.setOnClickListener(v -> {
            if (tvSelectedTime.getText().toString().contains("الصباحية")) {
                tvSelectedTime.setText("🕒  الفترة المسائية (1:00 م - 5:00 م)");
            } else {
                tvSelectedTime.setText("🕒  الفترة الصباحية (8:00 ص - 12:00 م)");
            }
        });

        btnConfirmSubscription.setOnClickListener(v -> {
            Intent intent = new Intent(this, Review_Order_Activity.class);
            intent.putExtra("service_id", "subscription_monthly"); 
            intent.putExtra("provider_id", providerId);
            intent.putExtra("provider_name", providerName);
            intent.putExtra("quantity", selectedQuantity);
            intent.putExtra("unit", "لتر (اشتراك)");
            intent.putExtra("address", "موقع المشترك المسجل");
            intent.putExtra("total_price_from_plan", (double)selectedPlanPrice);
            intent.putExtra("notes", "اشتراك شهري: " + selectedPlanName + " - " + selectedFrequency);
            intent.putExtra("scheduledTime", selectedDay + " | " + tvSelectedTime.getText().toString().replace("🕒  ", ""));
            intent.putExtra("lat", 31.51); 
            intent.putExtra("lng", 34.44); 
            startActivity(intent);
        });

        setupBottomNavigation();
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

    private void selectPlan(int price, String name, int qty, CardView selectedCard) {
        selectedPlanPrice = price;
        selectedPlanName = name;
        selectedQuantity = qty;
        planBasic.setCardBackgroundColor(Color.WHITE);
        planStandard.setCardBackgroundColor(Color.WHITE);
        planFamily.setCardBackgroundColor(Color.WHITE);
        selectedCard.setCardBackgroundColor(Color.parseColor("#E0F2FE"));
        tvSummaryPrice.setText(price + ".00 ₪");
        tvBottomPrice.setText(price + ".00 ₪");
    }

    private void selectFrequency(String freq, CardView selected, CardView unselected) {
        selectedFrequency = freq;
        selected.setCardBackgroundColor(Color.WHITE);
        if (selected.getChildCount() > 0 && selected.getChildAt(0) instanceof TextView) {
            ((TextView)selected.getChildAt(0)).setTextColor(Color.parseColor("#0D63B3"));
        }
        unselected.setCardBackgroundColor(Color.TRANSPARENT);
        if (unselected.getChildCount() > 0 && unselected.getChildAt(0) instanceof TextView) {
            ((TextView)unselected.getChildAt(0)).setTextColor(Color.parseColor("#64748B"));
        }
    }

    private void selectDay(int index) {
        selectedDay = dayTexts[index].getText().toString();
        for (int i = 0; i < dayCards.length; i++) {
            dayCards[i].setCardBackgroundColor(Color.WHITE);
            dayTexts[i].setTextColor(Color.parseColor("#64748B"));
        }
        dayCards[index].setCardBackgroundColor(Color.parseColor("#0D63B3"));
        dayTexts[index].setTextColor(Color.WHITE);
    }
}
