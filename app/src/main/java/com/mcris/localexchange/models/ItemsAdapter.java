package com.mcris.localexchange.models;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.google.android.gms.maps.model.LatLng;
import com.mcris.localexchange.R;
import com.mcris.localexchange.models.entities.Item;

import java.util.Collection;
import java.util.Locale;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ItemViewHolder>
        implements ClickableAdapter<Item> {

    private ClickableAdapterListener<Item> listener;
    private LatLng referencePosition;
    private final SortedList<Item> sortedItems;

    public ItemsAdapter() {
        referencePosition = new LatLng(0, 0);
        sortedItems = new SortedList<>(Item.class, new SortedList.Callback<Item>() {
            @Override
            public int compare(Item i1, Item i2) {
                int multiplier = 100_000;
                int dist1 = (int) (Math.sqrt(
                        Math.pow(referencePosition.latitude - i1.getLatitude(), 2) +
                                Math.pow(referencePosition.longitude - i1.getLongitude(), 2))
                        * multiplier);
                int dist2 = (int) (Math.sqrt(
                        Math.pow(referencePosition.latitude - i2.getLatitude(), 2) +
                                Math.pow(referencePosition.longitude - i2.getLongitude(), 2))
                        * multiplier);
                return dist1 - dist2;
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(Item oldItem, Item newItem) {
                return areItemsTheSame(oldItem, newItem);
            }

            @Override
            public boolean areItemsTheSame(Item item1, Item item2) {
                return item1.equals(item2);
            }

            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }
        });
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_recycler_view_row, parent, false);
        return new ItemViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = sortedItems.get(position);
        holder.imageView.setImageBitmap(item.getThumbnailBitmap());
        holder.titleTextView.setText(item.getName());
        holder.priceTextView.setText(String.format(Locale.getDefault(),
                "%.2f €", item.getPrice()));
        holder.descriptionTextView.setText(item.getDescription());
    }

    @Override
    public int getItemCount() {
        return sortedItems.size();
    }

    public LatLng getReferencePosition() {
        return referencePosition;
    }

    public void setReferencePosition(LatLng referencePosition) {
        this.referencePosition = referencePosition;
    }

    public void addItem(Item item) {
        sortedItems.add(item);
    }

    public void addItems(Collection<Item> items) {
        sortedItems.beginBatchedUpdates();
        for (Item i : items) {
            addItem(i);
        }
        sortedItems.endBatchedUpdates();
    }

    public void clearItems() {
        sortedItems.clear();
    }

    @Override
    public void setOnClickListener(ClickableAdapterListener<Item> listener) {
        this.listener = listener;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView titleTextView;
        TextView priceTextView;
        TextView descriptionTextView;

        public ItemViewHolder(@NonNull View itemView, ItemsAdapter adapter) {
            super(itemView);
            imageView = itemView.findViewById(R.id.rowImageView);
            titleTextView = itemView.findViewById(R.id.rowTitleTextView);
            priceTextView = itemView.findViewById(R.id.rowPriceTextView);
            descriptionTextView = itemView.findViewById(R.id.rowDescriptionTextView);
            itemView.setOnClickListener(v -> {
                if (adapter.listener != null) {
                    int pos = getAdapterPosition();
                    adapter.listener.onListItemClick(adapter.sortedItems.get(pos), pos);
                }
            });
        }
    }
}
