package com.example.graduationproject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class UserTypeActivity extends AppCompatActivity {

    private int selectedType = 0; // 1 for Client, 2 for Provider

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_type);

        ImageView btnBack = findViewById(R.id.btnBack);
        CardView btnNext = findViewById(R.id.btnNext);
        TextView tvNext = btnNext.findViewById(android.R.id.text1); // Or finding child
        // If btnNext contains a TextView without an ID, I'll find it by class or just find the one inside
        TextView btnNextText = (TextView) btnNext.getChildAt(0);

        CardView cardClient = findViewById(R.id.cardClient);
        CardView cardProvider = findViewById(R.id.cardProvider);

        cardClient.setOnClickListener(v -> {
            selectedType = 1;
            cardClient.setCardBackgroundColor(Color.parseColor("#E0F2F1"));
            cardProvider.setCardBackgroundColor(Color.WHITE);
            btnNext.setCardBackgroundColor(Color.parseColor("#0069B4"));
            btnNextText.setTextColor(Color.WHITE);
        });

        cardProvider.setOnClickListener(v -> {
            selectedType = 2;
            cardProvider.setCardBackgroundColor(Color.parseColor("#E0F2F1"));
            cardClient.setCardBackgroundColor(Color.WHITE);
            btnNext.setCardBackgroundColor(Color.parseColor("#0069B4"));
            btnNextText.setTextColor(Color.WHITE);
        });

        btnBack.setOnClickListener(v -> finish());

        findViewById(R.id.btnAdminEntry).setOnClickListener(v -> {
            Intent intent = new Intent(UserTypeActivity.this, AdminLoginActivity.class);
            startActivity(intent);
        });

        btnNext.setOnClickListener(v -> {
            if (selectedType == 1) {
                Intent intent = new Intent(UserTypeActivity.this, RegisterActivity.class);
                startActivity(intent);
            } else if (selectedType == 2) {
                Intent intent = new Intent(UserTypeActivity.this, RegisterProviderActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "يرجى اختيار نوع الحساب للمتابعة", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
