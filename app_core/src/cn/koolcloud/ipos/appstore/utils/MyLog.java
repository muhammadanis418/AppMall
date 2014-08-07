package cn.koolcloud.ipos.appstore.utils;

import android.util.Log;

/**
 * <p>Title: Logger.java</p>
 * <p>Description: Simple Logger class it funds on Android native logger but permits also some extra features .
 * Two level of filters and systematically print class name when this is not static </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: All In Pay</p>
 * @author 		Teddy
 * @date 		2013-11-10
 * @version 	
 */
public class MyLog {
	
//	private static String className;
//	private static String methodName;
//	private static int lineNumber;
	private static final String TAG = "mylog";

	private MyLog() {
		/* Protect from instantiations */
	}

	public static boolean isDebuggable() {
		return BuildingConfig.DEBUG;
	}

	private static String createLog(String log) {

		StringBuffer buffer = new StringBuffer();
//		buffer.append("[");
//		buffer.append(className);
//		buffer.append(":");
//		buffer.append(methodName);
//		buffer.append(":");
//		buffer.append(lineNumber);
//		buffer.append("]\n");
		buffer.append(log);

		return buffer.toString();
	}

	private static void getMethodNames(StackTraceElement[] sElements) {
//		className = sElements[1].getFileName();
//		methodName = sElements[1].getMethodName();
//		lineNumber = sElements[1].getLineNumber();
	}

	public static void e(String message) {
		if (!isDebuggable())
			return;

		// Throwable instance must be created before any methods
		getMethodNames(new Throwable().getStackTrace());
		Log.e(TAG, createLog(message));
	}

	public static void i(String message) {
		if (!isDebuggable())
			return;

		getMethodNames(new Throwable().getStackTrace());
		Log.i(TAG, createLog(message));
	}

	public static void d(String message) {
		if (!isDebuggable())
			return;

		getMethodNames(new Throwable().getStackTrace());
		Log.d(TAG, createLog(message));
	}

	public static void v(String message) {
		if (!isDebuggable())
			return;

		getMethodNames(new Throwable().getStackTrace());
		Log.v(TAG, createLog(message));
	}

	public static void w(String message) {
		if (!isDebuggable())
			return;

		getMethodNames(new Throwable().getStackTrace());
		Log.w(TAG, createLog(message));
	}

	public static void wtf(String message) {
		if (!isDebuggable())
			return;

		getMethodNames(new Throwable().getStackTrace());
		Log.wtf(TAG, createLog(message));
	}
	
}
