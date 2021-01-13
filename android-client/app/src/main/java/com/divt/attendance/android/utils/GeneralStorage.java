package com.divt.attendance.android.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.divt.attendance.android.model.Attendance;
import com.divt.attendance.android.model.Outlet;

public class GeneralStorage {
  private final static String TAG = GeneralStorage.class.getSimpleName();

  private static final String DATABASE_NAME = "storage.db";

  private static final String OUTLET_TABLE = "outlet";
  private static final String ATTENDANCE_TABLE = "attendance";

  private static DatabaseHelper mDBHelper = null;

  public GeneralStorage(Context context) {
    super();
    if (mDBHelper == null) {
      Log.d(TAG, "Create new DB Helper");
      mDBHelper = new DatabaseHelper(context);
    }
  }

  public SQLiteDatabase getDB() {
    SQLiteDatabase dataStorageDB = null;
    try {
      dataStorageDB = mDBHelper.getWritableDatabase();
    } catch (SQLiteException sqle) {
      sqle.printStackTrace();
      Log.e(TAG, "FATAL ERROR IN DB, SQLE: " + sqle.toString());
      System.exit(0);
      return null;
    } catch (Exception e) {
      e.printStackTrace();
      Log.e(TAG, "FATAL ERROR IN DB: " + e.toString());
      System.exit(0);
      return null;
    }
    return dataStorageDB;
  }

  public boolean isOutletExists(Outlet outlet) {
    final SQLiteDatabase db = getDB();
    if (db == null) return false;

    Cursor cursor = db.query(OUTLET_TABLE, null, Key.IDX + "=" + outlet.getIdx(),
        null, null, null, null);

    long count = 0;
    if (cursor != null) {
      count = cursor.getCount();
      cursor.close();
    }
    cursor = null;

    return (count > 0);
  }

  public boolean isAttendanceExists(Attendance attendance) {
    final SQLiteDatabase db = getDB();
    if (db == null) return false;

    Cursor cursor = db.query(OUTLET_TABLE, null, Key.IDX + "=" + attendance.getIdx(),
        null, null, null, null);

    long count = 0;
    if (cursor != null) {
      count = cursor.getCount();
      cursor.close();
    }
    cursor = null;

    return (count > 0);
  }

  public boolean storeSingleOutlet(Outlet outlet) {
    final SQLiteDatabase db = getDB();
    if (db == null) return false;

    if (isOutletExists(outlet)) {
      Log.d(TAG, "storeSingleOutlet:" + outlet.getId() + ", already exists!");
      return false;
    }

    ContentValues cv = new ContentValues();
    cv.put(Key.ID, outlet.getId());
    cv.put(Key.IDX, outlet.getIdx());
    cv.put(Key.NAME, outlet.getName());
    cv.put(Key.AREA, outlet.getArea());
    cv.put(Key.LOCATION, outlet.getLocation());
    cv.put(Key.DESCRIPTION, outlet.getDescription());
    cv.put(Key.SEQ, System.currentTimeMillis());

    long nres = db.insert(OUTLET_TABLE, null, cv);

    if (nres > 0) {
      Log.d(TAG, "Insert with name:" + outlet.getName());
    } else {
      Log.d(TAG, "Insert with name:" + outlet.getName() + " FAIL!!!");
    }

    cv.clear();
    cv = null;

    return (nres > 0);
  }


  public boolean storeSingleAttendance(Attendance attendance) {
    final SQLiteDatabase db = getDB();
    if (db == null) return false;

    if (isAttendanceExists(attendance)) {
      Log.d(TAG, "storeSingleAttendance:" + attendance.getId() + ", already exists!");
      return false;
    }

    ContentValues cv = new ContentValues();
    cv.put(Key.ID, attendance.getId());
    cv.put(Key.IDX, attendance.getIdx());
    cv.put(Key.USER_ID, attendance.getUserId());
    cv.put(Key.USER_NAME, attendance.getUserName());
    cv.put(Key.TYPE, attendance.getType());
    cv.put(Key.OUTLET_IDX, attendance.getOutletIdx());
    cv.put(Key.OUTLET_NAME, attendance.getOutletName());
    cv.put(Key.OUTLET_AREA, attendance.getOutletArea());
    cv.put(Key.SHIFT, attendance.getShift());
    cv.put(Key.INCOME, attendance.getIncome());
    cv.put(Key.UPDATED_AT, attendance.getUpdatedAt());
    cv.put(Key.CREATED_AT, attendance.getCreatedAt());
    cv.put(Key.SEQ, System.currentTimeMillis());

    long nres = db.insert(ATTENDANCE_TABLE, null, cv);

    if (nres > 0) {
      Log.d(TAG, "Insert with id:" + attendance.getId());
    } else {
      Log.d(TAG, "Insert with id:" + attendance.getId() + " FAIL!!!");
    }

    cv.clear();
    cv = null;

    return (nres > 0);
  }

