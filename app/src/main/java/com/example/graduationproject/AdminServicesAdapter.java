package com.example.graduationproject;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Locale;

public class AdminServicesAdapter extends RecyclerView.Adapter<AdminServicesAdapter.ViewHolder> {

    private List<ServiceModel> list;
    private OnAdminActionListener listener;

    public interface OnAdminActionListener {
        void onApprove(ServiceModel service);
        void onReject(ServiceModel service, String reason);
    }

    public AdminServicesAdapter(List<ServiceModel> list, OnAdminActionListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_service, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ServiceModel s = list.get(position);

        // التحقق من وجود اسم للخدمة لتجنب ظهورها بشكل فارغ
        String serviceName = !TextUtils.isEmpty(s.getNameAr()) ? s.getNameAr() : "خدمة بدون اسم";
        holder.tvName.setText(serviceName);
        
        holder.tvDescription.setText(s.getDescriptionAr() != null ? s.getDescriptionAr() : "");
        holder.tvPrice.setText(String.format(Locale.getDefault(), "%.2f ₪", s.getPrice()));
        holder.tvPriceCup.setText(String.format(Locale.getDefault(), "%.2f ₪", s.getPriceCup()));
        
        // Provider Details - Initial values from ServiceModel
        String pId = s.getProviderId();
        holder.tvProviderId.setText("ID المزود: " + (pId != null ? pId : "غير متوفر"));
        holder.tvProvider.setText(s.getProviderName() != null ? s.getProviderName() : "جاري تحميل الاسم...");
        holder.tvProviderPhone.setText(s.getProviderPhone() != null ? s.getProviderPhone() : "...");
        
        // Fetch fresh provider data from Firestore to ensure name and ID are correct
        if (pId != null) {
            FirebaseFirestore.getInstance().collection("providers").document(pId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String businessName = doc.getString("business_name");
                        String phone = doc.getString("phone");
                        if (businessName != null) holder.tvProvider.setText(businessName);
                        if (phone != null) holder.tvProviderPhone.setText(phone);
                    }
                });
        }
        
        // Provider Type Mapping
        String typeText = "غير محدد";
        if ("storage".equals(s.getProviderType())) typeText = "محطة تحلية / تخزين";
        else if ("truck".equals(s.getProviderType())) typeText = "صهريج مياه";
        else if ("well".equals(s.getProviderType())) typeText = "بئر مياه";
        holder.tvProviderType.setText("نوع المنشأة: " + typeText);

        // Created At
        if (s.getCreatedAt() != null) {
            android.text.format.DateFormat df = new android.text.format.DateFormat();
            holder.tvCreatedAt.setText("تاريخ الطلب: " + df.format("yyyy-MM-dd HH:mm", s.getCreatedAt().toDate()));
        }

        holder.btnApprove.setOnClickListener(v -> {
            if (listener != null) listener.onApprove(s);
        });

        holder.btnReject.setOnClickListener(v -> showRejectDialog(v.getContext(), s));
    }

    private void showRejectDialog(android.content.Context context, ServiceModel service) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("رفض الخدمة");
        builder.setMessage("يرجى كتابة سبب رفض هذه الخدمة (سيظهر للمزود):");

        final EditText input = new EditText(context);
        input.setHint("اكتب السبب هنا...");
        input.setTextDirection(View.TEXT_DIRECTION_RTL);
        builder.setView(input, 50, 20, 50, 0);

        builder.setPositiveButton("إرسال الرفض", (dialog, which) -> {
            String reason = input.getText().toString().trim();
            if (listener != null) {
                listener.onReject(service, TextUtils.isEmpty(reason) ? "لم يتم تحديد سبب" : reason);
            }
        });
        builder.setNegativeButton("إلغاء", null);
        builder.show();
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView tvName, tvDescription, tvPrice, tvPriceCup, tvProvider, tvProviderId, tvProviderPhone, tvProviderType, tvCreatedAt;
        MaterialButton btnApprove, btnReject;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardAdminService);
            tvName = itemView.findViewById(R.id.tvAdminServiceName);
            tvDescription = itemView.findViewById(R.id.tvAdminServiceDesc);
            tvPrice = itemView.findViewById(R.id.tvAdminServicePrice);
            tvPriceCup = itemView.findViewById(R.id.tvAdminServicePriceCup);
            tvProvider = itemView.findViewById(R.id.tvAdminServiceProvider);
            tvProviderId = itemView.findViewById(R.id.tvAdminProviderId);
            tvProviderPhone = itemView.findViewById(R.id.tvAdminProviderPhone);
            tvProviderType = itemView.findViewById(R.id.tvAdminProviderType);
            tvCreatedAt = itemView.findViewById(R.id.tvAdminCreatedAt);
            btnApprove = itemView.findViewById(R.id.btnApproveService);
            btnReject = itemView.findViewById(R.id.btnRejectService);
        }
    }
}
