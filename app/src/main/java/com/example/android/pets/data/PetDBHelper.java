package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PetDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "shelter.db";
    private static final int DATABASE_VERSION = 2;

    private static final String CREATE_PETS_TABLE = "CREATE TABLE " + PetsContract.PetsFeedEntry.TABLE_NAME + " ( "
            + PetsContract.PetsFeedEntry._ID + " INTEGER PRIMARY KEY, "
            + PetsContract.PetsFeedEntry.COLUMN_PET_NAME + " TEXT NOT NULL , "
            + PetsContract.PetsFeedEntry.COLUMN_PET_BREED + " TEXT NOT NULL , "
            + PetsContract.PetsFeedEntry.COLUMN_PET_GENDER + " INT NOT NULL , "
            + PetsContract.PetsFeedEntry.COLUMN_PET_WEIGHT + " INT NOT NULL "
            + ");";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + PetsContract.PetsFeedEntry.TABLE_NAME;


    public PetDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_PETS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }

    public String getTableCreatingString() {
        return CREATE_PETS_TABLE;
    }

    public void deleteDatabase(SQLiteDatabase db) {
        db.execSQL(SQL_DELETE_ENTRIES);
    }
}
