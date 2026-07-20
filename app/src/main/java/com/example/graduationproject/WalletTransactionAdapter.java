package com.example.graduationproject;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WalletTransactionAdapter extends RecyclerView.Adapter<WalletTransactionAdapter.TransactionViewHolder> {

    private Context context;
    private List<TransactionModel> transactions;

    public WalletTransactionAdapter(Context context, List<TransactionModel> transactions) {
        this.context = context;
        this.transactions = transactions;
    }

    public void updateList(List<TransactionModel> newList) {
        this.transactions = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionModel transaction = transactions.get(position);

        holder.tvTransactionTitle.setText(transaction.getDescription());
        
        if (transaction.getTimestamp() != null) {
            Date date = transaction.getTimestamp().toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM، hh:mm a", new Locale("ar"));
            holder.tvTransactionDate.setText(sdf.format(date));
        }

        double amount = transaction.getAmount() != null ? transaction.getAmount() : 0.0;
        if ("recharge".equals(transaction.getType())) {
            holder.tvTransactionAmount.setText(String.format(Locale.getDefault(), "+%.2f ₪", amount));
            holder.tvTransactionAmount.setTextColor(Color.parseColor("#166534"));
            holder.ivTransactionIcon.setImageResource(R.drawable.wallet); // Or a specific recharge icon
        } else {
            holder.tvTransactionAmount.setText(String.format(Locale.getDefault(), "-%.2f ₪", amount));
            holder.tvTransactionAmount.setTextColor(Color.parseColor("#991B1B"));
            holder.ivTransactionIcon.setImageResource(R.drawable.truck); // Or a specific payment icon
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTransactionTitle, tvTransactionDate, tvTransactionAmount;
        ImageView ivTransactionIcon;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTransactionTitle = itemView.findViewById(R.id.tvTransactionTitle);
            tvTransactionDate = itemView.findViewById(R.id.tvTransactionDate);
            tvTransactionAmount = itemView.findViewById(R.id.tvTransactionAmount);
            ivTransactionIcon = itemView.findViewById(R.id.ivTransactionIcon);
        }
    }
}
