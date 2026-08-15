package com.example.graduationproject;


import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

public class InitiativesImpactActivity extends AppCompatActivity {

    private TextView tvTotalLiters, tvTotalFamilies;
    private ImageView btnBack;
    private TextView btnFilter;
    private MaterialCardView cardCompleted1, cardCompleted2;
    private LinearLayout navHome, navInitiatives, navMap, navWallet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_initiatives_impact_log);

        initViews();
        setupClickListeners();
        // مثال لتحديث البيانات برمجياً بشكل ديناميكي مستقبلاً
        loadImpactStatistics(150000, 2450);
    }

    private void initViews() {
        tvTotalLiters = findViewById(R.id.tv_total_liters);
        tvTotalFamilies = findViewById(R.id.tv_total_families);
        btnBack = findViewById(R.id.btn_back);
        btnFilter = findViewById(R.id.btn_filter);

        cardCompleted1 = findViewById(R.id.card_completed_1);
        cardCompleted2 = findViewById(R.id.card_completed_2);

        // الملاحة السفلية
        navHome = findViewById(R.id.nav_home);
        navInitiatives = findViewById(R.id.nav_initiatives);
        navMap = findViewById(R.id.nav_map);
        navWallet = findViewById(R.id.nav_wallet);
    }

    private void setupClickListeners() {
        // العودة للخلف أو الشاشة السابقة
        btnBack.setOnClickListener(v -> finish());

        // تصفية الفرز الزمني أو الجغرافي للمبادرات
        btnFilter.setOnClickListener(v ->
                Toast.makeText(this, "فتح خيارات تصفية السجلات المكتملة...", Toast.LENGTH_SHORT).show());

        // تفاصيل مبادرة حي الأمل
        cardCompleted1.setOnClickListener(v ->
                Toast.makeText(this, "عرض تقرير الإغلاق الميداني ومستندات التوزيع لحي الأمل", Toast.LENGTH_SHORT).show());

        // تفاصيل حملة روافد الخير
        cardCompleted2.setOnClickListener(v ->
                Toast.makeText(this, "عرض إحصائيات حملة روافد الخير بالتفصيل", Toast.LENGTH_SHORT).show());

        // أزرار التنقل السفلية
        navHome.setOnClickListener(v -> Toast.makeText(this, "العودة للوحة القيادة", Toast.LENGTH_SHORT).show());
        navMap.setOnClickListener(v -> Toast.makeText(this, "الانتقال لخريطة الاحتياج المائي الميدانية", Toast.LENGTH_SHORT).show());
        navWallet.setOnClickListener(v -> Toast.makeText(this, "الانتقال للمحفظة المالية والتقارير", Toast.LENGTH_SHORT).show());
    }

    /**
     * دالة لتحديث الأرقام برمجياً عند جلبها من السيرفر أو الـ API
     */
    private void loadImpactStatistics(long liters, long families) {
        tvTotalLiters.setText(String.format("%,d", liters));
        tvTotalFamilies.setText(String.format("%,d", families));
    }
}
