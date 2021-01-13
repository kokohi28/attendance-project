package com.divt.attendance.android.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.divt.attendance.android.Const;
import com.divt.attendance.android.utils.imagecompress.Compressor;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Utils {
  public static String SHA1(String input) throws NoSuchAlgorithmException {
    MessageDigest mDigest = MessageDigest.getInstance("SHA1");
    byte[] result = mDigest.digest(input.getBytes());
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < result.length; i++) {
      sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
    }

    return sb.toString();
  }

  // ***.....Make toast center in toast field..... ***//
  public static void toastView(Context context, int strId) {
    toastView(context, context.getString(strId));
  }

  public static void toastView(Context context, String text) {
    if (context == null) return;

    Toast toast = Toast.makeText(context, "   " + text + "   ", Toast.LENGTH_SHORT);
    TextView v = toast.getView().findViewById(android.R.id.message);
    if (v != null) v.setGravity(Gravity.CENTER);
    toast.show();
  }

  public static void closeSilently(@Nullable Closeable c) {
    if (c == null) return;
    try {
      c.close();
    } catch (Throwable t) {
      // Do nothing
    }
  }

  public static void makeDirUnscanable(String path) {
    File dir = new File(path);
    if (!dir.exists()) dir.mkdirs();
    dir = null;

    try {
      // Make this directory unscanable
      File noscan = new File(path, ".nomedia");
      if (!noscan.exists()) noscan.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @SuppressLint("SimpleDateFormat")
  public static long timeSQLToTimestamp(String time) {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); // "yyyy-MM-dd'T'HH:mm:ss:SS'Z'"
    df.setTimeZone(TimeZone.getDefault());
    // val dateIs = df.parse("2019-06-09T07:53:55.000Z")
    Date dateIs = null;
    try {
      dateIs = df.parse(time);
      return dateIs.getTime();
    } catch (ParseException e) {
      e.printStackTrace();
    }

    return 0;
  }

  public static String getTimeAndDate(long timestamp) {
    try {
      SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm, dd MMM yyyy"); // dd/MM/yyyy
      Date date = new Date();
      date.setTime(timestamp);
      String strDate = sdfDate.format(date);
      return (strDate);
    } catch (Exception e) {
    }
    return "";
  }

  public static String makeCopyPhotoCompressed(Context ctx, final String path, String basePath) {
    makeDirUnscanable(basePath);

    final String fName = "compressed.jpg";
    try {
      int id = ctx.getResources().getIdentifier("max_width_compressed_image", "integer", Const.PACKAGE);
      final int MAX_WIDTH_COMPRESSED = ctx.getResources().getInteger(id);
      id = ctx.getResources().getIdentifier("max_height_compressed_image", "integer", Const.PACKAGE);
      final int MAX_HEIGHT_COMPRESSED = ctx.getResources().getInteger(id);
      id = ctx.getResources().getIdentifier("quality_compressed_image", "integer", Const.PACKAGE);
      final int QUALITY_COMPRESSED = ctx.getResources().getInteger(id);

      new Compressor(ctx)
          .setMaxWidth(MAX_WIDTH_COMPRESSED)
          .setMaxHeight(MAX_HEIGHT_COMPRESSED)
          .setQuality(QUALITY_COMPRESSED)
          .setCompressFormat(Bitmap.CompressFormat.JPEG)
          .setDestinationDirectoryPath(basePath)
          .compressAndSave(new File(path), fName);

      return fName;
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }
}
