package com.mcris.localexchange.models;

@FunctionalInterface
public interface ClickableAdapterListener<T> {
    void onListItemClick(T item, int position);
}
