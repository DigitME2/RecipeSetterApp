package uk.co.digitme.recipesetter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import static uk.co.digitme.recipesetter.MainActivity.DEFAULT_URL;

public class DbHelper extends SQLiteOpenHelper {

    private static final String TAG = "Database Helper";

    private static final String DATABASE_NAME = "Prod.db";
    private static final int DATABASE_VERSION = 1;

    public static final String SETTINGS_TABLE_NAME = "SETTINGS";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_SERVER_ADDRESS = "SERVER_ADDRESS";

    public static final String PRODUCTION_LINES_TABLE_NAME = "PRODUCTION_LINES";
    public static final String COLUMN_PROD_LINE_NAME = "LINE_NAME";

    public static final String RECIPES_TABLE_NAME = "RECIPES";
    public static final String COLUMN_RECIPE_NAME = "RECIPE_NAME";


    public DbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_SETTINGS_TABLE = "CREATE TABLE IF NOT EXISTS " + SETTINGS_TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_SERVER_ADDRESS + " TEXT NOT NULL); ";

        final String SQL_CREATE_PRODUCTION_LINE_OPTIONS_TABLE = "CREATE TABLE IF NOT EXISTS " + PRODUCTION_LINES_TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PROD_LINE_NAME + " TEXT NOT NULL); ";

        final String SQL_CREATE_RECIPES_OPTIONS_TABLE = "CREATE TABLE IF NOT EXISTS " + RECIPES_TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_RECIPE_NAME + " TEXT NOT NULL); ";

        // Create the Settings table and enter the default values
        db.execSQL(SQL_CREATE_SETTINGS_TABLE);
        ContentValues settingsCV = new ContentValues();
        settingsCV.put(COLUMN_ID, 1);
        settingsCV.put(COLUMN_SERVER_ADDRESS, DEFAULT_URL);
        db.replace("SETTINGS", null, settingsCV);
        Log.d(TAG, "Saving server address: " + DEFAULT_URL);

        // Create the production line table
        db.execSQL(SQL_CREATE_PRODUCTION_LINE_OPTIONS_TABLE);
        // Create the recipe table
        db.execSQL(SQL_CREATE_RECIPES_OPTIONS_TABLE);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        String address = getServerAddress();
    }

    public String getServerAddress(){
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + SETTINGS_TABLE_NAME ,null);
        if(cursor.moveToFirst()) {
            String address;
            try {
                address = cursor.getString(cursor.getColumnIndex(COLUMN_SERVER_ADDRESS));
                cursor.close();
            } catch (CursorIndexOutOfBoundsException e) {
                address = "";
            }
            Log.d(TAG, "Returning server address: " + address);
            return address;
        }
        return "";
    }

    public void saveServerAddress(String address){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ID, 1);
        cv.put(COLUMN_SERVER_ADDRESS, address);

        db.replace(SETTINGS_TABLE_NAME, null ,cv);
        Log.d(TAG, "Saving server address: " + address);
    }

    public ArrayList<String> getRecipeOptions(){
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<String> options = new ArrayList<>();

        final String SQL_GET_OPTIONS = "SELECT * FROM " + RECIPES_TABLE_NAME + ";";
        Cursor cursor = db.rawQuery(SQL_GET_OPTIONS,null);
        while (cursor.moveToNext()){
            String option = cursor.getString(cursor.getColumnIndex(COLUMN_RECIPE_NAME));
            options.add(option);
        }
        cursor.close();
        return options;
    }

    public void saveRecipeOptions(ArrayList<String> options){
        SQLiteDatabase db = getWritableDatabase();

        for (int i = 0; i < options.size(); i++ ) {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_ID, i+1);
            cv.put(COLUMN_RECIPE_NAME, options.get(i));
            db.replace(RECIPES_TABLE_NAME, null ,cv);
            Log.d(TAG, "Adding " + options.get(i) + " to list of recipes");
        }
    }

    public ArrayList<String> getProductionLineOptions(){
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<String> options = new ArrayList<>();


        final String SQL_GET_OPTIONS = "SELECT * FROM " + PRODUCTION_LINES_TABLE_NAME + ";";
        Cursor cursor = db.rawQuery(SQL_GET_OPTIONS,null);
        while (cursor.moveToNext()){
            String option = cursor.getString(cursor.getColumnIndex(COLUMN_PROD_LINE_NAME));
            options.add(option);
        }
        cursor.close();
        return options;
    }

    public void saveProductionLineOptions(ArrayList<String> options){
        SQLiteDatabase db = getWritableDatabase();

        for (int i = 0; i < options.size(); i++ ) {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_ID, i+1);
            cv.put(COLUMN_PROD_LINE_NAME, options.get(i));
            db.replace(PRODUCTION_LINES_TABLE_NAME, null ,cv);
            Log.d(TAG, "Adding " + options.get(i) + " to list of production lines");
        }
    }
}
