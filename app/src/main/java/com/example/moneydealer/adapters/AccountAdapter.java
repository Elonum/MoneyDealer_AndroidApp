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

    public AccountAdapter(List<Account> accounts) {
        this.accounts = accounts;
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
        holder.tvAccountBalance.setText(String.format("%.2f %s", account.getBalance(), account.currency));
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