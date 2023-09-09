package com.example.groupassignment;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SQLiteAdapter {

    //SQL Commands
    private static final String MYDATABASE_NAME = "RESRAURANT_DATABASE";
    private static final String DATABASE_TABLE = "RESTAURANT_TABLE"; //table

    //column
    private static final String NAME = "Name";
    private static final String PLACE_ID = "PlaceID";
    private static final String PHOTO_REFERENCE = "PhotoReference";
    private static final String OPENING_NOW = "OpeningNow";
    private static final String ADDED_WISHLIST = "AddedWishlist";
    private static final String RATING = "Rating";
    private static final String DISTANCE = "Distance";
    private static final String PRICE_LEVEL = "PriceLevel";
    private static final String USER_RATING_TOTAL = "UserRatingTotal";
    private static final int MYDATABASE_VERSION = 1; //version



    //sql command to create the table with the column
    //ID, Content, Ingredients, Price
    private static final String SCRIPT_CREATE_DATABASE =
            "create table "+DATABASE_TABLE+
                    " (id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                    PLACE_ID+" text not null, " +
                    NAME + " text, "+
                    PHOTO_REFERENCE + " text, "+
                    OPENING_NOW + " int, "+
                    RATING + " double, "+
                    DISTANCE + " double, "+
                    PRICE_LEVEL + " int, "+
                    USER_RATING_TOTAL + " int);";

    //variables
    private Context context;
    private SQLiteHelper sqLiteHelper;
    private SQLiteDatabase sqLiteDatabase;

    ///constructor
    public SQLiteAdapter(Context c) {
        context = c;
    }

    //open the database to insert data/to write data
    public SQLiteAdapter openToWrite() throws android.database.SQLException{

        //create a table with MYDATABASE_NAME and
        //the version of MYDATABASE_VERSION
        sqLiteHelper = new SQLiteHelper(context, MYDATABASE_NAME, null, MYDATABASE_VERSION);

        //open to write
        sqLiteDatabase = sqLiteHelper.getWritableDatabase();

        return this;
    }

    //open the database to read
    public SQLiteAdapter openToRead() throws android.database.SQLException {

        sqLiteHelper = new SQLiteHelper(context, MYDATABASE_NAME, null, MYDATABASE_VERSION);

        //open to read
        sqLiteDatabase = sqLiteHelper.getReadableDatabase();

        return this;
    }


    public long insert(String placeID, String name, String photoReference, boolean openingNow, double rating, double distance, int priceLevel, int userRatingTotal){

        ContentValues contentValues = new ContentValues();

        contentValues.put(PLACE_ID,placeID);
        contentValues.put(NAME,name);
        contentValues.put(PHOTO_REFERENCE,photoReference);
        contentValues.put(OPENING_NOW,openingNow);
        contentValues.put(RATING, rating);
        contentValues.put(DISTANCE, distance);
        contentValues.put(PRICE_LEVEL, priceLevel);
        contentValues.put(USER_RATING_TOTAL, userRatingTotal);

        return sqLiteDatabase.insert(DATABASE_TABLE,null,contentValues);

    }

    public String getString(String column, int position){
        String[] columns = new String[]{column};
        Cursor cursor = sqLiteDatabase.query(DATABASE_TABLE, columns, null,null,null,null,null);

        int index_CONTENT = cursor.getColumnIndex(column);
        cursor.moveToPosition(position);
        return cursor.getString(index_CONTENT);
    }

    public boolean getBoolean(String column, int position){
        String[] columns = new String[]{column};
        Cursor cursor = sqLiteDatabase.query(DATABASE_TABLE, columns, null,null,null,null,null);

        int index_CONTENT = cursor.getColumnIndex(column);
        cursor.moveToPosition(position);

        // Retrieve the integer value from the database
        int intValue = cursor.getInt(index_CONTENT);

        // Convert the integer to a boolean
        return intValue == 1;
    }

    public int getInteger(String column, int position){
        String[] columns = new String[]{column};
        Cursor cursor = sqLiteDatabase.query(DATABASE_TABLE, columns, null,null,null,null,null);

        int index_CONTENT = cursor.getColumnIndex(column);
        cursor.moveToPosition(position);
        return cursor.getInt(index_CONTENT);
    }

    public double getDouble(String column, int position){
        String[] columns = new String[]{column};
        Cursor cursor = sqLiteDatabase.query(DATABASE_TABLE, columns, null,null,null,null,null);

        int index_CONTENT = cursor.getColumnIndex(column);
        cursor.moveToPosition(position);

        return cursor.getDouble(index_CONTENT);
    }

    public int getCount() {
        int count = 0;
        String[] columns = new String[] {PLACE_ID};
        Cursor cursor = sqLiteDatabase.query(DATABASE_TABLE, columns, null,null,null,null,null);
        for(cursor.moveToFirst(); !(cursor.isAfterLast()); cursor.moveToNext()){
            count++;
        }

        return count;
    }

    public int deleteRow(String placeID) {
        // Define the WHERE clause based on the row's ID
        String whereClause = "PlaceID = ?";
        String[] whereArgs = new String[]{placeID};

        // Perform the delete operation
        return sqLiteDatabase.delete(DATABASE_TABLE, whereClause, whereArgs);
    }




    //close the database
    public void close(){
        sqLiteHelper.close();
    }

    //delete all the content in the table/ delete the table
    public int deleteAll(){
        return sqLiteDatabase.delete(DATABASE_TABLE,null,null);
    }


    //superclass of SQLiteOpenHelper --> implement both the
    //override methods which creates the database
    public class SQLiteHelper extends SQLiteOpenHelper {

        //Constructor with 4 parameters
        public SQLiteHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        //to create the database
        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(SCRIPT_CREATE_DATABASE);
        }

        //version control
        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL(SCRIPT_CREATE_DATABASE);
        }
    }

}
