package com.example.graduationproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import androidx.annotation.Nullable;

public class InitiativesListActivity extends AppCompatActivity {

    private LinearLayout btnToolbarProfile;
    private ImageView btnNotifications;
    private TabLayout tabLayoutInitiatives;
    private RecyclerView rvInitiatives;
    private FloatingActionButton fabAddInitiative;
    private LinearLayout navDashboard, navInitiatives, navMap, navWallet;
    private InitiativesAdapter adapter;
    private List<InitiativeModel> activeInitiativesList = new ArrayList<>();
    private List<InitiativeModel> completedInitiativesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_initiatives);

        initViews();
        setupRecyclerView();
        setupInteractions();
        listenToInitiativesRealtime();
    }

    private void initViews() {
        btnToolbarProfile = findViewById(R.id.btnToolbarProfile);
        btnNotifications = findViewById(R.id.btn_notifications);
        tabLayoutInitiatives = findViewById(R.id.tab_layout_initiatives);
        rvInitiatives = findViewById(R.id.rv_initiatives);
        fabAddInitiative = findViewById(R.id.fab_add_initiative);

        navDashboard = findViewById(R.id.nav_dashboard);
        navInitiatives = findViewById(R.id.nav_initiatives);
        navMap = findViewById(R.id.nav_map);
        navWallet = findViewById(R.id.nav_wallet);
    }

    private void setupRecyclerView() {
        rvInitiatives.setLayoutManager(new LinearLayoutManager(this));
        rvInitiatives.setHasFixedSize(true);

        // ربط الـ Adapter في البداية بقائمة فارغة مؤقتاً لحين تحميل بيانات Firestore
        adapter = new InitiativesAdapter(activeInitiativesList, new InitiativesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(InitiativeModel initiative) {
                openInitiativeDetails(initiative);
            }

            @Override
            public void onTrackProgressClick(InitiativeModel initiative) {
                openInitiativeDetails(initiative);
            }
        });

        rvInitiatives.setAdapter(adapter);
    }

    //  دالة موحدة للانتقال لصفحة تفاصيل المبادرة الحقيقية وتمرير الـ ID الخاص بها
    private void openInitiativeDetails(InitiativeModel initiative) {
        if (initiative == null || initiative.getId() == null || initiative.getId().isEmpty()) {
            Toast.makeText(this, "خطأ: لا يوجد معرف لهذه المبادرة", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(InitiativesListActivity.this, InitiativeDetailsActivity.class);
        intent.putExtra("initiative_id", initiative.getId());
        startActivity(intent);
    }

    private void setupInteractions() {
        // إدارة تبديل التبويبات (مبادرات نشطة / منتهية) ديناميكياً
        tabLayoutInitiatives.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    updateAdapterData(activeInitiativesList);
                    Toast.makeText(InitiativesListActivity.this, "عرض المبادرات الجارية والنشطة حالياً", Toast.LENGTH_SHORT).show();
                } else {
                    updateAdapterData(completedInitiativesList);
                    Toast.makeText(InitiativesListActivity.this, "عرض المبادرات المكتملة والمؤرشفة", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        //  الانتقال لملف المستخدم الحالي (ProfileInitiatorsActivity) عند الضغط على الهيدر
        btnToolbarProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileInitiatorsActivity.class);
            startActivity(intent);
        });

        //  مركز تنبيهات المبادرات المعدل (UserNotificationActivity)
        btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserNotificationActivity.class);
            startActivity(intent);
        });

        // إضافة مبادرة سقيا جديدة
        fabAddInitiative.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateInitiativeActivity.class);
            startActivity(intent);
        });

        // --- أزرار الملاحة السفلية لتأمين التنقل الفعلي المتكامل ---
        navDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(this, InitiatorDashboardActivity.class);
            startActivity(intent);
            finish();
        });

        navInitiatives.setOnClickListener(v ->
                Toast.makeText(this, "أنت بالفعل في صفحة المبادرات", Toast.LENGTH_SHORT).show());

        navMap.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapExplorerActivity.class);
            startActivity(intent);
        });

        navWallet.setOnClickListener(v -> {
            Intent intent = new Intent(this, WalletActivity.class);
            startActivity(intent);
        });
    }


    private void updateAdapterData(List<InitiativeModel> newList) {
        if (adapter != null) {
            adapter = new InitiativesAdapter(newList, new InitiativesAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(InitiativeModel initiative) {
                    openInitiativeDetails(initiative);
                }

                @Override
                public void onTrackProgressClick(InitiativeModel initiative) {
                    openInitiativeDetails(initiative);
                }
            });
            rvInitiatives.setAdapter(adapter);
        }
    }

    //  ميثود جلب البيانات الحقيقية من الفايرستور وتصفيتها ديناميكياً بشكل لحظي ومستمر
    private void listenToInitiativesRealtime() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("initiatives")
                .addSnapshotListener(new com.google.firebase.firestore.EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(InitiativesListActivity.this, "خطأ في المزامنة الحية: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (value != null) {
                            activeInitiativesList.clear();
                            completedInitiativesList.clear();

                            for (QueryDocumentSnapshot document : value) {
                                InitiativeModel initiative = document.toObject(InitiativeModel.class);
                                initiative.setId(document.getId());

                                // تصنيف المبادرات بناءً على حقل الحالة (status) في الفايرستور
                                String status = initiative.getStatus();
                                if ("مكتملة".equals(status) || "completed".equalsIgnoreCase(status)) {
                                    completedInitiativesList.add(initiative);
                                } else {
                                    activeInitiativesList.add(initiative);
                                }
                            }

                            // تحديث القائمة المعروضة حالياً فوراً وبشكل حي
                            if (tabLayoutInitiatives.getSelectedTabPosition() == 0) {
                                updateAdapterData(activeInitiativesList);
                            } else {
                                updateAdapterData(completedInitiativesList);
                            }
                        }
                    }
                });
    }
}