package cn.koolcloud.ipos.appstore.cache;

import java.io.File;

import cn.koolcloud.ipos.appstore.utils.Logger;

import android.content.Context;
import android.os.Environment;

public class DataCleanManager {
	private static final String TAG = "DataCleanManager";
	/**
	 * @Title: cleanInternalCache
	 * @Description: Clear app internal cache
	 * (/data/data/com.xxx.xxx/cache)
	 * @param context
	 * @return: void
	 */
	public static void cleanInternalCache(Context context) {
		deleteFilesByDirectory(context.getCacheDir());
	}

	/**
	 * @Title: cleanDatabases
	 * @Description: Remove databases
	 * (/data/data/com.xxx.xxx/databases)
	 * @param context
	 * @return: void
	 */
	public static void cleanDatabases(Context context) {
		deleteFilesByDirectory(new File("/data/data/"
				+ context.getPackageName() + "/databases"));
	}

	/**
	 * @Title: cleanSharedPreference
	 * @Description: Clear SharedPreference
	 * (/data/data/com.xxx.xxx/shared_prefs)
	 * @param context
	 * @return: void
	 */
	public static void cleanSharedPreference(Context context) {
		deleteFilesByDirectory(new File("/data/data/"
				+ context.getPackageName() + "/shared_prefs"));
	}

	/**
	 * @Title: cleanDatabaseByName
	 * @Description: Remove database by name
	 * @param context
	 * @param dbName
	 * @return: void
	 */
	public static void cleanDatabaseByName(Context context, String dbName) {
		context.deleteDatabase(dbName);
	}

	/**
	 * @Title: cleanFiles
	 * @Description: Clear app internal files
	 * (/data/data/com.xxx.xxx/files)
	 * @param context
	 * @return: void
	 */
	public static void cleanFiles(Context context) {
		deleteFilesByDirectory(context.getFilesDir());
	}

	/**
	 * @Title: cleanExternalCache
	 * @Description: Clear external cache files
	 * (/mnt/sdcard/android/data/com.xxx.xxx/cache)
	 * @param context
	 * @return: void
	 */
	public static void cleanExternalCache(Context context) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			deleteFilesByDirectory(new File(context.getExternalCacheDir() + "/download/"));
		}
	}

	/**
	 * @Title: cleanCustomCache
	 * @Description: Clear customized files of path, Be Care.
	 * This method only support delete files in folders.
	 * @param filePath
	 * @return: void
	 */
	public static void cleanCustomCache(String filePath) {
		deleteFilesByDirectory(new File(filePath));
	}

	/**
	 * @Title: cleanApplicationData
	 * @Description: Clear all the data of this app
	 * @param context
	 * @param filepath
	 * @return: void
	 */
	public static void cleanApplicationData(Context context, String... filepath) {
		cleanInternalCache(context);
		cleanExternalCache(context);
		cleanDatabases(context);
		cleanSharedPreference(context);
		cleanFiles(context);
		for (String filePath : filepath) {
			cleanCustomCache(filePath);
		}
	}

	/**
	 * @Title: deleteFilesByDirectory
	 * @Description: Delete the files in folders, there is nothing will be happen when you 
	 * pass a single file as a argument.
	 * @param directory
	 * @return: void
	 */
	private static void deleteFilesByDirectory(File directory) {
		if (directory != null && directory.exists() && directory.isDirectory()) {
			for (File item : directory.listFiles()) {
				Logger.d(item.getPath());
				item.delete();
			}
		}
	}
	
	/**
	 * @Title: deleteFile
	 * @Description: delete single file
	 * @param file
	 * @return: void
	 */
	public static void deleteFile(File file) {
		if (file != null && file.exists()) {
			file.delete();
		}
	}

}
