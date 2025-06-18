package com.example.moneydealer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class ProfileWindow extends AppCompatActivity {
    private EditText etName, etSurname;
    private TextView tvEmail, tvCurrency;
    private ImageView btnChangeCurrency, ivDeleteUser;
    private Button btnDeleteUser;
    private RecyclerView rvAccounts;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView menuIcon;

    private DatabaseReference usersRef;
    private FirebaseUser currentUser;
    private User user;
    private AccountAdapter accountAdapter;
    private List<Account> accounts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_window);

        etName = findViewById(R.id.etName);
        etSurname = findViewById(R.id.etSurname);
        tvEmail = findViewById(R.id.tvEmail);
        tvCurrency = findViewById(R.id.tvCurrency);
        btnChangeCurrency = findViewById(R.id.btnChangeCurrency);
        ivDeleteUser = findViewById(R.id.ivDeleteUser);
        rvAccounts = findViewById(R.id.rvAccounts);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        menuIcon = findViewById(R.id.menuIcon);

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
        accountAdapter = new AccountAdapter(accounts);
        rvAccounts.setLayoutManager(new LinearLayoutManager(this));
        rvAccounts.setAdapter(accountAdapter);

        loadUserInfo();
        loadAccounts();
        setupListeners();

        findViewById(android.R.id.content).setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                View currentFocus = getCurrentFocus();
                if (currentFocus != null && (currentFocus instanceof EditText)) {
                    currentFocus.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                    }
                }
            }
            return false;
        });
    }

    private void loadUserInfo() {
        if (currentUser == null) return;
        usersRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                if (user != null) {
                    etName.setText(user.name);
                    etSurname.setText(user.surname);
                    tvEmail.setText("\uD83D\uDCE7 Email: " + user.email);
                    tvCurrency.setText("\uD83E\uDE99 Валюта: " + (user.selectedCurrency == null ? "-" : user.selectedCurrency));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
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
                if (accounts.isEmpty()) {
                    TextView tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
                    tvEmptyMessage.setVisibility(View.VISIBLE);
                    rvAccounts.setVisibility(View.GONE);
                } else {
                    TextView tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
                    tvEmptyMessage.setVisibility(View.GONE);
                    rvAccounts.setVisibility(View.VISIBLE);
                }
                accountAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupListeners() {
        btnChangeCurrency.setOnClickListener(v -> {
            // Проверяем, что пользователь авторизован
            if (currentUser == null) {
                Toast.makeText(ProfileWindow.this, "Ошибка: пользователь не найден", Toast.LENGTH_SHORT).show();
                return;
            }
            // Очищаем выбранную валюту в SharedPreferences
            SharedPreferences.Editor editor = getSharedPreferences("com.example.moneydealer", MODE_PRIVATE).edit();
            editor.remove("selected_currency");
            editor.apply();

            // Очищаем выбранную валюту в Firebase
            usersRef.child(currentUser.getUid()).child("selectedCurrency").removeValue((error, ref) -> {
                if (error != null) {
                    Toast.makeText(ProfileWindow.this, "Ошибка сброса валюты: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }
                // После успешного сброса переходим на окно выбора валюты
                Intent intent = new Intent(ProfileWindow.this, CurrencyWindow.class);
                startActivity(intent);
                finish();
            });
        });

        ivDeleteUser.setOnClickListener(v -> {
            if (currentUser == null) return;
            // Диалог подтверждения
            new androidx.appcompat.app.AlertDialog.Builder(ProfileWindow.this)
                .setTitle("Удалить аккаунт?")
                .setMessage("Вы уверены, что хотите удалить аккаунт? Это действие необратимо.")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    usersRef.child(currentUser.getUid()).removeValue((error, ref) -> {
                        if (error == null) {
                            currentUser.delete().addOnCompleteListener(task -> {
                                FirebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(this, WelcomeWindow1.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            });
                        } else {
                            Toast.makeText(this, "Ошибка удаления: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Отмена", null)
                .show();
        });

        etName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) saveNameSurname();
        });
        etSurname.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) saveNameSurname();
        });
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
    }

    private void saveNameSurname() {
        if (currentUser == null || user == null) return;
        String newName = etName.getText().toString().trim();
        String newSurname = etSurname.getText().toString().trim();
        boolean valid = true;

        if (TextUtils.isEmpty(newName) || newName.length() < 2 || !newName.matches("[а-яА-ЯёЁa-zA-Z]+")) {
            etName.setError("Введите корректное имя (только буквы, без пробелов и цифр)");
            valid = false;
        }
        if (TextUtils.isEmpty(newSurname) || newSurname.length() < 2 || !newSurname.matches("[а-яА-ЯёЁa-zA-Z]+")) {
            etSurname.setError("Введите корректную фамилию (только буквы, без пробелов и цифр)");
            valid = false;
        }
        if (!valid) return;

        newName = capitalize(newName);
        newSurname = capitalize(newSurname);

        if (!TextUtils.isEmpty(newName) && !newName.equals(user.name)) {
            usersRef.child(currentUser.getUid()).child("name").setValue(newName);
            user.name = newName;
            etName.setText(newName);
        }
        if (!TextUtils.isEmpty(newSurname) && !newSurname.equals(user.surname)) {
            usersRef.child(currentUser.getUid()).child("surname").setValue(newSurname);
            user.surname = newSurname;
            etSurname.setText(newSurname);
        }
    }
} 