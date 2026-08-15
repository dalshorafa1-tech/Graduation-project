package com.example.graduationproject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

public class UserTypeActivity extends AppCompatActivity {

    private int selectedType = 0; // 1 for Client, 2 for Provider, 3 for Initiator

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_type);

        ImageView btnBack = findViewById(R.id.btnBack);
        MaterialCardView btnNext = findViewById(R.id.btnNext);
        
        TextView btnNextText = null;
        if (btnNext.getChildAt(0) instanceof TextView) {
            btnNextText = (TextView) btnNext.getChildAt(0);
        } else if (btnNext.getChildAt(0) instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) btnNext.getChildAt(0);
            for (int i = 0; i < group.getChildCount(); i++) {
                if (group.getChildAt(i) instanceof TextView) {
                    btnNextText = (TextView) group.getChildAt(i);
                    break;
                }
            }
        }

        MaterialCardView cardClient = findViewById(R.id.cardClient);
        MaterialCardView cardProvider = findViewById(R.id.cardProvider);
        MaterialCardView cardInitiator = findViewById(R.id.cardInitiator);

        final TextView finalBtnNextText = btnNextText;

        cardClient.setOnClickListener(v -> {
            selectedType = 1;
            updateSelectionUI(cardClient, cardProvider, cardInitiator, btnNext, finalBtnNextText);
        });

        cardProvider.setOnClickListener(v -> {
            selectedType = 2;
            updateSelectionUI(cardProvider, cardClient, cardInitiator, btnNext, finalBtnNextText);
        });

        cardInitiator.setOnClickListener(v -> {
            selectedType = 3;
            updateSelectionUI(cardInitiator, cardClient, cardProvider, btnNext, finalBtnNextText);
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
            } else if (selectedType == 3) {
                // تم التعديل للانتقال إلى صفحة إنشاء حساب مبادر بدلاً من صفحة المبادرة مباشرة
                Intent intent = new Intent(UserTypeActivity.this, RegisterInitiatorActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "يرجى اختيار نوع الحساب للمتابعة", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSelectionUI(MaterialCardView selected, MaterialCardView other1, MaterialCardView other2, MaterialCardView btnNext, TextView btnNextText) {
        selected.setCardBackgroundColor(Color.parseColor("#E0F2FE"));
        selected.setStrokeColor(Color.parseColor("#0069B4"));
        selected.setStrokeWidth(2);
        
        other1.setCardBackgroundColor(Color.WHITE);
        other1.setStrokeWidth(0);
        
        other2.setCardBackgroundColor(Color.WHITE);
        other2.setStrokeWidth(0);
        
        btnNext.setCardBackgroundColor(Color.parseColor("#0069B4"));
        if (btnNextText != null) btnNextText.setTextColor(Color.WHITE);
    }
}
