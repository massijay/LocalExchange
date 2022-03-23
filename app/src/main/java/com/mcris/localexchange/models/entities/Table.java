package com.mcris.localexchange.models.entities;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Table<TableType> {
    @SerializedName("records")
    @Expose
    private List<Record<TableType>> records = null;

    public List<Record<TableType>> getRecords() {
        return records;
    }

    public void setRecords(List<Record<TableType>> records) {
        this.records = records;
    }
}
