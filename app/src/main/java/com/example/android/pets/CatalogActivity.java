package com.example.android.pets;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.pets.data.PetsContract.PetsEntry;


/**
 * Displays list of pets that were entered and stored in the app.
 */
     public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    private static int PET_LOADER = 0; // unique identifier for a particular loader.

    PetCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                startActivity(intent);
            }
        });


        ListView listView = (ListView) findViewById(R.id.petview);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);

        mCursorAdapter  = new PetCursorAdapter(this,null);
        listView.setAdapter(mCursorAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Uri currentPetUri = ContentUris.withAppendedId(PetsEntry.CONTENT_URI, id); // for appending the Pet_ID with the ContentUri.
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                intent.setData(currentPetUri); //  for sending currentPetUri to the editor activity
                startActivity(intent);
            }
        });
        // start the loader
        getSupportLoaderManager().initLoader(PET_LOADER,null,this);



        //displayDatabaseInfo();
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
    /*private void displayDatabaseInfo() {
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.



        String[] projection = {PetsEntry._ID,PetsEntry.COLUMN_PET_NAME, PetsEntry.COLUMN_PET_BREED,PetsEntry.COLUMN_PET_GENDER,
        PetsEntry.COLUMN_PET_WEIGHT}; // specifying the columns
        // String selection = PetsEntry.COLUMN_PET_WEIGHT + ">?"; // specifying the where clause...no need for 'where' keyword
        // String[] args = new String[]{String.valueOf(4)}; // specifying the arguments in the selection ie where clause.
        Cursor cursor = getContentResolver().query(PetsEntry.CONTENT_URI,projection,null,null,null,null);

            ListView listView = (ListView) findViewById(R.id.petview);

           // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
            View emptyView = findViewById(R.id.empty_view);
            listView.setEmptyView(emptyView);

            PetCursorAdapter  cursorAdapter = new PetCursorAdapter(this,cursor);

            listView.setAdapter(cursorAdapter);


            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.


    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

   public void insertPet(){


        ContentValues values = new ContentValues();
        values.put(PetsEntry.COLUMN_PET_NAME,"Toto");
        values.put(PetsEntry.COLUMN_PET_BREED,"Terrier");
        values.put(PetsEntry.COLUMN_PET_GENDER,PetsEntry.GENDER_MALE);
        values.put(PetsEntry.COLUMN_PET_WEIGHT,7);

        Uri newUri = getContentResolver().insert(PetsEntry.CONTENT_URI,values);


       if (newUri == null) {
           // If the new content URI is null, then there was an error with insertion.
           Toast.makeText(this, getString(R.string.editor_insert_pet_failed),
                   Toast.LENGTH_SHORT).show();
       } else {
           // Otherwise, the insertion was successful and we can display a toast.
           Toast.makeText(this, getString(R.string.editor_insert_pet_successful),
                   Toast.LENGTH_SHORT).show();
       }





    }

    /*@Override
    protected void onStart() {
        super.onStart();
       // displayDatabaseInfo();
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:

                insertPet();
              // displayDatabaseInfo();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // Do nothing for now
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // In the onCreate() method, when the getLoaderManage().initLoader() is called, then onCreateLoader() is called on background thread to query
    // database and load the cursor with the latest data.
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //NOTE: @param id , denotes the unique identifier of the Loader defined in onCreate() method of this activity.
        // It does not refers to the id of the item which is clicked. This id has no relation with the item clicked in listView.
        String projection[] = {PetsEntry._ID,PetsEntry.COLUMN_PET_NAME,PetsEntry.COLUMN_PET_BREED};

        // This loader will execute the Content Provider's query() method in the background thread.
        return new CursorLoader(this,PetsEntry.CONTENT_URI,projection,null,null,null);
    }

    // After onCreate() method, the onLoadFinished() method is called, which is used to provide the CursorAdapter with the latest data fetched
    // by swapping the current cursor with new Cursor ie data as defined.
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    mCursorAdapter.swapCursor(data);
    }

    // After the onLoadFinished(), the loader is reset by calling onLoaderReset() and the current cursor of the cursorAdapter is again swapped
    // so that it does not references to any old data and this takes place after the entire loading operation for a particular set of rows
    // from the database is done and after data is entirely placed on the listview.
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}