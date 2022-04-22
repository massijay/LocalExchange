package com.mcris.localexchange.views;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.ObservableMap;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.maps.android.clustering.ClusterManager;
import com.mcris.localexchange.R;
import com.mcris.localexchange.databinding.ActivityMainBinding;
import com.mcris.localexchange.models.ItemClusterRenderer;
import com.mcris.localexchange.models.entities.Item;
import com.mcris.localexchange.viewmodels.MainViewModel;

import java.util.Collection;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityMainBinding binding;
    private MainViewModel mainViewModel;

    private GoogleMap mMap;
    private BottomSheetBehavior<LinearLayout> sheetBehavior;

    private ClusterManager<Item> clusterManager;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        LinearLayout bottomMenuLayout = findViewById(R.id.bottomMenuLayout);
        sheetBehavior = BottomSheetBehavior.from(bottomMenuLayout);
        ImageView menuHandler = findViewById(R.id.menuHandler);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.bottom_menu_fragment_container, ItemsListFragment.class, null)
                    .commit();
        }

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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        @SuppressLint("MissingPermission") // This code checks the granted permissions already
        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                            // Map.getOrDefault() requires Android API 24
                            // Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                            boolean fineLocationGranted = getOrDefault(result, Manifest.permission.ACCESS_FINE_LOCATION, false);
                            boolean coarseLocationGranted = getOrDefault(result, Manifest.permission.ACCESS_COARSE_LOCATION, false);
                            if (fineLocationGranted || coarseLocationGranted) {
                                Log.d("AAA", "LOCAZIONE CONCESSA");
                                CancellationTokenSource cts = new CancellationTokenSource();

                                // Get the last known location
//                                fusedLocationClient.getLastLocation()
//                                        .addOnSuccessListener(this, location -> {
//                                            // Got last known location. In some rare situations this can be null.
//                                            if (location != null) {
//                                                mainViewModel.getUserLocation().setValue(new LatLng(location.getLatitude(), location.getLongitude()));
//                                            }
//                                        });

                                fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, cts.getToken())
                                        .addOnSuccessListener(location -> {
                                            // Got last known location. In some rare situations this can be null.
                                            if (location != null) {
                                                mainViewModel.getUserLocation().setValue(new LatLng(location.getLatitude(), location.getLongitude()));
                                            }
                                        });

                                // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                        .findFragmentById(R.id.map);
                                if (mapFragment != null) {
                                    mapFragment.getMapAsync(this);
                                }
                            } else {
                                Log.d("AAA", "NESSUNA LOCAZIONE CONCESSA");
                            }
                        }
                );

        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
        mainViewModel.obtainCategories();
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
        mMap.setPadding(0, binding.typologyToggleGroup.getHeight(), 0, sheetBehavior.getPeekHeight());

        binding.supplyToggleButton.addOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                mainViewModel.setTypeOfSearch(Item.Typology.SELL);
                mainViewModel.getObservableItems().clear();
                obtainItems();
            }
        });
        binding.demandToggleButton.addOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                mainViewModel.setTypeOfSearch(Item.Typology.BUY);
                mainViewModel.getObservableItems().clear();
                obtainItems();
            }
        });

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        clusterManager = new ClusterManager<Item>(this, mMap) {
            @Override
            public void onCameraIdle() {
                super.onCameraIdle();
                obtainItems();
            }
        };
        clusterManager.setRenderer(new ItemClusterRenderer(this, mMap, clusterManager));

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);

        mainViewModel.getUserLocation().observe(this, latLng -> setCurrentLocationOnMap(latLng));

        setCurrentLocationOnMap(mainViewModel.getUserLocation().getValue());

        addItemsToMap(mainViewModel.getObservableItems().values());

        mainViewModel.getObservableItems().addOnMapChangedCallback(
                new ObservableMap.OnMapChangedCallback<ObservableMap<String, Item>, String, Item>() {
                    @Override
                    public void onMapChanged(ObservableMap<String, Item> sender, String key) {
                        if (sender.size() == 0) clearItemsOnMap();
                        if (key != null) addItemToMap(sender.get(key));
                    }
                });
    }

    private void obtainItems() {
        // latLngBounds contain the north-east and south-west coordinates
        // of the rectangle just outside of the screen
        // i.e. the screen vertices are on the sides of the map rectangle
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;

        double minLatitude = bounds.southwest.latitude;
        double minLongitude = bounds.southwest.longitude;
        double maxLatitude = bounds.northeast.latitude;
        double maxLongitude = bounds.northeast.longitude;
        mainViewModel.obtainItems(minLatitude, maxLatitude, minLongitude, maxLongitude);
    }

    private void setCurrentLocationOnMap(LatLng latLng) {
        mMap.moveCamera(CameraUpdateFactory.zoomTo(17f));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    private void addItemToMap(Item item) {
        clusterManager.addItem(item);
        clusterManager.cluster();
    }

    private void addItemsToMap(Collection<Item> items) {
        clusterManager.addItems(items);
        clusterManager.cluster();
    }

    private void clearItemsOnMap() {
        clusterManager.clearItems();
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

    private <K, V> V getOrDefault(Map<K, V> map, K key, V defaultValue) {
        V v;
        return (((v = map.get(key)) != null) || map.containsKey(key))
                ? v
                : defaultValue;
    }
}