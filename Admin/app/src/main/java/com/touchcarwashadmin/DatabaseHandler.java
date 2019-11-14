package com.touchcarwashadmin;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "dbadmintcw";
    private static final int DATABASE_VERSION = 5;

    private static final String TABLE_name2 = "scwidth";
    private static final String TABLE_name1 = "washvehiclelist";
    private static final String TABLE_name3= "vehiclelist";

    private static final String pkey = "pkey";
    private static final String screenwidth = "screenwidth";
    private static final String title = "title";

    private static final String wvsn = "wvsn";
    private static final String wvname = "wvname";


    private static final String vehiclesn = "vehiclesn";
    private static final String vehiclename = "vehiclename";

    private static String CREATE_videoid_TABLE2 = "CREATE TABLE scwidth(pkey INTEGER PRIMARY KEY AUTOINCREMENT,screenwidth TEXT)";
    private static String CREATE_videoid_TABLE1 = "CREATE TABLE "+TABLE_name1+"("+pkey +" INTEGER PRIMARY KEY AUTOINCREMENT,"+wvsn+" TEXT,"+wvname+" TEXT"+")";
    private static String CREATE_videoid_TABLE3 = "CREATE TABLE "+TABLE_name3+"("+pkey +" INTEGER PRIMARY KEY AUTOINCREMENT,"+vehiclesn+" TEXT,"+vehiclename+" TEXT"+")";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_videoid_TABLE2);
        db.execSQL(CREATE_videoid_TABLE1);
        db.execSQL(CREATE_videoid_TABLE3);

    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS scwidth");
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_name1);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_name3);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS scwidth");
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_name1);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_name3);
        onCreate(db);
    }


    public void add_vehicles(String vehiclesn1,String vehiclename1) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(vehiclesn, vehiclesn1);
        values.put(vehiclename,vehiclename1);
        db.insert(TABLE_name3, null, values);
        db.close();
    }

    public ArrayList<String> getvehicles() {
        ArrayList<String> arraylist = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT  * FROM "+TABLE_name3, null);
        while (c.moveToNext()) {
            arraylist.add(c.getString(1));
            arraylist.add(c.getString(2));
        }
        c.close();
        return arraylist;
    }

    public void addvehicles_update(String vehiclesn1, String vehiclename1) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(vehiclename, vehiclename1);
        db.update(TABLE_name3, values, "vehiclesn = ?", new String[]{vehiclesn1});
        db.close();
    }

    public void deletevehicles_byid(String id1) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_name3, "vehiclesn="+id1, null);
    }

    public void deletevehicles() {
        getWritableDatabase().execSQL("delete from "+TABLE_name3);
    }



    public void add_washvehicle(String wvsn1,String wvname1) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(wvsn, wvsn1);
        values.put(wvname,wvname1);
        db.insert(TABLE_name1, null, values);
        db.close();
    }

    public ArrayList<String> getwashvehicle() {
        ArrayList<String> arraylist = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT  * FROM "+TABLE_name1, null);
        while (c.moveToNext()) {
            arraylist.add(c.getString(1));
            arraylist.add(c.getString(2));
        }
        c.close();
        return arraylist;
    }

    public void addwashvehicle_update(String wvsn1, String wvname1) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(wvname, wvname1);
        db.update(TABLE_name1, values, "wvsn = ?", new String[]{wvsn1});
        db.close();
    }

    public void deletewashvehicle_byid(String id1) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_name1, "wvsn="+id1, null);
    }

    public void deletewashvehicle() {
        getWritableDatabase().execSQL("delete from "+TABLE_name1);
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
