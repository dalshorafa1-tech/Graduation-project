package com.example.graduationproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<OrderModel> orders;

    public OrderAdapter(Context context, List<OrderModel> orders) {
        this.context = context;
        this.orders = orders;
    }

    public void updateList(List<OrderModel> newList) {
        this.orders = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trip_record, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderModel order = orders.get(position);

        String providerName = order.getProviderName();
        holder.tvCustomerName.setText(providerName != null ? providerName : "طلب تزويد مياه");
        
        String shortId = order.getId() != null && order.getId().length() > 8 ? order.getId().substring(0, 8) : "---";
        holder.tvOrderMeta.setText("طلب #" + shortId + " • " + (order.getScheduledTime() != null ? order.getScheduledTime() : "الآن"));
        
        double price = order.getTotalPrice() != null ? order.getTotalPrice() : 0.0;
        holder.tvOrderPrice.setText(String.format(Locale.getDefault(), "%.2f ₪", price));
        holder.tvWaterAmount.setText(order.getQuantity() + " " + (order.getUnit() != null ? order.getUnit() : "لتر"));
        
        String status = order.getStatus();
        holder.tvStatusText.setText(getStatusArabic(status));
        holder.tvStatusText.setTextColor(getStatusColor(status));

        if (order.getCreatedAt() != null) {
            try {
                String dateOnly = formatCreatedAt(order.getCreatedAt());
                holder.tvSectionDate.setText(dateOnly);
                if (position > 0 && orders.get(position - 1).getCreatedAt() != null) {
                    String prevDate = formatCreatedAt(orders.get(position - 1).getCreatedAt());
                    if (dateOnly.equals(prevDate)) {
                        holder.tvSectionDate.setVisibility(View.GONE);
                    } else {
                        holder.tvSectionDate.setVisibility(View.VISIBLE);
                    }
                } else {
                    holder.tvSectionDate.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                holder.tvSectionDate.setVisibility(View.GONE);
            }
        } else {
            holder.tvSectionDate.setVisibility(View.GONE);
        }

        holder.btnGoToDetails.setOnClickListener(v -> {
            Intent intent;
            // إذا كان الطلب لا يزال قيد الانتظار، نفتح شاشة حالة الانتظار
            if ("pending".equals(status)) {
                intent = new Intent(context, Order_Status_Activity.class);
            } else if ("accepted".equals(status)) {
                intent = new Intent(context, Order_Accepted_Activity.class);
            } else {
                intent = new Intent(context, Order_Details_Activity.class);
            }
            intent.putExtra("order_id", order.getId());
            context.startActivity(intent);
        });
        
        holder.itemView.setOnClickListener(v -> holder.btnGoToDetails.performClick());
    }

    private String formatCreatedAt(Object createdAt) {
        if (createdAt == null) return "";
        if (createdAt instanceof Timestamp) {
            Date date = ((Timestamp) createdAt).toDate();
            return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
        }
        return createdAt.toString();
    }

    private String getStatusArabic(String status) {
        if (status == null) return "قيد المعالجة";
        switch (status) {
            case "pending": return "قيد الانتظار";
            case "accepted": return "تم القبول";
            case "on_way": return "في الطريق";
            case "delivered": return "تم التوصيل";
            case "cancelled": return "ملغي";
            default: return "نشط";
        }
    }

    private int getStatusColor(String status) {
        if (status == null) return Color.parseColor("#F59E0B");
        switch (status) {
            case "pending": return Color.parseColor("#F59E0B");
            case "accepted": return Color.parseColor("#3B82F6");
            case "on_way": return Color.parseColor("#10B981");
            case "delivered": return Color.parseColor("#15803D");
            case "cancelled": return Color.parseColor("#EF4444");
            default: return Color.GRAY;
        }
    }

    @Override
    public int getItemCount() { return orders.size(); }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvCustomerName, tvOrderMeta, tvOrderPrice, tvStatusText, tvWaterAmount, tvSectionDate;
        View btnGoToDetails;
        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvOrderMeta = itemView.findViewById(R.id.tvOrderMeta);
            tvOrderPrice = itemView.findViewById(R.id.tvOrderPrice);
            tvStatusText = itemView.findViewById(R.id.tvStatusText);
            tvWaterAmount = itemView.findViewById(R.id.tvWaterAmount);
            tvSectionDate = itemView.findViewById(R.id.tvSectionDate);
            btnGoToDetails = itemView.findViewById(R.id.btnGoToDetails);
        }
    }
}
