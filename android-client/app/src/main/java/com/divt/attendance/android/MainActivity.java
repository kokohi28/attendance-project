package com.divt.attendance.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.Volley;
import com.divt.attendance.android.utils.Log;
import com.divt.attendance.android.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends NavigationActivity {
  private static final String TAG = MainActivity.class.getSimpleName();

  private static final boolean DEBUG = false;

  private static Handler mHandler = new Handler();
  public static Handler getHandler() {
    return mHandler;
  }

  private SharedPreferences mPref;

  public static String getEndPoint(Context ctx) {
    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
    if (DEBUG) {
      return "http://192.168.43.28:6004/api/v1/";
    } else {
      return MessageFormat.format("http://{0}:{1}/api/v1/",
          pref.getString("host", ""),
          String.valueOf(pref.getInt("port", 3000))
      );
    }
  }

  public static final String mAndroidDataPath = Environment.getExternalStorageDirectory().getAbsolutePath()
      + "/Android/data/" + Const.PACKAGE;

  public static String getAppCacheDir() {
    return mAndroidDataPath + "/pcache";
  }

  public static String getAppInPhoto() {
    return mAndroidDataPath + "/in";
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mPref = PreferenceManager.getDefaultSharedPreferences(this);

    Log.d(TAG, "cache: " + mAndroidDataPath);
    Utils.makeDirUnscanable(mAndroidDataPath);
    Utils.makeDirUnscanable(getAppCacheDir());
    Utils.makeDirUnscanable(getAppInPhoto());

    this.getHost(this);

    if (mPref.getString("user_id", null) != null &&
        mPref.getString("sess", null) != null
    ) {
      navigateTo(Page.F_HOME);
    } else {
      navigateTo(Page.F_LOGIN);
    }
  }

  public SharedPreferences getPref() {
    return mPref;
  }

  @SuppressLint("ApplySharedPref")
  public void getHost(
    @NonNull MainActivity ctx
  ) {
    SharedPreferences pref = getPref();
    if (pref.getString("host", null) != null) return;

    RequestQueue queue = Volley.newRequestQueue(ctx);
    String url = ctx.getString(R.string.src);
    queue.getCache().clear();

    StringRequest stringReq = new StringRequest(Request.Method.GET, url,
      response -> {
        Log.d(TAG, "getHost Response: " + response);
        try {
          JSONObject respJSON = new JSONObject(response);
          if (respJSON.has("host")) {
            pref.edit().putString("host", respJSON.getString("host")).commit();
          }
          if (respJSON.has("port")) {
            pref.edit().putInt("port", respJSON.getInt("port")).commit();
          }
        } catch (JSONException e) {
          e.printStackTrace();
        }
      },
      error -> Log.d(TAG, "Volley Error : " + error.getMessage())
    ) {
      @Override
      public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return headers;
      }
    };

    queue.add(stringReq);
  }
}