package com.example.moneydealer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.moneydealer.adapters.AccountAdapter;
import com.example.moneydealer.models.Account;
import com.example.moneydealer.models.User;
import com.example.moneydealer.utils.CurrencyUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AccountWindow extends AppCompatActivity {
    private RecyclerView rvAccounts;
    private AccountAdapter accountAdapter;
    private List<Account> accounts;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView menuIcon;
    private FloatingActionButton fabAddAccount;
    private DatabaseReference usersRef;
    private FirebaseUser currentUser;
    private String currencySymbol = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_window);

        rvAccounts = findViewById(R.id.rvAccounts);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        menuIcon = findViewById(R.id.menuIcon);
        fabAddAccount = findViewById(R.id.fabAddAccount);

        DrawerHelper.setupDrawer(this, drawerLayout, navigationView, menuIcon);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            v.setPadding(
                insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            );
            return insets;
        });

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        accounts = new ArrayList<>();
        accountAdapter = new AccountAdapter(accounts, currencySymbol);
        rvAccounts.setLayoutManager(new LinearLayoutManager(this));
        rvAccounts.setAdapter(accountAdapter);

        loadUserCurrencyAndAccounts();
        setupListeners();
    }

    private void loadUserCurrencyAndAccounts() {
        if (currentUser == null) return;
        usersRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                String code = user != null ? user.selectedCurrency : null;
                currencySymbol = CurrencyUtils.getCurrencySymbol(code);
                accountAdapter.setCurrencySymbol(currencySymbol);
                loadAccounts();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadAccounts();
            }
        });
    }

    private void loadAccounts() {
        if (currentUser == null) return;
        usersRef.child(currentUser.getUid()).child("accounts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                accounts.clear();
                for (DataSnapshot accountSnapshot : snapshot.getChildren()) {
                    Account account = accountSnapshot.getValue(Account.class);
                    if (account != null) {
                        accounts.add(account);
                    }
                }
                accountAdapter.notifyDataSetChanged();
                findViewById(R.id.tvEmptyMessage).setVisibility(accounts.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AccountWindow.this, "Ошибка загрузки счетов: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        fabAddAccount.setOnClickListener(v -> showAddAccountDialog());
    }

    private void showAddAccountDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_account, null);
        android.widget.EditText etAccountName = dialogView.findViewById(R.id.etAccountName);
        android.widget.EditText etInitialBalance = dialogView.findViewById(R.id.etInitialBalance);
        android.widget.Button btnSave = dialogView.findViewById(R.id.btnSaveAccount);
        builder.setView(dialogView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(androidx.core.content.ContextCompat.getColor(this, R.color.background_color)));
        }
        btnSave.setOnClickListener(v -> {
            String name = etAccountName.getText().toString().trim();
            String balanceStr = etInitialBalance.getText().toString().trim();
            if (name.isEmpty()) {
                etAccountName.setError("Введите название счёта");
                return;
            }
            for (Account acc : accounts) {
                if (acc.name.replaceAll("\\s+", "").equalsIgnoreCase(name.replaceAll("\\s+", ""))) {
                    etAccountName.setError("Счёт с таким именем уже существует");
                    return;
                }
            }
            double initialBalance = 0;
            if (!balanceStr.isEmpty()) {
                try {
                    initialBalance = Double.parseDouble(balanceStr);
                } catch (NumberFormatException e) {
                    etInitialBalance.setError("Некорректный баланс");
                    return;
                }
            }
            if (currentUser == null) return;
            String id = usersRef.child(currentUser.getUid()).child("accounts").push().getKey();
            if (id == null) return;
            long createdAt = System.currentTimeMillis();
            com.example.moneydealer.models.Account account = new com.example.moneydealer.models.Account(id, name, createdAt, initialBalance);
            usersRef.child(currentUser.getUid()).child("accounts").child(id).setValue(account)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Счёт добавлен", Toast.LENGTH_SHORT).show();
                    loadAccounts();
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
        });
        dialog.show();
    }
} 