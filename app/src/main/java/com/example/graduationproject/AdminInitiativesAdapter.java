package com.example.graduationproject;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AdminInitiativesAdapter extends RecyclerView.Adapter<AdminInitiativesAdapter.ViewHolder> {

    private List<InitiativeModel> initiativeList;
    private OnInitiativeActionListener listener;

    public interface OnInitiativeActionListener {
        void onApprove(InitiativeModel initiative);
        void onReject(InitiativeModel initiative);
    }

    public AdminInitiativesAdapter(List<InitiativeModel> initiativeList, OnInitiativeActionListener listener) {
        this.initiativeList = initiativeList;
        this.listener = listener;
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
        holder.tvTarget.setText("الهدف: " + initiative.getTargetLiters() + " لتر");
        holder.tvStatus.setText(initiative.getStatus());
        
        // تغيير لون الحالة بناءً على النص
        if ("قيد المراجعة".equals(initiative.getStatus())) {
            holder.tvStatus.setTextColor(Color.parseColor("#F59E0B")); // برتقالي
            holder.layoutActions.setVisibility(View.VISIBLE);
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#0069B4")); // أزرق
            holder.layoutActions.setVisibility(View.GONE);
        }

        holder.pbProgress.setProgress(initiative.getProgressPercentage());
        holder.tvProgressPercent.setText(initiative.getProgressPercentage() + "% مكتمل");

        holder.btnApprove.setOnClickListener(v -> {
            if (listener != null) listener.onApprove(initiative);
        });

        holder.btnReject.setOnClickListener(v -> {
            if (listener != null) listener.onReject(initiative);
        });
    }

    @Override
    public int getItemCount() {
        return initiativeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTarget, tvStatus, tvProgressPercent;
        ProgressBar pbProgress;
        View layoutActions;
        MaterialButton btnApprove, btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvInitiativeTitle);
            tvTarget = itemView.findViewById(R.id.tvInitiativeTarget);
            tvStatus = itemView.findViewById(R.id.tvInitiativeStatus);
            tvProgressPercent = itemView.findViewById(R.id.tvProgressPercent);
            pbProgress = itemView.findViewById(R.id.pbInitiativeProgress);
            layoutActions = itemView.findViewById(R.id.layoutActions);
            btnApprove = itemView.findViewById(R.id.btnApproveInitiative);
            btnReject = itemView.findViewById(R.id.btnRejectInitiative);
        }
    }
}
