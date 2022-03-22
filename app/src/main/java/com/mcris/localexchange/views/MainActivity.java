package com.mcris.localexchange.views;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.maps.android.clustering.ClusterManager;
import com.mcris.localexchange.R;
import com.mcris.localexchange.databinding.ActivityMainBinding;
import com.mcris.localexchange.models.CustomClusterRenderer;
import com.mcris.localexchange.models.Item;
import com.mcris.localexchange.models.ItemsAdapter;
import com.mcris.localexchange.models.Record;
import com.mcris.localexchange.models.Table;
import com.mcris.localexchange.services.AirtableApiService;
import com.mcris.localexchange.services.GsonRequest;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMainBinding binding;
    private ClusterManager<Item> clusterManager;

    private RecyclerView recyclerView;
    private ItemsAdapter itemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        LinearLayout bottomMenuLayout = findViewById(R.id.bottomMenuLayout);
        BottomSheetBehavior<LinearLayout> sheetBehavior = BottomSheetBehavior.from(bottomMenuLayout);
        ImageView menuHandler = findViewById(R.id.menuHandler);

        recyclerView = findViewById(R.id.mainRecyclerView);
        itemsAdapter = new ItemsAdapter(new ArrayList<Item>());
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
    // with mMap.setOnCameraIdleListener(clusterManager)
    // click listener has to be set on the clusterManager itself
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        clusterManager = new ClusterManager<>(this, mMap);
        clusterManager.setRenderer(new CustomClusterRenderer<>(this, mMap, clusterManager));

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);

        // HARDCODED current Coordinates
        LatLng casa = new LatLng(45.8727, 13.4792);
        mMap.moveCamera(CameraUpdateFactory.zoomTo(17.0001f));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(casa));

        RequestQueue queue = Volley.newRequestQueue(this);
        GsonRequest<Table<Item>> itemTableRequest = AirtableApiService.getInstance(this)
                .requestItemTable(
                        response -> {
                            for (Record<Item> record : response.getRecords()) {
                                Item item = record.getRow();
                                if (item.getThumbnailUrl() == null || item.getThumbnailUrl().isEmpty()) {
//                                    item.setThumbnailBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.test_product));
                                    item.setMarkerBitmap(drawBitmapMarker(item));
                                    clusterManager.addItem(item);
                                    itemsAdapter.addItem(item);
                                    clusterManager.cluster();
                                } else {
                                    FirebaseStorage storage = FirebaseStorage.getInstance();

                                    StorageReference imgRef = storage.getReferenceFromUrl(item.getThumbnailUrl());

                                    final long ONE_MEGABYTE = 1024 * 1024;
                                    imgRef.getBytes(ONE_MEGABYTE)
                                            .addOnSuccessListener(bytes -> {
                                                item.setThumbnailBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                                                item.setMarkerBitmap(drawBitmapMarker(item));
                                                clusterManager.addItem(item);
                                                itemsAdapter.addItem(item);
                                                clusterManager.cluster();
                                            })
                                            .addOnFailureListener(e -> {
                                            });
                                }
                            }
                        },
                        error -> Toast.makeText(MainActivity.this, "ERRORE: " + error.getMessage(), Toast.LENGTH_LONG).show());
        queue.add(itemTableRequest);
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

    public Bitmap drawBitmapMarker(Item item) {
        View markerLayout = getLayoutInflater().inflate(R.layout.custom_marker_layout, binding.getRoot(), false);
        ImageView imageView = markerLayout.findViewById(R.id.markerImageView);
        TextView mainTextView = markerLayout.findViewById(R.id.mainTextView);
        TextView priceTextView = markerLayout.findViewById(R.id.priceTextView);

        mainTextView.setText(item.getName());
        priceTextView.setText(String.format(Locale.getDefault(), "%.0f€", item.getPrice()));

        if (item.getThumbnailBitmap() == null) {
            imageView.setVisibility(View.GONE);
            return renderBitmap(markerLayout);
        }
        imageView.setImageBitmap(item.getThumbnailBitmap());
        imageView.setContentDescription(item.getName());


        return renderBitmap(markerLayout);
    }

    @NonNull
    private Bitmap renderBitmap(View markerLayout) {
        markerLayout.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        markerLayout.layout(0, 0, markerLayout.getMeasuredWidth(), markerLayout.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(
                markerLayout.getMeasuredWidth(),
                markerLayout.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        float radius = 15f;
        Path clipPath = new Path();
        RectF rect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
        clipPath.addRoundRect(rect, radius, radius, Path.Direction.CW);
        canvas.clipPath(clipPath);

        markerLayout.draw(canvas);
        return bitmap;
    }
}