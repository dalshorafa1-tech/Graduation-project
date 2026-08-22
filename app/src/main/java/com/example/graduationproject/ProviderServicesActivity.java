package com.example.graduationproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ProviderServicesActivity extends AppCompatActivity {

    private static final String TAG = "ProviderServices";
    private RecyclerView rvServices;
    private MaterialButton btnAddNewService;
    private TextView tvActiveServicesCount;
    private ImageView imgProfile;
    private ProviderServicesAdapter adapter;
    private List<ServiceModel> serviceList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration servicesListener;
    private ListenerRegistration profileListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_services);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        rvServices = findViewById(R.id.rvServices);
        btnAddNewService = findViewById(R.id.btnAddNewService);
        tvActiveServicesCount = findViewById(R.id.tvActiveServicesCount);
        imgProfile = findViewById(R.id.imgProfile);

        rvServices.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProviderServicesAdapter(serviceList, new ProviderServicesAdapter.OnServiceToggleListener() {
            @Override
            public void onToggle(ServiceModel service, boolean isActive) {
                toggleServiceActive(service, isActive);
            }

            @Override
            public void onEdit(ServiceModel service) {
                Intent intent = new Intent(ProviderServicesActivity.this, EditServiceActivity.class);
                intent.putExtra("service_id", service.getId());
                startActivity(intent);
            }

            @Override
            public void onDetails(ServiceModel service) {
                Intent intent = new Intent(ProviderServicesActivity.this, ServiceDetailsActivity.class);
                intent.putExtra("service_id", service.getId());
                startActivity(intent);
            }
        });
        rvServices.setAdapter(adapter);

        btnAddNewService.setOnClickListener(v -> {
            startActivity(new Intent(this, AddServiceActivity.class));
        });

        if (imgProfile != null) {
            imgProfile.setOnClickListener(v -> startActivity(new Intent(this, Profile.class)));
        }

        findViewById(R.id.btnNotifications).setOnClickListener(v ->
                startActivity(new Intent(this, NotificationsActivity.class)));

        listenForServices();
        listenToProfile();
        setupBottomNavigation();
    }

    private void listenToProfile() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();
        
        profileListener = db.collection("providers").document(userId).addSnapshotListener((doc, e) -> {
            if (e != null || doc == null || !doc.exists()) return;
            String imageUrl = doc.getString("profile_image");
            if (imgProfile != null) {
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(this).load(imageUrl).placeholder(R.drawable.user).into(imgProfile);
                } else {
                    imgProfile.setImageResource(R.drawable.user);
                }
            }
        });
    }

    private void listenForServices() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        Log.d(TAG, "Listening for services for provider: " + userId);

        // تم تبسيط الاستعلام بحذف orderBy مؤقتاً للتأكد من عدم وجود مشكلة في الفهارس (Indexes)
        servicesListener = db.collection("services")
                .whereEqualTo("provider_id", userId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error listening for services: ", e);
                        Toast.makeText(this, "خطأ في جلب البيانات: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    if (snapshots != null) {
                        Log.d(TAG, "Found documents: " + snapshots.size());

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            ServiceModel service = dc.getDocument().toObject(ServiceModel.class);
                            service.setId(dc.getDocument().getId());
                            
                            int index = indexOf(service.getId());
                            switch (dc.getType()) {
                                case ADDED:
                                    if (index == -1) serviceList.add(0, service);
                                    break;
                                case REMOVED:
                                    if (index != -1) serviceList.remove(index);
                                    break;
                                case MODIFIED:
                                    if (index != -1) serviceList.set(index, service);
                                    break;
                            }
                        }
                        adapter.notifyDataSetChanged();
                        updateActiveCount();
                    }
                });
    }

    private void updateActiveCount() {
        int count = 0;
        for (ServiceModel s : serviceList) {
            if ("approved".equals(s.getStatus()) && s.isActive()) {
                count++;
            }
        }
        if (tvActiveServicesCount != null) {
            tvActiveServicesCount.setText(count + " خدمات نشطة");
        }
    }

    private int indexOf(String id) {
        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).getId() != null && serviceList.get(i).getId().equals(id))
                return i;
        }
        return -1;
    }

    private void toggleServiceActive(ServiceModel service, boolean isActive) {
        if (service.getId() == null) return;

        service.setActive(isActive);
        updateActiveCount();

        db.collection("services").document(service.getId())
                .update("isActive", isActive)
                .addOnFailureListener(e -> {
                    service.setActive(!isActive);
                    updateActiveCount();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "فشل تحديث الحالة", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupBottomNavigation() {
        findViewById(R.id.navProviderDashboard).setOnClickListener(v -> {
            startActivity(new Intent(this, ProviderDashboardActivity.class));
            finish();
        });
        findViewById(R.id.navProviderOrders).setOnClickListener(v -> {
            startActivity(new Intent(this, OrdersManagementActivity.class));
            finish();
        });
        findViewById(R.id.navProviderHistory).setOnClickListener(v -> {
            startActivity(new Intent(this, TripsHistoryActivity.class));
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (servicesListener != null) servicesListener.remove();
        if (profileListener != null) profileListener.remove();
    }
}
