package com.example.moneydealer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ViewAnimator;
import android.animation.ObjectAnimator;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.graphics.drawable.ColorDrawable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.EdgeToEdge;

import com.example.moneydealer.adapters.CategoryAdapter;
import com.example.moneydealer.models.Category;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CategoryWindow extends AppCompatActivity {
    private RecyclerView rvCategories;
    private Button btnAddCategory;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView menuIcon;
    private CategoryAdapter adapter;
    private List<Category> categories = new ArrayList<>();
    private DatabaseReference categoriesRef;
    private FirebaseUser currentUser;

    private final int[] COLORS = new int[] {
            0xFFFF8000, // Orange
            0xFF4CAF50, // Green
            0xFF2196F3, // Blue
            0xFFFFC107, // Yellow
            0xFFE91E63, // Pink
            0xFF9C27B0, // Purple
            0xFF607D8B, // Gray
            0xFF795548  // Brown
    };

    private int selectedColor = COLORS[0];

    // ItemDecoration для равных отступов
    private static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private final int spanCount;
        private final int spacing;
        private final boolean includeEdge;
        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }
        @Override
        public void getItemOffsets(android.graphics.Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int column = position % spanCount;
            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount;
                outRect.right = (column + 1) * spacing / spanCount;
                if (position < spanCount) outRect.top = spacing;
                outRect.bottom = spacing;
            } else {
                outRect.left = column * spacing / spanCount;
                outRect.right = spacing - (column + 1) * spacing / spanCount;
                if (position >= spanCount) outRect.top = spacing;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.category_window);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        rvCategories = findViewById(R.id.rvCategories);
        btnAddCategory = findViewById(R.id.btnAddCategory);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        menuIcon = findViewById(R.id.menuIcon);

        DrawerHelper.setupDrawer(this, drawerLayout, navigationView, menuIcon);

        rvCategories.setLayoutManager(new GridLayoutManager(this, 2));
        int spacing = getResources().getDimensionPixelSize(R.dimen.category_grid_spacing);
        rvCategories.addItemDecoration(new GridSpacingItemDecoration(2, spacing, true));
        adapter = new CategoryAdapter(this, categories, this::onDeleteCategory);
        rvCategories.setAdapter(adapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Пользователь не найден", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        categoriesRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid()).child("categories");

        loadCategories();

        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());
    }

    private void loadCategories() {
        categoriesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categories.clear();
                for (DataSnapshot catSnap : snapshot.getChildren()) {
                    Category cat = catSnap.getValue(Category.class);
                    if (cat != null) categories.add(cat);
                }
                adapter.notifyDataSetChanged();
                handleEmptyState();
                runAppearAnimation();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CategoryWindow.this, "Ошибка загрузки: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onDeleteCategory(Category category) {
        categoriesRef.child(category.id).removeValue((error, ref) -> {
            if (error == null) {
                Toast.makeText(this, "Категория удалена", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Ошибка удаления: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
        EditText etCategoryName = dialogView.findViewById(R.id.etCategoryName);
        LinearLayout colorPicker = dialogView.findViewById(R.id.colorPicker);
        Button btnSave = dialogView.findViewById(R.id.btnSaveCategory);

        // Динамически добавляем цветные круги
        for (int color : COLORS) {
            View colorView = new View(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(64, 64);
            lp.setMargins(12, 0, 12, 0);
            colorView.setLayoutParams(lp);
            Drawable bg = getResources().getDrawable(R.drawable.bg_category_circle).mutate();
            bg.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            colorView.setBackground(bg);
            colorView.setClickable(true);
            colorView.setFocusable(true);
            colorView.setOnClickListener(v -> {
                selectedColor = color;
                // Подсветка выбранного круга
                for (int i = 0; i < colorPicker.getChildCount(); i++) {
                    colorPicker.getChildAt(i).setAlpha(0.5f);
                }
                v.setAlpha(1f);
            });
            colorView.setAlpha(color == selectedColor ? 1f : 0.5f);
            colorPicker.addView(colorView);
        }

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        // Ставим фон
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.background_color)));

        btnSave.setOnClickListener(v -> {
            String name = etCategoryName.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                etCategoryName.setError("Введите название");
                return;
            }
            // Проверка на уникальность (без учёта регистра и пробелов)
            for (Category cat : categories) {
                if (cat.name.replaceAll("\\s+", "").equalsIgnoreCase(name.replaceAll("\\s+", ""))) {
                    etCategoryName.setError("Такая категория уже есть");
                    return;
                }
            }
            String id = categoriesRef.push().getKey();
            Category cat = new Category(id, name, selectedColor, "expense");
            categoriesRef.child(id).setValue(cat);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void handleEmptyState() {
        TextView tvEmpty = findViewById(R.id.tvEmptyCategories);
        if (categories.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvCategories.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvCategories.setVisibility(View.VISIBLE);
        }
    }

    private void runAppearAnimation() {
        for (int i = 0; i < rvCategories.getChildCount(); i++) {
            View v = rvCategories.getChildAt(i);
            if (v != null) {
                v.setAlpha(0f);
                v.animate().alpha(1f).setDuration(400).setStartDelay(i * 80).start();
            }
        }
    }
} 