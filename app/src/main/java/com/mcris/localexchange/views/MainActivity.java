package com.mcris.localexchange.views;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.maps.android.clustering.ClusterManager;
import com.mcris.localexchange.R;
import com.mcris.localexchange.databinding.ActivityMainBinding;
import com.mcris.localexchange.models.CustomClusterRenderer;
import com.mcris.localexchange.models.Item;
import com.mcris.localexchange.models.Record;
import com.mcris.localexchange.models.Table;
import com.mcris.localexchange.services.AirtableApiService;
import com.mcris.localexchange.services.GsonRequest;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMainBinding binding;
    private ClusterManager<Item> clusterManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        binding.detailsLayout.post(() -> {
            if (mMap != null) {
                int bottomPadding = binding.detailsLayout.getHeight();
                Log.i("AAA", "Bottom padding = " + bottomPadding);
                mMap.setPadding(0, 0, 0, bottomPadding);
            }
        });


        binding.testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("AAA", "Cliccato");
                Intent intent = new Intent(v.getContext(), SecondActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        clusterManager = new ClusterManager<Item>(this, mMap);
        clusterManager.setRenderer(new CustomClusterRenderer<Item>(this, mMap, clusterManager));

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);

        // Add a marker in Sydney and move the camera
//        LatLng units = new LatLng(45.6595, 13.7947);
//        mMap.addMarker(new MarkerOptions().position(units).title("Marker in Trieste"));
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
                                if (item.getPictureUrl() == null || item.getPictureUrl().isEmpty()) {
                                    item.setMarkerBitmap(drawBitmapMarker(item, null));
                                    clusterManager.addItem(item);
                                    clusterManager.cluster();
                                } else {
                                    FirebaseStorage storage = FirebaseStorage.getInstance();

                                    StorageReference imgRef = storage.getReferenceFromUrl(item.getPictureUrl());

                                    final long FIVE_MEGABYTES = 5 * 1024 * 1024;
                                    imgRef.getBytes(FIVE_MEGABYTES)
                                            .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                                @Override
                                                public void onSuccess(byte[] bytes) {
                                                    Bitmap pic = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                                    item.setMarkerBitmap(drawBitmapMarker(item, pic));
                                                    clusterManager.addItem(item);
                                                    clusterManager.cluster();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                }
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

    public Bitmap drawBitmapMarker(Item item, Bitmap picture) {
        View markerLayout = getLayoutInflater().inflate(R.layout.custom_marker_layout, binding.getRoot(), false);
        ImageView imageView = markerLayout.findViewById(R.id.markerImageView);
        TextView mainTextView = markerLayout.findViewById(R.id.mainTextView);
        TextView priceTextView = markerLayout.findViewById(R.id.priceTextView);

        mainTextView.setText(item.getName());
        priceTextView.setText(String.format(Locale.getDefault(), "%.0f€", item.getPrice()));

        if (item.getPictureUrl() == null || item.getPictureUrl().isEmpty()) {
            imageView.setVisibility(View.GONE);
            return renderBitmap(markerLayout);
        }
        imageView.setImageBitmap(picture);
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