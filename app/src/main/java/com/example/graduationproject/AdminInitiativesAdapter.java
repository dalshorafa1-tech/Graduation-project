package com.example.graduationproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminInitiativesAdapter extends RecyclerView.Adapter<AdminInitiativesAdapter.ViewHolder> {

    private List<InitiativeModel> initiativeList;

    public AdminInitiativesAdapter(List<InitiativeModel> initiativeList) {
        this.initiativeList = initiativeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_initiative, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InitiativeModel initiative = initiativeList.get(position);
        holder.tvTitle.setText(initiative.getTitle());
        holder.tvTarget.setText("الهدف: " + initiative.getTarget());
        holder.tvStatus.setText(initiative.getStatus());
        holder.pbProgress.setProgress(initiative.getProgress());
        holder.tvProgressPercent.setText(initiative.getProgress() + "% مكتمل");
    }

    @Override
    public int getItemCount() {
        return initiativeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTarget, tvStatus, tvProgressPercent;
        ProgressBar pbProgress;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvInitiativeTitle);
            tvTarget = itemView.findViewById(R.id.tvInitiativeTarget);
            tvStatus = itemView.findViewById(R.id.tvInitiativeStatus);
            tvProgressPercent = itemView.findViewById(R.id.tvProgressPercent);
            pbProgress = itemView.findViewById(R.id.pbInitiativeProgress);
        }
    }
}
