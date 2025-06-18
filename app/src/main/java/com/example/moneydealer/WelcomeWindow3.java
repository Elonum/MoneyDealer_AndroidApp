package com.example.moneydealer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

public class WelcomeWindow3 extends AppCompatActivity {

    private OnSwipeTouchListener onSwipeTouchListener;
    private boolean isTransitioning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.welcome_window_3);
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
            public void onSwipeRight() {
                if (!isTransitioning) {
                    Intent intent = new Intent(WelcomeWindow3.this, WelcomeWindow2.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    finish();
                }
            }
        });

        Button btnStart = findViewById(R.id.btn_start);
        btnStart.setOnClickListener(v -> {
            if (!isTransitioning) {
                isTransitioning = true;
                startFinalAnimation();
            }
        });

        ImageView radioBtn1 = findViewById(R.id.radioBtn_1);
        ImageView radioBtn2 = findViewById(R.id.radioBtn_2);

        radioBtn1.setOnClickListener(v -> {
            if (!isTransitioning) {
                Intent intent = new Intent(WelcomeWindow3.this, WelcomeWindow1.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            }
        });

        radioBtn2.setOnClickListener(v -> {
            if (!isTransitioning) {
                Intent intent = new Intent(WelcomeWindow3.this, WelcomeWindow2.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            }
        });
    }

    private void setupAnimations() {
        ImageView categoryRegularPayment = findViewById(R.id.category_regular_payment_ic);
        Animation bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.bounce_vertical);
        categoryRegularPayment.startAnimation(bounceAnimation);
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

    private void startFinalAnimation() {
        TextView tvSectionTitle = findViewById(R.id.tv_section_title);
        TextView tvSectionDescription = findViewById(R.id.tv_section_description);
        ImageView categoryRegularPayment = findViewById(R.id.category_regular_payment_ic);
        Button btnStart = findViewById(R.id.btn_start);
        View llIndicators = findViewById(R.id.ll_indicators);

        ImageView ivAppLogo = findViewById(R.id.iv_app_logo);
        TextView tvAppTitle = findViewById(R.id.tv_app_title);

        Animation fadeOutSlideDown = AnimationUtils.loadAnimation(this, R.anim.fade_out_slide_down);
        Animation logoTitleMoveToCenter = AnimationUtils.loadAnimation(this, R.anim.logo_title_move_to_center);

        tvSectionTitle.startAnimation(fadeOutSlideDown);
        tvSectionDescription.startAnimation(fadeOutSlideDown);
        categoryRegularPayment.startAnimation(fadeOutSlideDown);
        btnStart.startAnimation(fadeOutSlideDown);
        llIndicators.startAnimation(fadeOutSlideDown);

        ivAppLogo.startAnimation(logoTitleMoveToCenter);
        tvAppTitle.startAnimation(logoTitleMoveToCenter);

        fadeOutSlideDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                tvSectionTitle.setVisibility(View.GONE);
                tvSectionDescription.setVisibility(View.GONE);
                categoryRegularPayment.setVisibility(View.GONE);
                btnStart.setVisibility(View.GONE);
                llIndicators.setVisibility(View.GONE);

                new Handler().postDelayed(() -> {
                    Intent intent = new Intent(WelcomeWindow3.this, LoginWindow.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                }, 2000);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return onSwipeTouchListener.onTouch(findViewById(R.id.main), event) || super.onTouchEvent(event);
    }
}