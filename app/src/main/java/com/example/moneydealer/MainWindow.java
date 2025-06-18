package com.example.moneydealer;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.app.AlertDialog;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.GravityCompat;

import com.example.moneydealer.data.FinanceRepository;
import com.example.moneydealer.models.Category;
import com.example.moneydealer.models.Transaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class        MainWindow extends AppCompatActivity {

    public static class CategoryBarItem {
        public String name;
        public float amount;
        public int color;
        public CategoryBarItem(String name, float amount, int color) {
            this.name   = name;
            this.amount = amount;
            this.color  = color;
        }
    }

    private FinanceRepository repo;
    private List<Category> categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.main_window);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        repo = new FinanceRepository();
        repo.seedDefaultCategoriesIfEmpty();
        categories = new ArrayList<>();
        loadCategoriesAndTransactions();

        // --- Navigation Drawer ---
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        ImageView menuIcon = findViewById(R.id.menuIcon);
        DrawerHelper.setupDrawer(this, drawerLayout, navigationView, menuIcon);
    }

    private void loadCategoriesAndTransactions() {
        repo.loadCategories(new FinanceRepository.CategoriesCallback() {
            @Override
            public void onLoaded(List<Category> cats) {
                categories = cats;
                long now = System.currentTimeMillis();
                long monthAgo = now - 30L*24*60*60*1000;
                repo.loadTransactions(monthAgo, now, new FinanceRepository.TransactionsCallback() {
                    @Override
                    public void onLoaded(List<Transaction> txs) {
                        runOnUiThread(() -> drawCategoryBar(aggregate(txs)));
                    }
                    @Override
                    public void onError(DatabaseError error) {
                        // TODO: показать ошибку
                    }
                });
            }
            @Override
            public void onError(DatabaseError error) {
                // TODO: показать ошибку
            }
        });
    }

    private List<CategoryBarItem> aggregate(List<Transaction> txs) {
        Map<String, Float> sums = new HashMap<>();
        float total = 0f;
        for (Transaction t : txs) {
            if (!"expense".equals(t.type)) continue;
            float prev = sums.getOrDefault(t.categoryId, 0f);
            sums.put(t.categoryId, prev + t.amount);
            total += t.amount;
        }
        List<CategoryBarItem> items = new ArrayList<>();
        for (Category c : categories) {
            float amt = sums.getOrDefault(c.id, 0f);
            if (amt > 0f) {
                items.add(new CategoryBarItem(c.name, amt, c.color));
            }
        }
        return items;
    }

    private void drawCategoryBar(List<CategoryBarItem> items) {
        LinearLayout bar = findViewById(R.id.categoryProgressBar);
        bar.removeAllViews();
        float sum = 0f;
        for (CategoryBarItem i : items) sum += i.amount;
        for (CategoryBarItem cat : items) {
            View segment = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    cat.amount / sum
            );
            params.setMarginEnd(2);
            segment.setLayoutParams(params);
            segment.setBackgroundColor(cat.color);
            bar.addView(segment);
        }
    }
}
