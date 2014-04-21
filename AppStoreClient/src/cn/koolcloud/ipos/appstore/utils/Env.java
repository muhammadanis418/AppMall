package cn.koolcloud.ipos.appstore.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import cn.koolcloud.ipos.appstore.cache.FileManager;
import cn.koolcloud.ipos.appstore.constant.Constants;
import cn.koolcloud.ipos.appstore.entity.AppInfo;

public class Env {
	
	private final static String TAG = "ENV";
	
	public static String DATA_FILES = Environment.getDataDirectory().getPath() + "/data/com.able.androidclient.apptop/files";
	public static String DATA_DATA_BASES = Environment.getDataDirectory().getPath() + "/data/com.able.androidclient.apptop/databases";
	public static String DATA_DATA_CACHE = Environment.getDataDirectory().getPath() + "/data/com.able.androidclient.apptop/cache";
	public static String DATA_DATA_SHARED_PREFERENCE = Environment.getDataDirectory().getPath() + "/data/com.able.androidclient.apptop/cache";
	public static String SD_CARD_APK_CACHE_DIR = Environment.getExternalStorageDirectory() + "/download/";
	public static String SD_CARD_IMAGE_CACHE_DIR = Environment.getExternalStorageDirectory() + "/AppStore/ImageCache/";
//	private static final String DATA_FILE = Environment.getDataDirectory().getPath() + "/data/com.able.androidclient.player/files";
//  private static final String DATA_FILE = Environment.getDataDirectory().getPath() + "/data/com.able.androidclient.%s/files";

	private static final String SD_HOME = "AppStore";
	private static final String SD_IMG = "ImageCache";
	
	private static File cacheDir;
	
	public static File getImageCacheDirectory(Context context) {
		if(FileManager.exsitSdcard()) {
			cacheDir = new File(getExternalStorageDirectory(), SD_IMG);
		} else {
			cacheDir = null;
		}
		if(cacheDir != null) {
			if(!cacheDir.exists()) {
				cacheDir.mkdirs();//create all directory.
			}
		}
		return cacheDir;
	}
	
	public static File getDataDirectoryPath(String appName) {
		if(appName == null) {
			return getAppDataDirectory();
		}
		File appDir = new File(getAppDataDirectory(), appName);
		if(!appDir.exists()) {
			appDir.mkdirs();//create all directory.
		}
		return appDir;
	}
	
	public static File getExternalStorageDirectory() {
		File file = new File(Environment.getExternalStorageDirectory().getPath(), SD_HOME);
		if(!file.exists()) {
			file.mkdirs();//create all directory.
		}
		if(cacheDir != null) {
			if(!cacheDir.exists()) {
				cacheDir.mkdirs();//create all directory.
			}
		}
		return new File(Environment.getExternalStorageDirectory().getPath(), SD_HOME);
	}
	
	public static File getAppDataDirectory() {
	  
		//StackTraceElement[] stackTraceElements = new Throwable().getStackTrace();
		//String componentName = getCallingComponent(stackTraceElements[stackTraceElements.length-1].getClassName());

		//    File file = new File(String.format(DATA_FILE,componentName));
		File file = new File(String.format(DATA_FILES));

    	if(!file.exists()) {
		  
			Logger.d("manifest mount disk = " + Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS);
			Logger.d("manifest write disk = " + Environment.getDataDirectory().canWrite());
			boolean b = file.mkdirs();//create all directory.
			if (!b) {
				Logger.e("CANNOT CREATE DIRECTORY :" + DATA_FILES);
				return null;
			}
		}
		return file;
	}
	
