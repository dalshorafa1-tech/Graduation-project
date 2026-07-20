package com.example.graduationproject;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_DEFAULT = 0;
    private static final int TYPE_ORDER = 1;
    private static final int TYPE_RATING = 2;
    private static final int TYPE_PAYMENT = 3;

    private Context context;
    private List<NotificationModel> notifications;
    private FirebaseFirestore db;

    public NotificationAdapter(Context context, List<NotificationModel> notifications) {
        this.context = context;
        this.notifications = notifications;
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public int getItemViewType(int position) {
        String type = notifications.get(position).getType();
        if ("new_order".equals(type) || "order_update".equals(type)) return TYPE_ORDER;
        else if ("rating".equals(type)) return TYPE_RATING;
        else if ("payment".equals(type)) return TYPE_PAYMENT;
        return TYPE_DEFAULT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        switch (viewType) {
            case TYPE_ORDER:
                return new OrderViewHolder(inflater.inflate(R.layout.item_notification_order, parent, false));
            case TYPE_RATING:
                return new RatingViewHolder(inflater.inflate(R.layout.item_notification_rating, parent, false));
            case TYPE_PAYMENT:
                return new PaymentViewHolder(inflater.inflate(R.layout.item_notification_payment, parent, false));
            default:
                return new DefaultViewHolder(inflater.inflate(R.layout.item_notification, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NotificationModel notification = notifications.get(position);

        if (holder instanceof DefaultViewHolder) bindDefault((DefaultViewHolder) holder, notification);
        else if (holder instanceof OrderViewHolder) bindOrder((OrderViewHolder) holder, notification);
        else if (holder instanceof RatingViewHolder) bindRating((RatingViewHolder) holder, notification);
        else if (holder instanceof PaymentViewHolder) bindPayment((PaymentViewHolder) holder, notification);

        holder.itemView.setAlpha(notification.isRead() ? 0.6f : 1.0f);

        holder.itemView.setOnClickListener(v -> {
            if (!notification.isRead() && notification.getId() != null) {
                db.collection("notifications").document(notification.getId()).update("is_read", true);
                notification.setRead(true);
                notifyItemChanged(position);
            }

            // التوجيه الذكي
            if ("service_status".equals(notification.getType())) {
                Intent intent = new Intent(context, ProviderServicesActivity.class);
                context.startActivity(intent);
            } else if (notification.getOrder_id() != null) {
                Intent intent = new Intent(context, ProviderOrderDetailsActivity.class);
                intent.putExtra("order_id", notification.getOrder_id());
                context.startActivity(intent);
            }
        });
    }

    private void bindDefault(DefaultViewHolder holder, NotificationModel notification) {
        holder.tvTitle.setText(notification.getTitle());
        holder.tvMessage.setText(notification.getMessage());
        setTime(holder.tvTime, notification);
        holder.unreadIndicator.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);
        
        if ("service_status".equals(notification.getType())) {
            holder.ivIcon.setImageResource(R.drawable.sure);
            holder.ivIcon.setColorFilter(context.getResources().getColor(android.R.color.holo_green_dark));
        }
    }

    private void bindOrder(OrderViewHolder holder, NotificationModel notification) {
        holder.tvTitle.setText(notification.getTitle());
        holder.tvMessage.setText(notification.getMessage());
        setTime(holder.tvTime, notification);
        holder.unreadIndicator.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);
        
        if ("order_update".equals(notification.getType())) {
            holder.btnAccept.setVisibility(View.GONE);
        }
        
        holder.btnDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProviderOrderDetailsActivity.class);
            intent.putExtra("order_id", notification.getOrder_id());
            context.startActivity(intent);
        });
    }

    private void bindRating(RatingViewHolder holder, NotificationModel notification) {
        holder.tvTitle.setText(notification.getTitle());
        holder.tvMessage.setText(notification.getMessage());
        setTime(holder.tvTime, notification);
        holder.unreadIndicator.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);
    }

    private void bindPayment(PaymentViewHolder holder, NotificationModel notification) {
        holder.tvTitle.setText(notification.getTitle());
        holder.tvMessage.setText(notification.getMessage());
        setTime(holder.tvTime, notification);
        holder.unreadIndicator.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);
    }

    private void setTime(TextView tvTime, NotificationModel notification) {
        if (notification.getCreated_at() instanceof Timestamp) {
            long diff = System.currentTimeMillis() - ((Timestamp) notification.getCreated_at()).toDate().getTime();
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            if (minutes < 1) tvTime.setText("الآن");
            else if (minutes < 60) tvTime.setText("منذ " + minutes + " د");
            else tvTime.setText("منذ " + (minutes / 60) + " س");
        }
    }

    @Override
    public int getItemCount() { return notifications.size(); }

    public static class DefaultViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTime;
        View unreadIndicator;
        ImageView ivIcon;
        DefaultViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvMessage = v.findViewById(R.id.tvMessage);
            tvTime = v.findViewById(R.id.tvTime);
            unreadIndicator = v.findViewById(R.id.unreadIndicator);
            ivIcon = v.findViewById(R.id.ivIcon);
        }
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTime;
        View unreadIndicator;
        MaterialButton btnAccept, btnDetails;
        OrderViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvMessage = v.findViewById(R.id.tvMessage);
            tvTime = v.findViewById(R.id.tvTime);
            unreadIndicator = v.findViewById(R.id.unreadIndicator);
            btnAccept = v.findViewById(R.id.btnAccept);
            btnDetails = v.findViewById(R.id.btnDetails);
        }
    }

    public static class RatingViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTime, tvRatingValue;
        View unreadIndicator;
        RatingViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvMessage = v.findViewById(R.id.tvMessage);
            tvTime = v.findViewById(R.id.tvTime);
            tvRatingValue = v.findViewById(R.id.tvRatingValue);
            unreadIndicator = v.findViewById(R.id.unreadIndicator);
        }
    }

    public static class PaymentViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTime, tvAmount;
        View unreadIndicator;
        PaymentViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvMessage = v.findViewById(R.id.tvMessage);
            tvTime = v.findViewById(R.id.tvTime);
            tvAmount = v.findViewById(R.id.tvAmount);
            unreadIndicator = v.findViewById(R.id.unreadIndicator);
        }
    }
}
