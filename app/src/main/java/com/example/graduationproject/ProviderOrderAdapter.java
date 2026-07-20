package com.example.graduationproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ProviderOrderAdapter extends RecyclerView.Adapter<ProviderOrderAdapter.OrderViewHolder> {

    private Context context;
    private List<OrderModel> orders;
    private String currentTab;
    private FirebaseFirestore db;
    private Map<String, String> customerNameCache = new HashMap<>();

    public ProviderOrderAdapter(Context context, List<OrderModel> orders, String currentTab) {
        this.context = context;
        this.orders = orders;
        this.currentTab = currentTab;
        this.db = FirebaseFirestore.getInstance();
    }

    public void setCurrentTab(String tab) {
        this.currentTab = tab;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_provider_order_action, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderModel order = orders.get(position);
        
        // جلب اسم العميل
        fetchCustomerName(order.getCustomerId(), holder);

        holder.tvClientAddress.setText(order.getAddressDetails() != null ? order.getAddressDetails() : "غزة - فلسطين");
        holder.tvOrderQuantity.setText(order.getQuantity() + " " + (order.getUnit() != null ? order.getUnit() : "لتر"));
        holder.tvOrderType.setText(order.getOrderType() != null ? order.getOrderType() : "تزويد مياه");
        holder.tvScheduledTime.setText(order.getScheduledTime() != null ? order.getScheduledTime() : "توصيل عاجل");

        setTimeAgo(holder.tvTimeAgo, order);

        if (order.getTotalPrice() != null) {
            holder.tvOrderPrice.setText(String.format("%.0f ₪", order.getTotalPrice()));
        }

        setupStatusUI(holder, order);

        holder.btnAccept.setOnClickListener(v -> updateStatus(order, "accepted"));
        holder.btnReject.setOnClickListener(v -> updateStatus(order, "cancelled"));

        holder.btnUpdateStatus.setOnClickListener(v -> {
            String status = order.getStatus();
            String nextStatus = "accepted".equals(status) ? "on_way" : "delivered";
            updateStatus(order, nextStatus);
        });

        // زر "التفاصيل" للانتقال لشاشة التفاصيل
        holder.tvClickDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProviderOrderDetailsActivity.class);
            intent.putExtra("order_id", order.getId());
            context.startActivity(intent);
        });

        // النقر على الصندوق بالكامل كخيار إضافي
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProviderOrderDetailsActivity.class);
            intent.putExtra("order_id", order.getId());
            context.startActivity(intent);
        });
    }

    private void setupStatusUI(OrderViewHolder holder, OrderModel order) {
        String status = order.getStatus();
        holder.tvOrderStatus.setVisibility(View.VISIBLE);
        
        if ("new".equals(currentTab)) {
            holder.layoutActions.setVisibility(View.VISIBLE);
            holder.btnUpdateStatus.setVisibility(View.GONE);
            holder.tvOrderStatus.setVisibility(View.GONE);
        } else if ("in_progress".equals(currentTab)) {
            holder.layoutActions.setVisibility(View.GONE);
            holder.btnUpdateStatus.setVisibility(View.VISIBLE);
            
            if ("accepted".equals(status)) {
                holder.btnUpdateStatus.setText("بدء التحرك");
                holder.btnUpdateStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#3B82F6")));
                holder.tvOrderStatus.setText("تم القبول");
                holder.tvOrderStatus.setTextColor(Color.parseColor("#3B82F6"));
            } else {
                holder.btnUpdateStatus.setText("تأكيد التوصيل");
                holder.btnUpdateStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#10B981")));
                holder.tvOrderStatus.setText("في الطريق");
                holder.tvOrderStatus.setTextColor(Color.parseColor("#10B981"));
            }
        } else {
            holder.layoutActions.setVisibility(View.GONE);
            holder.btnUpdateStatus.setVisibility(View.GONE);
            String text = "delivered".equals(status) ? "مكتمل" : "ملغي";
            int color = "delivered".equals(status) ? Color.parseColor("#15803D") : Color.parseColor("#EF4444");
            holder.tvOrderStatus.setText(text);
            holder.tvOrderStatus.setTextColor(color);
        }
    }

    private void updateStatus(OrderModel order, String newStatus) {
        if (order.getId() == null) return;

        db.collection("orders").document(order.getId())
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "تم تحديث حالة الطلب", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchCustomerName(String customerId, OrderViewHolder holder) {
        if (customerId == null) {
            holder.tvClientName.setText("عميل");
            return;
        }
        if (customerNameCache.containsKey(customerId)) {
            holder.tvClientName.setText(customerNameCache.get(customerId));
            return;
        }
        db.collection("users").document(customerId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String name = doc.getString("full_name");
                customerNameCache.put(customerId, name);
                holder.tvClientName.setText(name != null ? name : "عميل");
            }
        });
    }

    private void setTimeAgo(TextView tv, OrderModel order) {
        if (order.getCreatedAt() instanceof Timestamp) {
            long diff = System.currentTimeMillis() - ((Timestamp) order.getCreatedAt()).toDate().getTime();
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            if (minutes < 1) tv.setText("الآن");
            else if (minutes < 60) tv.setText("منذ " + minutes + " د");
            else tv.setText("منذ " + (minutes / 60) + " س");
        }
    }

    @Override
    public int getItemCount() { return orders.size(); }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvClientName, tvClientAddress, tvOrderQuantity, tvOrderType, tvScheduledTime, tvTimeAgo, tvOrderPrice, tvOrderStatus, tvClickDetails;
        MaterialButton btnAccept, btnReject, btnUpdateStatus;
        View layoutActions;
        MaterialCardView cardOrderDetails;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvClientAddress = itemView.findViewById(R.id.tvClientAddress);
            tvOrderQuantity = itemView.findViewById(R.id.tvOrderQuantity);
            tvOrderType = itemView.findViewById(R.id.tvOrderType);
            tvScheduledTime = itemView.findViewById(R.id.tvScheduledTime);
            tvTimeAgo = itemView.findViewById(R.id.tvTimeAgo);
            tvOrderPrice = itemView.findViewById(R.id.tvOrderPrice);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvClickDetails = itemView.findViewById(R.id.tvClickDetails);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnUpdateStatus = itemView.findViewById(R.id.btnUpdateStatus);
            layoutActions = itemView.findViewById(R.id.layoutActions);
            cardOrderDetails = itemView.findViewById(R.id.cardOrderDetails);
        }
    }
}
