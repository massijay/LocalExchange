package com.mcris.localexchange.views;

import static com.mcris.localexchange.helpers.Utils.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.databinding.ObservableMap;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.maps.android.clustering.ClusterManager;
import com.mcris.localexchange.R;
import com.mcris.localexchange.databinding.ActivityMainBinding;
import com.mcris.localexchange.models.ItemClusterRenderer;
import com.mcris.localexchange.models.entities.Item;
import com.mcris.localexchange.viewmodels.MainViewModel;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityMainBinding binding;
    private MainViewModel mainViewModel;

    private GoogleMap mMap;
    private BottomSheetBehavior<LinearLayout> sheetBehavior;
    private int sheetStateBeforeLastNavigation = BottomSheetBehavior.STATE_COLLAPSED;

    private ClusterManager<Item> clusterManager;
    private FusedLocationProviderClient fusedLocationClient;

    // See: https://developer.android.com/training/basics/intents/result
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            result -> onSignInResult(result)
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        LinearLayout bottomMenuLayout = findViewById(R.id.bottomMenuLayout);
        sheetBehavior = BottomSheetBehavior.from(bottomMenuLayout);
        ImageView menuHandler = findViewById(R.id.menuHandler);

        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.EmailBuilder().build()
        );
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build();

        PopupMenu popupMenu = new PopupMenu(this, binding.moreButton);
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.string.add_menu_item) {
                navigateToFragment(UploadItemFragment.class);
                expandBottomSheet();
            } else if (item.getItemId() == R.string.login_menu_item) {
                signInLauncher.launch(signInIntent);
            } else if (item.getItemId() == R.string.logout_menu_name) {
                AuthUI.getInstance().signOut(MainActivity.this);
            }
            return true;
        });

        if (savedInstanceState == null) {
            navigateToFragment(ItemsListFragment.class, true);
        }

        binding.filterButton.setOnClickListener(v -> {
            navigateToFragment(CategoriesSelectionFragment.class);
            expandBottomSheet();
        });

        binding.moreButton.setOnClickListener(v -> {
            popupMenu.getMenu().clear();
            if (mainViewModel.isUserLoggedIn()) {
                popupMenu.getMenu().add(Menu.NONE, R.string.add_menu_item, 2, R.string.add_menu_item);
                popupMenu.getMenu().add(Menu.NONE, R.string.logout_menu_name, 10, R.string.logout_menu_name);
            } else {
                popupMenu.getMenu().add(Menu.NONE, R.string.login_menu_item, 0, R.string.login_menu_item);
            }
            popupMenu.show();
        });

        menuHandler.setOnClickListener(v -> {
            if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                collapseBottomSheet();
            } else {
                expandBottomSheet();
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
                            Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                            Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                            if (Boolean.TRUE.equals(fineLocationGranted) || Boolean.TRUE.equals(coarseLocationGranted)) {
                                Log.d(TAG, "Location permission granted");
                                CancellationTokenSource cts = new CancellationTokenSource();

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
                                Log.w(TAG, "Location permission denied");
                            }
                        }
                );

        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
        completeSignUpIfNeeded();
        mainViewModel.downloadCategories();
    }

    @SuppressLint("PotentialBehaviorOverride")
    // that can occur with mMap.setOnMarkerClickListener(clusterManager)
    // click listener has to be set on the clusterManager itself
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setPadding(0, binding.typologyToggleGroup.getHeight(), 0, sheetBehavior.getPeekHeight());

        binding.supplyToggleButton.addOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                selectSearchTypology(Item.Typology.SELL);
                goBackToRootFragment();
            }
        });
        binding.demandToggleButton.addOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                selectSearchTypology(Item.Typology.BUY);
                goBackToRootFragment();
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

        clusterManager.setOnClusterItemClickListener(item -> {
            focusItemOnMap(item);
            mainViewModel.setSelectedItem(item);
            navigateToFragment(ItemDetailsFragment.class);
            expandBottomSheet();
            return true;
        });

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

    public void selectSearchTypology(Item.Typology typology) {
        mainViewModel.setTypeOfSearch(typology);
        mainViewModel.getObservableItems().clear();
        obtainItems();
    }

    private void obtainItems() {
        // latLngBounds contain the north-east and south-west coordinates
        // of the rectangle just outside of the screen
        // i.e. the screen vertices are on the sides of the map rectangle
        mainViewModel.setLatLngBounds(mMap.getProjection().getVisibleRegion().latLngBounds);
        mainViewModel.downloadItems();
    }

    public void focusItemOnMap(Item item) {
        setCurrentLocationOnMap(item.getLatLng(), 18f);
    }

    private void setCurrentLocationOnMap(LatLng latLng) {
        setCurrentLocationOnMap(latLng, 17f);
    }

    private void setCurrentLocationOnMap(LatLng latLng, float zoom) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
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

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        if (result.getResultCode() == RESULT_OK) {
            completeSignUpIfNeeded();
        }
    }

    public void goBackToRootFragment() {
        sheetStateBeforeLastNavigation = BottomSheetBehavior.STATE_COLLAPSED;
        collapseBottomSheet();
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry entry = getSupportFragmentManager().getBackStackEntryAt(0);
            getSupportFragmentManager().popBackStackImmediate(entry.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    public <T extends Fragment> void navigateToFragment(Class<T> fragmentClass) {
        navigateToFragment(fragmentClass, false);
    }

    private <T extends Fragment> void navigateToFragment(Class<T> fragmentClass, boolean isRoot) {
        sheetStateBeforeLastNavigation = sheetBehavior.getState();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.bottom_menu_fragment_container, fragmentClass, null);
        if (!isRoot) {
            transaction = transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    private void completeSignUpIfNeeded() {
        mainViewModel.getLoggedUserInfoIfExisting(user -> {
            if (user == null) {
                navigateToFragment(FinishSignUpFragment.class);
                expandBottomSheet();
            }
        });
    }

    private void setSheetBehaviorState(int state) {
        sheetBehavior.setState(state);
    }

    public void collapseBottomSheet() {
        setSheetBehaviorState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    public void expandBottomSheet() {
        setSheetBehaviorState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void onBackPressed() {
        if (sheetBehavior != null) {
            setSheetBehaviorState(sheetStateBeforeLastNavigation);
        }
        super.onBackPressed();
    }

    public void hideSoftKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm.isAcceptingText()) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            hideSoftKeyboard(getCurrentFocus());
        }
        return super.dispatchTouchEvent(ev);
    }
}