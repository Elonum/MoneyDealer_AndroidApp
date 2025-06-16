package com.example.moneydealer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegistrationWindow extends AppCompatActivity {

    private TextInputLayout tilName, tilSurname, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etName, etSurname, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.registration_window);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        tilName = findViewById(R.id.til_name);
        tilSurname = findViewById(R.id.til_surname);
        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);
        
        etName = findViewById(R.id.et_name);
        etSurname = findViewById(R.id.et_surname);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> attemptRegistration());

        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegistrationWindow.this, LoginWindow.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });
    }

    private void attemptRegistration() {
        tilName.setError(null);
        tilSurname.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        String name = etName.getText().toString().trim();
        String surname = etSurname.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(name)) {
            tilName.setError("Введите имя");
            focusView = etName;
            cancel = true;
        } else if (name.length() < 2) {
            tilName.setError("Имя должно содержать минимум 2 символа");
            focusView = etName;
            cancel = true;
        } else if (!isNameValid(name)) {
            tilName.setError("Имя должно содержать только буквы");
            focusView = etName;
            cancel = true;
        }

        if (TextUtils.isEmpty(surname)) {
            tilSurname.setError("Введите фамилию");
            focusView = etSurname;
            cancel = true;
        } else if (surname.length() < 2) {
            tilSurname.setError("Фамилия должна содержать минимум 2 символа");
            focusView = etSurname;
            cancel = true;
        } else if (!isNameValid(surname)) {
            tilSurname.setError("Фамилия должна содержать только буквы");
            focusView = etSurname;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Введите email");
            focusView = etEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            tilEmail.setError("Некорректный email");
            focusView = etEmail;
            cancel = true;
        } else if (email.contains(" ")) {
            tilEmail.setError("Email не должен содержать пробелы");
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
        } else if (password.contains(" ")) {
            tilPassword.setError("Пароль не должен содержать пробелы");
            focusView = etPassword;
            cancel = true;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError("Подтвердите пароль");
            focusView = etConfirmPassword;
            cancel = true;
        } else if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Пароли не совпадают");
            focusView = etConfirmPassword;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            // Capitalize first letter of name and surname
            name = capitalizeFirstLetter(name);
            surname = capitalizeFirstLetter(surname);
            
            // TODO: Implement actual registration logic
            Toast.makeText(this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(RegistrationWindow.this, MainWindow.class);
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

    private boolean isNameValid(String name) {
        return name.matches("[а-яА-ЯёЁa-zA-Z]+");
    }

    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
}