package com.mcris.localexchange.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Record<TableType> {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("fields")
    @Expose
    private TableType row;
    @SerializedName("createdTime")
    @Expose
    private String createdTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TableType getRow() {
        return row;
    }

    public void setRow(TableType row) {
        this.row = row;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }
}
