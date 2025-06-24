package com.example.moneydealer;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WelcomeWindow1 extends AppCompatActivity {

    private OnSwipeTouchListener onSwipeTouchListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.welcome_window_1);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        View contentLayout = findViewById(R.id.contentLayout);
        View progressBar = findViewById(R.id.progressBar);
        contentLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
            usersRef.child(user.getUid()).child("selectedCurrency")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String currency = snapshot.getValue(String.class);
                        Intent intent;
                        if (currency != null && !currency.isEmpty()) {
                            intent = new Intent(WelcomeWindow1.this, MainWindow.class);
                            intent.putExtra("selectedCurrency", currency);
                        } else {
                            intent = new Intent(WelcomeWindow1.this, CurrencyWindow.class);
                        }
                        startActivity(intent);
                        finish();
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        Intent intent = new Intent(WelcomeWindow1.this, CurrencyWindow.class);
                        startActivity(intent);
                        finish();
                    }
                });
            return;
        }
        // Если не авторизован — показываем основной контент, скрываем ProgressBar
        contentLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        setupNavigation();
        setupAnimations();
        setupTextAnimations();
    }

    private void setupNavigation() {
        onSwipeTouchListener = new OnSwipeTouchListener(this, new OnSwipeTouchListener.OnSwipeListener() {
            @Override
            public void onSwipeLeft() {
                Intent intent = new Intent(WelcomeWindow1.this, WelcomeWindow2.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }
        });

        Button btnNext = findViewById(R.id.btn_next);
        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeWindow1.this, WelcomeWindow2.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        ImageView radioBtn2 = findViewById(R.id.radioBtn_2);
        ImageView radioBtn3 = findViewById(R.id.radioBtn_3);

        radioBtn2.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeWindow1.this, WelcomeWindow2.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        radioBtn3.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeWindow1.this, WelcomeWindow3.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });
    }

    private void setupAnimations() {
        ImageView ivSectionIllustration = findViewById(R.id.category_cash_account_ic);
        Animation bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.bounce_vertical);
        ivSectionIllustration.startAnimation(bounceAnimation);
    }

    private void setupTextAnimations() {
        TextView tvSectionTitle = findViewById(R.id.tv_section_title);
        TextView tvSectionDescription = findViewById(R.id.tv_section_description);

        Animation fadeInSlideUp = AnimationUtils.loadAnimation(this, R.anim.fade_in_slide_up);

        tvSectionTitle.startAnimation(fadeInSlideUp);

        Animation fadeInSlideUpDelayed = AnimationUtils.loadAnimation(this, R.anim.fade_in_slide_up);
        fadeInSlideUpDelayed.setStartOffset(200);
        tvSectionDescription.startAnimation(fadeInSlideUpDelayed);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return onSwipeTouchListener.onTouch(findViewById(R.id.main), event) || super.onTouchEvent(event);
    }
}