package com.example.finalproject;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddRestaurantActivity extends AppCompatActivity {

    private EditText editName;
    private EditText editAddress;
    private EditText editPhone;
    private EditText editDescription;
    private EditText editTags;
    private RatingBar ratingBar;

    private RestaurantDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_restaurant);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new RestaurantDbHelper(this);

        editName = findViewById(R.id.editName);
        editAddress = findViewById(R.id.editAddress);
        editPhone = findViewById(R.id.editPhone);
        editDescription = findViewById(R.id.editDescription);
        editTags = findViewById(R.id.editTags);
        ratingBar = findViewById(R.id.editRatingBar);
        Button buttonSave = findViewById(R.id.buttonSaveRestaurant);

        buttonSave.setOnClickListener(v -> saveRestaurant());
    }

    private void saveRestaurant() {
        String name = editName.getText().toString().trim();
        String address = editAddress.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String description = editDescription.getText().toString().trim();
        String tags = editTags.getText().toString().trim();
        int rating = (int) ratingBar.getRating();

        if (name.isEmpty()) {
            editName.setError("Name is required");
            editName.requestFocus();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(RestaurantDbHelper.COL_NAME, name);
        values.put(RestaurantDbHelper.COL_ADDRESS, address);
        values.put(RestaurantDbHelper.COL_PHONE, phone);
        values.put(RestaurantDbHelper.COL_DESCRIPTION, description);
        values.put(RestaurantDbHelper.COL_TAGS, tags);
        values.put(RestaurantDbHelper.COL_RATING, rating);

        long newId = db.insert(RestaurantDbHelper.TABLE_NAME, null, values);

        if (newId == -1) {
            Toast.makeText(this, "Error saving restaurant", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Restaurant saved", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}