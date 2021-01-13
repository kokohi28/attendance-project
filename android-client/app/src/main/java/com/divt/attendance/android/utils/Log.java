package com.divt.attendance.android.utils;

public class Log {
  private static final boolean _ACTIVATE_LOG = true;

	public static void d(String TAG, String logtext){
		if (_ACTIVATE_LOG) android.util.Log.d(TAG, logtext);
	}

	public static void e(String TAG, String logtext){
		if (_ACTIVATE_LOG) android.util.Log.e(TAG, logtext);
	}

	public static void e(String TAG, String logtext, Throwable tr){
		if (_ACTIVATE_LOG) android.util.Log.e(TAG, logtext, tr);
	}
}

