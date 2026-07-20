package com.example.graduationproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class UrgentOrderAdapter extends RecyclerView.Adapter<UrgentOrderAdapter.UrgentOrderViewHolder> {

    private Context context;
    private List<OrderModel> orders;
    private String providerId;
    private FirebaseFirestore db;

    public UrgentOrderAdapter(Context context, List<OrderModel> orders, String providerId) {
        this.context = context;
        this.orders = orders;
        this.providerId = providerId;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public UrgentOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_urgent_order, parent, false);
        return new UrgentOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UrgentOrderViewHolder holder, int position) {
        OrderModel order = orders.get(position);
        String orderId = order.getId();

        holder.tvOrderTitle.setText("طلب تزويد مياه" + (order.getAddressDetails() != null ? " - " + order.getAddressDetails() : ""));
        holder.tvCustomerName.setText("العميل: " + (order.getCustomerId() != null ? order.getCustomerId().substring(0, Math.min(6, order.getCustomerId().length())) : "---"));
        holder.tvQuantity.setText("💧 " + order.getQuantity() + " " + (order.getUnit() != null ? order.getUnit() : "لتر"));

        if (order.getTotalPrice() != null) {
            holder.tvPrice.setText("💰 " + String.format(Locale.getDefault(), "%.0f", order.getTotalPrice()) + " شيكل");
        } else {
            holder.tvPrice.setVisibility(View.GONE);
        }

        holder.tvDistance.setText("📍 " + (order.getAddressDetails() != null ? order.getAddressDetails() : "---"));

        if (order.getCreatedAt() != null) {
            try {
                if (order.getCreatedAt() instanceof com.google.firebase.Timestamp) {
                    Date date = ((com.google.firebase.Timestamp) order.getCreatedAt()).toDate();
                    long diff = System.currentTimeMillis() - date.getTime();
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
                    if (minutes < 1) holder.tvTimeAgo.setText("الآن");
                    else if (minutes < 60) holder.tvTimeAgo.setText("منذ " + minutes + " د");
                    else holder.tvTimeAgo.setText("منذ " + TimeUnit.MILLISECONDS.toHours(diff) + " س");
                }
            } catch (Exception e) {
                holder.tvTimeAgo.setText("منذ لحظات");
            }
        }

        holder.btnAccept.setOnClickListener(v -> acceptOrder(order, position));
        holder.btnReject.setOnClickListener(v -> removeOrder(position));
    }

    private void acceptOrder(OrderModel order, int position) {
        if (order.getId() == null) return;
        db.collection("orders").document(order.getId())
                .update("status", "accepted", "provider_id", providerId)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "تم قبول الطلب بنجاح", Toast.LENGTH_SHORT).show();
                    removeOrder(position);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "فشل قبول الطلب", Toast.LENGTH_SHORT).show());
    }

    private void removeOrder(int position) {
        orders.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class UrgentOrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderTitle, tvCustomerName, tvTimeAgo, tvQuantity, tvDistance, tvPrice;
        MaterialButton btnAccept, btnReject;

        public UrgentOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderTitle = itemView.findViewById(R.id.tvOrderTitle);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvTimeAgo = itemView.findViewById(R.id.tvTimeAgo);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnAccept = itemView.findViewById(R.id.btnAcceptOrder);
            btnReject = itemView.findViewById(R.id.btnRejectOrder);
        }
    }
}
