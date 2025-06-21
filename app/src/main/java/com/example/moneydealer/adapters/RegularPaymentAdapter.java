package com.example.moneydealer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneydealer.R;
import com.example.moneydealer.models.RegularPayment;
import com.example.moneydealer.models.Category;
import com.example.moneydealer.models.Account;

import java.util.List;
import java.util.Map;

public class RegularPaymentAdapter extends RecyclerView.Adapter<RegularPaymentAdapter.RegularPaymentViewHolder> {
    private List<RegularPayment> regularPayments;
    private OnRegularPaymentClickListener listener;
    private Map<String, Category> categoryMap;
    private Map<String, Account> accountMap;

    public interface OnRegularPaymentClickListener {
        void onEditClick(RegularPayment payment);
        void onDeleteClick(RegularPayment payment);
    }

    public RegularPaymentAdapter(List<RegularPayment> regularPayments, OnRegularPaymentClickListener listener, Map<String, Category> categoryMap, Map<String, Account> accountMap) {
        this.regularPayments = regularPayments;
        this.listener = listener;
        this.categoryMap = categoryMap;
        this.accountMap = accountMap;
    }

    @NonNull
    @Override
    public RegularPaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_regular_payment, parent, false);
        return new RegularPaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RegularPaymentViewHolder holder, int position) {
        RegularPayment payment = regularPayments.get(position);
        Category cat = categoryMap != null ? categoryMap.get(payment.categoryId) : null;
        String typeStr = "Расход";
        if (payment.type != null && payment.type.equals("income")) typeStr = "Доход";
        String catName = cat != null ? cat.name : payment.categoryId;
        holder.tvName.setText(typeStr + " - " + catName);
        holder.tvAmount.setText(String.format("%.2f", payment.amount) + " " + getAccountCurrency(payment.accountId));
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm");
        holder.tvDate.setText("Следующий: " + sdf.format(new java.util.Date(payment.startTimestamp)));
        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(payment));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(payment));
        // Анимация появления
        holder.itemView.setAlpha(0f);
        holder.itemView.setScaleX(0.8f);
        holder.itemView.setScaleY(0.8f);
        holder.itemView.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500)
            .setStartDelay(position * 60)
            .start();
    }

    private String getAccountCurrency(String accountId) {
        Account acc = accountMap != null ? accountMap.get(accountId) : null;
        // TODO: если у Account появится поле currency, возвращать его, иначе вернуть "₽" по умолчанию
        return "₽";
    }

    @Override
    public int getItemCount() {
        return regularPayments.size();
    }

    public void setRegularPayments(List<RegularPayment> regularPayments) {
        this.regularPayments = regularPayments;
        notifyDataSetChanged();
    }

    static class RegularPaymentViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAmount, tvDate;
        ImageButton btnEdit, btnDelete;

        RegularPaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRegularPaymentName);
            tvAmount = itemView.findViewById(R.id.tvRegularPaymentAmount);
            tvDate = itemView.findViewById(R.id.tvRegularPaymentDate);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
} 