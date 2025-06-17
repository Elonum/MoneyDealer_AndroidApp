package com.example.moneydealer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginWindow extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;

    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login_window);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginWindow.this, RegistrationWindow.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginWindow.this, ForgotWindow.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });
    }

    private void attemptLogin() {
        tilEmail.setError(null);
        tilPassword.setError(null);

        String email = etEmail.getText().toString().trim();
        String pass  = etPassword.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Введите email");
            focusView = etEmail;
            cancel = true;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Некорректный email");
            focusView = etEmail;
            cancel = true;
        }

        if (TextUtils.isEmpty(pass)) {
            tilPassword.setError("Введите пароль");
            if (focusView == null) focusView = etPassword;
            cancel = true;
        } else if (pass.length() < 6) {
            tilPassword.setError("Пароль должен содержать минимум 6 символов");
            if (focusView == null) focusView = etPassword;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
            return;
        }

        auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            checkUserCurrency(user.getUid());
                        }
                    } else {
                        Exception ex = task.getException();
                        if (ex instanceof com.google.firebase.auth.FirebaseAuthInvalidUserException) {
                            tilEmail.setError("Пользователь с таким email не зарегистрирован");
                            etEmail.requestFocus();
                        }
                        else if (ex instanceof com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
                            tilPassword.setError("Неверный email или пароль");
                            etPassword.requestFocus();
                        }
                    }
                });
    }

    private void checkUserCurrency(String uid) {
        usersRef.child(uid).child("selectedCurrency")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String currency = snapshot.getValue(String.class);
                        Intent intent;
                        if (currency != null && !currency.isEmpty()) {
                            intent = new Intent(LoginWindow.this, MainWindow.class);
                            intent.putExtra("selectedCurrency", currency);
                        } else {
                            intent = new Intent(LoginWindow.this, CurrencyWindow.class);
                        }
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Intent intent = new Intent(LoginWindow.this, CurrencyWindow.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }
}
