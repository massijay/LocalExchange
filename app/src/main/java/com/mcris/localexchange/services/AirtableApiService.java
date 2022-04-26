package com.mcris.localexchange.services;


import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;

import com.android.volley.Request;
import com.android.volley.Response;
import com.google.gson.reflect.TypeToken;
import com.mcris.localexchange.models.entities.Category;
import com.mcris.localexchange.models.entities.Item;
import com.mcris.localexchange.models.entities.Table;

import java.util.HashMap;

public class AirtableApiService {
    // It's safe (not a memory leak) to save context in a static field as long it is
    // the Application context, which we get in the getInstance() method
    @SuppressLint("StaticFieldLeak")
    private static AirtableApiService instance;
    private Context appContext;

    private AirtableApiService() {
    }

    public static AirtableApiService getInstance(Context context) {
        if (instance == null) {
            instance = new AirtableApiService();
        }
        // IMPORTANT to save the Application context and not just the context
        // which is passed in this method that could be the activity context.
        // Saving the activity context in a static field would cause a memory leak
        // since the activity, not losing all references, would survive for the entire
        // application life
        instance.appContext = context.getApplicationContext();
        return instance;
    }

    // TODO: Remove hardcoded API KEY (?)
    private final String API_KEY = "keyKTETfgRQ5SHHLK";
    private final String apiUrl = "https://api.airtable.com/v0/";
    private final String baseId = "appT9OoAOwKHPXfYX";
    private final String baseUrl = apiUrl + baseId + "/";
    private final String itemsBaseQuery = baseUrl + "Item?" +
            "fields%5B%5D=ID" +
            "&fields%5B%5D=Name" +
            "&fields%5B%5D=Description" +
            "&fields%5B%5D=Latitude" +
            "&fields%5B%5D=Longitude" +
            "&fields%5B%5D=Price" +
            "&fields%5B%5D=Picture" +
            "&fields%5B%5D=Thumbnail" +
            "&fields%5B%5D=Type" +
            "&fields%5B%5D=Category";
    private final String categoryBaseQuery = baseUrl + "Category";

    public GsonRequest<Table<Item>> requestItemTable(Response.Listener<Table<Item>> listener, Response.ErrorListener errorListener) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + API_KEY);
        return new GsonRequest<>(
                Request.Method.GET, itemsBaseQuery, headers,
                new TypeToken<Table<Item>>() {
                }.getType(),
                listener, errorListener);
    }

    public GsonRequest<Table<Item>> requestItemTable(double minLatitude, double maxLatitude,
                                                     double minLongitude, double maxLongitude,
                                                     Item.Typology type, String categoryId,
                                                     String searchText,
                                                     Response.Listener<Table<Item>> listener,
                                                     Response.ErrorListener errorListener) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + API_KEY);

        if (categoryId != null && categoryId.trim().isEmpty()) {
            categoryId = null;
        }
        if (searchText != null && searchText.trim().isEmpty()) {
            searchText = null;
        }

        String formula = "AND(" +
                "Latitude>=" + minLatitude +
                ",Latitude<=" + maxLatitude +
                ",Longitude>=" + minLongitude +
                ",Longitude<=" + maxLongitude +
                (type != null ? ",Type=\"" + type + "\"" : "") +
                (categoryId != null ? ",Category=\"" + categoryId + "\"" : "") +
                (searchText != null ? ",FIND(LOWER(\"" + searchText + "\"),LOWER(Name))>0" : "") +
                ")";

        String url = itemsBaseQuery + "&filterByFormula=" + Uri.encode(formula);

        return new GsonRequest<>(
                Request.Method.GET, url, headers,
                new TypeToken<Table<Item>>() {
                }.getType(),
                listener, errorListener);
    }

    public GsonRequest<Table<Category>> requestCategoryTable(Response.Listener<Table<Category>> listener,
                                                             Response.ErrorListener errorListener) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + API_KEY);

        return new GsonRequest<>(
                Request.Method.GET, categoryBaseQuery, headers,
                new TypeToken<Table<Category>>() {
                }.getType(),
                listener, errorListener);
    }
}
