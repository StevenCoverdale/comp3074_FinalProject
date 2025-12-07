package com.example.finalproject;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DetailsActivity extends AppCompatActivity {

    private TextView textName;
    private TextView textAddress;
    private TextView textPhone;
    private TextView textTags;
    private TextView textDescription;
    private RatingBar ratingBar;
    private Button buttonViewOnMap;
    private Button buttonDirections;

    private RestaurantDbHelper dbHelper;
    private long restaurantId = -1;
    private Restaurant currentRestaurant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_details);

        // Handle system insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new RestaurantDbHelper(this);

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.detailToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Restaurant Details");
        }

        // Bind views
        textName = findViewById(R.id.textDetailName);
        textAddress = findViewById(R.id.textDetailAddress);
        textPhone = findViewById(R.id.textDetailPhone);
        textTags = findViewById(R.id.textDetailTags);
        textDescription = findViewById(R.id.textDetailDescription);
        ratingBar = findViewById(R.id.detailRatingBar);
        buttonViewOnMap = findViewById(R.id.buttonViewOnMap);
        buttonDirections = findViewById(R.id.buttonDirections);

        // Get the ID passed from MainActivity
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("restaurant_id")) {
            restaurantId = intent.getLongExtra("restaurant_id", -1);
        }

        if (restaurantId == -1) {
            Toast.makeText(this, "No restaurant ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadRestaurant();

        ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            if (fromUser && currentRestaurant != null) {
                updateRatingInDb((int) rating);
            }
        });

        buttonViewOnMap.setOnClickListener(v -> openMap());
        buttonDirections.setOnClickListener(v -> openDirections());

        // Could not add Uber API due to lack of time; using Uber Eats website instead.
        Button uberButton = findViewById(R.id.buttonOrderWithUber);
        uberButton.setOnClickListener(v -> {
            String uberUrl = "https://www.ubereats.com/ca";
            Intent intentUber = new Intent(Intent.ACTION_VIEW, Uri.parse(uberUrl));
            startActivity(intentUber);
        });
    }

    // Inflate menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }

    // Handle menu clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_share) {
            shareRestaurant();
            return true;
        } else if (id == R.id.action_edit) {
            editRestaurant();
            return true;
        } else if (id == R.id.action_delete) {
            confirmDelete();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void loadRestaurant() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                RestaurantDbHelper.TABLE_NAME,
                null,
                RestaurantDbHelper.COL_ID + "=?",
                new String[]{String.valueOf(restaurantId)},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(RestaurantDbHelper.COL_NAME));
            String address = cursor.getString(cursor.getColumnIndexOrThrow(RestaurantDbHelper.COL_ADDRESS));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow(RestaurantDbHelper.COL_PHONE));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(RestaurantDbHelper.COL_DESCRIPTION));
            String tags = cursor.getString(cursor.getColumnIndexOrThrow(RestaurantDbHelper.COL_TAGS));
            int rating = cursor.getInt(cursor.getColumnIndexOrThrow(RestaurantDbHelper.COL_RATING));

            currentRestaurant = new Restaurant(restaurantId, name, address, phone, description, tags, rating);

            textName.setText(name);
            textAddress.setText(address);
            textPhone.setText(phone);
            textTags.setText(tags);
            textDescription.setText(description);
            ratingBar.setRating(rating);
        } else {
            Toast.makeText(this, "Restaurant not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        cursor.close();
    }

    private void updateRatingInDb(int newRating) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(RestaurantDbHelper.COL_RATING, newRating);

        db.update(RestaurantDbHelper.TABLE_NAME,
                values,
                RestaurantDbHelper.COL_ID + "=?",
                new String[]{String.valueOf(restaurantId)});

        if (currentRestaurant != null) {
            currentRestaurant.setRating(newRating);
        }
    }

    private void openMap() {
        if (currentRestaurant == null) return;

        String address = currentRestaurant.getAddress();
        if (address == null || address.trim().isEmpty()) {
            Toast.makeText(this, "No address available", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(DetailsActivity.this, MapActivity.class);
        intent.putExtra("restaurant_name", currentRestaurant.getName());
        intent.putExtra("restaurant_address", address);
        startActivity(intent);
    }

    private void openDirections() {
        if (currentRestaurant == null) return;

        String address = currentRestaurant.getAddress();
        if (address == null || address.trim().isEmpty()) {
            Toast.makeText(this, "No address available", Toast.LENGTH_SHORT).show();
            return;
        }

        String uri = "google.navigation:q=" + Uri.encode(address);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "Google Maps not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareRestaurant() {
        if (currentRestaurant == null) return;

        String subject = "Check out this restaurant: " + currentRestaurant.getName();
        String body = "Name: " + currentRestaurant.getName() + "\n" +
                "Address: " + currentRestaurant.getAddress() + "\n" +
                "Phone: " + currentRestaurant.getPhone() + "\n" +
                "Description: " + currentRestaurant.getDescription() + "\n" +
                "Tags: " + currentRestaurant.getTags() + "\n" +
                "Rating: " + currentRestaurant.getRating() + "/5";

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);

        startActivity(Intent.createChooser(intent, "Share via"));
    }

    private void editRestaurant() {
        if (currentRestaurant == null) return;

        Intent intent = new Intent(this, AddRestaurantActivity.class);
        intent.putExtra("restaurant_id", restaurantId);
        startActivity(intent);
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete restaurant")
                .setMessage("Are you sure you want to delete this restaurant?")
                .setPositiveButton("Delete", (dialog, which) -> deleteRestaurant())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteRestaurant() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(RestaurantDbHelper.TABLE_NAME,
                RestaurantDbHelper.COL_ID + "=?",
                new String[]{String.valueOf(restaurantId)});
        Toast.makeText(this, "Restaurant deleted", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (restaurantId != -1) {
            loadRestaurant();
        }
    }
}