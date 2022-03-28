package com.mcris.localexchange.services;


import android.annotation.SuppressLint;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.google.gson.reflect.TypeToken;
import com.mcris.localexchange.models.entities.Item;
import com.mcris.localexchange.models.entities.Table;

import java.text.MessageFormat;
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
            "&fields%5B%5D=Thumbnail";

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
                                                     Response.Listener<Table<Item>> listener,
                                                     Response.ErrorListener errorListener) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + API_KEY);

        //Url formatted: &filterByFormula=AND(AND(Latitude >= {0}, Latitude <= {1}), AND(Longitude >= {2}, Longitude <= {3}))
        String formula = MessageFormat.format("&filterByFormula=AND%28AND%28Latitude%20%3E%3D%20{0}%2C%20Latitude%20%3C%3D%20{1}%29%2C%20AND%28Longitude%20%3E%3D%20{2}%2C%20Longitude%20%3C%3D%20{3}%29%29",
                minLatitude, maxLatitude, minLongitude, maxLongitude);

        return new GsonRequest<>(
                Request.Method.GET, itemsBaseQuery + formula, headers,
                new TypeToken<Table<Item>>() {
                }.getType(),
                listener, errorListener);
    }
}
