package com.touchcarwash_driver.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDatabaseHandler extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "touchudb_driver";
    private static final int DATABASE_VERSION = 12;
    private static final String TABLE_name1 = "user";
    private static final String TABLE_name2 = "fcmid";
    private static final String TABLE_name3 = "onlinestatus";
    private static final String TABLE_name4 = "scwidth";

    private static final String screenwidth = "screenwidth";
    private static final String fcmid = "fcmid";
    private static final String pkey = "pkey";
    private static final String userid = "userid";
    private static final String username = "username";
    private static final String vehicleid = "vehicleid";
    private static final String vehicleregno = "vehicleregno";
    private static final String vehicleimgisg = "vehicleimgisg";
    private static final String userimgisg = "userimgisg";

    private static final String workaddress = "address";
    private static final String worklocation = "location";
    private static final String radiouskm = "radiouskm";

    private static final String isonline= "isonline";


    private static String CREATE_TABLE1 = "CREATE TABLE " + TABLE_name1 + "("+pkey +" INTEGER PRIMARY KEY AUTOINCREMENT,"+userid+" TEXT,"+username+" TEXT,"+vehicleid+" TEXT,"+vehicleregno+" TEXT,"+vehicleimgisg+" TEXT,"+userimgisg+" TEXT,"+workaddress+" TEXT,"+worklocation+" TEXT,"+radiouskm+" TEXT"+")";
    private static String CREATE_TABLE2= "CREATE TABLE " + TABLE_name2 + "("+pkey +" INTEGER PRIMARY KEY AUTOINCREMENT,"+fcmid+" TEXT"+")";
    private static String CREATE_TABLE3 = "CREATE TABLE " + TABLE_name3+ "("+pkey +" INTEGER PRIMARY KEY AUTOINCREMENT,"+isonline+" TEXT"+")";
    private static String CREATE_TABLE4 = "CREATE TABLE " + TABLE_name4+ "("+pkey +" INTEGER PRIMARY KEY AUTOINCREMENT,"+screenwidth+" TEXT"+")";
    public UserDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE1);
        db.execSQL(CREATE_TABLE2);
        db.execSQL(CREATE_TABLE3);
        db.execSQL(CREATE_TABLE4);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_name1);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_name2);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_name3);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_name4);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_name1);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_name2);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_name3);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_name4);
        onCreate(db);
    }

   /*User DEtails DB */

    public String get_radiouskm() {
        String link = "";
        SQLiteDatabase sql = getReadableDatabase();
        Cursor c = sql.rawQuery("SELECT  * FROM "+TABLE_name1, null);
        while (c.moveToNext()) {
            link = c.getString(9);
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
            link = c.getString(8);
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
            link = c.getString(7);
        }
        c.close();
        sql.close();
        return link;
    }
    public void user_updateradius(String radiouskm1) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(radiouskm, radiouskm1);
        db.update(TABLE_name1, values, null, null);
        db.close();
    }

    public void user_updateaddress(String workaddress1,String worklocation1) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(workaddress,workaddress1);
        values.put(worklocation, worklocation1);
        db.update(TABLE_name1, values, null, null);
        db.close();
    }


    public String get_userimgsig() {
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

    public String get_vehicleimgisg() {
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

    public String get_vehicleregno() {
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

    public String get_vehicleid() {
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

    public String get_username() {
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


    public void user_vehiceupdate(String vehicleid1,String vehicleregno1,String vehicleimgsig1) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(vehicleregno,vehicleregno1);
        values.put(vehicleimgisg,vehicleimgsig1);
        db.update(TABLE_name1, values, "vehicleid = ?", new String[]{vehicleid1});
        db.close();
    }

    public void user_imgsigupdate(String userimgisg1) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(userimgisg,userimgisg1);
        db.update(TABLE_name1, values, null,null);
        db.close();
    }

    public void adduser(String userid1,String username1,String vehicleid1,String vehicleregno1,String vehicleimgisg1,String userimgsig1,String workaddress1,String worklocation1,String radiouskm1) {
        deleteuser();
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(userid, userid1);
        values.put(username,username1);
        values.put(vehicleid,vehicleid1);
        values.put(vehicleregno,vehicleregno1);
        values.put(vehicleimgisg,vehicleimgisg1);
        values.put(userimgisg,userimgsig1);
        values.put(workaddress,workaddress1);
        values.put(worklocation,worklocation1);
        values.put(radiouskm,radiouskm1);
        db.insert(TABLE_name1, null, values);
        db.close();
    }
    public void deleteuser() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from "+TABLE_name1);
        db.close();
    }


    /* Online status DB */

    public void update_isonline(String isonline1) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(isonline, isonline1);
        db.update(TABLE_name3, values, null, null);
        db.close();
    }

    public String get_isonline() {
        String link = "";
        SQLiteDatabase sql = getReadableDatabase();
        Cursor c = sql.rawQuery("SELECT  * FROM "+TABLE_name3, null);
        while (c.moveToNext()) {
            link = c.getString(1);
        }
        c.close();
        sql.close();
        return link;
    }

    public void addonlinestatus(String isonline1) {
        deleteonlinestatus();
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(isonline,isonline1);
        db.insert(TABLE_name3, null, values);
        db.close();
    }

    public String getonlinestatus() {
        String link = "";
        Cursor c = getReadableDatabase().rawQuery("SELECT  * FROM "+TABLE_name3, null);
        while (c.moveToNext()) {
            link = c.getString(1);
        }
        c.close();
        return link;
    }

    public void deleteonlinestatus() {
        getWritableDatabase().execSQL("delete from "+TABLE_name3);
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
