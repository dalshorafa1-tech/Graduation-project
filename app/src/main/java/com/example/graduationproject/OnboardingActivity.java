package com.example.graduationproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class OnboardingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        CardView btnStart = findViewById(R.id.btnStart);
        TextView tvSkip = findViewById(R.id.tvSkip);

        btnStart.setOnClickListener(v -> {
            Intent intent = new Intent(OnboardingActivity.this, OnboardingActivity2.class);
            startActivity(intent);
        });

        tvSkip.setOnClickListener(v -> {
            markOnboardingFinished();
            Intent intent = new Intent(OnboardingActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void markOnboardingFinished() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefs.edit().putBoolean("isFirstTime", false).apply();
    }
}
