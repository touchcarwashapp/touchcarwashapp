package com.touchcarwash_driver;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "touchdriverdb";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_name2 = "scwidth";
    private static final String TABLE_name1 = "address";

    private static final String pkey = "pkey";
    private static final String screenwidth = "screenwidth";

    private static final String address = "address";
    private static final String location = "location";
    private static final String radiouskm = "radiouskm";


    private static String CREATE_TABLE2 = "CREATE TABLE scwidth(pkey INTEGER PRIMARY KEY AUTOINCREMENT,screenwidth TEXT)";
    private static String CREATE_TABLE1 = "CREATE TABLE " + TABLE_name1+ "("+pkey +" INTEGER PRIMARY KEY AUTOINCREMENT,"+address+" TEXT,"+location+" TEXT,"+radiouskm+" TEXT"+")";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_TABLE2);
        db.execSQL(CREATE_TABLE1);


    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS scwidth");
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_name1);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS scwidth");
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_name1);
        onCreate(db);
    }

    public String get_radiouskm() {
        String link = "";
        SQLiteDatabase sql = getReadableDatabase();
        Cursor c = sql.rawQuery("SELECT  * FROM "+TABLE_name1, null);
        while (c.moveToNext()) {
            link = c.getString(3);
        }
        c.close();
        sql.close();
        return link;
    }

    public String get_location() {
        String link = "";
        SQLiteDatabase sql = getReadableDatabase();
        Cursor c = sql.rawQuery("SELECT  * FROM "+TABLE_name1, null);
        while (c.moveToNext()) {
            link = c.getString(2);
        }
        c.close();
        sql.close();
        return link;
    }
    public String get_address() {
        String link = "";
        SQLiteDatabase sql = getReadableDatabase();
        Cursor c = sql.rawQuery("SELECT  * FROM "+TABLE_name1, null);
        while (c.moveToNext()) {
            link = c.getString(1);
        }
        c.close();
        sql.close();
        return link;
    }


    public void addaddress(String address1,String location1,String radiouskm1) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(address, address1);
        values.put(location, location1);
        values.put(radiouskm, radiouskm1);
        db.insert(TABLE_name1, null, values);
        db.close();
    }

    public void addscreenwidth(String screenwidth1) {
        deletescreenwidth();
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(screenwidth, screenwidth1);
        db.insert(TABLE_name2, null, values);
        db.close();
    }

    public String getscreenwidth() {
        String link = "";
        Cursor c = getReadableDatabase().rawQuery("SELECT  * FROM scwidth", null);
        while (c.moveToNext()) {
            link = c.getString(1);
        }
        c.close();
        return link;
    }

    public void deletescreenwidth() {
        getWritableDatabase().execSQL("delete from scwidth");
    }
}
