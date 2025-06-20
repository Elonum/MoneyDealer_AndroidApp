package com.example.moneydealer;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.app.AlertDialog;
import android.app.DatePickerDialog;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.moneydealer.data.FinanceRepository;
import com.example.moneydealer.models.Category;
import com.example.moneydealer.models.Transaction;
import com.google.firebase.database.DatabaseError;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.moneydealer.models.Account;
import com.example.moneydealer.models.User;
import com.example.moneydealer.utils.CurrencyUtils;
import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.NumberFormat;
import java.util.Locale;
import java.lang.NumberFormatException;
import java.util.Calendar;
import java.util.Collections;

public class MainWindow extends AppCompatActivity {

    public static class CategoryBarItem {
        public String name;
        public float amount;
        public int color;
        public CategoryBarItem(String name, float amount, int color) {
            this.name   = name;
            this.amount = amount;
            this.color  = color;
        }
    }

    private FinanceRepository repo;
    private List<Category> categories;
    private List<Account> accounts = new ArrayList<>();
    private Account selectedAccount = null;
    private String currencySymbol = "";
    private TextView tvAccountName, tvAmount, tvTotalTypeAmount;
    private SharedPreferences prefs;
    private FirebaseUser currentUser;
    private DatabaseReference usersRef;
    private String selectedType = "expense"; // "income" или "expense"
    private List<Transaction> allTransactions = new ArrayList<>();
    private LinearLayout categoryProgressBar;
    private LinearLayout categoryLabelsBar;
    private LinearLayout percentBarContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.main_window);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        repo = new FinanceRepository();
        repo.seedDefaultCategoriesIfEmpty();
        categories = new ArrayList<>();
        loadCategoriesAndTransactions();

        tvAccountName = findViewById(R.id.accountName);
        tvAmount = findViewById(R.id.amount);
        tvTotalTypeAmount = findViewById(R.id.tvTotalTypeAmount);
        prefs = getSharedPreferences("com.example.moneydealer", MODE_PRIVATE);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        loadUserCurrencyAndAccounts();

        // --- Navigation Drawer ---
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        ImageView menuIcon = findViewById(R.id.menuIcon);
        DrawerHelper.setupDrawer(this, drawerLayout, navigationView, menuIcon);

        categoryLabelsBar = findViewById(R.id.categoryLabelsBar);
        categoryProgressBar = findViewById(R.id.categoryProgressBar);
        percentBarContainer = findViewById(R.id.percentBarContainer);

        MaterialButtonToggleGroup toggleType = findViewById(R.id.toggleType);
        Button btnExpenses = findViewById(R.id.btnExpenses);
        Button btnIncome = findViewById(R.id.btnIncome);
        // Восстанавливаем выбранный тип из prefs
        selectedType = prefs.getString("selected_type", "expense");
        if ("income".equals(selectedType)) {
            toggleType.check(R.id.btnIncome);
        } else {
            toggleType.check(R.id.btnExpenses);
        }
        toggleType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnExpenses) {
                    selectedType = "expense";
                } else if (checkedId == R.id.btnIncome) {
                    selectedType = "income";
                }
                prefs.edit().putString("selected_type", selectedType).apply();
                updateCategoryBar();
            }
        });

        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> showAddTransactionDialog());

        Button historyButton = findViewById(R.id.historyButton);
        historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainWindow.this, TransactionWindow.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Восстанавливаем выбранный тип из prefs
        selectedType = prefs.getString("selected_type", "expense");
        MaterialButtonToggleGroup toggleType = findViewById(R.id.toggleType);
        if ("income".equals(selectedType)) {
            toggleType.check(R.id.btnIncome);
        } else {
            toggleType.check(R.id.btnExpenses);
        }
        updateCategoryBar();
    }

    private void loadCategoriesAndTransactions() {
        repo.loadCategories(new FinanceRepository.CategoriesCallback() {
            @Override
            public void onLoaded(List<Category> cats) {
                categories = cats;
                long now = System.currentTimeMillis();
                // Загружаем все транзакции за всё время
                repo.loadTransactions(0, now, new FinanceRepository.TransactionsCallback() {
                    @Override
                    public void onLoaded(List<Transaction> txs) {
                        allTransactions = txs;
                        runOnUiThread(() -> updateCategoryBar());
                    }
                    @Override
                    public void onError(DatabaseError error) {
                        // TODO: показать ошибку
                    }
                });
            }
            @Override
            public void onError(DatabaseError error) {
                // TODO: показать ошибку
            }
        });
    }

    private void updateCategoryBar() {
        if (selectedAccount == null) return;
        // Фильтруем транзакции по счёту и типу
        List<Transaction> filtered = new ArrayList<>();
        float total = 0f;
        for (Transaction t : allTransactions) {
            if (selectedAccount.id.equals(t.accountId) && selectedType.equals(t.type)) {
                filtered.add(t);
                total += t.amount;
            }
        }
        // Обновляем сумму доходов/расходов
        NumberFormat nf = NumberFormat.getInstance(new Locale("ru", "RU"));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        String symbol = currencySymbol != null ? currencySymbol : "₽";
        String label = ("income".equals(selectedType) ? "Доходы: " : "Расходы: ");
        tvTotalTypeAmount.setText(label + nf.format(total) + " " + symbol);
        drawCategoryBarWithLabels(filtered);
    }

    private void drawCategoryBarWithLabels(List<Transaction> txs) {
        categoryProgressBar.removeAllViews();
        categoryLabelsBar.removeAllViews();
        percentBarContainer.removeAllViews();
        Map<String, Float> sums = new HashMap<>();
        float total = 0f;
        for (Transaction t : txs) {
            float prev = sums.getOrDefault(t.categoryId, 0f);
            sums.put(t.categoryId, prev + t.amount);
            total += t.amount;
        }
        final float totalFinal = total;
        List<CategoryBarItem> items = new ArrayList<>();
        for (Category c : categories) {
            float amt = sums.getOrDefault(c.id, 0f);
            if (amt > 0f && c.type.equals(selectedType)) {
                items.add(new CategoryBarItem(c.name, amt, c.color));
            }
        }
        // Сортировка по проценту (amount/total) по убыванию
        if (totalFinal > 0) {
            Collections.sort(items, (a, b) -> Float.compare(b.amount / totalFinal, a.amount / totalFinal));
        }
        // Добавляем анимированные подписи в percentBarContainer (вертикально)
        for (int i = 0; i < items.size(); i++) {
            CategoryBarItem item = items.get(i);
            float percent = totalFinal > 0 ? (item.amount / totalFinal * 100f) : 0f;
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            row.setPadding(16, 8, 16, 8);

            TextView tvName = new TextView(this);
            tvName.setText(item.name);
            tvName.setTextColor(item.color);
            tvName.setTextSize(15f);
            tvName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f));

            TextView tvPercent = new TextView(this);
            tvPercent.setText(String.format("%.0f%%", percent));
            tvPercent.setTextColor(item.color);
            tvPercent.setTextSize(15f);
            tvPercent.setGravity(Gravity.CENTER);
            tvPercent.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView tvAmount = new TextView(this);
            tvAmount.setText(String.format("%.2f %s", item.amount, currencySymbol));
            tvAmount.setTextColor(item.color);
            tvAmount.setTextSize(15f);
            tvAmount.setGravity(Gravity.END);
            tvAmount.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f));

            row.addView(tvName);
            row.addView(tvPercent);
            row.addView(tvAmount);
            row.setAlpha(0f);
            row.setScaleX(0.8f);
            row.setScaleY(0.8f);
            percentBarContainer.addView(row);
            row.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .setStartDelay(i * 80)
                .start();
        }
        // Добавляем полоску
        float sum = 0f;
        for (CategoryBarItem i : items) sum += i.amount;
        for (CategoryBarItem cat : items) {
            View segment = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    cat.amount / sum
            );
            params.setMarginEnd(2);
            segment.setLayoutParams(params);
            segment.setBackgroundColor(cat.color);
            categoryProgressBar.addView(segment);
        }
    }

    private void loadUserCurrencyAndAccounts() {
        if (currentUser == null) return;
        usersRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                String code = user != null ? user.selectedCurrency : null;
                currencySymbol = CurrencyUtils.getCurrencySymbol(code);
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
                selectInitialAccount();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void selectInitialAccount() {
        String savedId = prefs.getString("selected_account_id", null);
        selectedAccount = null;
        for (Account acc : accounts) {
            if (acc.id.equals(savedId)) {
                selectedAccount = acc;
                break;
            }
        }
        if (selectedAccount == null && !accounts.isEmpty()) {
            selectedAccount = accounts.get(0);
        }
        updateHeader();
    }

    private void updateHeader() {
        NumberFormat nf = NumberFormat.getInstance(new Locale("ru", "RU"));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        if (selectedAccount != null) {
            double income = 0, expense = 0;
            for (Transaction t : allTransactions) {
                if (t.accountId.equals(selectedAccount.id)) {
                    if ("income".equals(t.type)) income += t.amount;
                    else if ("expense".equals(t.type)) expense += t.amount;
                }
            }
            double balance = selectedAccount.initialBalance + income - expense;
            String formatted = nf.format(balance);
            tvAccountName.setText(selectedAccount.name + " ▼");
            tvAmount.setText(formatted + " " + currencySymbol);
        } else {
            tvAccountName.setText("Нет счетов ▼");
            tvAmount.setText("-");
        }
        tvAccountName.setOnClickListener(v -> showSelectAccountDialog());
    }

    private void showSelectAccountDialog() {
        if (accounts.isEmpty()) return;
        NumberFormat nf = NumberFormat.getInstance(new Locale("ru", "RU"));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_select_account, null);
        android.widget.RadioGroup rgAccounts = dialogView.findViewById(R.id.rgAccounts);
        int checkedId = -1;
        for (int i = 0; i < accounts.size(); i++) {
            Account acc = accounts.get(i);
            android.widget.RadioButton rb = new android.widget.RadioButton(this);
            rb.setId(i);
            double shownBalance = acc.balance;
            // fallback если вдруг balance не задан
            if (shownBalance == 0 && acc.getBalance() != 0) shownBalance = acc.getBalance();
            rb.setText(acc.name + "    " + nf.format(shownBalance) + " " + currencySymbol);
            rb.setTextColor(getResources().getColor(android.R.color.white));
            rb.setButtonTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.welcome_orange)));
            rb.setBackgroundResource(R.drawable.rounded_border_gray);
            rb.setPadding(32, 24, 32, 24);
            android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 24);
            rb.setLayoutParams(params);
            if (selectedAccount != null && acc.id.equals(selectedAccount.id)) {
                rb.setChecked(true);
                checkedId = i;
            }
            rgAccounts.addView(rb);
        }
        builder.setView(dialogView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(androidx.core.content.ContextCompat.getColor(this, R.color.background_color)));
        }
        rgAccounts.setOnCheckedChangeListener((group, checked) -> {
            if (checked >= 0 && checked < accounts.size()) {
                selectedAccount = accounts.get(checked);
                prefs.edit().putString("selected_account_id", selectedAccount.id).apply();
                prefs.edit().putString("selected_type", selectedType).apply();
                MaterialButtonToggleGroup toggleType = findViewById(R.id.toggleType);
                if ("income".equals(selectedType)) {
                    toggleType.check(R.id.btnIncome);
                } else {
                    toggleType.check(R.id.btnExpenses);
                }
                updateHeader();
                updateCategoryBar();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void showAddTransactionDialog() {
        if (selectedAccount == null) return;
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_transaction, null);
        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        EditText etComment = dialogView.findViewById(R.id.etComment);
        Button btnSave = dialogView.findViewById(R.id.btnSaveTransaction);
        TextView tvSelectedDate = dialogView.findViewById(R.id.tvSelectedDate);
        TextView tvSelectedTime = dialogView.findViewById(R.id.tvSelectedTime);
        Button btnPickDate = dialogView.findViewById(R.id.btnPickDate);
        Button btnPickTime = dialogView.findViewById(R.id.btnPickTime);
        // Фильтруем категории по типу
        List<Category> filteredCategories = new ArrayList<>();
        for (Category c : categories) {
            if (c.type != null && c.type.equals(selectedType)) filteredCategories.add(c);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (Category c : filteredCategories) adapter.add(c.name);
        spinnerCategory.setAdapter(adapter);
        // Дата и время по умолчанию — сейчас
        final Calendar selectedDate = Calendar.getInstance();
        updateDateText(tvSelectedDate, selectedDate);
        updateTimeText(tvSelectedTime, selectedDate);
        btnPickDate.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedDate.set(Calendar.YEAR, year);
                selectedDate.set(Calendar.MONTH, month);
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateText(tvSelectedDate, selectedDate);
                updateTimeText(tvSelectedTime, selectedDate);
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)).show();
        });
        btnPickTime.setOnClickListener(v -> {
            int hour = selectedDate.get(Calendar.HOUR_OF_DAY);
            int minute = selectedDate.get(Calendar.MINUTE);
            new android.app.TimePickerDialog(this, (view, h, m) -> {
                selectedDate.set(Calendar.HOUR_OF_DAY, h);
                selectedDate.set(Calendar.MINUTE, m);
                updateTimeText(tvSelectedTime, selectedDate);
            }, hour, minute, true).show();
        });
        builder.setView(dialogView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(androidx.core.content.ContextCompat.getColor(this, R.color.background_color)));
        }
        btnSave.setOnClickListener(v -> {
            String amountStr = etAmount.getText().toString().trim();
            if (amountStr.isEmpty()) {
                etAmount.setError("Введите сумму");
                return;
            }
            float amount;
            try {
                amount = Float.parseFloat(amountStr);
            } catch (NumberFormatException e) {
                etAmount.setError("Некорректная сумма");
                return;
            }
            if (amount <= 0) {
                etAmount.setError("Сумма должна быть больше нуля");
                return;
            }
            int catPos = spinnerCategory.getSelectedItemPosition();
            if (catPos < 0 || catPos >= filteredCategories.size()) {
                Toast.makeText(this, "Выберите категорию", Toast.LENGTH_SHORT).show();
                return;
            }
            Category cat = filteredCategories.get(catPos);
            String comment = etComment.getText().toString().trim();
            if (comment.length() > 100) {
                etComment.setError("Комментарий не должен превышать 100 символов");
                return;
            }
            // Проверка даты (не в будущем)
            Calendar now = Calendar.getInstance();
            Calendar selectedCopy = (Calendar) selectedDate.clone();
            selectedCopy.set(Calendar.SECOND, 0);
            selectedCopy.set(Calendar.MILLISECOND, 0);
            if (selectedCopy.after(now)) {
                Toast.makeText(this, "Дата и время не могут быть в будущем", Toast.LENGTH_SHORT).show();
                return;
            }
            long timestamp = selectedCopy.getTimeInMillis();
            Transaction tx = new Transaction(null, selectedAccount.id, amount, cat.id, selectedType, timestamp, comment);
            repo.addTransaction(tx);
            allTransactions.add(tx);
            // --- Обновление баланса счета в Firebase ---
            double income = 0, expense = 0;
            for (Transaction t : allTransactions) {
                if (t.accountId.equals(selectedAccount.id)) {
                    if ("income".equals(t.type)) income += t.amount;
                    else if ("expense".equals(t.type)) expense += t.amount;
                }
            }
            double newBalance = selectedAccount.initialBalance + income - expense;
            usersRef.child(currentUser.getUid())
                .child("accounts").child(selectedAccount.id)
                .child("balance").setValue(newBalance);
            updateHeader();
            updateCategoryBar();
            Toast.makeText(this, "Операция добавлена", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            // Обновить данные
            loadCategoriesAndTransactions();
        });
        dialog.show();
    }

    private void updateDateText(TextView tv, Calendar cal) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault());
        tv.setText("Дата: " + sdf.format(cal.getTime()));
    }
    private void updateTimeText(TextView tv, Calendar cal) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        tv.setText("Время: " + sdf.format(cal.getTime()));
    }
}
