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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.moneydealer.models.User;

public class RegistrationWindow extends AppCompatActivity {

    private TextInputLayout tilName, tilSurname, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etName, etSurname, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.registration_window);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        tilName            = findViewById(R.id.til_name);
        tilSurname         = findViewById(R.id.til_surname);
        tilEmail           = findViewById(R.id.til_email);
        tilPassword        = findViewById(R.id.til_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);
        etName             = findViewById(R.id.et_name);
        etSurname          = findViewById(R.id.et_surname);
        etEmail            = findViewById(R.id.et_email);
        etPassword         = findViewById(R.id.et_password);
        etConfirmPassword  = findViewById(R.id.et_confirm_password);
        btnRegister        = findViewById(R.id.btn_register);
        tvLogin            = findViewById(R.id.tv_login);

        btnRegister.setOnClickListener(v -> attemptRegistration());

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegistrationWindow.this, LoginWindow.class));
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

        String name            = etName.getText().toString().trim();
        String surname         = etSurname.getText().toString().trim();
        String email           = etEmail.getText().toString().trim();
        String password        = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(name) || name.length() < 2 || !name.matches("[а-яА-ЯёЁa-zA-Z]+")) {
            tilName.setError("Введите корректное имя");
            focusView = etName;
            cancel = true;
        }
        if (TextUtils.isEmpty(surname) || surname.length() < 2 || !surname.matches("[а-яА-ЯёЁa-zA-Z]+")) {
            tilSurname.setError("Введите корректную фамилию");
            if (focusView == null) focusView = etSurname;
            cancel = true;
        }
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Введите корректный email");
            if (focusView == null) focusView = etEmail;
            cancel = true;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            tilPassword.setError("Пароль должен содержать ≥ 6 символов");
            if (focusView == null) focusView = etPassword;
            cancel = true;
        }
        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Пароли не совпадают");
            if (focusView == null) focusView = etConfirmPassword;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser fUser = auth.getCurrentUser();
                        if (fUser != null) {
                            String uid = fUser.getUid();
                            User user = new User(
                                    capitalize(name),
                                    capitalize(surname),
                                    email
                            );
                            usersRef.child(uid).setValue(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(
                                                RegistrationWindow.this,
                                                "Регистрация успешна!",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                        startActivity(new Intent(
                                                RegistrationWindow.this,
                                                LoginWindow.class
                                        ));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(
                                                RegistrationWindow.this,
                                                "Ошибка записи: " + e.getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show();
                                    });
                        }
                    } else {
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            tilEmail.setError("Пользователь с таким email уже существует");
                            etEmail.requestFocus();
                        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            tilEmail.setError("Некорректный формат email");
                            etEmail.requestFocus();
                        } else {
                            Toast.makeText(
                                    RegistrationWindow.this,
                                    "Ошибка регистрации: " + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                });
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
