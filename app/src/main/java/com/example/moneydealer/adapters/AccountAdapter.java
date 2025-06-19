package com.example.moneydealer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.moneydealer.R;
import com.example.moneydealer.models.Account;
import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {
    private List<Account> accounts;
    private OnAccountClickListener clickListener;
    private OnAccountLongClickListener longClickListener;
    private String currencySymbol = "";

    public interface OnAccountClickListener {
        void onAccountClick(Account account);
    }

    public interface OnAccountLongClickListener {
        void onAccountLongClick(Account account);
    }

    public void setOnAccountClickListener(OnAccountClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnAccountLongClickListener(OnAccountLongClickListener listener) {
        this.longClickListener = listener;
    }

    public AccountAdapter(List<Account> accounts, String currencySymbol) {
        this.accounts = accounts;
        this.currencySymbol = currencySymbol;
    }

    public void setCurrencySymbol(String symbol) {
        this.currencySymbol = symbol;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        Account account = accounts.get(position);
        holder.tvAccountName.setText(account.name);
        holder.tvAccountBalance.setText(String.format("%.2f %s", account.getBalance(), currencySymbol));
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onAccountClick(account);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onAccountLongClick(account);
                return true;
            }
            return false;
        });

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

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    static class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView tvAccountName, tvAccountBalance;

        AccountViewHolder(View itemView) {
            super(itemView);
            tvAccountName = itemView.findViewById(R.id.tvAccountName);
            tvAccountBalance = itemView.findViewById(R.id.tvAccountBalance);
        }
    }
} 