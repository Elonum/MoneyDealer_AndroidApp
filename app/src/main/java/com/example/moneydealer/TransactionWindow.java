package com.example.moneydealer;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.moneydealer.adapters.TransactionAdapter;
import com.example.moneydealer.models.Transaction;
import com.example.moneydealer.models.User;
import com.example.moneydealer.models.Category;
import com.example.moneydealer.data.FinanceRepository;
import com.example.moneydealer.utils.CurrencyUtils;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import android.app.AlertDialog;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.ViewCompat;
import androidx.core.graphics.Insets;

public class TransactionWindow extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView menuIcon;
    private TextView tvTitle;
    private RecyclerView rvTransactions;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> allTransactions = new ArrayList<>();
    private List<Transaction> filteredTransactions = new ArrayList<>();
    private MaterialButtonToggleGroup toggleType, togglePeriod;
    private String selectedType = "expense";
    private String selectedPeriod = "all";
    private DatabaseReference usersRef;
    private FirebaseUser currentUser;
    private String currencySymbol = "";
    private List<Category> categories = new ArrayList<>();
    private Map<String, Category> categoryMap = new java.util.HashMap<>();
    private FinanceRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.transaction_window);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            androidx.core.graphics.Insets sb = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        menuIcon = findViewById(R.id.menuIcon);
        tvTitle = findViewById(R.id.tvTitle);
        rvTransactions = findViewById(R.id.rvTransactions);
        toggleType = findViewById(R.id.toggleType);
        togglePeriod = findViewById(R.id.togglePeriod);

        DrawerHelper.setupDrawer(this, drawerLayout, navigationView, menuIcon);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        repo = new FinanceRepository();

        transactionAdapter = new TransactionAdapter(filteredTransactions, categoryMap, currencySymbol);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(transactionAdapter);

        loadCategoriesAndTransactions();
        setupToggleListeners();

        transactionAdapter.setOnTransactionLongClickListener((transaction, position) -> {
            new AlertDialog.Builder(this)
                .setTitle("Удалить транзакцию?")
                .setMessage("Вы уверены, что хотите удалить эту транзакцию?")
                .setPositiveButton("Удалить", (dialog, which) -> deleteTransaction(transaction, position))
                .setNegativeButton("Отмена", null)
                .show();
        });
    }

    private void loadCategoriesAndTransactions() {
        repo.loadCategories(new FinanceRepository.CategoriesCallback() {
            @Override
            public void onLoaded(List<Category> cats) {
                categories = cats;
                categoryMap.clear();
                for (Category c : cats) categoryMap.put(c.id, c);
                loadUserCurrencyAndTransactions();
            }
            @Override
            public void onError(DatabaseError error) {
                loadUserCurrencyAndTransactions();
            }
        });
    }

    private void loadUserCurrencyAndTransactions() {
        if (currentUser == null) return;
        usersRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                String code = user != null ? user.selectedCurrency : null;
                currencySymbol = CurrencyUtils.getCurrencySymbol(code);
                transactionAdapter.setCurrencySymbol(currencySymbol);
                loadTransactions();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadTransactions();
            }
        });
    }

    private void loadTransactions() {
        if (currentUser == null) return;
        usersRef.child(currentUser.getUid()).child("transactions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allTransactions.clear();
                for (DataSnapshot txSnapshot : snapshot.getChildren()) {
                    Transaction tx = txSnapshot.getValue(Transaction.class);
                    if (tx != null) {
                        allTransactions.add(tx);
                    }
                }
                applyFilters();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupToggleListeners() {
        toggleType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btnExpenses) selectedType = "expense";
            else if (checkedId == R.id.btnIncome) selectedType = "income";
            applyFilters();
        });
        togglePeriod.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btnDay) selectedPeriod = "day";
            else if (checkedId == R.id.btnWeek) selectedPeriod = "week";
            else if (checkedId == R.id.btnMonth) selectedPeriod = "month";
            else if (checkedId == R.id.btnYear) selectedPeriod = "year";
            else selectedPeriod = "all";
            applyFilters();
        });
    }

    private void applyFilters() {
        filteredTransactions.clear();
        long now = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        for (Transaction tx : allTransactions) {
            if (!selectedType.equals(tx.type)) continue;
            if (selectedPeriod.equals("all")) {
                filteredTransactions.add(tx);
            } else {
                cal.setTimeInMillis(now);
                Calendar txCal = Calendar.getInstance();
                txCal.setTimeInMillis(tx.timestamp);
                boolean add = false;
                if (selectedPeriod.equals("day")) {
                    add = cal.get(Calendar.YEAR) == txCal.get(Calendar.YEAR) &&
                          cal.get(Calendar.DAY_OF_YEAR) == txCal.get(Calendar.DAY_OF_YEAR);
                } else if (selectedPeriod.equals("week")) {
                    add = cal.get(Calendar.YEAR) == txCal.get(Calendar.YEAR) &&
                          cal.get(Calendar.WEEK_OF_YEAR) == txCal.get(Calendar.WEEK_OF_YEAR);
                } else if (selectedPeriod.equals("month")) {
                    add = cal.get(Calendar.YEAR) == txCal.get(Calendar.YEAR) &&
                          cal.get(Calendar.MONTH) == txCal.get(Calendar.MONTH);
                } else if (selectedPeriod.equals("year")) {
                    add = cal.get(Calendar.YEAR) == txCal.get(Calendar.YEAR);
                }
                if (add) filteredTransactions.add(tx);
            }
        }
        transactionAdapter.notifyDataSetChanged();
    }

    private void deleteTransaction(Transaction tx, int position) {
        if (currentUser == null || tx.id == null || tx.accountId == null) return;
        usersRef.child(currentUser.getUid()).child("transactions").child(tx.id).removeValue((error, ref) -> {
            if (error == null) {
                filteredTransactions.remove(position);
                for (int i = 0; i < allTransactions.size(); i++) {
                    if (allTransactions.get(i).id != null && allTransactions.get(i).id.equals(tx.id)) {
                        allTransactions.remove(i);
                        break;
                    }
                }
                transactionAdapter.notifyItemRemoved(position);
                updateAccountBalanceAfterTransactionChange(tx.accountId);
            } else {
                android.widget.Toast.makeText(this, "Ошибка удаления: " + error.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateAccountBalanceAfterTransactionChange(String accountId) {
        usersRef.child(currentUser.getUid()).child("accounts").child(accountId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                com.example.moneydealer.models.Account account = snapshot.getValue(com.example.moneydealer.models.Account.class);
                if (account == null) return;
                double initialBalance = account.initialBalance;
                usersRef.child(currentUser.getUid()).child("transactions").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snap) {
                        double income = 0, expense = 0;
                        for (com.google.firebase.database.DataSnapshot txSnap : snap.getChildren()) {
                            com.example.moneydealer.models.Transaction t = txSnap.getValue(com.example.moneydealer.models.Transaction.class);
                            if (t != null && accountId.equals(t.accountId)) {
                                if ("income".equals(t.type)) income += t.amount;
                                else if ("expense".equals(t.type)) expense += t.amount;
                            }
                        }
                        double newBalance = initialBalance + income - expense;
                        usersRef.child(currentUser.getUid())
                                .child("accounts").child(accountId)
                                .child("balance").setValue(newBalance);
                    }
                    @Override
                    public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
                });
            }
            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
        });
    }
} 