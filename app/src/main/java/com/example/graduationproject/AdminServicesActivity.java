package com.example.graduationproject;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminServicesActivity extends AppCompatActivity {

    private RecyclerView rvPendingServices;
    private TextView tvEmptyState;
    private AdminServicesAdapter adapter;
    private List<ServiceModel> pendingList = new ArrayList<>();
    private FirebaseFirestore db;
    private ListenerRegistration listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_services);

        db = FirebaseFirestore.getInstance();

        rvPendingServices = findViewById(R.id.rvPendingServices);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        rvPendingServices.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminServicesAdapter(pendingList, new AdminServicesAdapter.OnAdminActionListener() {
            @Override
            public void onApprove(ServiceModel service) {
                updateServiceStatus(service, "approved", null);
            }

            @Override
            public void onReject(ServiceModel service, String reason) {
                updateServiceStatus(service, "rejected", reason);
            }
        });
        rvPendingServices.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        listenForPendingServices();
    }

    private void listenForPendingServices() {
        listener = db.collection("services")
                .whereEqualTo("status", "pending")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("AdminServices", "Listen failed: " + e.getMessage());
                        return;
                    }

                    if (snapshots != null) {
                        pendingList.clear();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            ServiceModel service = doc.toObject(ServiceModel.class);
                            service.setId(doc.getId());
                            pendingList.add(service);
                        }
                        adapter.notifyDataSetChanged();
                        
                        tvEmptyState.setVisibility(pendingList.isEmpty() ? View.VISIBLE : View.GONE);
                        rvPendingServices.setVisibility(pendingList.isEmpty() ? View.GONE : View.VISIBLE);
                    }
                });
    }

    private void updateServiceStatus(ServiceModel service, String status, String reason) {
        if (service.getId() == null) return;
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("rejectReason", reason); 
        if ("approved".equals(status)) {
            updates.put("isActive", true);
        } else {
            updates.put("isActive", false);
        }

        db.collection("services").document(service.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    String msg = "approved".equals(status) ? "تم قبول الخدمة بنجاح ✅" : "تم رفض الخدمة";
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    
                    // إرسال الإشعار للمزود فور النجاح في التحديث
                    sendNotificationToProvider(service, status, reason);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "فشل تحديث الحالة: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void sendNotificationToProvider(ServiceModel service, String status, String reason) {
        String providerId = service.getProviderId();
        if (providerId == null || providerId.isEmpty()) {
            Log.e("AdminServices", "Provider ID missing for service: " + service.getId());
            return;
        }

        String title = "تحديث حالة الخدمة";
        String message;
        
        if ("approved".equals(status)) {
            message = "🎉 مبروك! تمت الموافقة على خدمتك: (" + service.getNameAr() + "). أصبحت الخدمة الآن نشطة وتظهر للزبائن في الخريطة.";
        } else {
            message = "⚠️ نعتذر، تم رفض طلبك للخدمة: (" + service.getNameAr() + "). السبب: " + (reason != null ? reason : "لم يتم تحديد سبب.");
        }

        // إنشاء كائن الإشعار بأسماء الحقول المتوافقة مع NotificationModel
        Map<String, Object> notif = new HashMap<>();
        notif.put("userId", providerId);           // الحقل الذي يبحث عنه تطبيق المزود
        notif.put("provider_id", providerId);      // لزيادة التوافق
        notif.put("title", title);
        notif.put("message", message);
        notif.put("type", "service_status");
        notif.put("order_id", service.getId());    // نضع معرف الخدمة هنا للاستخدام مستقبلاً
        notif.put("read", false);                  // يجب أن يكون "read" ليتوافق مع الموديل
        notif.put("created_at", com.google.firebase.Timestamp.now()); // يجب أن يكون "created_at" للفرز الزمني

        // حفظ الإشعار في مجموعة الإشعارات العامة
        db.collection("notifications")
                .add(notif)
                .addOnSuccessListener(docRef -> Log.d("AdminServices", "Notification document created: " + docRef.getId()))
                .addOnFailureListener(e -> Log.e("AdminServices", "Firestore notification failed: " + e.getMessage()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null) listener.remove();
    }
}
