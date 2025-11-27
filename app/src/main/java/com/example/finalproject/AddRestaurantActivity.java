package com.example.finalproject;

import android.content.ContentValues;
import android.database.Cursor;
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
    private Button buttonSave;

    private RestaurantDbHelper dbHelper;

    private long restaurantId = -1;
    private boolean isEditMode = false;

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
        buttonSave = findViewById(R.id.buttonSaveRestaurant);

        if (getIntent() != null && getIntent().hasExtra("restaurant_id")) {
            restaurantId = getIntent().getLongExtra("restaurant_id", -1);
            if (restaurantId != -1) {
                isEditMode = true;
                loadRestaurantForEdit();
            }
        }

        if (isEditMode) {
            buttonSave.setText("Update Restaurant");
        } else {
            buttonSave.setText("Save Restaurant");
        }

        buttonSave.setOnClickListener(v -> saveRestaurant());
    }

    private void loadRestaurantForEdit() {
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

            editName.setText(name);
            editAddress.setText(address);
            editPhone.setText(phone);
            editDescription.setText(description);
            editTags.setText(tags);
            ratingBar.setRating(rating);
        }

        cursor.close();
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

        if (isEditMode && restaurantId != -1) {
            int rows = db.update(RestaurantDbHelper.TABLE_NAME,
                    values,
                    RestaurantDbHelper.COL_ID + "=?",
                    new String[]{String.valueOf(restaurantId)});
            if (rows > 0) {
                Toast.makeText(this, "Restaurant updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error updating restaurant", Toast.LENGTH_SHORT).show();
            }
        } else {
            long newId = db.insert(RestaurantDbHelper.TABLE_NAME, null, values);
            if (newId == -1) {
                Toast.makeText(this, "Error saving restaurant", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Restaurant saved with ID " + newId, Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }
}