package com.example.patil.tabtest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by patil on 5/20/2017.
 */

public class DBHandler extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "closetItemInfo";
    // table name
    private static final String TABLE_CLOSET = "closet";

    // closet Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_TYPE = "type";
    private static final String KEY_COLOR = "color";
    private static final String KEY_IMAGESOURCE = "imageSource";
    private static final String KEY_FAV= "fav";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CLOSET_TABLE = "CREATE TABLE " + TABLE_CLOSET + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TYPE + " TEXT,"
                + KEY_COLOR + " TEXT," + KEY_IMAGESOURCE + " BLOB,"+ KEY_FAV + " INTEGER" +
                ")";
        db.execSQL(CREATE_CLOSET_TABLE);

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLOSET);
        // Creating tables again
        onCreate(db);
    }

    // Adding new ClosetItem
    public void addClosetItem(ClosetItem closetItem) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TYPE, closetItem.getType()); // ClosetItem type
        values.put(KEY_COLOR, closetItem.getColor()); // ClosetItem color
        values.put(KEY_IMAGESOURCE, closetItem.getImageSource());//ClosetItem imageSource
        values.put(KEY_FAV, closetItem.getFav());//ClosetItem fav

        // Inserting Row
        db.insert(TABLE_CLOSET, null, values);
        db.close(); // Closing database connection
    }

    public List<ClosetItem> getAllItems() {
        List<ClosetItem> closetItemsList = new ArrayList<ClosetItem>();

        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_CLOSET;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);


        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ClosetItem closetItem = new ClosetItem();
                closetItem.setId(Integer.parseInt(cursor.getString(0)));
                closetItem.setType(cursor.getString(1));
                closetItem.setColor(cursor.getString(2));
                closetItem.setImageSource(cursor.getBlob(3));
                closetItem.setFav(Integer.parseInt(cursor.getString(4)));

                // Adding closet item to list
                closetItemsList.add(closetItem);
            } while (cursor.moveToNext());
        }

        db.close();
        // return closet item list
        return closetItemsList;
    }

    public void updateFavorite(){

        String updateFavQuery = "UPDATE " + TABLE_CLOSET + " set fav = 1 WHERE id = (SELECT MAX(id) FROM " + TABLE_CLOSET + " )";

        SQLiteDatabase db = this.getWritableDatabase();
        //Cursor cursor = db.rawQuery(deleteQuery, null);
        db.execSQL(updateFavQuery);
        db.close();
        System.out.println("############# Update done ####################");

    }

    public void deleteAllItems(){


        String deleteQuery = "DELETE FROM " + TABLE_CLOSET;

        SQLiteDatabase db = this.getWritableDatabase();
        //Cursor cursor = db.rawQuery(deleteQuery, null);
        db.execSQL(deleteQuery);
        db.close();
        System.out.println("############# Delete done ####################");

    }
}