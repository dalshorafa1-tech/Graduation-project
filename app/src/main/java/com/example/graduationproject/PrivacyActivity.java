package com.example.graduationproject;


import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class PrivacyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ربط الكلاس بملف تصميم شاشة "سياسة الخصوصية"
        setContentView(R.layout.activity_privacy_guidelines);

        // تفعيل زر العودة
        ImageView btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                finish();
            });
        }
    }
}