package com.mcris.localexchange.models.entities;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.maps.android.clustering.ClusterItem;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

public class Item implements ClusterItem {
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("Longitude")
    @Expose
    private Double longitude;
    @SerializedName("Latitude")
    @Expose
    private Double latitude;
    @SerializedName("ID")
    @Expose
    private String id;
    @SerializedName("Description")
    @Expose
    private String description;
    @SerializedName("Price")
    @Expose
    private Double price;
    @SerializedName("Picture")
    @Expose
    private String pictureUrl;
    @SerializedName("Thumbnail")
    @Expose
    private String thumbnailUrl;
    @SerializedName("Type")
    @Expose
    private Typology typology;
    @SerializedName("Category")
    @Expose
    private List<String> categories;
    @SerializedName("Owner")
    @Expose
    private List<String> ownerId;
    @SerializedName("Owner Name")
    @Expose
    private List<String> ownerName;
    @SerializedName("Date Added")
    @Expose
    private String dateString;

    private Bitmap thumbnailBitmap;

    private Bitmap picture;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public LatLng getLatLng() {
        return new LatLng(latitude, longitude);
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Typology getTypology() {
        return typology;
    }

    public void setTypology(Typology typology) {
        this.typology = typology;
    }

    public List<String> getCategories() {
        return categories;
    }

    public String getCategoryId() {
        return categories.get(0);
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public String getOwnerId() {
        return ownerId.get(0);
    }

    public String getOwnerName() {
        return ownerName.get(0);
    }

    public String getDateString() {
        return dateString;
    }

    public LocalDate getDate() {
        try {
            return LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return getLatLng();
    }

    @Nullable
    @Override
    public String getTitle() {
        return name;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return description;
    }

    public Bitmap getThumbnailBitmap() {
        return thumbnailBitmap;
    }

    public void setThumbnailBitmap(Bitmap thumbnailBitmap) {
        this.thumbnailBitmap = thumbnailBitmap;
    }

    public Bitmap getPicture() {
        return picture;
    }

    public void setPicture(Bitmap picture) {
        this.picture = picture;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        Item item = (Item) o;
        return Objects.equals(id, item.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public enum Typology {
        @SerializedName("buy")
        BUY("buy"),
        @SerializedName("sell")
        SELL("sell");

        private final String typologyString;

        Typology(String typologyString) {
            this.typologyString = typologyString;
        }

        @Override
        public String toString() {
            return typologyString;
        }
    }
}