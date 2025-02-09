package com.example.Nutrition_Analysis;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "NutritionDB";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "nutrition_records";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_IMAGE_PATH = "image_path";
    private static final String COLUMN_NUTRITION_DETAILS = "nutrition_details";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_IMAGE_PATH + " TEXT, " +
                COLUMN_NUTRITION_DETAILS + " TEXT)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertRecord(String imagePath, String nutritionDetails) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IMAGE_PATH, imagePath);
        values.put(COLUMN_NUTRITION_DETAILS, nutritionDetails);

        long result = db.insert(TABLE_NAME, null, values);
        db.close();
        return result != -1;  // Return true if inserted successfully
    }

    public List<NutritionRecord> getAllRecords() {
        List<NutritionRecord> records = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_ID + " DESC", null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH));
                String nutritionDetails = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NUTRITION_DETAILS));

                records.add(new NutritionRecord(id, imagePath, nutritionDetails));
            }
            cursor.close();
        }
        db.close();
        return records;
    }

    public void clearAllRecords() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }
}
