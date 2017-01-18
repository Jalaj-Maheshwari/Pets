
package com.example.android.pets.data;

/**
 * Created by Jalaj on 1/2/2017.
 */

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.pets.data.PetsContract.PetsEntry;

import static android.R.attr.id;

/**
 * {@link ContentProvider} for Pets app.
 */
public class PetsProvider extends ContentProvider { // NOTE: Do add a provider in the android Manifest File.

    /** Tag for the log messages */
    public static final String LOG_TAG = PetsProvider.class.getSimpleName();

    private PetsDbHelper mDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */


    /** URI matcher code for the content URI for the pets table */
    private static final int PETS = 100;

    /** URI matcher code for the content URI for a single pet in the pets table */
    private static final int PET_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

     sUriMatcher.addURI(PetsContract.CONTENT_AUTHORITY,PetsContract.PATH_PETS,PETS);         // for adding URI Matcher for entire table
        sUriMatcher.addURI(PetsContract.CONTENT_AUTHORITY,PetsContract.PATH_PETS + "/#",PET_ID);   // for adding URI Matcher for a particular entry in table
         }
    @Override
    public boolean onCreate() {
        mDbHelper = new PetsDbHelper(getContext()); // Here we are using getcontext() instead of 'this' as class Content provider
        // does not extends class Context. So we are unable to get the context inherited, which is by default inherited when we extend
        // AppCompatActivity.

        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        mDbHelper = new PetsDbHelper(getContext());
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor = null;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                // For the PETS code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.

                cursor = database.query(PetsEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = PetsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(PetsEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);



                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        // Specifying which URI to keep looking for any change to happen, so that the new changes are reflected automatically.
        cursor.setNotificationUri(getContext().getContentResolver(),uri);

        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Inserts a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertPet(Uri uri, ContentValues values) {


        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String name = values.getAsString(PetsEntry.COLUMN_PET_NAME);

        int gender  = values.getAsInteger(PetsEntry.COLUMN_PET_GENDER);
        Log.v("PetsProvider",String.valueOf(gender));

        int weight = values.getAsInteger(PetsEntry.COLUMN_PET_WEIGHT);

        // Sanity Checking
        if (name == null){
            throw new IllegalArgumentException("Pet requires a name");
        }
        else if ( gender != PetsEntry.GENDER_UNKNOWN && gender != PetsEntry.GENDER_FEMALE && gender!= PetsEntry.GENDER_MALE)
             {
            throw new IllegalArgumentException("Pet requires a gender");
        }
        else if (weight <= 0){
                throw new IllegalArgumentException("Pet requires a weight");
            }
        else {

            // Sanity Checking done, insertion to take place
            long id = db.insert(PetsEntry.TABLE_NAME, null, values);
            if (id == -1) {
                Log.e(LOG_TAG, "Failed to insert row for " + uri);
                return null;
            }
            else
                // Notifying any insertion for this URI and reflecting the new changes automatically.
                getContext().getContentResolver().notifyChange(uri,null);


        }


        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }
    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override

    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return updatePet(uri, contentValues, selection, selectionArgs);

            case PET_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = PetsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }

    }

    /**
     * Update pets in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number of rows that were successfully updated.
     */
    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        int rowsUpdated = 0;
        int flag = 0;
       SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Sanity Checking
        if (values.containsKey(PetsEntry.COLUMN_PET_NAME)) {
            // containsKey() is used to check whether  a field ie name,weight or gender
            // which is to be sanity checked, is actually intended to be updated by user..
            // This is done by checking the object of ContentValues passed in this update method by editor activity.
            String name = values.getAsString(PetsEntry.COLUMN_PET_NAME);
            if (name == null)
                throw new IllegalArgumentException("Pet requires a name");
                flag = 1;
        }

        if (values.containsKey(PetsEntry.COLUMN_PET_GENDER)){
            int gender  = values.getAsInteger(PetsEntry.COLUMN_PET_GENDER);
            if(gender != PetsEntry.GENDER_UNKNOWN && gender != PetsEntry.GENDER_FEMALE && gender!= PetsEntry.GENDER_MALE)
            throw new IllegalArgumentException("Pet requires a gender");
            flag = 1;
        }

        if (values.containsKey(PetsEntry.COLUMN_PET_WEIGHT)) {
            int weight = values.getAsInteger(PetsEntry.COLUMN_PET_WEIGHT);
            if (weight <= 0)
                throw new IllegalArgumentException("Pet requires a weight");
                flag = 1;
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        if (flag != 1){
         // Sanity Checking done, updation to take place
             rowsUpdated = database.update(PetsEntry.TABLE_NAME,values,selection,selectionArgs);
                   if (id == -1) {
                    Log.e(LOG_TAG, "Failed to update row for " + uri);
                    return 0;
            }
        }
        // Notifying any updation for this URI and reflecting the new changes automatically.
        if(rowsUpdated != 0)
            getContext().getContentResolver().notifyChange(uri,null);

        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);

        int rowsDeleted = 0;
        switch (match) {
            case PETS:
                // Delete all rows that match the selection and selection args
                rowsDeleted =  database.delete(PetsEntry.TABLE_NAME, selection, selectionArgs);
                break;

                // NOTE: Here as there is no need for sanity checking
            // in deletion, so no need to call an additional deletePet() method, we perform deletion directly.
            case PET_ID:
                // Delete a single row given by the ID in the URI
                selection = PetsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(PetsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);

        }
        //Notifying any deletion for this URI and reflecting the new changes automatically.
        if (rowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri,null);


        return rowsDeleted;


    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetsEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}

