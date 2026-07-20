package com.example.graduationproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NeighborAdapter extends RecyclerView.Adapter<NeighborAdapter.NeighborViewHolder> {

    private List<Neighbor> neighborList;
    private OnNeighborRemoveListener removeListener;

    public interface OnNeighborRemoveListener {
        void onRemove(int position);
    }

    public NeighborAdapter(List<Neighbor> neighborList, OnNeighborRemoveListener removeListener) {
        this.neighborList = neighborList;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public NeighborViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_neighbor, parent, false);
        return new NeighborViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NeighborViewHolder holder, int position) {
        Neighbor neighbor = neighborList.get(position);
        holder.tvName.setText(neighbor.getName());
        holder.tvQuantity.setText(neighbor.getQuantity() + " لتر");
        holder.btnRemove.setOnClickListener(v -> removeListener.onRemove(position));
    }

    @Override
    public int getItemCount() {
        return neighborList.size();
    }

    static class NeighborViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity;
        ImageView btnRemove;

        public NeighborViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvNeighborName);
            tvQuantity = itemView.findViewById(R.id.tvNeighborQuantity);
            btnRemove = itemView.findViewById(R.id.btnRemoveNeighbor);
        }
    }
}
