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
    private int selected;

    // Needed only for visual purposes
    private final List<CategoryViewHolder> holders;

    public CategoriesAdapter(List<Category> categories) {
        this.categories = categories;
        selected = -1;
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
        holder.radioButton.setChecked(position == selected);
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
        return selected > -1 ? categories.get(selected) : null;
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
                int pos = getAdapterPosition();
                adapter.selected = adapter.selected != pos ? pos : -1;
                // UI changes here are just visual while this holder (row) is visible
                // Once it goes outside of the screen the RecyclerView could recycle it.
                // So we have to save the state in another place (i.e. adapter.selected variable)
                // and update UI in the onBindViewHolder() method too for rows that are appearing
                // in the screen
                for (CategoryViewHolder h : adapter.holders) {
                    h.radioButton.setChecked(false);
                }
                radioButton.setChecked(pos == adapter.selected);
                // Finally, if exists, call the function the user passed as listener
                if (adapter.listener != null) {
                    adapter.listener.onListItemClick(adapter.categories.get(pos), pos);
                }
            });
        }
    }
}
