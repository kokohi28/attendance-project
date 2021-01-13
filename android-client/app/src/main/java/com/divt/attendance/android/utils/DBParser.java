package com.divt.attendance.android.utils;

import android.database.Cursor;

import com.divt.attendance.android.model.Attendance;
import com.divt.attendance.android.model.Outlet;

import java.util.ArrayList;
import java.util.List;

public class DBParser {
  public static List<Outlet> parseOutlets(Cursor cursor) {
    List<Outlet> outlets = new ArrayList<>();

    Outlet outlet;
    while ((outlet = parseOutlet(cursor)) != null) {
      outlets.add(outlet);
    }

    return outlets;
  }

  public static Outlet parseOutlet(Cursor cursor) {
    if (cursor == null) return null;
    if (!cursor.moveToNext()) return null;

    int id_idx = cursor.getColumnIndex(Key.ID);
    int idx_idx = cursor.getColumnIndex(Key.IDX);
    int name_idx = cursor.getColumnIndex(Key.NAME);
    int area_idx = cursor.getColumnIndex(Key.AREA);
    int loc_idx = cursor.getColumnIndex(Key.LOCATION);
    int desc_idx = cursor.getColumnIndex(Key.DESCRIPTION);
//    int seq_idx = cursor.getColumnIndex(Key.SEQ);

    Outlet outlet = new Outlet();
    outlet.setId(cursor.getString(id_idx));
    outlet.setIdx(cursor.getLong(idx_idx));
    outlet.setName(cursor.getString(name_idx));
    outlet.setArea(cursor.getString(area_idx));
    outlet.setLocation(cursor.getString(loc_idx));
    outlet.setDescription(cursor.getString(desc_idx));

    return outlet;
  }

  public static List<Attendance> parseAttendances(Cursor cursor) {
    List<Attendance> attendances = new ArrayList<>();

    Attendance attendance;
    while ((attendance = parseAttendance(cursor)) != null) {
      attendances.add(attendance);
    }

    return attendances;
  }

  public static Attendance parseAttendance(Cursor cursor) {
    if (cursor == null) return null;
    if (!cursor.moveToNext()) return null;

    int id_idx = cursor.getColumnIndex(Key.ID);
    int idx_idx = cursor.getColumnIndex(Key.IDX);
    int name_idx = cursor.getColumnIndex(Key.NAME);
    int shift_idx = cursor.getColumnIndex(Key.SHIFT);
    int in_idx = cursor.getColumnIndex(Key.IN);
    int out_idx = cursor.getColumnIndex(Key.OUT);
//    int seq_idx = cursor.getColumnIndex(Key.SEQ);

    Attendance attendance = new Attendance();
    attendance.setId(cursor.getString(id_idx));
    attendance.setIdx(cursor.getLong(idx_idx));
    attendance.setUserName(cursor.getString(name_idx));
    attendance.setShift(cursor.getInt(shift_idx));
    attendance.setIn(cursor.getLong(in_idx));
    attendance.setOut(cursor.getLong(out_idx));

    return attendance;
  }
}
