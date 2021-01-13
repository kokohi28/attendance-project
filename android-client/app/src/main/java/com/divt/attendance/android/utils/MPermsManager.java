package com.divt.attendance.android.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

public class MPermsManager {
  private final static String TAG = MPermsManager.class.getSimpleName();

  public final static int REQUEST_ALL_MANDATORY = 100;
  public final static int REQUEST_ALL_OPTIONAL = 101;
  public final static int REQUEST_SINGLE_PERMISSION = 102;
  public final static int REQUEST_WITH_SPECIFIC_PURPOSE = 103;

  public static boolean hasM() {
    /* Android 6.0 API Level 23 */
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
  }

  @SuppressLint("InlinedApi")
  // @Jelly bean
  public final static String P_READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE; // mandatory needs
  public final static String P_WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE; // mandatory needs
  public final static String P_CAMERA = Manifest.permission.CAMERA; // [v] used for take photo or video

  // public boolean getAllPermsState() {
  // return mAllPermsGranted;
  // }
  //
  // public boolean hasPendingReq() {
  // return mHasPendingRequestPermission;
  // }

  public static boolean isPermGranted(Activity activity, String perm) {
    if (hasM()) {
      if (ActivityCompat.checkSelfPermission(activity, perm) == PackageManager.PERMISSION_GRANTED) {
        Log.d(TAG, " --- isPermsGranted: " + perm);
        return true;
      }
      return false;
    } else {
      Log.d(TAG, " --- UNDER M isPermsGranted: " + perm);
      return true;
    }
  }

  public static boolean isAllPermsGranted(Context context, String[] perms) {
    boolean hasNotPermit = false;

    if (hasM()) {
      for (int i = 0; i < perms.length; i++) {
        if (ActivityCompat.checkSelfPermission(context, perms[i]) != PackageManager.PERMISSION_GRANTED) {
          Log.d(TAG, " --- isPermGranted[" + i + "]:" + perms[i]);
          hasNotPermit = true;
        }
      }
    } else {
      Log.d(TAG, " --- UNDER M all isPermsGranted");
    }

    Log.d(TAG, " --- isAllPermsGranted:" + !hasNotPermit);

    return !hasNotPermit;
  }

  public static boolean verifyPermissions(int[] grantResults) {
    // At least one result must be checked.
    if (grantResults.length < 1) {
      return false;
    }

    // Verify that each required permission has been granted, otherwise return false.
    for (int result : grantResults) {
      if (result != PackageManager.PERMISSION_GRANTED) {
        return false;
      }
    }
    return true;
  }

  public static boolean verifyWithSpecificPermission(int[] grantResults, String[] permissions, String specificPerm) {
    // At least one result must be checked.
    if (grantResults.length < 1 || permissions.length < 1) {
      return false;
    }

    Log.d(TAG, " --- verifyWithSpecificPermission pre:" + grantResults.length + " " + permissions.length);

    // Ambil yg kecil (JIKA tidak sama)
    int n = (Math.min(grantResults.length, permissions.length));

    // Verify that each required permission has been granted, otherwise return false.
    for (int i = 0; i < n; i++) {
      int result = grantResults[i];
      String perm = permissions[i];

      Log.d(TAG, " --- Permissions data:" + result + " " + perm);
      if (perm.equals(specificPerm) && result == PackageManager.PERMISSION_GRANTED) {
        return true;
      }
    }

    return false;
  }

  public static boolean verifyWithSpecificPermissions(int[] grantResults, String[] permissions, String[] specificPerms) {
    // At least one result must be checked.
    if (grantResults.length < 1 || permissions.length < 1) {
      return false;
    }

    Log.d(TAG, " --- verifyWithSpecificPermissions pre:" + grantResults.length + " " + permissions.length + " " + specificPerms.length);

    boolean success = true; // default all granted,
    // Verify that each required permission has been granted, otherwise return false.
    for (int i = 0; i < specificPerms.length; i++) {
      String reqPerm = specificPerms[i];
      boolean reqPerm_pass = true;
      for (int j = 0; i < permissions.length; j++) {
        String perm = permissions[j];
        if (reqPerm.equals(perm)) {
          Log.d(TAG, " --- perm :" + reqPerm + " " + grantResults[j]);
          if (grantResults[j] == PackageManager.PERMISSION_GRANTED) {
            reqPerm_pass = true;
          } else {
            reqPerm_pass = false;
          }
          break;
        }
      }

      if (!reqPerm_pass) {
        success = false;
        Log.d(TAG, " --- failed @perm :" + reqPerm);
        break;
      }
    }

    return success;
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  public static void requestPerm(Activity activity, String perm, int reqCode) {
    String[] perms = new String[]{perm};
    requestPerms(activity, perms, reqCode);
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  public static void requestPerms(Activity activity, String[] perms, int reqCode) {
    if (hasM()) {
      ActivityCompat.requestPermissions(activity, perms, reqCode);
    } else {
      Log.d(TAG, " --- UNDER M no need requestPerms");
    }
  }
}
