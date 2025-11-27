package com.example.finalproject;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button aboutButton;
    private EditText searchEditText;
    private ListView restaurantListView;
    private Button addRestaurantButton;

    private RestaurantDbHelper dbHelper;
    private RestaurantAdapter adapter;
    private List<Restaurant> allRestaurants = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new RestaurantDbHelper(this);

        searchEditText = findViewById(R.id.searchEditText);
        restaurantListView = findViewById(R.id.restaurantListView);
        addRestaurantButton = findViewById(R.id.addRestaurantButton);
        aboutButton = findViewById(R.id.aboutButton);

        aboutButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                filterRestaurants(s.toString());
            }
        });

        addRestaurantButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddRestaurantActivity.class);
            startActivity(intent);
        });

        restaurantListView.setOnItemClickListener((parent, view, position, id) -> {
            Restaurant selected = (Restaurant) parent.getItemAtPosition(position);
            Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
            intent.putExtra("restaurant_id", selected.getId());
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRestaurantsFromDb();
    }

    private void loadRestaurantsFromDb() {
        allRestaurants.clear();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                RestaurantDbHelper.TABLE_NAME,
                null,
                null, null, null, null,
                RestaurantDbHelper.COL_NAME + " ASC"
        );

        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(RestaurantDbHelper.COL_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(RestaurantDbHelper.COL_NAME));
            String address = cursor.getString(cursor.getColumnIndexOrThrow(RestaurantDbHelper.COL_ADDRESS));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow(RestaurantDbHelper.COL_PHONE));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(RestaurantDbHelper.COL_DESCRIPTION));
            String tags = cursor.getString(cursor.getColumnIndexOrThrow(RestaurantDbHelper.COL_TAGS));
            int rating = cursor.getInt(cursor.getColumnIndexOrThrow(RestaurantDbHelper.COL_RATING));

            Restaurant r = new Restaurant(id, name, address, phone, description, tags, rating);
            allRestaurants.add(r);
        }
        cursor.close();

        //Unused debug code
        //android.widget.Toast.makeText(this,
        //        "Loaded " + allRestaurants.size() + " restaurants",
        //        android.widget.Toast.LENGTH_SHORT).show();

        adapter = new RestaurantAdapter(this, allRestaurants);
        restaurantListView.setAdapter(adapter);
    }

    private void filterRestaurants(String query) {
        query = query.toLowerCase().trim();

        if (query.isEmpty()) {
            adapter = new RestaurantAdapter(this, allRestaurants);
            restaurantListView.setAdapter(adapter);
            return;
        }

        java.util.List<Restaurant> filtered = new java.util.ArrayList<>();
        for (Restaurant r : allRestaurants) {
            String name = r.getName() != null ? r.getName().toLowerCase() : "";
            String tags = r.getTags() != null ? r.getTags().toLowerCase() : "";

            if (name.contains(query) || tags.contains(query)) {
                filtered.add(r);
            }
        }

        adapter = new RestaurantAdapter(this, filtered);
        restaurantListView.setAdapter(adapter);
    }
}