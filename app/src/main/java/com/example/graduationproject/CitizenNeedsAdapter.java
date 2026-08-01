package com.example.graduationproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CitizenNeedsAdapter extends RecyclerView.Adapter<CitizenNeedsAdapter.ViewHolder> {

    private List<CitizenNeedModel> needsList;

    public CitizenNeedsAdapter(List<CitizenNeedModel> needsList) {
        this.needsList = needsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_citizen_need, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CitizenNeedModel need = needsList.get(position);
        holder.tvRegion.setText(need.getRegion());
        holder.tvNeedType.setText(need.getNeedType());
        
        holder.btnView.setOnClickListener(v -> {
            // Handle view report detail
        });
    }

    @Override
    public int getItemCount() {
        return needsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRegion, tvNeedType, btnView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRegion = itemView.findViewById(R.id.tvNeedRegion);
            tvNeedType = itemView.findViewById(R.id.tvNeedType);
            btnView = itemView.findViewById(R.id.btnViewReport);
        }
    }
}
