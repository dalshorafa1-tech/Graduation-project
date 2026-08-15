package com.example.graduationproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AdminInitiativesActivity extends AppCompatActivity implements AdminInitiativesAdapter.OnInitiativeActionListener {

    private static final String TAG = "AdminInitiatives";
    private FirebaseFirestore db;
    private TextView tvTotalWater, tvBeneficiaries, tvAreas;
    private RecyclerView rvInitiatives, rvCitizenNeeds;
    private AdminInitiativesAdapter initiativesAdapter;
    private CitizenNeedsAdapter needsAdapter;
    private List<InitiativeModel> initiativeList = new ArrayList<>();
    private List<CitizenNeedModel> needsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_initiatives);

        db = FirebaseFirestore.getInstance();

        initViews();
        setupNavigation();
        loadStats();
        loadInitiatives();
        loadCitizenNeeds();
    }

    private void initViews() {
        tvTotalWater = findViewById(R.id.tvTotalWaterDistributed);
        tvBeneficiaries = findViewById(R.id.tvBeneficiaryFamilies);
        tvAreas = findViewById(R.id.tvCoveredAreasCount);
        
        rvInitiatives = findViewById(R.id.rvAdminInitiatives);
        rvCitizenNeeds = findViewById(R.id.rvCitizenNeeds);

        rvInitiatives.setLayoutManager(new LinearLayoutManager(this));
        rvCitizenNeeds.setLayoutManager(new LinearLayoutManager(this));

        initiativesAdapter = new AdminInitiativesAdapter(initiativeList, this);
        needsAdapter = new CitizenNeedsAdapter(needsList);

        rvInitiatives.setAdapter(initiativesAdapter);
        rvCitizenNeeds.setAdapter(needsAdapter);

        findViewById(R.id.btnAdminNotifications).setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationsActivity.class));
        });

        findViewById(R.id.btnAdminAddInitiative).setOnClickListener(v -> {
            Toast.makeText(this, "سيتم إضافة ميزة إضافة مبادرة قريباً", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupNavigation() {
        findViewById(R.id.navAdminHome).setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminDashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        findViewById(R.id.navAdminInitiatives).setOnClickListener(v -> {
            // Already here
        });

        findViewById(R.id.navAdminUsers).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminUsersActivity.class));
        });

        findViewById(R.id.navAdminOrders).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminServicesActivity.class));
        });

        findViewById(R.id.navAdminSettings).setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });
    }

    private void loadStats() {
        db.collection("orders")
                .whereEqualTo("status", "تم التسليم")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    long totalQuantity = 0;
                    Set<String> uniqueCustomers = new HashSet<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        OrderModel order = doc.toObject(OrderModel.class);
                        if (order != null) {
                            totalQuantity += order.getQuantity();
                            if (order.getCustomerId() != null) {
                                uniqueCustomers.add(order.getCustomerId());
                            }
                        }
                    }
                    tvTotalWater.setText(String.format(Locale.getDefault(), "%,d لتر", totalQuantity));
                    tvBeneficiaries.setText(String.format(Locale.getDefault(), "%,d", uniqueCustomers.size()));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading order stats", e));

        db.collection("citizen_needs")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Set<String> regions = new HashSet<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String region = doc.getString("region");
                        if (region != null) {
                            regions.add(region);
                        }
                    }
                    tvAreas.setText(String.valueOf(regions.size()));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading areas stats", e));
    }

    private void loadInitiatives() {
        db.collection("initiatives")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed.", error);
                        return;
                    }

                    if (value != null) {
                        initiativeList.clear();
                        for (DocumentSnapshot doc : value) {
                            InitiativeModel initiative = doc.toObject(InitiativeModel.class);
                            if (initiative != null) {
                                initiative.setId(doc.getId());
                                initiativeList.add(initiative);
                            }
                        }
                        initiativesAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void loadCitizenNeeds() {
        db.collection("citizen_needs")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed.", error);
                        return;
                    }

                    if (value != null) {
                        needsList.clear();
                        for (DocumentSnapshot doc : value) {
                            CitizenNeedModel need = doc.toObject(CitizenNeedModel.class);
                            if (need != null) {
                                need.setId(doc.getId());
                                needsList.add(need);
                            }
                        }
                        needsAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onApprove(InitiativeModel initiative) {
        db.collection("initiatives").document(initiative.getId())
                .update("status", "نشط")
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "تمت الموافقة على المبادرة بنجاح", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "فشل في الموافقة: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onReject(InitiativeModel initiative) {
        db.collection("initiatives").document(initiative.getId())
                .update("status", "مرفوض")
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "تم رفض المبادرة", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "فشل في الرفض: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
