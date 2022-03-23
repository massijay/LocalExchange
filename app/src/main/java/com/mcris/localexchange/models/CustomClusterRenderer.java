package com.mcris.localexchange.models;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.mcris.localexchange.models.entities.Item;

public class CustomClusterRenderer<T extends Item> extends DefaultClusterRenderer<T> {
    private final ItemMarkerGenerator markerGenerator;

    public CustomClusterRenderer(Context context, GoogleMap map, ClusterManager<T> clusterManager) {
        super(context, map, clusterManager);
        markerGenerator = new ItemMarkerGenerator(context);
    }

    @Override
    protected void onBeforeClusterItemRendered(@NonNull T item, @NonNull MarkerOptions markerOptions) {
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(markerGenerator.drawMarker(item)));
    }
}
