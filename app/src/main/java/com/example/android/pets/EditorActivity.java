/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.data.PetsContract;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * EditText field to enter the pet's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the pet's breed
     */
    private EditText mBreedEditText;

    /**
     * EditText field to enter the pet's weight
     */
    private EditText mWeightEditText;

    /**
     * EditText field to enter the pet's gender
     */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;

    // private Loader _ID
    private static final int PET_LOADER_ID = 1;

    // content URI of the selected pet from the {@link CatalogActivity}
    Uri mCurrentPetUri = null;

    // boolean value which changes to true when changes were made on the
    // pet selected
    private boolean mPetHasChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new pet or editing an existing one
        Intent intent = getIntent();
        mCurrentPetUri = intent.getData();

        // if the intent DOES NOT contain a pet content URI, then we know that we're
        // creating a new pet
        if (mCurrentPetUri == null) {
            // This is a new pet, so change the App bar to say "Add a Pet"
            setTitle("Add a Pet");

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();

        } else {
            // otherwise this is an existing pet, so change the app bar to say "Edit Pet"
            setTitle(getString(R.string.editor_activity_title_edit_pet));
            // initializing the loader for calling data on the passed URI
            getLoaderManager().initLoader(PET_LOADER_ID, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        // setting onTouchListeners to all edit fields
        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
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
                        mGender = 1; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = 2; // Female
                    } else {
                        mGender = 0; // Unknown
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

    /**
     * This function adds the data to the database when the user chooses
     * to save the data
     */
    public void savePetData() {

        // checks if any of the fields are not filled before saving
        // to avoid crash
        if (TextUtils.isEmpty(mNameEditText.getText()) || TextUtils.isEmpty(mBreedEditText.getText())) {
            // don't save the data
            return;
        }

        // get the pet name from the correspondant edittextview
        String petName = mNameEditText.getText().toString();

        // get the pet breed from the correspondant edittextview
        String breedName = mBreedEditText.getText().toString();

        // initializing petWeight value
        int petWeight = 0;

        // check if the user has entered valid value in the mWeightEditText field
        if (TextUtils.isEmpty(mWeightEditText.getText())) {
            // check if its a valid number
            try {
                // if yes, save the value
                petWeight = Integer.parseInt(mWeightEditText.getText().toString());
            } catch (Exception e) {
                // else, save the old  value
            }
        }


        // creates new content value object to enter the data to the database
        ContentValues contentValues = new ContentValues();
        // puts all the data required to the corresponding columns
        contentValues.put(PetsContract.PetsFeedEntry.COLUMN_PET_NAME, petName);
        contentValues.put(PetsContract.PetsFeedEntry.COLUMN_PET_BREED, breedName);
        contentValues.put(PetsContract.PetsFeedEntry.COLUMN_PET_GENDER, mGender);
        contentValues.put(PetsContract.PetsFeedEntry.COLUMN_PET_WEIGHT, petWeight);

        // check if the user is inserting a new pet by checking
        // the @mCurrentPetUri is null
        if (mCurrentPetUri == null) {
            try {

                // getting content resolver for the content provider to insert data to database
                getContentResolver().insert(PetsContract.PetsFeedEntry.CONTENT_URI, contentValues);

                NavUtils.navigateUpFromSameTask(this);

            } catch (Exception e) {
                e.printStackTrace();

            }
        } else {
            // Otherwise this is an EXISTING pet, so update the pet with content URI: mCurrentPetUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentPetUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentPetUri, contentValues, null, null);

            NavUtils.navigateUpFromSameTask(this);
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentPetUri == null) {

            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save the pet data
                savePetData();
                return true;
            // Respond to a click on the "Delete" menu option
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mPetHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // creating the loader to load the data of the single pet was clicked
    // from the {@link CatalogActivity}
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        // projection string array describes the columns which should be
        // fetched from the query
        String[] projection = {PetsContract.PetsFeedEntry._ID, PetsContract.PetsFeedEntry.COLUMN_PET_NAME
                , PetsContract.PetsFeedEntry.COLUMN_PET_BREED, PetsContract.PetsFeedEntry.COLUMN_PET_GENDER
                , PetsContract.PetsFeedEntry.COLUMN_PET_WEIGHT};

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed
        return new CursorLoader(this, mCurrentPetUri, projection,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        // move to the cursor pointer to the first result
        cursor.moveToFirst();

        // getting data from the cursor
        String petName = cursor.getString(cursor.getColumnIndexOrThrow(PetsContract.PetsFeedEntry.COLUMN_PET_NAME));
        String petBreed = cursor.getString(cursor.getColumnIndexOrThrow(PetsContract.PetsFeedEntry.COLUMN_PET_BREED));
        int petGender = cursor.getInt(cursor.getColumnIndexOrThrow(PetsContract.PetsFeedEntry.COLUMN_PET_GENDER));
        int petWeight = cursor.getInt(cursor.getColumnIndexOrThrow(PetsContract.PetsFeedEntry.COLUMN_PET_WEIGHT));

        // filling the input views with data from the cursor
        mNameEditText.setText(petName);
        mBreedEditText.setText(petBreed);
        mGenderSpinner.setSelection(petGender);
        mWeightEditText.setText(String.valueOf(petWeight));

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        // clear all fields if this happens
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mGenderSpinner.setSelection(0);
        mWeightEditText.setText("");
    }

    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
    // the view, and we change the mPetHasChanged boolean to true.

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPetHasChanged = true;
            return false;
        }
    };

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mPetHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showDeleteConfirmationDialog() {

        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deletePet() {

        // Only perform the delete if this is an existing pet.
        if (mCurrentPetUri != null) {

            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentPetUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();

    }
}