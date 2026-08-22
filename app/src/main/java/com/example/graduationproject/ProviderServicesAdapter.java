package com.example.graduationproject;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

public class ProviderServicesAdapter extends RecyclerView.Adapter<ProviderServicesAdapter.ServiceViewHolder> {

    private List<ServiceModel> services;
    private OnServiceToggleListener toggleListener;

    public interface OnServiceToggleListener {
        void onToggle(ServiceModel service, boolean isActive);
        void onEdit(ServiceModel service);
        void onDetails(ServiceModel service);
    }

    public ProviderServicesAdapter(List<ServiceModel> services, OnServiceToggleListener listener) {
        this.services = services;
        this.toggleListener = listener;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_provider_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        ServiceModel service = services.get(position);
        
        String serviceName = !TextUtils.isEmpty(service.getNameAr()) ? service.getNameAr() : "خدمة جديدة";
        String serviceType = !TextUtils.isEmpty(service.getServiceType()) ? service.getServiceType() : "غير محدد";
        
        holder.tvServiceName.setText(serviceName + " (" + serviceType + ")");
        holder.tvServiceDesc.setText(service.getDescriptionAr() != null ? service.getDescriptionAr() : "");
        holder.tvServicePrice.setText(String.format("%.2f ₪", service.getPrice()));

        // تعيين أيقونة بناءً على النوع
        if ("صهريج".equals(serviceType)) {
            holder.imgServiceIcon.setImageResource(R.drawable.truck);
        } else if ("آبار".equals(serviceType)) {
            holder.imgServiceIcon.setImageResource(R.drawable.water);
        } else {
            holder.imgServiceIcon.setImageResource(R.drawable.barrel);
        }

        String status = service.getStatus() != null ? service.getStatus() : "pending";
        setupStatusBadge(holder.tvStatusBadge, status);

        if ("rejected".equals(status) && !TextUtils.isEmpty(service.getRejectReason())) {
            holder.layoutRejectReason.setVisibility(View.VISIBLE);
            holder.tvRejectReason.setText(service.getRejectReason());
        } else {
            holder.layoutRejectReason.setVisibility(View.GONE);
        }

        holder.switchStatus.setOnCheckedChangeListener(null);
        holder.switchStatus.setChecked(service.isActive());
        
        boolean isApproved = "approved".equals(status);
        holder.switchStatus.setEnabled(isApproved);
        
        updateSwitchColors(holder.switchStatus, service.isActive());
        holder.switchStatus.setAlpha(isApproved ? 1.0f : 0.5f);

        holder.switchStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateSwitchColors(holder.switchStatus, isChecked);
            if (toggleListener != null) {
                toggleListener.onToggle(service, isChecked);
            }
        });

        holder.btnEditService.setOnClickListener(v -> {
            if (toggleListener != null) {
                toggleListener.onEdit(service);
            }
        });

        holder.btnServiceDetails.setOnClickListener(v -> {
            if (toggleListener != null) {
                toggleListener.onDetails(service);
            }
        });
    }

    private void updateSwitchColors(SwitchMaterial sw, boolean isActive) {
        if (isActive) {
            sw.setThumbTintList(ColorStateList.valueOf(Color.parseColor("#0069B4")));
            sw.setTrackTintList(ColorStateList.valueOf(Color.parseColor("#B3E5FC")));
        } else {
            sw.setThumbTintList(ColorStateList.valueOf(Color.parseColor("#94A3B8")));
            sw.setTrackTintList(ColorStateList.valueOf(Color.parseColor("#E2E8F0")));
        }
    }

    private void setupStatusBadge(TextView badge, String status) {
        badge.setVisibility(View.VISIBLE);
        switch (status) {
            case "pending":
                badge.setText("قيد المراجعة");
                badge.setTextColor(Color.parseColor("#F59E0B"));
                badge.setBackgroundResource(R.drawable.bg_alert_blue);
                if (badge.getBackground() != null) badge.getBackground().setTint(Color.parseColor("#FEF3C7"));
                break;
            case "approved":
                badge.setText("مقبول");
                badge.setTextColor(Color.parseColor("#10B981"));
                badge.setBackgroundResource(R.drawable.bg_alert_blue);
                if (badge.getBackground() != null) badge.getBackground().setTint(Color.parseColor("#D1FAE5"));
                break;
            case "rejected":
                badge.setText("مرفوض");
                badge.setTextColor(Color.parseColor("#EF4444"));
                badge.setBackgroundResource(R.drawable.bg_alert_blue);
                if (badge.getBackground() != null) badge.getBackground().setTint(Color.parseColor("#FEE2E2"));
                break;
            default:
                badge.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return services != null ? services.size() : 0;
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceName, tvServicePrice, tvServiceDesc, tvStatusBadge, tvRejectReason;
        ImageView imgServiceIcon;
        SwitchMaterial switchStatus;
        LinearLayout layoutRejectReason;
        View btnEditService, btnServiceDetails;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvServicePrice = itemView.findViewById(R.id.tvServicePrice);
            tvServiceDesc = itemView.findViewById(R.id.tvServiceDesc);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvRejectReason = itemView.findViewById(R.id.tvRejectReason);
            imgServiceIcon = itemView.findViewById(R.id.imgServiceIcon);
            switchStatus = itemView.findViewById(R.id.switchServiceStatus);
            layoutRejectReason = itemView.findViewById(R.id.layoutRejectReason);
            btnEditService = itemView.findViewById(R.id.btnEditService);
            btnServiceDetails = itemView.findViewById(R.id.btnServiceDetails);
        }
    }
}
