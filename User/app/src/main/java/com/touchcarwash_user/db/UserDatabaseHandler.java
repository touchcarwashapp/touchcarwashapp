package com.touchcarwash_user.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class UserDatabaseHandler extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "touchudb_user";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_name1 = "user";
    private static final String TABLE_name2 = "fcmid";
    private static final String TABLE_name4 = "scwidth";
    private static final String TABLE_name5 = "currentlocation";

    private static final String screenwidth = "screenwidth";
    private static final String fcmid = "fcmid";
    private static final String pkey = "pkey";
    private static final String userid = "userid";
    private static final String name = "username";
    private static final String place = "vehicleid";
    private static final String pincode = "vehicleregno";
    private static final String contactone = "vehicleimgisg";
    private static final String contacttwo = "userimgisg";
    private static final String imgsig= "userimgisg";

  
    private static final String lat="lat";
    private static final String lng="lng";
   

    private static String CREATE_TABLE1 = "CREATE TABLE " + TABLE_name1 + "("+pkey +" INTEGER PRIMARY KEY AUTOINCREMENT,"+userid+" TEXT,"+name+" TEXT,"+place+" TEXT,"+pincode+" TEXT,"+contactone+" TEXT,"+contacttwo+" TEXT,"+imgsig+" TEXT"+")";
    private static String CREATE_TABLE2= "CREATE TABLE " + TABLE_name2 + "("+pkey +" INTEGER PRIMARY KEY AUTOINCREMENT,"+fcmid+" TEXT"+")";
    private static String CREATE_TABLE4 = "CREATE TABLE " + TABLE_name4+ "("+pkey +" INTEGER PRIMARY KEY AUTOINCREMENT,"+screenwidth+" TEXT"+")";
    private static String CREATE_TABLE5= "CREATE TABLE " + TABLE_name5 + "("+pkey +" INTEGER PRIMARY KEY AUTOINCREMENT,"+lat+" TEXT,"+lng+" TEXT"+")";

    public UserDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE1);
        db.execSQL(CREATE_TABLE2);
        db.execSQL(CREATE_TABLE4);
        db.execSQL(CREATE_TABLE5);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_name1);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_name2);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_name4);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_name5);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_name1);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_name2);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_name4);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_name5);
        onCreate(db);
    }

     private static final String ="lat";
    private static final String ="lng";
     
    public void addlocation(String lat1,String lng1) {
        deletelocation();
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(lat, lat1);
         values.put(lng, lng1);
        db.insert(TABLE_name5, null, values);
        db.close();
    }

   public String get_latitude() {
        String link = "";
        SQLiteDatabase sql = getReadableDatabase();
        Cursor c = sql.rawQuery("SELECT  * FROM "+TABLE_name5, null);
        while (c.moveToNext()) {
            link = c.getString(1);
        }
        c.close();
        sql.close();
        return link;
    }

  public String get_longtitude() {
        String link = "";
        SQLiteDatabase sql = getReadableDatabase();
        Cursor c = sql.rawQuery("SELECT  * FROM "+TABLE_name5, null);
        while (c.moveToNext()) {
            link = c.getString(2);
        }
        c.close();
        sql.close();
        return link;
    }

    public void deletelocation() {
        getWritableDatabase().execSQL("delete from "+TABLE_name5);
    }

 

   /*User DEtails DB */
 
 public String get_imgsig() {
        String link = "";
        SQLiteDatabase sql = getReadableDatabase();
        Cursor c = sql.rawQuery("SELECT  * FROM "+TABLE_name1, null);
        while (c.moveToNext()) {
            link = c.getString(7);
        }
        c.close();
        sql.close();
        return link;
    }
    public String get_contacttwo() {
        String link = "";
        SQLiteDatabase sql = getReadableDatabase();
        Cursor c = sql.rawQuery("SELECT  * FROM "+TABLE_name1, null);
        while (c.moveToNext()) {
            link = c.getString(6);
        }
        c.close();
        sql.close();
        return link;
    }

    public String get_contactone() {
        String link = "";
        SQLiteDatabase sql = getReadableDatabase();
        Cursor c = sql.rawQuery("SELECT  * FROM "+TABLE_name1, null);
        while (c.moveToNext()) {
            link = c.getString(5);
        }
        c.close();
        sql.close();
        return link;
    }

    public String get_pincode() {
        String link = "";
        SQLiteDatabase sql = getReadableDatabase();
        Cursor c = sql.rawQuery("SELECT  * FROM "+TABLE_name1, null);
        while (c.moveToNext()) {
            link = c.getString(4);
        }
        c.close();
        sql.close();
        return link;
    }

    public String get_place() {
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

    public String get_name() {
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

    public String get_userid() {
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


    public void update_userdetails(String name1,String place1,String pincode1,String contactone1,String contacttwo1,String imgsig1) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(userid, userid1);
        values.put(name,name1);
        values.put(place,place1);
        values.put(pincode,pincode1);
        values.put(contactone,contactone1);
        values.put(contacttwo,contacttwo1);
        values.put(imgsig,imgsig1);
        db.update(TABLE_name1, values, null, null);
        db.close();
    }

    public void user_imgsigupdate(String userimgisg1) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(imgsig,imgsig1);
        db.update(TABLE_name1, values, null,null);
        db.close();
    }

    public void adduser(String userid1,String name1,String place1,String pincode1,String contactone1,String contacttwo1,String imgsig1) {
        deleteuser();
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(userid, userid1);
        values.put(name,name1);
        values.put(place,place1);
        values.put(pincode,pincode1);
        values.put(contactone,contactone1);
        values.put(contacttwo,contacttwo1);
        values.put(imgsig,imgsig1);
        db.insert(TABLE_name1, null, values);
        db.close();
    }
    public void deleteuser() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from "+TABLE_name1);
        db.close();
    }

    /*Screen Width DB */

    public void addscreenwidth(String screenwidth1) {
        deletescreenwidth();
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(screenwidth, screenwidth1);
        db.insert(TABLE_name4, null, values);
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

    /* FCM Details DB */

    public void addfcmid(String fcmid1) {
        deletefcmid();
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(fcmid, fcmid1);
        db.insert(TABLE_name2, null, values);
        db.close();
    }

    public String getfcmid() {
        String link = "";
        Cursor c = getReadableDatabase().rawQuery("SELECT  * FROM "+TABLE_name2, null);
        while (c.moveToNext()) {
            link = c.getString(1);
        }
        c.close();
        return link;
    }

    public void deletefcmid() {
        getWritableDatabase().execSQL("delete from "+TABLE_name2);
    }


}