  public Cursor getAllOutlet() {
    final SQLiteDatabase db = getDB();
    if (db == null) return null;

    Cursor cursor = db.query(OUTLET_TABLE, null, null,
        null, null, null, Key.AREA + " DESC");

    // db = null;
    return cursor;
  }

  public Cursor getAllAttendances() {
    final SQLiteDatabase db = getDB();
    if (db == null) return null;

    Cursor cursor = db.query(ATTENDANCE_TABLE, null, null,
        null, null, null, Key.IDX + " DESC");

    // db = null;
    return cursor;
  }

  private void dropOutlet() {
    final SQLiteDatabase db = getDB();
    if (db == null) return;

    db.execSQL("delete from " + OUTLET_TABLE);
    Log.d(TAG, "@@@ dropOutlet");
  }

  private void dropAttendance() {
    final SQLiteDatabase db = getDB();
    if (db == null) return;

    db.execSQL("delete from " + ATTENDANCE_TABLE);
    Log.d(TAG, "@@@ dropAttendance");
  }

  public void deleteAllData() {
    dropOutlet();
    dropAttendance();
  }

  private static class DatabaseHelper extends SQLiteOpenHelper {
    // ----DATABASE NAME and VERSION---
    private static final String DATABASE_NAME = GeneralStorage.DATABASE_NAME;
    private static final int DATABASE_VERSION = 1;

    private DatabaseHelper(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private void buildOutletTable(final SQLiteDatabase db) {
      db.execSQL("CREATE TABLE IF NOT EXISTS " + OUTLET_TABLE + "(" + "_id INTEGER PRIMARY KEY,"
          + Key.ID + " TEXT,"
          + Key.IDX + " INTEGER NOT NULL DEFAULT 0,"
          + Key.NAME + " TEXT,"
          + Key.AREA + " TEXT,"
          + Key.LOCATION + " TEXT,"
          + Key.DESCRIPTION + " TEXT,"
          + Key.SEQ + " INTEGER NOT NULL DEFAULT 0,"
          + Key.CREATED_AT + " INTEGER NOT NULL DEFAULT 0,"
          + "UNIQUE (" + Key.SEQ + ")" + ");");
    }

    private void buildAttendanceTable(final SQLiteDatabase db) {
      db.execSQL("CREATE TABLE IF NOT EXISTS " + ATTENDANCE_TABLE + "(" + "_id INTEGER PRIMARY KEY,"
          + Key.ID + " TEXT,"
          + Key.IDX + " INTEGER NOT NULL DEFAULT 0,"
          + Key.TYPE + " INTEGER NOT NULL DEFAULT 0,"
          + Key.SHIFT + " INTEGER NOT NULL DEFAULT 0,"
          + Key.IN + " INTEGER NOT NULL DEFAULT 0,"
          + Key.OUT + " INTEGER NOT NULL DEFAULT 0,"
          + Key.USER_ID + " TEXT,"
          + Key.USER_NAME + " TEXT,"
          + Key.OUTLET_IDX + " INTEGER NOT NULL DEFAULT 0,"
          + Key.OUTLET_NAME + " TEXT,"
          + Key.OUTLET_AREA + " TEXT,"
          + Key.REF + " TEXT,"
          + Key.REPORT_ID + " TEXT,"
          + Key.INCOME + " INTEGER NOT NULL DEFAULT 0,"
          + Key.IN_AT + " INTEGER NOT NULL DEFAULT 0,"
          + Key.SEQ + " INTEGER NOT NULL DEFAULT 0,"
          + Key.CREATED_AT + " INTEGER NOT NULL DEFAULT 0,"
          + Key.UPDATED_AT + " INTEGER NOT NULL DEFAULT 0,"
          + "UNIQUE (" + Key.SEQ + ")" + ");");
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
      Log.d(TAG, "~~~ onCreate:" + DATABASE_NAME);
      buildOutletTable(db);
      buildAttendanceTable(db);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, int oldVersion, int newVersion) {
      Log.d(TAG, "~~~ Upgrade DB from version: " + oldVersion + " to version: " + newVersion);
    }
  } // DatabaseHelper
}
