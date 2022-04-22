package com.mcris.localexchange.models;

public interface ClickableAdapter<T> {
    void setOnClickListener(ClickableAdapterListener<T> listener);
}
