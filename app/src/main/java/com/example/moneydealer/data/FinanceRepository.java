package com.example.moneydealer.data;

import androidx.annotation.NonNull;

import com.example.moneydealer.models.Category;
import com.example.moneydealer.models.Transaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class FinanceRepository {
    private final DatabaseReference rootRef;

    private static final List<Category> DEFAULT_CATEGORIES = Arrays.asList(
            new Category("", "Жильё", 0xFFFF0000, "expense"),              // Красный
            new Category("", "Еда", 0xFFFF6600, "expense"),                // Оранжевый
            new Category("", "Транспорт", 0xFFFFFF00, "expense"),          // Ярко-желтый
            new Category("", "Здоровье", 0xFF00FF00, "expense"),           // Чистый зеленый
            new Category("", "Связь", 0xFF00FFFF, "expense"),              // Циан
            new Category("", "Развлечения", 0xFF0066FF, "expense"),        // Ярко-синий
            new Category("", "Подарки", 0xFF6600FF, "expense"),            // Фиолетово-синий
            new Category("", "Благотворительность", 0xFFFF00FF, "expense"),// Маджента

            new Category("", "Зарплата", 0xFF33FF00, "income"),            // Неоново-зеленый
            new Category("", "Фриланс", 0xFFFFCC00, "income"),            // Золотистый
            new Category("", "Кэшбэк", 0xFF00FF99, "income"),              // Бирюзово-зеленый
            new Category("", "Подарки", 0xFF9900FF, "income")             // Фиолетовый
    );

    public FinanceRepository() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid);
    }

    public void seedDefaultCategoriesIfEmpty() {
        rootRef.child("categories")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        if (!snap.exists() || !snap.hasChildren()) {
                            for (Category c : DEFAULT_CATEGORIES) {
                                addCategory(c);
                            }
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError err) {}
                });
    }

    public interface CategoriesCallback {
        void onLoaded(List<Category> categories);
        void onError(DatabaseError error);
    }

    public void loadCategories(@NonNull CategoriesCallback cb) {
        rootRef.child("categories")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        List<Category> list = new ArrayList<>();
                        for (DataSnapshot ds : snap.getChildren()) {
                            Category c = ds.getValue(Category.class);
                            if (c != null) list.add(c);
                        }
                        cb.onLoaded(list);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError err) {
                        cb.onError(err);
                    }
                });
    }

    public void addCategory(Category category) {
        String key = rootRef.child("categories").push().getKey();
        if (key != null) {
            category.id = key;
            rootRef.child("categories").child(key).setValue(category);
        }
    }

    public interface TransactionsCallback {
        void onLoaded(List<Transaction> transactions);
        void onError(DatabaseError error);
    }

    public void loadTransactions(long startTs, long endTs, @NonNull TransactionsCallback cb) {
        rootRef.child("transactions")
                .orderByChild("timestamp")
                .startAt(startTs)
                .endAt(endTs)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        List<Transaction> list = new ArrayList<>();
                        for (DataSnapshot ds : snap.getChildren()) {
                            Transaction t = ds.getValue(Transaction.class);
                            if (t != null) list.add(t);
                        }
                        cb.onLoaded(list);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError err) {
                        cb.onError(err);
                    }
                });
    }

    public void addTransaction(Transaction t) {
        String key = rootRef.child("transactions").push().getKey();
        if (key != null) {
            t.id = key;
            rootRef.child("transactions").child(key).setValue(t);
        }
    }

    public void updateTransaction(Transaction t) {
        rootRef.child("transactions").child(t.id).setValue(t);
    }

    public void deleteTransaction(String id) {
        rootRef.child("transactions").child(id).removeValue();
    }
}
