package com.mcris.localexchange.views;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mcris.localexchange.R;
import com.mcris.localexchange.databinding.ActivityMapsBinding;
import com.mcris.localexchange.models.Item;
import com.mcris.localexchange.models.Record;
import com.mcris.localexchange.models.Table;
import com.mcris.localexchange.services.AirtableApiService;
import com.mcris.localexchange.services.GsonRequest;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(45.6595, 13.7947);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Trieste"));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(17));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


        RequestQueue queue = Volley.newRequestQueue(this);
        GsonRequest<Table<Item>> itemTableRequest = AirtableApiService.getInstance(this)
                .requestItemTable(
                        response -> {
                            int n = response.getRecords().size();
                            double latTot = 0d;
                            double longTot = 0d;
                            for (Record<Item> record : response.getRecords()) {
                                Item item = record.getRow();
                                latTot += item.getLatitude();
                                longTot += item.getLongitude();
                                LatLng p = new LatLng(item.getLatitude(), item.getLongitude());
                                mMap.addMarker(new MarkerOptions().position(p).title(item.getName()));
                            }
                            LatLng center = new LatLng(latTot / n, longTot / n);
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(center));
                            mMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                        },
                        error -> Toast.makeText(MapsActivity.this, "ERRORE: " + error.getMessage(), Toast.LENGTH_LONG).show());
        queue.add(itemTableRequest);
    }
}