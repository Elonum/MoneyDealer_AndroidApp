package com.example.moneydealer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.navigation.NavigationView;
import com.example.moneydealer.models.RegularPayment;
import com.example.moneydealer.models.Category;
import com.example.moneydealer.adapters.TransactionAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.moneydealer.adapters.RegularPaymentAdapter;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.example.moneydealer.models.Account;
import com.example.moneydealer.data.FinanceRepository;
import java.util.Calendar;
import java.util.Map;
import android.graphics.Rect;
import android.content.res.Resources;

public class RegularPaymentWindow extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView menuIcon;
    private TextView tvTitle;
    private RecyclerView rvRegularPayments;
    private Button btnAddRegularPayment;
    private List<RegularPayment> regularPayments = new ArrayList<>();
    private RegularPaymentAdapter adapter;
    private DatabaseReference regularPaymentsRef;
    private FirebaseUser currentUser;
    private TextView tvEmptyRegularPayments;
    private List<Account> accounts = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();
    private FinanceRepository repo;
    private Map<String, Category> categoryMap = new java.util.HashMap<>();
    private Map<String, Account> accountMap = new java.util.HashMap<>();
    private RegularPayment editingPayment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.regular_payment_window);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        menuIcon = findViewById(R.id.menuIcon);
        tvTitle = findViewById(R.id.tvTitle);
        rvRegularPayments = findViewById(R.id.rvRegularPayments);
        btnAddRegularPayment = findViewById(R.id.btnAddRegularPayment);
        tvEmptyRegularPayments = findViewById(R.id.tvEmptyRegularPayments);
        DrawerHelper.setupDrawer(this, drawerLayout, navigationView, menuIcon);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        regularPaymentsRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid()).child("regular_payments");
        adapter = new RegularPaymentAdapter(regularPayments, new RegularPaymentAdapter.OnRegularPaymentClickListener() {
            @Override
            public void onEditClick(RegularPayment payment) {
                editingPayment = payment;
                showAddRegularPaymentDialog(payment);
            }
            @Override
            public void onDeleteClick(RegularPayment payment) {
                new androidx.appcompat.app.AlertDialog.Builder(RegularPaymentWindow.this)
                        .setTitle("Удалить платёж?")
                        .setMessage("Вы уверены, что хотите удалить этот регулярный платёж?")
                        .setPositiveButton("Удалить", (dialog, which) -> {
                            regularPaymentsRef.child(payment.id).removeValue();
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
            }
        }, categoryMap, accountMap);
        rvRegularPayments.setLayoutManager(new LinearLayoutManager(this));
        rvRegularPayments.setAdapter(adapter);
        rvRegularPayments.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                int spacing = (int) (12 * Resources.getSystem().getDisplayMetrics().density);
                if (parent.getChildAdapterPosition(view) != parent.getAdapter().getItemCount() - 1) {
                    outRect.bottom = spacing;
                }
            }
        });
        loadRegularPayments();
        btnAddRegularPayment.setOnClickListener(v -> showAddRegularPaymentDialog());
        repo = new FinanceRepository();
        loadAccounts();
        loadCategories();
    }

    private void loadRegularPayments() {
        regularPaymentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                regularPayments.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    RegularPayment rp = snap.getValue(RegularPayment.class);
                    if (rp != null) regularPayments.add(rp);
                }
                adapter.setRegularPayments(regularPayments);
                if (regularPayments.isEmpty()) {
                    tvEmptyRegularPayments.setVisibility(View.VISIBLE);
                    rvRegularPayments.setVisibility(View.GONE);
                } else {
                    tvEmptyRegularPayments.setVisibility(View.GONE);
                    rvRegularPayments.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadAccounts() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.child(currentUser.getUid()).child("accounts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                accounts.clear();
                accountMap.clear();
                for (DataSnapshot accountSnapshot : snapshot.getChildren()) {
                    Account account = accountSnapshot.getValue(Account.class);
                    if (account != null) {
                        accounts.add(account);
                        accountMap.put(account.id, account);
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadCategories() {
        repo.loadCategories(new FinanceRepository.CategoriesCallback() {
            @Override
            public void onLoaded(List<Category> cats) {
                categories = cats;
                categoryMap.clear();
                for (Category c : cats) categoryMap.put(c.id, c);
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onError(DatabaseError error) {}
        });
    }

    private void showAddRegularPaymentDialog() {
        showAddRegularPaymentDialog(null);
    }

    private void showAddRegularPaymentDialog(RegularPayment toEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_regular_payment, null);
        Spinner spinnerAccount = dialogView.findViewById(R.id.spinnerAccount);
        RadioGroup rgType = dialogView.findViewById(R.id.rgType);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        EditText etComment = dialogView.findViewById(R.id.etComment);
        Spinner spinnerPeriod = dialogView.findViewById(R.id.spinnerPeriod);
        TextView tvSelectedDate = dialogView.findViewById(R.id.tvSelectedDate);
        TextView tvSelectedTime = dialogView.findViewById(R.id.tvSelectedTime);
        Button btnPickDate = dialogView.findViewById(R.id.btnPickDate);
        Button btnPickTime = dialogView.findViewById(R.id.btnPickTime);
        Button btnSave = dialogView.findViewById(R.id.btnSaveRegularPayment);

        // Заполняем счета
        ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (Account acc : accounts) accountAdapter.add(acc.name);
        spinnerAccount.setAdapter(accountAdapter);

        // Типы и периоды
        String[] periodValues = {"Каждый день", "Каждую неделю", "Каждый месяц", "Каждый год"};
        ArrayAdapter<String> periodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, periodValues);
        periodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPeriod.setAdapter(periodAdapter);

        // Категории по типу
        RadioButton rbExpense = dialogView.findViewById(R.id.rbExpense);
        RadioButton rbIncome = dialogView.findViewById(R.id.rbIncome);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
        rgType.setOnCheckedChangeListener((group, checkedId) -> {
            String type = checkedId == R.id.rbIncome ? "income" : "expense";
            categoryAdapter.clear();
            for (Category c : categories) if (c.type != null && c.type.equals(type)) categoryAdapter.add(c.name);
            if (categoryAdapter.getCount() > 0) spinnerCategory.setSelection(0);
        });

        // Если редактируем, заполняем поля
        final Calendar selectedDate = Calendar.getInstance();
        if (toEdit != null) {
            int accPos = 0;
            for (int i = 0; i < accounts.size(); i++) if (accounts.get(i).id.equals(toEdit.accountId)) accPos = i;
            spinnerAccount.setSelection(accPos);
            if (toEdit.type != null && toEdit.type.equals("income")) rgType.check(R.id.rbIncome);
            else rgType.check(R.id.rbExpense);
            categoryAdapter.clear();
            for (Category c : categories) if (c.type != null && c.type.equals(toEdit.type)) categoryAdapter.add(c.name);
            int catPos = 0;
            for (int i = 0; i < categoryAdapter.getCount(); i++) if (categories.get(i).id.equals(toEdit.categoryId)) catPos = i;
            spinnerCategory.setSelection(catPos);
            etAmount.setText(String.valueOf(toEdit.amount));
            etComment.setText(toEdit.comment);
            int periodPos = 0;
            for (int i = 0; i < periodValues.length; i++) if (periodValues[i].equals(toEdit.period)) periodPos = i;
            spinnerPeriod.setSelection(periodPos);
            selectedDate.setTimeInMillis(toEdit.startTimestamp);
        } else {
            rgType.check(R.id.rbExpense);
            categoryAdapter.clear();
            for (Category c : categories) if (c.type != null && c.type.equals("expense")) categoryAdapter.add(c.name);
            if (categoryAdapter.getCount() > 0) spinnerCategory.setSelection(0);
        }
        updateDateText(tvSelectedDate, selectedDate);
        updateTimeText(tvSelectedTime, selectedDate);
        btnPickDate.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedDate.set(Calendar.YEAR, year);
                selectedDate.set(Calendar.MONTH, month);
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateText(tvSelectedDate, selectedDate);
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)).show();
        });
        btnPickTime.setOnClickListener(v -> {
            int hour = selectedDate.get(Calendar.HOUR_OF_DAY);
            int minute = selectedDate.get(Calendar.MINUTE);
            new TimePickerDialog(this, (view, h, m) -> {
                selectedDate.set(Calendar.HOUR_OF_DAY, h);
                selectedDate.set(Calendar.MINUTE, m);
                updateTimeText(tvSelectedTime, selectedDate);
            }, hour, minute, true).show();
        });

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(androidx.core.content.ContextCompat.getColor(this, R.color.background_color)));
        }
        btnSave.setOnClickListener(v -> {
            int accountPos = spinnerAccount.getSelectedItemPosition();
            int categoryPos = spinnerCategory.getSelectedItemPosition();
            if (accountPos < 0 || categoryPos < 0) {
                Toast.makeText(this, "Выберите счёт и категорию", Toast.LENGTH_SHORT).show();
                return;
            }
            String amountStr = etAmount.getText().toString().trim();
            if (amountStr.isEmpty()) {
                etAmount.setError("Введите сумму");
                return;
            }
            float amount;
            try { amount = Float.parseFloat(amountStr); } catch (NumberFormatException e) {
                etAmount.setError("Некорректная сумма");
                return;
            }
            String comment = etComment.getText().toString().trim();
            String period = spinnerPeriod.getSelectedItem().toString();
            String type = rgType.getCheckedRadioButtonId() == R.id.rbIncome ? "income" : "expense";
            Account selectedAccount = accounts.get(accountPos);
            String accountId = selectedAccount.id;
            String categoryName = categoryAdapter.getItem(categoryPos);
            String categoryId = null;
            for (Category c : categories) if (c.name.equals(categoryName) && c.type.equals(type)) categoryId = c.id;
            if (categoryId == null) {
                Toast.makeText(this, "Ошибка категории", Toast.LENGTH_SHORT).show();
                return;
            }
            long startTimestamp = selectedDate.getTimeInMillis();
            if (startTimestamp < System.currentTimeMillis()) {
                Toast.makeText(this, "Дата не может быть в прошлом", Toast.LENGTH_SHORT).show();
                return;
            }
            String id = (toEdit != null) ? toEdit.id : regularPaymentsRef.push().getKey();
            RegularPayment rp = new RegularPayment(id, accountId, amount, categoryId, type, comment, startTimestamp, period, (toEdit != null ? toEdit.lastAppliedTimestamp : 0));
            regularPaymentsRef.child(id).setValue(rp).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, (toEdit != null ? "Изменения сохранены" : "Регулярный платёж добавлен"), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        });
        dialog.show();
    }

    private void updateDateText(TextView tv, Calendar cal) {
        tv.setText("Дата: " + String.format("%02d.%02d.%04d", cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH)+1, cal.get(Calendar.YEAR)));
    }
    private void updateTimeText(TextView tv, Calendar cal) {
        tv.setText("Время: " + String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)));
    }
} 