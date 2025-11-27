package com.example.finalproject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RestaurantDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "restaurants.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "restaurants";
    public static final String COL_ID = "_id";
    public static final String COL_NAME = "name";
    public static final String COL_ADDRESS = "address";
    public static final String COL_PHONE = "phone";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_TAGS = "tags";
    public static final String COL_RATING = "rating";

    public RestaurantDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT NOT NULL, " +
                COL_ADDRESS + " TEXT, " +
                COL_PHONE + " TEXT, " +
                COL_DESCRIPTION + " TEXT, " +
                COL_TAGS + " TEXT, " +
                COL_RATING + " INTEGER DEFAULT 0" +
                ");";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}