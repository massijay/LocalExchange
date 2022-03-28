package com.mcris.localexchange.views;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.ObservableMap;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.maps.android.clustering.ClusterManager;
import com.mcris.localexchange.R;
import com.mcris.localexchange.databinding.ActivityMainBinding;
import com.mcris.localexchange.models.ItemClusterRenderer;
import com.mcris.localexchange.models.ItemsAdapter;
import com.mcris.localexchange.models.entities.Item;
import com.mcris.localexchange.viewmodels.MainViewModel;

import java.util.Collection;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMainBinding binding;
    private ClusterManager<Item> clusterManager;

    private RecyclerView recyclerView;
    private ItemsAdapter itemsAdapter;
    private MainViewModel mainViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        LinearLayout bottomMenuLayout = findViewById(R.id.bottomMenuLayout);
        BottomSheetBehavior<LinearLayout> sheetBehavior = BottomSheetBehavior.from(bottomMenuLayout);
        ImageView menuHandler = findViewById(R.id.menuHandler);

        recyclerView = findViewById(R.id.mainRecyclerView);
        itemsAdapter = new ItemsAdapter();
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(itemsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        menuHandler.setOnClickListener(v -> {
            if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else {
                sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        sheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("PotentialBehaviorOverride")
    // that can occur with mMap.setOnMarkerClickListener(clusterManager)
    // click listener has to be set on the clusterManager itself
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        clusterManager = new ClusterManager<Item>(this, mMap) {
            @Override
            public void onCameraIdle() {
                super.onCameraIdle();
                // latLngBounds contain the north-east and south-west coordinates
                // of the rectangle just outside of the screen
                // i.e. the screen vertices are on the sides of the map rectangle
                LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                // Tests
//                LatLng farLeft = region.farLeft;
//                LatLng nearLeft = region.nearLeft;
//                LatLng farRight = region.farRight;
//                LatLng nearRight = region.nearRight;
//
//                double maxLat = Math.max(farLeft.latitude, Math.max(nearLeft.latitude, Math.max(farRight.latitude, nearRight.latitude)));
//                double minLat = Math.min(farLeft.latitude, Math.min(nearLeft.latitude, Math.min(farRight.latitude, nearRight.latitude)));
//                double maxLong = Math.max(farLeft.longitude, Math.max(nearLeft.longitude, Math.max(farRight.longitude, nearRight.longitude)));
//                double minLong = Math.min(farLeft.longitude, Math.min(nearLeft.longitude, Math.min(farRight.longitude, nearRight.longitude)));
//
//                if (bounds.northeast.latitude == maxLat &&
//                        bounds.northeast.longitude == maxLong &&
//                        bounds.southwest.latitude == minLat &&
//                        bounds.southwest.longitude == minLong) {
//                    Log.i("WUT", bounds.toString());
//                } else {
//                    Log.e("WUT", bounds.toString());
//                }
                double minLatitude = bounds.southwest.latitude;
                double minLongitude = bounds.southwest.longitude;
                double maxLatitude = bounds.northeast.latitude;
                double maxLongitude = bounds.northeast.longitude;
                mainViewModel.obtainItems(minLatitude, maxLatitude, minLongitude, maxLongitude);
            }
        };
        clusterManager.setRenderer(new ItemClusterRenderer(this, mMap, clusterManager));

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);

        // HARDCODED current Coordinates
        LatLng casa = new LatLng(45.8727, 13.4792);
        mMap.moveCamera(CameraUpdateFactory.zoomTo(17.0001f));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(casa));

        itemsAdapter.setReferencePosition(casa);
        addItemsToMapAndList(mainViewModel.getObservableItems().values());

        mainViewModel.getObservableItems().addOnMapChangedCallback(
                new ObservableMap.OnMapChangedCallback<ObservableMap<String, Item>, String, Item>() {
                    @Override
                    public void onMapChanged(ObservableMap<String, Item> sender, String key) {
                        addItemToMapAndList(sender.get(key));
                    }
                });

//        mainViewModel.obtainItems();
    }

    private void addItemToMapAndList(Item item) {
        clusterManager.addItem(item);
        itemsAdapter.addItem(item);
        clusterManager.cluster();
    }

    private void addItemsToMapAndList(Collection<Item> items) {
        clusterManager.addItems(items);
        itemsAdapter.addItems(items);
        clusterManager.cluster();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("AAA", "DESTROY MainActivity");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("AAA", "STOP MainActivity");
    }
}