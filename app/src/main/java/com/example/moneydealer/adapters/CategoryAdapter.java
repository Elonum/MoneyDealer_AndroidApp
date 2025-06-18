package com.example.moneydealer.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.moneydealer.R;
import com.example.moneydealer.models.Category;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private List<Category> categories;
    private OnCategoryDeleteListener deleteListener;
    private Context context;

    public interface OnCategoryDeleteListener {
        void onDelete(Category category);
    }

    public CategoryAdapter(Context context, List<Category> categories, OnCategoryDeleteListener deleteListener) {
        this.context = context;
        this.categories = categories;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.tvCategoryName.setText(category.name);
        // Цвет круга
        holder.colorCircle.getBackground().setColorFilter(category.color, PorterDuff.Mode.SRC_IN);
        // Долгое нажатие для удаления
        holder.itemView.setOnLongClickListener(v -> {
            if (deleteListener != null) deleteListener.onDelete(category);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        View colorCircle;
        TextView tvCategoryName;
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            colorCircle = itemView.findViewById(R.id.colorCircle);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
        }
    }
} 