package cn.koolcloud.ipos.appstore;


import java.util.Map;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import cn.koolcloud.ipos.appstore.utils.Env;
import cn.koolcloud.ipos.appstore.utils.Logger;

import com.baidu.frontia.FrontiaApplication;

/**
 * <p>Title: AppStoreApplication.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: All In Pay</p>
 * @author 		Teddy
 * @date 		2013-11-7
 * @version 	
 */
public class AppStoreApplication extends FrontiaApplication {
	private static final String TAG = "AppStoreApplication";
	
	public static int THEME = R.style.Theme_Sherlock;
	private LayoutInflater inflater;				//view inflater
	private Map<String, PackageInfo> installedPackage;
	private static final int INIT_APP_INFOS = 3;
	private boolean isFirstStart = false;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		initApps();
	}
	
	public LayoutInflater getInflater() {
		return inflater;
	}
	
	public void saveInstalledAppsInfo(Map<String, PackageInfo> packages) {
		this.installedPackage = packages;
	}
	
	public Map<String, PackageInfo> getInstalledAppsInfo() {
		return this.installedPackage;
	}
	
	public void addAppInfoCache(PackageInfo packageInfo) {
		if (packageInfo != null) {
			
			installedPackage.put(packageInfo.applicationInfo.packageName, packageInfo);
		}
	}
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case INIT_APP_INFOS:
				Map<String, PackageInfo> packages = (Map<String, PackageInfo>) msg.obj;
				saveInstalledAppsInfo(packages);
			default:
				break;
			}
		}
	};
	
	public void initApps() {
		new InitAppsThread().start();
	}
	
	public boolean isFirstStart() {
		return this.isFirstStart;
	}
	
	public void setFirstStart(boolean firstStarted) {
		this.isFirstStart = firstStarted;
	}
	
	//init apps in a new thread
	class InitAppsThread extends Thread {

		@Override
		public void run() {
			Logger.d("invoke scan local apps");
			Long start = System.currentTimeMillis();
			Map<String, PackageInfo> installedPackage = Env.scanInstalledAppToMap(getApplicationContext());
			Long end = System.currentTimeMillis();
			Logger.d("total time:" + (end - start));
			
			Message msg = mHandler.obtainMessage();
			msg.obj = installedPackage;
			msg.what = INIT_APP_INFOS;
			mHandler.sendMessage(msg);
		}
		
	}
	
}
