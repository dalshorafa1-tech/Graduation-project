package com.example.graduationproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WalletActivity extends AppCompatActivity {

    private TextView tvWalletBalance, tvTotalRecharge, tvTotalPayments;
    private RecyclerView rvTransactions;
    private WalletTransactionAdapter adapter;
    private List<TransactionModel> transactionList = new ArrayList<>();
    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration walletListener;
    private ListenerRegistration transactionsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvWalletBalance = findViewById(R.id.tvWalletBalance);
        tvTotalRecharge = findViewById(R.id.tvTotalRecharge);
        tvTotalPayments = findViewById(R.id.tvTotalPayments);
        
        rvTransactions = findViewById(R.id.rvWalletTransactions);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WalletTransactionAdapter(this, transactionList);
        rvTransactions.setAdapter(adapter);

        setupBottomNavigation();
        listenToWalletUpdates();
        listenToTransactions();
    }

    private void listenToWalletUpdates() {
        if (mAuth.getCurrentUser() == null) return;
        
        String userId = mAuth.getCurrentUser().getUid();

        walletListener = db.collection("wallets").document(userId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.w("WalletActivity", "Listen failed.", e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        Double balance = snapshot.getDouble("balance");
                        Double totalRecharge = snapshot.getDouble("total_recharge");
                        Double totalPayments = snapshot.getDouble("total_payments");

                        tvWalletBalance.setText(String.format(Locale.getDefault(), "%.2f ₪", balance != null ? balance : 0.0));
                        tvTotalRecharge.setText(String.format(Locale.getDefault(), "%.2f ₪", totalRecharge != null ? totalRecharge : 0.0));
                        tvTotalPayments.setText(String.format(Locale.getDefault(), "%.2f ₪", totalPayments != null ? totalPayments : 0.0));
                    }
                });
    }

    private void listenToTransactions() {
        if (mAuth.getCurrentUser() == null) return;
        
        String userId = mAuth.getCurrentUser().getUid();

        transactionsListener = db.collection("wallets").document(userId)
                .collection("transactions")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w("WalletActivity", "Transactions listen failed.", e);
                        return;
                    }

                    if (snapshots != null) {
                        transactionList.clear();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            TransactionModel transaction = doc.toObject(TransactionModel.class);
                            transaction.setId(doc.getId());
                            transactionList.add(transaction);
                        }
                        adapter.updateList(transactionList);
                    }
                });
    }

    private void setupBottomNavigation() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            Intent intent = new Intent(this, MapExplorerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        findViewById(R.id.navWallet).setOnClickListener(v -> {
            // Already on Wallet activity
        });

        findViewById(R.id.navOrders).setOnClickListener(v -> {
            startActivity(new Intent(this, My_Orders_Activity.class));
        });

        findViewById(R.id.navProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (walletListener != null) walletListener.remove();
        if (transactionsListener != null) transactionsListener.remove();
    }
}
