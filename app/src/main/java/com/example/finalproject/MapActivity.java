package com.example.finalproject;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;

    private String restaurantName = "Restaurant";
    private String restaurantAddress = "";

    private double restaurantLat = 0.0;
    private double restaurantLng = 0.0;

    private TextView textMapAddress;
    private Button btnStartNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Receive name + address from DetailsActivity
        Intent intent = getIntent();
        if (intent != null) {
            restaurantName = intent.getStringExtra("restaurant_name");
            restaurantAddress = intent.getStringExtra("restaurant_address");
        }

        // Toolbar back arrow
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            //Tool Bar Title
            getSupportActionBar().setTitle("Map");
        }

        // UI at bottom
        textMapAddress = findViewById(R.id.textMapAddress);
        btnStartNavigation = findViewById(R.id.btnStartNavigation);

        textMapAddress.setText(restaurantAddress);

        // Directions button
        btnStartNavigation.setOnClickListener(v -> {
            if (restaurantAddress.isEmpty()) {
                Toast.makeText(this, "No address provided", Toast.LENGTH_SHORT).show();
                return;
            }
            String uri = "google.navigation:q=" + Uri.encode(restaurantAddress);
            Intent navIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            navIntent.setPackage("com.google.android.apps.maps");
            startActivity(navIntent);
        });

        // Load map
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.mapFragment);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        LatLng location = geocodeAddress(restaurantAddress);
        if (location != null) {
            googleMap.addMarker(new MarkerOptions().position(location).title(restaurantName));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
        } else {
            Toast.makeText(this, "Unable to locate address", Toast.LENGTH_LONG).show();
        }

        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
    }
    private LatLng geocodeAddress(String addressString) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> results = geocoder.getFromLocationName(addressString, 1);

            if (results != null && !results.isEmpty()) {
                Address addr = results.get(0);
                restaurantLat = addr.getLatitude();
                restaurantLng = addr.getLongitude();
                return new LatLng(restaurantLat, restaurantLng);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    // For Toolbar back arrow
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
