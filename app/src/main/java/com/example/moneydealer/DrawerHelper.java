package com.example.moneydealer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.widget.ImageView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class DrawerHelper {
    public static void setupDrawer(Activity activity, DrawerLayout drawerLayout, NavigationView navigationView, ImageView menuIcon) {
        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                // TODO: Открыть профиль
            } else if (id == R.id.nav_main) {
                // TODO: Открыть главную (MainWindow)
            } else if (id == R.id.nav_cash_accounts) {
                // TODO: Открыть счета
            } else if (id == R.id.nav_regular) {
                // TODO: Открыть регулярные платежи
            } else if (id == R.id.nav_categories) {
                // TODO: Открыть категории
            } else if (id == R.id.nav_rate) {
                // TODO: Оценить приложение
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