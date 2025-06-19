package com.example.moneydealer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.moneydealer.R;
import com.example.moneydealer.models.Transaction;
import com.example.moneydealer.models.Category;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    private List<Transaction> transactions;
    private Map<String, Category> categoryMap;
    private String currencySymbol;

    public TransactionAdapter(List<Transaction> transactions, Map<String, Category> categoryMap, String currencySymbol) {
        this.transactions = transactions;
        this.categoryMap = categoryMap;
        this.currencySymbol = currencySymbol;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction tx = transactions.get(position);
        Category cat = categoryMap.get(tx.categoryId);
        holder.tvTransactionName.setText(cat != null ? cat.name : "-");
        NumberFormat nf = NumberFormat.getInstance(new Locale("ru", "RU"));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        String amountStr = nf.format(tx.amount) + " " + currencySymbol;
        holder.tvTransactionAmount.setText(amountStr);
        holder.tvTransactionAmount.setTextColor(holder.itemView.getResources().getColor(
            "income".equals(tx.type) ? R.color.income_green : R.color.expense_red
        ));
        holder.ivCategoryIcon.setColorFilter(cat != null ? cat.color : 0xFFAAAAAA);
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        holder.tvTransactionDate.setText(sdf.format(new Date(tx.timestamp)));
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategoryIcon;
        TextView tvTransactionName, tvTransactionAmount, tvTransactionDate;
        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvTransactionName = itemView.findViewById(R.id.tvTransactionName);
            tvTransactionAmount = itemView.findViewById(R.id.tvTransactionAmount);
            tvTransactionDate = itemView.findViewById(R.id.tvTransactionDate);
        }
    }
} 