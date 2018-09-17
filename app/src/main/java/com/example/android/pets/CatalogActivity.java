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

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.pets.data.PetDBHelper;
import com.example.android.pets.data.PetsContract;

import java.util.ArrayList;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // getting an instance of petDBHelper
    private PetDBHelper petDBHelper;

    // Declaring the ListView to list the pets in shelter database
    private ListView petListView;

    // Declaring the cursor adapter to list pet data to list view
    private PetCursorAdapter petCursorAdapter;

    // declaring and initializing cursorLoader _ID
    private static final int CURSOR_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // initializing the petDBHelper through the constructor method
        petDBHelper = new PetDBHelper(this);

        // initializing the petListView
        petListView = (ListView) findViewById(R.id.pet_list_view);

        // setting up an onClickListener on the listView
        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                // creates an intent to go to {@link EditorActivity}
                Intent EditorActivityIntent = new Intent(CatalogActivity.this, EditorActivity.class);

                // form the CONTENT_URI that represents the specific pet that was clicked on,
                // by appending the "id" ( passed as input to this method ) onto the
                // {@link PetsFeedEntry#CONTENT_URI}
                Uri contentUri = ContentUris.withAppendedId(PetsContract.PetsFeedEntry.CONTENT_URI, id);

                // set the URI on the data field of the intent
                EditorActivityIntent.setData(contentUri);

                // Launch the {@link EditorActivity} to display the data for the current pet
                startActivity(EditorActivityIntent);
            }
        });

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // initializing the petCursorAdapter to null
        petCursorAdapter = new PetCursorAdapter(this, null);

        // setting petCursorAdapter to the petListView
        petListView.setAdapter(petCursorAdapter);

        // calling the loader
        getLoaderManager().initLoader(CURSOR_ID, null, this);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                // Do nothing for now
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // delete all pets
                deleteAllPets();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Called when a new Loader needs to be created
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        // projection string array describes the columns which should be
        // fetched from the query
        String[] projection = {PetsContract.PetsFeedEntry._ID, PetsContract.PetsFeedEntry.COLUMN_PET_NAME
                , PetsContract.PetsFeedEntry.COLUMN_PET_BREED};
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed
        return new CursorLoader(this, PetsContract.PetsFeedEntry.CONTENT_URI, projection,
                null, null, null);
    }

    // Called when a previously created loader has finished loading
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Swap the new cursor in. (The framework will take care of closing the
        // old cursor once we return.)
        petCursorAdapter.swapCursor(cursor);
    }

    // Called when a previously created loader is reset, making the data unavailable
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed. We need to make sure we are no
        // longer using it.
        petCursorAdapter.swapCursor(null);
    }

    /**
     * Helper method to delete all pets in the database.
     */
    private void deleteAllPets() {
        int rowsDeleted = getContentResolver().delete(PetsContract.PetsFeedEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from pet database");
    }
}
