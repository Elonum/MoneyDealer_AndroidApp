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