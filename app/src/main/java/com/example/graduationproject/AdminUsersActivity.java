package com.example.graduationproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class AdminUsersActivity extends AppCompatActivity {

    private static final String TAG = "AdminUsersActivity";
    private FirebaseFirestore db;
    private RecyclerView rvUsers;
    private AdminUsersAdapter adapter;
    private List<UserModel> userList = new ArrayList<>();
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        db = FirebaseFirestore.getInstance();

        initViews();
        setupNavigation();
        loadUsers();
    }

    private void initViews() {
        rvUsers = findViewById(R.id.rvAdminUsers);
        progressBar = findViewById(R.id.progressBar);

        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminUsersAdapter(userList, user -> {
            Toast.makeText(this, "تفاصيل المستخدم: " + user.getFullName(), Toast.LENGTH_SHORT).show();
        });
        rvUsers.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupNavigation() {
        findViewById(R.id.navAdminHome).setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminDashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        findViewById(R.id.navAdminInitiatives).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminInitiativesActivity.class));
        });

        findViewById(R.id.navAdminUsers).setOnClickListener(v -> {
            // Already here
        });

        findViewById(R.id.navAdminOrders).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminServicesActivity.class));
        });

        findViewById(R.id.navAdminSettings).setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });
    }

    private void loadUsers() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        
        db.collection("users")
                .orderBy("created_at", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    
                    if (error != null) {
                        Log.e(TAG, "Listen failed.", error);
                        Toast.makeText(this, "فشل في تحميل المستخدمين", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        userList.clear();
                        for (DocumentSnapshot doc : value) {
                            UserModel user = doc.toObject(UserModel.class);
                            if (user != null) {
                                user.setId(doc.getId());
                                userList.add(user);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
