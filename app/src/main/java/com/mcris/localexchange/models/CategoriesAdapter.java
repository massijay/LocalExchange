package com.mcris.localexchange.models;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mcris.localexchange.R;
import com.mcris.localexchange.models.entities.Category;

import java.util.List;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder<Category>>
        implements ClickableAdapter<Category> {

    private ClickableAdapterListener<Category> listener;
    private final List<Category> categories;

    public CategoriesAdapter(List<Category> categories) {
        this.categories = categories;
    }

    @NonNull
    @Override
    public ViewHolder<Category> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.category_recycler_view_row, parent, false);
        return new ViewHolder<>(view, categories, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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

    public static class ViewHolder<T> extends RecyclerView.ViewHolder {

        TextView textView1;
        TextView textView2;

        public ViewHolder(@NonNull View itemView, List<T> items, ClickableAdapterListener<T> clickListener) {
            super(itemView);
            textView1 = itemView.findViewById(R.id.categoryNameTextView);
            textView2 = itemView.findViewById(R.id.categoryShortDescrTextView);
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    int pos = getAdapterPosition();
                    clickListener.onListItemClick(items.get(pos), pos);
                }
            });
        }
    }
}
