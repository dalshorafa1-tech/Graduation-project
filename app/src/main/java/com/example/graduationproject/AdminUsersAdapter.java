package com.example.graduationproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import java.util.List;

public class AdminUsersAdapter extends RecyclerView.Adapter<AdminUsersAdapter.ViewHolder> {

    private List<UserModel> userList;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(UserModel user);
    }

    public AdminUsersAdapter(List<UserModel> userList, OnUserClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel user = userList.get(position);
        holder.tvName.setText(user.getFullName());
        holder.tvEmail.setText(user.getEmail());
        holder.tvRegion.setText(user.getRegion());
        
        String roleText = "الدور: " + (user.getRole() != null ? user.getRole() : "غير محدد");
        if (user.isProvider()) {
            roleText += " (مزود)";
        }
        holder.tvRole.setText(roleText);

        holder.btnDetails.setOnClickListener(v -> {
            if (listener != null) listener.onUserClick(user);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvRole, tvRegion;
        View btnDetails;
        ShapeableImageView ivAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvEmail = itemView.findViewById(R.id.tvUserEmail);
            tvRole = itemView.findViewById(R.id.tvUserRole);
            tvRegion = itemView.findViewById(R.id.tvUserRegion);
            btnDetails = itemView.findViewById(R.id.btnViewDetails);
            ivAvatar = itemView.findViewById(R.id.ivUserAvatar);
        }
    }
}
