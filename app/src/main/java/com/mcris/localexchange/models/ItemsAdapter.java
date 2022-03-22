package com.mcris.localexchange.models;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mcris.localexchange.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ViewHolder> {
    private final ArrayList<Item> items;

    public ItemsAdapter(ArrayList<Item> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ItemsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_recycler_view_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemsAdapter.ViewHolder holder, int position) {
        Item item = items.get(position);
        holder.imageView.setImageBitmap(item.getThumbnailBitmap());
        holder.titleTextView.setText(item.getName());
        holder.priceTextView.setText(String.format(Locale.getDefault(),
                "%.2f €", item.getPrice()));
        holder.descriptionTextView.setText(item.getDescription());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(Item item) {
        items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    public boolean addAll(List<Item> items) {
        int oldSize = this.items.size();
        boolean result = this.items.addAll(items);
        if (result) {
            notifyItemRangeInserted(oldSize - 1, items.size());
        }
        return result;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView titleTextView;
        TextView priceTextView;
        TextView descriptionTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.rowImageView);
            titleTextView = itemView.findViewById(R.id.rowTitleTextView);
            priceTextView = itemView.findViewById(R.id.rowPriceTextView);
            descriptionTextView = itemView.findViewById(R.id.rowDescriptionTextView);
        }
    }
}
