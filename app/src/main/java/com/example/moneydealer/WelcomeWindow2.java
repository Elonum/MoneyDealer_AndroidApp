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

public class WelcomeWindow2 extends AppCompatActivity {

    private OnSwipeTouchListener onSwipeTouchListener;
    private boolean isTransitioning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.welcome_window_2);
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
                    Intent intent = new Intent(WelcomeWindow2.this, WelcomeWindow1.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    finish();
                }
            }

            @Override
            public void onSwipeLeft() {
                if (!isTransitioning) {
                    Intent intent = new Intent(WelcomeWindow2.this, WelcomeWindow3.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                }
            }
        });

        Button btnNext = findViewById(R.id.btn_next);
        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeWindow2.this, WelcomeWindow3.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        ImageView radioBtn1 = findViewById(R.id.radioBtn_1);
        ImageView radioBtn3 = findViewById(R.id.radioBtn_3);

        radioBtn1.setOnClickListener(v -> {
            if (!isTransitioning) {
                Intent intent = new Intent(WelcomeWindow2.this, WelcomeWindow1.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            }
        });

        radioBtn3.setOnClickListener(v -> {
            if (!isTransitioning) {
                Intent intent = new Intent(WelcomeWindow2.this, WelcomeWindow3.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }
        });
    }

    private void setupAnimations() {
        ImageView categoryFood = findViewById(R.id.category_gifts_ic);
        Animation bounceAnimation1 = AnimationUtils.loadAnimation(this, R.anim.bounce_vertical);
        categoryFood.startAnimation(bounceAnimation1);

        ImageView categoryTransport = findViewById(R.id.category_health_ic);
        Animation bounceAnimation2 = AnimationUtils.loadAnimation(this, R.anim.bounce_vertical);
        bounceAnimation2.setStartOffset(100);
        categoryTransport.startAnimation(bounceAnimation2);

        ImageView categoryEntertainment = findViewById(R.id.category_sport_ic);
        Animation bounceAnimation3 = AnimationUtils.loadAnimation(this, R.anim.bounce_vertical);
        bounceAnimation3.setStartOffset(200);
        categoryEntertainment.startAnimation(bounceAnimation3);
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