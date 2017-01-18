package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.pets.data.PetsContract.PetsEntry;

/**
 * Created by Jalaj on 12/28/2016.
 */

public class PetsDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "Shelter.db";

    public PetsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_PETS_TABLE = "CREATE TABLE " + PetsEntry.TABLE_NAME +
                "(" + PetsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT , " + PetsEntry.COLUMN_PET_NAME + "  TEXT NOT NULL, " +
                PetsEntry.COLUMN_PET_BREED + " TEXT , " + PetsEntry.COLUMN_PET_GENDER + " INTEGER NOT NULL ," +
                PetsEntry.COLUMN_PET_WEIGHT + " INTEGER NOT NULL DEFAULT 0) ";
        Log.d("PetsDbHelper.java",SQL_CREATE_PETS_TABLE);
        db.execSQL(SQL_CREATE_PETS_TABLE);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        final String SQL_DELETE_PETS_TABLE = "DROP TABLE IF EXISTS " + PetsEntry.TABLE_NAME;

        db.execSQL(SQL_DELETE_PETS_TABLE);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