	/**
	* @Title: getInstalledApps
	* @Description: TODO get installed apps
	* @param @param context
	* @param @param getSysPackages if system apps
	* @param @return
	* @return ArrayList<AppInfo> 
	* @throws
	*/
	public static ArrayList<AppInfo> getInstalledAppsToList(Context context, boolean getSysPackages) {
		
	    PackageManager pManager = context.getPackageManager();
	    ArrayList<AppInfo> res = new ArrayList<AppInfo>();     
	    List<PackageInfo> packs = pManager.getInstalledPackages(0);
	    int length = packs.size();
	    PackageInfo packageInfo;
	    String myPackageName = context.getApplicationInfo().packageName;
	    
	    Pattern pattern = Pattern.compile(Constants.REG_PACKAGE_MATCH);
		
	    for (int i = 0; i < length; i++) {
	    	packageInfo = packs.get(i);
	    	Matcher matcher = pattern.matcher(packageInfo.applicationInfo.packageName);
	    	if (packageInfo.applicationInfo.packageName.equals(myPackageName)) { 
	    		continue;   
	    	}
	    	if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
	    		if (!matcher.matches()) {
	    			continue;
	    		}
	    	}
	      
	    	AppInfo appInfo = new AppInfo();
	    	
	    	String dir = packageInfo.applicationInfo.publicSourceDir;
	    	long size =  new File(dir).length();
	    	
	    	appInfo.setName(packageInfo.applicationInfo.loadLabel(pManager).toString());
	    	appInfo.setSoftSize(size);
	    	appInfo.setIcon(pManager.getApplicationIcon(packageInfo.applicationInfo));
	    	appInfo.setPackageName(packageInfo.packageName);
	    	appInfo.setVersionName(packageInfo.versionName);
	    	appInfo.setVersionCode(packageInfo.versionCode);
	    	res.add(appInfo);
	    }
	    return res;
	}
	
	/**
	* @Title: uninstallApp
	* @Description: TODO uninstsall APP
	* @param @param context
	* @param @param packageName
	* @return void 
	* @throws
	*/
	public static void uninstallApp(Context context, String packageName) {
	    Uri packageURI = Uri.parse("package:" + packageName);   
	    Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);   
	    uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    context.startActivity(uninstallIntent);
	}
	
	/**
	 * install app
	 */
	public static void openAPK(File f, Context context) {
	    context.startActivity(getInstallApp(f, context));
	}

	public static Intent getInstallApp(File f, Context context) {
	    Intent intent = new Intent();
	    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    //set the place where the file come from
	    intent.putExtra("android.intent.extra.INSTALLER_PACKAGE_NAME", context.getPackageName());
	    intent.setAction(android.content.Intent.ACTION_VIEW);

	    /* set file for intent */
	    intent.setDataAndType(Uri.fromFile(f), "application/vnd.android.package-archive");
	    return intent;
	}
	
	public static int getVersionCode(Context context) {
		int versionCode = 0;
		try {
			versionCode = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionCode;
	}

	public static String getVersionName(Context context) {
		String versionName = "";
		try {
			versionName = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionName;
	}
	
	public static String getPackageName(Context context) {
		String packageName = "";
		try {
			packageName = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).packageName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return packageName;
	}
	
	public static Map<String, PackageInfo> scanInstalledAppToMap(Context ctx) {
		Map<String, PackageInfo> installedPackage = new HashMap<String, PackageInfo>();
		List<PackageInfo> appPackage = ctx.getPackageManager()
				.getInstalledPackages(0);// get installed apps info
		Pattern pattern = Pattern.compile(Constants.REG_PACKAGE_MATCH);
		for (int i = 0; i < appPackage.size(); i++) {
			PackageInfo packageInfo = appPackage.get(i);
			Matcher matcher = pattern.matcher(packageInfo.applicationInfo.packageName);
			/*if ((packageInfo.applicationInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0 ||
					matcher.matches()) {
				installedPackage.put(
						packageInfo.applicationInfo.packageName, packageInfo);
				Logger.d("appName:" + packageInfo.applicationInfo.loadLabel(
								ctx.getPackageManager()).toString());
				Logger.d("packageName:" + packageInfo.packageName);
				Logger.d("versionName:" + packageInfo.versionName);
				Logger.d("versionCode:" + packageInfo.versionCode);
			}*/
			if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
	    		if (!matcher.matches()) {
	    			continue;
	    		}
	    	}
			installedPackage.put(
					packageInfo.applicationInfo.packageName, packageInfo);
			Logger.d("appName:" + packageInfo.applicationInfo.loadLabel(
					ctx.getPackageManager()).toString());
			Logger.d("packageName:" + packageInfo.packageName);
			Logger.d("versionName:" + packageInfo.versionName);
			Logger.d("versionCode:" + packageInfo.versionCode);
		}
		
		return installedPackage;
	}
	
	/**
	* @Title: getAPKPackageName
	* @Description: get apk file package name
	* @param @param context
	* @param @param file
	* @param @return
	* @return String 
	* @throws
	*/
	public static String getAPKPackageName(Context context, File file) {
		String packageName = null;
		if (null != file && file.isFile() && file.exists()) {
			String fileName = file.getName();
			String apk_path = null;
			if (fileName.toLowerCase().endsWith(".apk")) {
				// apk absolute path
				apk_path = file.getAbsolutePath();
				PackageManager pm = context.getPackageManager();
				PackageInfo packageInfo = pm.getPackageArchiveInfo(apk_path, PackageManager.GET_ACTIVITIES);
				ApplicationInfo appInfo = packageInfo.applicationInfo;
				/* apk icon */
				appInfo.sourceDir = apk_path;
				appInfo.publicSourceDir = apk_path;
				Drawable apk_icon = appInfo.loadIcon(pm);
				/* apk package name*/
				packageName = packageInfo.packageName;
				/* apk version String */
				String versionName = packageInfo.versionName;
				/* apk version code int */
				int versionCode = packageInfo.versionCode;
			}
		}
		return packageName;
	}
	
	/**
	* @Title: getAPKPackageInfo
	* @Description: get apk file package information
	* @param @param context
	* @param @param file
	* @param @return
	* @return PackageInfo 
	* @throws
	*/
	public static PackageInfo getAPKPackageInfo(Context context, File file) {
		PackageInfo packageInfo = null;
		String packageName = null;
		if (null != file && file.isFile() && file.exists()) {
			String fileName = file.getName();
			String apk_path = null;
			if (fileName.toLowerCase().endsWith(".apk")) {
				// apk absolute path
				apk_path = file.getAbsolutePath();
				PackageManager pm = context.getPackageManager();
				packageInfo = pm.getPackageArchiveInfo(apk_path, PackageManager.GET_ACTIVITIES);
				if (packageInfo != null) {
					
					ApplicationInfo appInfo = packageInfo.applicationInfo;
					/* apk icon */
					appInfo.sourceDir = apk_path;
					appInfo.publicSourceDir = apk_path;
					Drawable apk_icon = appInfo.loadIcon(pm);
					/* apk package name*/
					packageName = packageInfo.packageName;
					/* apk version String */
					String versionName = packageInfo.versionName;
					/* apk version code int */
					int versionCode = packageInfo.versionCode;
				}
			}
		}
		return packageInfo;
	}
	
	/**
	* @Title: isAppInstalled
	* @Description: TODO
	* @param @param ctx
	* @param @param appName
	* @param @param versionName
	* @param @return
	* @return int 0 installed, -1 need update, -2 not installed
	* @throws
	*/
	public static int isAppInstalled(Context ctx, String packageName, int versionCode, Map<String, PackageInfo> installedPackage) {
		if (installedPackage != null) {
			PackageInfo pi = installedPackage.get(packageName);
			if (pi != null) {
				int ret = pi.versionCode - versionCode;
				if (ret < 0) {
					ret = Constants.APP_NEW_VERSION_UPDATE;
				} else if (ret >= 0) {
					ret = Constants.APP_INSTALLED_OPEN;
				}
				return ret;
			} else {
				return Constants.APP_NO_INSTALLED_DOWNLOAD;
			}
		}
		return Constants.APP_NO_INSTALLED_DOWNLOAD;
	}

	public static void install(Activity ctx, File file, int requestCode) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		try {
			Runtime run = Runtime.getRuntime();
			Process proc = run.exec("chmod 655 " + file.toString());
			int result = proc.waitFor();
			
			if (result == 0) {
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setDataAndType(Uri.fromFile(file),
						"application/vnd.android.package-archive");
				ctx.startActivityForResult(intent, requestCode);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	public static Intent getLaunchIntent(Context ctx, String packageName, Map<String, PackageInfo> installedPackage) {
		return ctx.getPackageManager().getLaunchIntentForPackage(
				installedPackage.get(packageName).packageName);
	}
	
	/**
	* @Title: getCallingComponent
	* @Description: TODO
	* @param @param callingClass
	* @param @return
	* @return String 
	* @throws
	*/
	private static String getCallingComponent(String callingClass) {

		Logger.d("calling class " + callingClass);

		String[] packageValue = callingClass.split("\\.");
		String callingComponent = packageValue[3];

		return callingComponent;
	}
	
	public static String getDeviceInfo(Context ctx) {
		TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
		StringBuilder sb = new StringBuilder();
		sb.append("\nDeviceId(IMEI) = " + tm.getDeviceId());
		sb.append("\nDeviceSoftwareVersion = " + tm.getDeviceSoftwareVersion());
		sb.append("\nLine1Number = " + tm.getLine1Number());
		sb.append("\nNetworkCountryIso = " + tm.getNetworkCountryIso());
		sb.append("\nNetworkOperator = " + tm.getNetworkOperator());
		sb.append("\nNetworkOperatorName = " + tm.getNetworkOperatorName());
		sb.append("\nNetworkType = " + tm.getNetworkType());
		sb.append("\nPhoneType = " + tm.getPhoneType());
		sb.append("\nSimCountryIso = " + tm.getSimCountryIso());
		sb.append("\nSimOperator = " + tm.getSimOperator());
		sb.append("\nSimOperatorName = " + tm.getSimOperatorName());
		sb.append("\nSimSerialNumber = " + tm.getSimSerialNumber());
		sb.append("\nSimState = " + tm.getSimState());
		sb.append("\nSubscriberId(IMSI) = " + tm.getSubscriberId());
		sb.append("\nVoiceMailNumber = " + tm.getVoiceMailNumber());
		sb.append("\nandroid.os.Build.BOARD = " + android.os.Build.BOARD);
		sb.append("\nandroid.os.Build.BOOTLOADER = "
				+ android.os.Build.BOOTLOADER);
		sb.append("\nandroid.os.Build.BRAND = " + android.os.Build.BRAND);
		sb.append("\nandroid.os.Build.CPU_ABI = " + android.os.Build.CPU_ABI);
		sb.append("\nandroid.os.Build.CPU_ABI2 = " + android.os.Build.CPU_ABI2);
		sb.append("\nandroid.os.Build.DEVICE = " + android.os.Build.DEVICE);
		sb.append("\nandroid.os.Build.DISPLAY = " + android.os.Build.DISPLAY);
		sb.append("\nandroid.os.Build.FINGERPRINT = "
				+ android.os.Build.FINGERPRINT);
		sb.append("\nandroid.os.Build.HARDWARE = " + android.os.Build.HARDWARE);
		sb.append("\nandroid.os.Build.HOST = " + android.os.Build.HOST);
		sb.append("\nandroid.os.Build.ID = " + android.os.Build.ID);
		sb.append("\nandroid.os.Build.MANUFACTURER = "
				+ android.os.Build.MANUFACTURER);
		sb.append("\nandroid.os.Build.MODEL = " + android.os.Build.MODEL);
		sb.append("\nandroid.os.Build.PRODUCT = " + android.os.Build.PRODUCT);
		// sb.append("\nandroid.os.Build.RADIO = " + android.os.Build.RADIO);
		sb.append("\nandroid.os.Build.SERIAL = " + android.os.Build.SERIAL);
		sb.append("\nandroid.os.Build.TAGS = " + android.os.Build.TAGS);
		sb.append("\nandroid.os.Build.TIME = " + android.os.Build.TIME);
		sb.append("\nandroid.os.Build.TYPE = " + android.os.Build.TYPE);
		sb.append("\nandroid.os.Build.UNKNOWN = " + android.os.Build.UNKNOWN);
		sb.append("\nandroid.os.Build.USER = " + android.os.Build.USER);
		sb.append("\nandroid.os.Build.VERSION.RELEASE = " + android.os.Build.VERSION.RELEASE);
		
		return sb.toString();
	}
}