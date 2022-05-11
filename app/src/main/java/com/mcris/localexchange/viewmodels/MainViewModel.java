package com.mcris.localexchange.viewmodels;


import android.app.Application;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayMap;
import androidx.databinding.ObservableMap;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mcris.localexchange.models.entities.Category;
import com.mcris.localexchange.models.entities.Item;
import com.mcris.localexchange.models.entities.Record;
import com.mcris.localexchange.models.entities.Table;
import com.mcris.localexchange.services.AirtableApiService;
import com.mcris.localexchange.services.GsonRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MainViewModel extends AndroidViewModel {

    private final ObservableArrayMap<String, Item> observableItems;
    private Map<String, Category> allCategories;

    private MutableLiveData<LatLng> userLocation;

    private LatLngBounds latLngBounds;
    private Item.Typology typeOfSearch;
    private String selectedCategoryId;
    private String searchText;

    private Item selectedItem;

    public LatLngBounds getLatLngBounds() {
        return latLngBounds;
    }

    public void setLatLngBounds(LatLngBounds latLngBounds) {
        this.latLngBounds = latLngBounds;
    }

    public ObservableMap<String, Item> getObservableItems() {
        return observableItems;
    }

    public List<Category> getAllCategories() {
        if (allCategories == null) {
            downloadCategories();
        }
        return new ArrayList<>(allCategories.values());
    }

    public Category getCategoryFromId(String id) {
        return allCategories.get(id);
    }

    public MutableLiveData<LatLng> getUserLocation() {
        if (userLocation == null) {
            userLocation = new MutableLiveData<>(new LatLng(0, 0));
        }
        return userLocation;
    }

    public Item.Typology getTypeOfSearch() {
        return typeOfSearch;
    }

    public void setTypeOfSearch(Item.Typology typeOfSearch) {
        this.typeOfSearch = typeOfSearch;
    }

    public String getSelectedCategoryId() {
        return selectedCategoryId;
    }

    public void setSelectedCategoryId(String selectedCategoryId) {
        this.selectedCategoryId = selectedCategoryId;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText != null ? searchText.trim() : null;
    }

    public Item getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(Item selectedItem) {
        this.selectedItem = selectedItem;
    }

    public MainViewModel(@NonNull Application application) {
        super(application);
        observableItems = new ObservableArrayMap<>();
        typeOfSearch = Item.Typology.SELL;
    }

    public void downloadItems() {
        double minLatitude = latLngBounds.southwest.latitude;
        double minLongitude = latLngBounds.southwest.longitude;
        double maxLatitude = latLngBounds.northeast.latitude;
        double maxLongitude = latLngBounds.northeast.longitude;
        RequestQueue queue = Volley.newRequestQueue(getApplication());
        GsonRequest<Table<Item>> itemTableRequest = AirtableApiService.getInstance(getApplication())
                .requestItemTable(minLatitude, maxLatitude, minLongitude, maxLongitude, typeOfSearch, selectedCategoryId, searchText,
                        response -> {
                            for (Record<Item> record : response.getRecords()) {
                                Item item = record.getRow();
                                if (!observableItems.containsKey(item.getId())) {
                                    if (item.getThumbnailUrl() == null || item.getThumbnailUrl().isEmpty()) {
//                                    item.setThumbnailBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.test_product));
                                        observableItems.putIfAbsent(item.getId(), item);
                                    } else {
                                        FirebaseStorage storage = FirebaseStorage.getInstance();

                                        StorageReference imgRef = storage.getReferenceFromUrl(item.getThumbnailUrl());

                                        final long ONE_MEGABYTE = 1024 * 1024;
                                        imgRef.getBytes(ONE_MEGABYTE)
                                                .addOnSuccessListener(bytes -> {
                                                    item.setThumbnailBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                                                    observableItems.putIfAbsent(item.getId(), item);
                                                })
                                                .addOnFailureListener(e -> {
                                                });
                                    }
                                }
                            }
                        },
                        error -> Toast.makeText(getApplication(), "ERROR: " + error.getMessage(), Toast.LENGTH_LONG).show());
        queue.add(itemTableRequest);
    }

    public void downloadSelectedItem(Consumer<Item> afterDownloadAction) {
        RequestQueue queue = Volley.newRequestQueue(getApplication());
        GsonRequest<Table<Item>> itemRequest = AirtableApiService.getInstance(getApplication())
                .requestItem(selectedItem.getId(),
                        response -> {
                            Item downloaded = response.getRecords().get(0).getRow();
                            if (downloaded != null) {
                                downloaded.setThumbnailBitmap(selectedItem.getThumbnailBitmap());
                                if (selectedItem.getPicture() != null) {
                                    downloaded.setPicture(selectedItem.getPicture());
                                    selectedItem = downloaded;
                                    observableItems.put(downloaded.getId(), downloaded);
                                    afterDownloadAction.accept(downloaded);
                                } else if (downloaded.getPictureUrl() != null && !downloaded.getPictureUrl().isEmpty()) {
                                    FirebaseStorage storage = FirebaseStorage.getInstance();

                                    StorageReference imgRef = storage.getReferenceFromUrl(downloaded.getPictureUrl());

                                    final long TWENTY_MEGABYTES = 20 * 1024 * 1024;
                                    imgRef.getBytes(TWENTY_MEGABYTES)
                                            .addOnSuccessListener(bytes -> {
                                                downloaded.setPicture(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                                                selectedItem = downloaded;
                                                observableItems.put(downloaded.getId(), downloaded);
                                                afterDownloadAction.accept(downloaded);
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(getApplication(), "ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                            });
                                }
                            }
                        },
                        error -> Toast.makeText(getApplication(), "ERROR: " + error.getMessage(), Toast.LENGTH_LONG).show());
        queue.add(itemRequest);
    }

    public void downloadCategories() {
        RequestQueue queue = Volley.newRequestQueue(getApplication());
        GsonRequest<Table<Category>> categoryTableRequest = AirtableApiService.getInstance(getApplication())
                .requestCategoryTable(
                        response -> {
                            allCategories = new HashMap<>(response.getRecords().size());
                            for (Record<Category> record : response.getRecords()) {
                                allCategories.put(record.getRow().getId(), record.getRow());
                            }
                        },
                        error -> Toast.makeText(getApplication(), "ERROR: " + error.getMessage(), Toast.LENGTH_LONG).show());
        queue.add(categoryTableRequest);
    }

}
