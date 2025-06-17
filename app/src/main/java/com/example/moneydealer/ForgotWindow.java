package com.example.moneydealer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

public class ForgotWindow extends AppCompatActivity {

    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;
    private Button btnSend;
    private TextView tvLogin;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.forgot_window);

        // handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        initializeViews();
        setupClickListeners();
        setupTextAnimations();
    }

    private void initializeViews() {
        tilEmail = findViewById(R.id.til_email);
        etEmail = findViewById(R.id.et_email);
        btnSend  = findViewById(R.id.btn_send);
        tvLogin  = findViewById(R.id.tv_login);
    }

    private void setupClickListeners() {
        btnSend.setOnClickListener(v -> attemptPasswordReset());
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(ForgotWindow.this, LoginWindow.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });
    }

    private void attemptPasswordReset() {
        // clear previous error
        tilEmail.setError(null);

        String email = etEmail.getText().toString().trim();
        boolean cancel = false;
        View focusView = null;

        // validation
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Введите email");
            focusView = etEmail;
            cancel = true;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Некорректный email");
            focusView = etEmail;
            cancel = true;
        } else if (email.contains(" ")) {
            tilEmail.setError("Email не должен содержать пробелы");
            focusView = etEmail;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
            return;
        }

        // send password reset email
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(
                                ForgotWindow.this,
                                "Инструкции по восстановлению пароля отправлены на ваш email",
                                Toast.LENGTH_LONG
                        ).show();
                        // go back to login
                        startActivity(new Intent(ForgotWindow.this, LoginWindow.class));
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        finish();
                    } else {
                        String err = task.getException() != null
                                ? task.getException().getMessage()
                                : "Ошибка отправки письма";
                        tilEmail.setError(err);
                        etEmail.requestFocus();
                    }
                });
    }

    private void setupTextAnimations() {
        TextView tvForgotTitle       = findViewById(R.id.tv_forgot_title);
        TextView tvForgotDescription = findViewById(R.id.tv_forgot_description);

        Animation fadeInSlideUp = AnimationUtils.loadAnimation(this, R.anim.fade_in_slide_up);
        tvForgotTitle.startAnimation(fadeInSlideUp);

        Animation fadeInSlideUpDelayed = AnimationUtils.loadAnimation(this, R.anim.fade_in_slide_up);
        fadeInSlideUpDelayed.setStartOffset(200);
        tvForgotDescription.startAnimation(fadeInSlideUpDelayed);
    }
}
