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
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TripsHistoryAdapter extends RecyclerView.Adapter<TripsHistoryAdapter.ViewHolder> {

    private Context context;
    private List<OrderModel> orders;
    private FirebaseFirestore db;
    private Map<String, String> nameCache = new HashMap<>(); // لتجنب تكرار التحميل من الفايربيز

    public TripsHistoryAdapter(Context context, List<OrderModel> orders) {
        this.context = context;
        this.orders = orders;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trip_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderModel order = orders.get(position);

        // 1. معالجة التاريخ والوقت وتصنيف الأقسام
        if (order.getCreatedAt() instanceof Timestamp) {
            Date date = ((Timestamp) order.getCreatedAt()).toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd MMMM", new Locale("ar"));
            String dateStr = sdf.format(date);
            
            holder.tvSectionDate.setText(dateStr);
            
            // إخفاء عنوان التاريخ إذا كان مماثلاً للطلب السابق (للتجميع البصري)
            if (position > 0) {
                OrderModel prev = orders.get(position - 1);
                if (prev.getCreatedAt() instanceof Timestamp) {
                    String prevDate = sdf.format(((Timestamp) prev.getCreatedAt()).toDate());
                    holder.tvSectionDate.setVisibility(prevDate.equals(dateStr) ? View.GONE : View.VISIBLE);
                }
            } else {
                holder.tvSectionDate.setVisibility(View.VISIBLE);
            }

            SimpleDateFormat timeSdf = new SimpleDateFormat("hh:mm a", new Locale("ar"));
            String orderIdStr = order.getId() != null ? (order.getId().length() > 6 ? order.getId().substring(0, 6) : order.getId()) : "---";
            holder.tvOrderMeta.setText("طلب #" + orderIdStr + " • " + timeSdf.format(date));
        }

        // 2. جلب اسم العميل مع التخزين المؤقت للسرعة
        String customerId = order.getCustomerId();
        if (customerId != null) {
            if (nameCache.containsKey(customerId)) {
                holder.tvCustomerName.setText(nameCache.get(customerId));
            } else {
                holder.tvCustomerName.setText("عميل");
                db.collection("users").document(customerId).get().addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("full_name");
                        nameCache.put(customerId, name);
                        holder.tvCustomerName.setText(name);
                    }
                });
            }
        }

        // 3. عرض البيانات المالية والكمية
        holder.tvOrderPrice.setText(String.format(Locale.getDefault(), "%.2f ₪", order.getTotalPrice() != null ? order.getTotalPrice() : 0.0));
        holder.tvWaterAmount.setText(order.getQuantity() + " " + (order.getUnit() != null ? order.getUnit() : "لتر"));

        // 4. تصنيف الحالة بصرياً
        String status = order.getStatus();
        if ("delivered".equals(status)) {
            holder.tvStatusText.setText("مكتمل");
            holder.tvStatusText.setTextColor(Color.parseColor("#15803D"));
            holder.tvPaymentBadge.setText("مدفوع");
            holder.tvPaymentBadge.setBackgroundColor(Color.parseColor("#DCFCE7"));
        } else {
            holder.tvStatusText.setText("ملغي");
            holder.tvStatusText.setTextColor(Color.parseColor("#EF4444"));
            holder.tvPaymentBadge.setText("ملغي");
            holder.tvPaymentBadge.setBackgroundColor(Color.parseColor("#FEE2E2"));
        }

        holder.btnGoToDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProviderOrderDetailsActivity.class);
            intent.putExtra("order_id", order.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void updateList(List<OrderModel> newList) {
        this.orders = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSectionDate, tvCustomerName, tvOrderMeta, tvOrderPrice, tvStatusText, tvWaterAmount, tvPaymentBadge;
        View btnGoToDetails;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSectionDate = itemView.findViewById(R.id.tvSectionDate);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvOrderMeta = itemView.findViewById(R.id.tvOrderMeta);
            tvOrderPrice = itemView.findViewById(R.id.tvOrderPrice);
            tvStatusText = itemView.findViewById(R.id.tvStatusText);
            tvWaterAmount = itemView.findViewById(R.id.tvWaterAmount);
            tvPaymentBadge = itemView.findViewById(R.id.tvPaymentBadge);
            btnGoToDetails = itemView.findViewById(R.id.btnGoToDetails);
        }
    }
}
