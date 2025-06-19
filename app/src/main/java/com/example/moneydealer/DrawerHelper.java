package com.example.moneydealer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.widget.ImageView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DrawerHelper {
    public static void setupDrawer(Activity activity, DrawerLayout drawerLayout, NavigationView navigationView, ImageView menuIcon) {
        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                Intent intent = new Intent(activity, ProfileWindow.class);
                activity.startActivity(intent);
            } else if (id == R.id.nav_main) {
                Intent intent = new Intent(activity, MainWindow.class);
                activity.startActivity(intent);
            } else if (id == R.id.nav_cash_accounts) {
                Intent intent = new Intent(activity, AccountWindow.class);
                activity.startActivity(intent);
            } else if (id == R.id.nav_regular) {
                // TODO: Открыть регулярные платежи
            } else if (id == R.id.nav_categories) {
                Intent intent = new Intent(activity, CategoryWindow.class);
                activity.startActivity(intent);
            } else if (id == R.id.nav_rate) {
                android.app.Activity act = activity;
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    android.widget.Toast.makeText(act, "Необходима авторизация", android.widget.Toast.LENGTH_SHORT).show();
                    return true;
                }
                String uid = user.getUid();
                DatabaseReference reviewRef = FirebaseDatabase.getInstance().getReference("reviews").child(uid);
                reviewRef.get().addOnSuccessListener(snapshot -> {
                    boolean hasReview = snapshot.exists();
                    Runnable showDialog = () -> {
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(act);
                        android.view.View dialogView = android.view.LayoutInflater.from(act).inflate(R.layout.dialog_rate_app, null);
                        android.widget.ImageView[] stars = new android.widget.ImageView[5];
                        stars[0] = dialogView.findViewById(R.id.star1);
                        stars[1] = dialogView.findViewById(R.id.star2);
                        stars[2] = dialogView.findViewById(R.id.star3);
                        stars[3] = dialogView.findViewById(R.id.star4);
                        stars[4] = dialogView.findViewById(R.id.star5);
                        android.widget.EditText etComment = dialogView.findViewById(R.id.etComment);
                        android.widget.Button btnSend = dialogView.findViewById(R.id.btnSendRate);
                        final int[] selectedStars = {0};
                        for (int i = 0; i < 5; i++) {
                            final int idx = i;
                            stars[i].setOnClickListener(v -> {
                                selectedStars[0] = idx + 1;
                                for (int j = 0; j < 5; j++) {
                                    stars[j].setImageResource(j <= idx ? R.drawable.star_ic : R.drawable.star_outline_ic);
                                }
                            });
                        }
                        builder.setView(dialogView);
                        android.app.AlertDialog dialog = builder.create();
                        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(androidx.core.content.ContextCompat.getColor(act, R.color.background_color)));
                        btnSend.setOnClickListener(v -> {
                            if (selectedStars[0] == 0) {
                                android.widget.Toast.makeText(act, "Поставьте оценку", android.widget.Toast.LENGTH_SHORT).show();
                                return;
                            }
                            com.example.moneydealer.models.UserReview review = new com.example.moneydealer.models.UserReview(
                                uid, selectedStars[0], etComment.getText().toString().trim(), System.currentTimeMillis()
                            );
                            reviewRef.setValue(review).addOnSuccessListener(aVoid -> {
                                android.widget.Toast.makeText(act, "Спасибо за отзыв!", android.widget.Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            }).addOnFailureListener(e -> {
                                android.widget.Toast.makeText(act, "Ошибка: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                            });
                        });
                        dialog.show();
                    };
                    if (hasReview) {
                        new android.app.AlertDialog.Builder(act)
                            .setTitle("Перезаписать отзыв?")
                            .setMessage("У вас уже есть отзыв. Перезаписать его новым?")
                            .setPositiveButton("Перезаписать", (d, w) -> showDialog.run())
                            .setNegativeButton("Отмена", null)
                            .show();
                    } else {
                        showDialog.run();
                    }
                });
            } else if (id == R.id.nav_logout) {
                new AlertDialog.Builder(activity)
                        .setTitle("Подтверждение выхода")
                        .setMessage("Вы действительно хотите выйти из аккаунта?")
                        .setPositiveButton("Выйти", (dialog, which) -> {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(activity, WelcomeWindow1.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            activity.startActivity(intent);
                            activity.finish();
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }
} 