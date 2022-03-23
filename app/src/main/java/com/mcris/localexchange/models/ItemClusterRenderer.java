package com.mcris.localexchange.models;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.UiContext;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.mcris.localexchange.models.entities.Item;

public class ItemClusterRenderer extends DefaultClusterRenderer<Item> {
    private final ItemMarkerGenerator markerGenerator;

    public ItemClusterRenderer(@UiContext Context context, GoogleMap map, ClusterManager<Item> clusterManager) {
        super(context, map, clusterManager);
        markerGenerator = new ItemMarkerGenerator(context);
    }

    @Override
    protected void onBeforeClusterItemRendered(@NonNull Item item, @NonNull MarkerOptions markerOptions) {
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(markerGenerator.drawMarker(item)));
    }
}
