package com.example.graduationproject;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class AboutErwaaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ربط الكلاس بملف تصميم شاشة "عن إرواء"
        setContentView(R.layout.activity_about_erwaa_full);

        // تفعيل زر العودة (افترضت أن الآي دي الخاص بزر الرجوع هو btnMenu أو btnBack)
        // تأكد من أن الـ ID يتطابق مع الموجود في ملف التصميم الخاص بهذه الشاشة
        ImageView btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                finish(); // إغلاق هذه الشاشة والعودة للشاشة السابقة (الإعدادات)
            });
        }
    }
}