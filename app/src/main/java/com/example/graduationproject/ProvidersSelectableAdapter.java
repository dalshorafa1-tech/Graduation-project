package com.example.graduationproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import java.util.List;

public class ProvidersSelectableAdapter extends RecyclerView.Adapter<ProvidersSelectableAdapter.ProviderViewHolder> {

    private List<ProviderModel> providerList;
    private OnProviderSelectedListener listener;
    private int selectedPosition = -1;

    public interface OnProviderSelectedListener {
        void onProviderSelected(ProviderModel provider);
    }

    public ProvidersSelectableAdapter(List<ProviderModel> providerList, OnProviderSelectedListener listener) {
        this.providerList = providerList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProviderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_provider_selectable, parent, false);
        return new ProviderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProviderViewHolder holder, int position) {
        ProviderModel provider = providerList.get(position);
        holder.tvProviderName.setText(provider.getBusinessName() != null ? provider.getBusinessName() : "مزود غير مسمى");
        
        String typeStr = "مزود مياه";
        if ("truck".equals(provider.getProviderType())) typeStr = "صهريج مياه";
        else if ("well".equals(provider.getProviderType())) typeStr = "بئر مياه";
        
        holder.tvProviderDetails.setText(typeStr + " - " + (provider.getStatus() != null ? provider.getStatus() : "نشط"));

        if (selectedPosition == position) {
            holder.cardProvider.setStrokeColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_blue_dark));
            holder.cardProvider.setStrokeWidth(4);
        } else {
            holder.cardProvider.setStrokeColor(holder.itemView.getContext().getResources().getColor(android.R.color.darker_gray));
            holder.cardProvider.setStrokeWidth(1);
        }

        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onProviderSelected(provider);
            }
        });
    }

    @Override
    public int getItemCount() {
        return providerList.size();
    }

    static class ProviderViewHolder extends RecyclerView.ViewHolder {
        TextView tvProviderName, tvProviderDetails;
        MaterialCardView cardProvider;
        ShapeableImageView ivProviderType;

        public ProviderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProviderName = itemView.findViewById(R.id.tvProviderName);
            tvProviderDetails = itemView.findViewById(R.id.tvProviderDetails);
            cardProvider = itemView.findViewById(R.id.cardProvider);
            ivProviderType = itemView.findViewById(R.id.ivProviderType);
        }
    }
}