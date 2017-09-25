
package com.example.android.pets;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.data.PetsContract.PetsEntry;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = -1;
    //PetsDbHelper mDbHelper;

    private static int SINGLE_PET_LOADER = 1;

    public Uri currentPetUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        currentPetUri = getIntent().getData(); // for etting the data ie URI passed from the previous activty.
        if( currentPetUri == null){ // Meaning this activty is called by clicking Floating Action Button
            setTitle("Add a Pet");
        }
        else {                     // Meanng this activity is called by clicking on a particular pet in the listview.
            setTitle("Edit Pet"); // We now set the title of the activity dynamically and not from the manifest file.
            getSupportLoaderManager().initLoader(SINGLE_PET_LOADER,null,this);
        }


        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        //createFromResource(Context context, int textArrayResId, int textViewResId) Note::: It is mostly used for Spinners.
        //Creates a new ArrayAdapter from external resources.

        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);


        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetsEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetsEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetsEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    public void savePet(){

        EditText mPetName = (EditText) findViewById(R.id.edit_pet_name);
        String  Pet_Name = mPetName.getText().toString().trim();

        EditText mBreedName = (EditText) findViewById(R.id.edit_pet_breed);
        String  Pet_Breed = mBreedName.getText().toString().trim();

        EditText mPetWeight = (EditText) findViewById(R.id.edit_pet_weight);
        int Pet_Weight = Integer.parseInt(mPetWeight.getText().toString().trim());

        Uri newUri = null;
        int updated_rows = 0;

        ContentValues values = new ContentValues();
        values.put(PetsEntry.COLUMN_PET_NAME,Pet_Name);
        values.put(PetsEntry.COLUMN_PET_BREED,Pet_Breed);
        values.put(PetsEntry.COLUMN_PET_GENDER,mGender);
        values.put(PetsEntry.COLUMN_PET_WEIGHT,Pet_Weight);

        if(currentPetUri == null) {
            newUri = getContentResolver().insert(PetsEntry.CONTENT_URI, values);
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
        // for updating the current Pet
        else {
            String selection = PetsEntry._ID + "=?";
            String[] selectionArgs = new String[] { String.valueOf(ContentUris.parseId(currentPetUri)) };
            updated_rows = getContentResolver().update(currentPetUri,values,selection,selectionArgs);
            if ( updated_rows == 0)
                Toast.makeText(this, getString(R.string.pet_update_failure),Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, getString(R.string.pet_update_success),Toast.LENGTH_SHORT).show();
        }


    }

    public void finish(){
        NavUtils.navigateUpFromSameTask(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:

                savePet();
                finish();

                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        //NOTE: @param id , denotes the unique identifier of the Loader defined in onCreate() method of this activity.
        // It does not refers to the id of the item which is clicked. This id has no relation with the item clicked in listView.
        String projection[] = {PetsEntry._ID,PetsEntry.COLUMN_PET_NAME,PetsEntry.COLUMN_PET_BREED,PetsEntry.COLUMN_PET_GENDER,PetsEntry.COLUMN_PET_WEIGHT};

        // This loader will execute the Content Provider's query() method in the background thread.
        return new CursorLoader(this,currentPetUri,projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        data.moveToFirst();  // Moving to the first (and only row in this case) of the cursor.

        String name = data.getString(data.getColumnIndexOrThrow(PetsEntry.COLUMN_PET_NAME));
        mNameEditText.setText(name);

        String Breed = data.getString(data.getColumnIndexOrThrow(PetsEntry.COLUMN_PET_BREED));
        mBreedEditText.setText(Breed);

        int weight = data.getInt(data.getColumnIndexOrThrow(PetsEntry.COLUMN_PET_WEIGHT));
        mWeightEditText.setText(String.valueOf(weight));

        int gender = data.getInt(data.getColumnIndexOrThrow(PetsEntry.COLUMN_PET_GENDER));

        switch(gender){
            case PetsEntry.GENDER_UNKNOWN:
                mGenderSpinner.setSelection(0);  // Here the argument of the setSelection() refers to the position of the value to be selected in the Adapter
                break;                           // used while setting up the spinner.
            case PetsEntry.GENDER_MALE:
                mGenderSpinner.setSelection(1);
                break;
            case PetsEntry.GENDER_FEMALE:
                mGenderSpinner.setSelection(2);
                break;
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("");
        mGenderSpinner.setSelection(PetsEntry.GENDER_UNKNOWN);

    }
}
