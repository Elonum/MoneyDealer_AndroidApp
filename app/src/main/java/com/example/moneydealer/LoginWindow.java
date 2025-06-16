package com.example.moneydealer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginWindow extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister, tvForgotPassword;

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
            Intent intent = new Intent(LoginWindow.this, RegistrationWindow.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginWindow.this, ForgotWindow.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });
    }

    private void attemptLogin() {
        tilEmail.setError(null);
        tilPassword.setError(null);

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Введите email");
            focusView = etEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            tilEmail.setError("Некорректный email");
            focusView = etEmail;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Введите пароль");
            focusView = etPassword;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            tilPassword.setError("Пароль должен содержать минимум 6 символов");
            focusView = etPassword;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            // TODO: Implement actual login logic (e.g., API call)

            // For demonstration, assume login is successful
            Toast.makeText(this, "Вход успешен", Toast.LENGTH_SHORT).show();

            // Simulate fetching selected currency from backend
            String selectedCurrency = fetchSelectedCurrencyFromBackend(email); // Pass user ID or email to fetch specific currency

            Intent intent;
            if (selectedCurrency != null && !selectedCurrency.isEmpty()) {
                // Currency already selected, go to Main Window
                intent = new Intent(LoginWindow.this, MainWindow.class);
            } else {
                // No currency selected, go to Currency Selection Window
                intent = new Intent(LoginWindow.this, CurrencyWindow.class);
            }
            startActivity(intent);
            finish();
        }
    }

    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    private String fetchSelectedCurrencyFromBackend(String userIdentifier) {
        // This method would make an API call to your backend to get the user's selected currency.
        // Example (conceptual): ApiService.getUserCurrency(userIdentifier, new Callback() { ... });
        // For now, it's just a placeholder.
        System.out.println("Simulating fetching currency from backend for: " + userIdentifier);

        // For demonstration, let's assume a currency is returned for a specific user, or null/empty otherwise.
        // You would replace this with actual backend logic.
        if (userIdentifier.equals("test@example.com")) { // Example user with a saved currency
            return "USD";
        } else {
            return null; // No currency saved for this user
        }
    }

}