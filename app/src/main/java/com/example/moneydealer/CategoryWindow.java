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

import android.widget.RadioGroup;
import android.widget.RadioButton;

public class CategoryWindow extends AppCompatActivity {
    private RecyclerView rvCategories;
    private Button btnAddCategory;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView menuIcon;
    private CategoryAdapter adapter;
    private List<Category> categories = new ArrayList<>();
    private DatabaseReference categoriesRef;
    private DatabaseReference transactionsRef;
    private FirebaseUser currentUser;

    private final int[] COLORS = new int[] {
            0xFFFF0000, // Красный
            0xFFFF3300, // Красно-оранжевый
            0xFFFF6600, // Оранжевый
            0xFFFF9900, // Желто-оранжевый
            0xFFFFCC00, // Золотистый
            0xFFFFFF00, // Ярко-желтый
            0xFFCCFF00, // Лаймовый
            0xFF99FF00, // Желто-зеленый
            0xFF66FF00, // Салатовый
            0xFF33FF00, // Неоново-зеленый
            0xFF00FF00, // Чистый зеленый
            0xFF00FF33, // Изумрудно-зеленый
            0xFF00FF66, // Мятный
            0xFF00FF99, // Бирюзово-зеленый
            0xFF00FFCC, // Аквамариновый
            0xFF00FFFF, // Циан
            0xFF00CCFF, // Небесно-голубой
            0xFF0099FF, // Лазурный
            0xFF0066FF, // Ярко-синий
            0xFF0033FF, // Темно-синий
            0xFF0000FF, // Чистый синий
            0xFF3300FF, // Индиго
            0xFF6600FF, // Фиолетово-синий
            0xFF9900FF, // Фиолетовый
            0xFFCC00FF, // Пурпурный
            0xFFFF00FF, // Маджента
            0xFFFF00CC, // Розово-фиолетовый
            0xFFFF0099, // Фуксия
            0xFFFF0066, // Ярко-розовый
            0xFFFF0033  // Розово-красный
    };

    private int selectedColor = COLORS[0];
    private String selectedType = "expense";

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

