package com.mcris.localexchange.models;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mcris.localexchange.R;
import com.mcris.localexchange.models.entities.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder>
        implements ClickableAdapter<Category> {

    private ClickableAdapterListener<Category> listener;
    private final List<Category> categories;

    private final List<CategoryViewHolder> holders;

    public CategoriesAdapter(List<Category> categories) {
        this.categories = categories;
        holders = new ArrayList<>();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.category_recycler_view_row, parent, false);
        CategoryViewHolder holder = new CategoryViewHolder(view, this);
        holders.add(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.textView1.setText(category.getName());
        holder.textView2.setText(category.getDescription());
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    @Override
    public void setOnClickListener(ClickableAdapterListener<Category> listener) {
        this.listener = listener;
    }

    public Category getSelectedCategory() {
        for (CategoryViewHolder h : holders) {
            if (h.radioButton.isChecked()) {
                return categories.get(h.getAdapterPosition());
            }
        }
        return null;
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {

        TextView textView1;
        TextView textView2;
        RadioButton radioButton;

        public CategoryViewHolder(@NonNull View itemView, CategoriesAdapter adapter) {
            super(itemView);
            textView1 = itemView.findViewById(R.id.categoryNameTextView);
            textView2 = itemView.findViewById(R.id.categoryShortDescrTextView);
            radioButton = itemView.findViewById(R.id.categoryRowRadioButton);
            radioButton.setClickable(false);
            itemView.setOnClickListener(v -> {
                if (adapter.listener != null) {
                    int pos = getAdapterPosition();
                    for (CategoryViewHolder h : adapter.holders) {
                        if (h.radioButton != radioButton) {
                            h.radioButton.setChecked(false);
                        }
                    }
                    radioButton.setChecked(!radioButton.isChecked());
                    adapter.listener.onListItemClick(adapter.categories.get(pos), pos);
                }
            });
        }
    }
}
