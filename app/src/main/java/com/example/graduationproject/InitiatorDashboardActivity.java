package com.example.graduationproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class InitiatorDashboardActivity extends AppCompatActivity {
    private static final String TAG = "DashboardDebug";

    private ImageView btnProfile;
    private TextView btnViewAll;
    private MaterialCardView btnCreateInitiative;
    private LinearLayout btnExploreMap;
    private RecyclerView rvInitiatives;
    private InitiativesAdapter initiativesAdapter;
    private List<InitiativeModel> initiativeList;

    private TextView tvActiveInitiativesCount, tvWelcome;
    private TextView tvTotalWaterDistributed;
    private TextView tvTotalFamiliesBenefited;

    private LinearLayout navHome, navMap, navAdd, navProfile;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initiator_dashboard);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupBottomNavigation();
        setupRecyclerView();
        loadUserData();
        fetchInitiativesFromFirebase();
        setupClickListeners();
    }

    private void initViews() {
        btnProfile = findViewById(R.id.btnNavNotification);
        btnViewAll = findViewById(R.id.btnViewAll);
        btnCreateInitiative = findViewById(R.id.btnCreateInitiative);
        btnExploreMap = findViewById(R.id.btnExploreMap);
        rvInitiatives = findViewById(R.id.rv_initiatives);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvActiveInitiativesCount = findViewById(R.id.tvActiveInitiativesCount);
        tvTotalWaterDistributed = findViewById(R.id.tvTotalWaterDistributed);
        tvTotalFamiliesBenefited = findViewById(R.id.tvTotalFamiliesBenefited);

        navHome = findViewById(R.id.navHome);
        navMap = findViewById(R.id.navMap);
        navAdd = findViewById(R.id.navAdd);
        navProfile = findViewById(R.id.navProfile);
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            if (name != null && !name.isEmpty()) {
                                tvWelcome.setText("مرحباً بعودتك، " + name);
                            }
                        }
                    });
        }
    }

    private void setupBottomNavigation() {
        if (navHome != null) {
            navHome.setAlpha(1.0f);
            navHome.setOnClickListener(v -> {
                fetchInitiativesFromFirebase();
                Toast.makeText(this, "تم تحديث لوحة التحكم", Toast.LENGTH_SHORT).show();
            });
        }

        if (navMap != null) {
            navMap.setOnClickListener(v -> {
                Intent intent = new Intent(InitiatorDashboardActivity.this, NeedMapActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        if (navAdd != null) {
            navAdd.setOnClickListener(v -> {
                Intent intent = new Intent(InitiatorDashboardActivity.this, CreateInitiativeActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(InitiatorDashboardActivity.this, ProfileInitiatorsActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }
    }

    private void setupRecyclerView() {
        if (rvInitiatives == null) return;

        initiativeList = new ArrayList<>();

        initiativesAdapter = new InitiativesAdapter(initiativeList, new InitiativesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(InitiativeModel initiative) {
                Intent intent = new Intent(InitiatorDashboardActivity.this, InitiativeDetailsActivity.class);
                intent.putExtra("initiative_id", initiative.getId());
                startActivity(intent);
            }

            @Override
            public void onTrackProgressClick(InitiativeModel initiative) {
                Intent intent = new Intent(InitiatorDashboardActivity.this, InitiativeDetailsActivity.class);
                intent.putExtra("initiative_id", initiative.getId());
                startActivity(intent);
            }
        });

        rvInitiatives.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvInitiatives.setAdapter(initiativesAdapter);
        rvInitiatives.setHasFixedSize(true);
    }

    private void fetchInitiativesFromFirebase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("initiatives")
                .whereEqualTo("initiatorId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    initiativeList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            InitiativeModel initiative = document.toObject(InitiativeModel.class);
                            initiative.setId(document.getId());
                            initiativeList.add(initiative);
                        } catch (Exception e) {
                            Log.e(TAG, "خطأ أثناء قراءة المبادرة: " + e.getMessage());
                        }
                    }

                    initiativesAdapter.notifyDataSetChanged();
                    calculateAndUpdateStats();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(InitiatorDashboardActivity.this, "⚠️ فشل تحديث المبادرات", Toast.LENGTH_SHORT).show();
                });
    }

    private void calculateAndUpdateStats() {
        if (initiativeList == null || initiativeList.isEmpty()) {
            updateStatsViews(0, 0, 0);
            return;
        }

        int activeCount = 0;
        long totalWaterDistributed = 0;

        for (InitiativeModel initiative : initiativeList) {
            String status = initiative.getStatus();
            if (status != null) {
                if (status.equalsIgnoreCase("نشط") || status.equalsIgnoreCase("نشطة") || 
                    status.equalsIgnoreCase("active")) {
                    activeCount++;
                }
            }
            totalWaterDistributed += initiative.getCurrentLiters();
        }

        long totalFamilies = totalWaterDistributed / 250;
        updateStatsViews(activeCount, totalWaterDistributed, totalFamilies);
    }

    private void updateStatsViews(int activeCount, long waterDistributed, long familiesBenefited) {
        if (tvActiveInitiativesCount != null) {
            tvActiveInitiativesCount.setText(" (" + activeCount + ") ");
        }

        if (tvTotalWaterDistributed != null) {
            if (waterDistributed >= 1000) {
                tvTotalWaterDistributed.setText(String.format("%.1fK لتر", waterDistributed / 1000.0));
            } else {
                tvTotalWaterDistributed.setText(waterDistributed + " لتر");
            }
        }

        if (tvTotalFamiliesBenefited != null) {
            tvTotalFamiliesBenefited.setText(familiesBenefited + " عائلة");
        }
    }

    private void setupClickListeners() {
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProfileInitiatorsActivity.class);
                startActivity(intent);
            });
        }

        if (btnViewAll != null) {
            btnViewAll.setOnClickListener(v -> {
                Intent intent = new Intent(this, InitiativesListActivity.class);
                startActivity(intent);
            });
        }

        if (btnCreateInitiative != null) {
            btnCreateInitiative.setOnClickListener(v -> {
                Intent intent = new Intent(this, CreateInitiativeActivity.class);
                startActivity(intent);
            });
        }

        if (btnExploreMap != null) {
            btnExploreMap.setOnClickListener(v -> {
                Intent intent = new Intent(this, NeedMapActivity.class);
                startActivity(intent);
            });
        }
    }
}