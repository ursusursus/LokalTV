package sk.ursus.lokaltv.util;

import android.util.Log;

public class LOG {

	private static final String TAG = "MainActivity";
	private static final boolean DEBUG = true;

	public static void d(String message) {
		if (DEBUG) {
			Log.d(TAG, message);
		}
	}

	public static void i(String message) {
		if (DEBUG) {
			Log.i(TAG, message);
		}
	}

	public static void e(String message) {
		if (DEBUG) {
			Log.e(TAG, message);
		}
	}

	public static void e(Exception e) {
		if (DEBUG) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

}