        // Добавляем переключатель для фильтрации категорий по типу
        LinearLayout topBar = findViewById(R.id.topBar);
        RadioGroup rgType = new RadioGroup(this);
        rgType.setOrientation(RadioGroup.HORIZONTAL);
        RadioButton rbExpense = new RadioButton(this);
        rbExpense.setText("Расходы");
        rbExpense.setId(View.generateViewId());
        rbExpense.setChecked(true);
        RadioButton rbIncome = new RadioButton(this);
        rbIncome.setText("Доходы");
        rbIncome.setId(View.generateViewId());
        rgType.addView(rbExpense);
        rgType.addView(rbIncome);
        topBar.addView(rgType);
        rgType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == rbExpense.getId()) selectedType = "expense";
            else selectedType = "income";
            adapter.notifyDataSetChanged();
        });

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Пользователь не найден", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        categoriesRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid()).child("categories");
        transactionsRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid()).child("transactions");

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
                adapter = new CategoryAdapter(CategoryWindow.this, categories, CategoryWindow.this::onDeleteCategoryWithDialog, CategoryWindow.this::showEditCategoryDialog) {
                    @Override
                    public int getItemCount() {
                        int count = 0;
                        for (Category c : categories) if (c.type != null && c.type.equals(selectedType)) count++;
                        return count;
                    }
                    @Override
                    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
                        int idx = -1, found = -1;
                        for (Category c : categories) {
                            if (c.type != null && c.type.equals(selectedType)) idx++;
                            if (idx == position) { found = categories.indexOf(c); break; }
                        }
                        if (found != -1) super.onBindViewHolder(holder, found);
                    }
                };
                rvCategories.setAdapter(adapter);
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
        // Проверяем, есть ли транзакции с этой категорией
        transactionsRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                boolean used = false;
                for (com.google.firebase.database.DataSnapshot txSnap : snapshot.getChildren()) {
                    com.example.moneydealer.models.Transaction tx = txSnap.getValue(com.example.moneydealer.models.Transaction.class);
                    if (tx != null && category.id.equals(tx.categoryId)) {
                        used = true;
                        break;
                    }
                }
                if (used) {
                    Toast.makeText(CategoryWindow.this, "Нельзя удалить категорию: она используется в транзакциях", Toast.LENGTH_LONG).show();
                } else {
                    categoriesRef.child(category.id).removeValue((error, ref) -> {
                        if (error == null) {
                            Toast.makeText(CategoryWindow.this, "Категория удалена", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CategoryWindow.this, "Ошибка удаления: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                Toast.makeText(CategoryWindow.this, "Ошибка проверки транзакций: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onDeleteCategoryWithDialog(Category category) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Удалить категорию?")
            .setMessage("Вы уверены, что хотите удалить категорию '" + category.name + "'?")
            .setPositiveButton("Удалить", (dialog, which) -> onDeleteCategory(category))
            .setNegativeButton("Отмена", null)
            .show();
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
        EditText etCategoryName = dialogView.findViewById(R.id.etCategoryName);
        LinearLayout colorPicker = dialogView.findViewById(R.id.colorPicker);
        Button btnSave = dialogView.findViewById(R.id.btnSaveCategory);
        RadioGroup rgType = dialogView.findViewById(R.id.rgCategoryType);

        // Очищаем colorPicker и делаем его вертикальным
        colorPicker.setOrientation(LinearLayout.VERTICAL);
        colorPicker.removeAllViews();
        int colorsPerRow = 6;
        int totalRows = (int) Math.ceil((double) COLORS.length / colorsPerRow);
        int colorIdx = 0;
        for (int row = 0; row < totalRows; row++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setGravity(android.view.Gravity.CENTER);
            for (int col = 0; col < colorsPerRow && colorIdx < COLORS.length; col++, colorIdx++) {
                int color = COLORS[colorIdx];
                View colorView = new View(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(64, 64);
                lp.setMargins(12, 12, 12, 12);
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
                        LinearLayout rowL = (LinearLayout) colorPicker.getChildAt(i);
                        for (int j = 0; j < rowL.getChildCount(); j++) {
                            rowL.getChildAt(j).setAlpha(0.5f);
                        }
                    }
                    v.setAlpha(1f);
                });
                colorView.setAlpha(color == selectedColor ? 1f : 0.5f);
                rowLayout.addView(colorView);
            }
            colorPicker.addView(rowLayout);
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
            for (Category cat : categories) {
                if (cat.name.replaceAll("\\s+", "").equalsIgnoreCase(name.replaceAll("\\s+", ""))) {
                    etCategoryName.setError("Такая категория уже есть");
                    return;
                }
            }
            String id = categoriesRef.push().getKey();
            String type = rgType.getCheckedRadioButtonId() == dialogView.findViewById(R.id.rbIncome).getId() ? "income" : "expense";
            Category cat = new Category(id, name, selectedColor, type);
            categoriesRef.child(id).setValue(cat);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showEditCategoryDialog(Category category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
        EditText etCategoryName = dialogView.findViewById(R.id.etCategoryName);
        LinearLayout colorPicker = dialogView.findViewById(R.id.colorPicker);
        Button btnSave = dialogView.findViewById(R.id.btnSaveCategory);
        RadioGroup rgType = dialogView.findViewById(R.id.rgCategoryType);

        etCategoryName.setText(category.name);
        int initialColor = category.color;
        selectedColor = initialColor;
        colorPicker.setOrientation(LinearLayout.VERTICAL);
        colorPicker.removeAllViews();
        int colorsPerRow = 6;
        int totalRows = (int) Math.ceil((double) COLORS.length / colorsPerRow);
        int colorIdx = 0;
        for (int row = 0; row < totalRows; row++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setGravity(android.view.Gravity.CENTER);
            for (int col = 0; col < colorsPerRow && colorIdx < COLORS.length; col++, colorIdx++) {
                int color = COLORS[colorIdx];
                View colorView = new View(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(64, 64);
                lp.setMargins(12, 12, 12, 12);
                colorView.setLayoutParams(lp);
                Drawable bg = getResources().getDrawable(R.drawable.bg_category_circle).mutate();
                bg.setColorFilter(color, PorterDuff.Mode.SRC_IN);
                colorView.setBackground(bg);
                colorView.setClickable(true);
                colorView.setFocusable(true);
                colorView.setOnClickListener(v -> {
                    selectedColor = color;
                    for (int i = 0; i < colorPicker.getChildCount(); i++) {
                        LinearLayout rowL = (LinearLayout) colorPicker.getChildAt(i);
                        for (int j = 0; j < rowL.getChildCount(); j++) {
                            rowL.getChildAt(j).setAlpha(0.5f);
                        }
                    }
                    v.setAlpha(1f);
                });
                colorView.setAlpha(color == selectedColor ? 1f : 0.5f);
                rowLayout.addView(colorView);
            }
            colorPicker.addView(rowLayout);
        }

        // Установить тип и заблокировать изменение
        RadioButton rbExpense = dialogView.findViewById(R.id.rbExpense);
        RadioButton rbIncome = dialogView.findViewById(R.id.rbIncome);
        if (category.type != null && category.type.equals("income")) {
            rgType.check(rbIncome.getId());
        } else {
            rgType.check(rbExpense.getId());
        }
        rgType.setEnabled(false);
        rbExpense.setEnabled(false);
        rbIncome.setEnabled(false);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.background_color)));

        btnSave.setText("Сохранить");
        btnSave.setOnClickListener(v -> {
            String name = etCategoryName.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                etCategoryName.setError("Введите название");
                return;
            }
            for (Category cat : categories) {
                if (!cat.id.equals(category.id) && cat.name.replaceAll("\\s+", "").equalsIgnoreCase(name.replaceAll("\\s+", ""))) {
                    etCategoryName.setError("Такая категория уже есть");
                    return;
                }
            }
            // Тип не меняем, сохраняем старый
            String type = category.type;
            new AlertDialog.Builder(this)
                .setTitle("Сохранить изменения?")
                .setMessage("Вы уверены, что хотите изменить категорию?")
                .setPositiveButton("Сохранить", (d, w) -> {
                    Category updated = new Category(category.id, name, selectedColor, type);
                    categoriesRef.child(category.id).setValue(updated);
                    dialog.dismiss();
                })
                .setNegativeButton("Отмена", null)
                .show();
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