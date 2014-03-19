package cn.koolcloud.ipos.appstore.receiver;

import java.io.File;

import cn.koolcloud.ipos.appstore.AppStorePreference;
import cn.koolcloud.ipos.appstore.cache.DataCleanManager;
import cn.koolcloud.ipos.appstore.download.database.DownloadDBOperator;
import cn.koolcloud.ipos.appstore.ui.CategoryMainActivity;
import cn.koolcloud.ipos.appstore.ui.MainFrameActivity;
import cn.koolcloud.ipos.appstore.ui.SearchActivity;
import cn.koolcloud.ipos.appstore.ui.SoftwareDetailActivity;
import cn.koolcloud.ipos.appstore.utils.Env;
import cn.koolcloud.ipos.appstore.utils.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

/**
 * <p>Title: AppBroadcastReceiver.java </p>
 * <p>Description: App install or remove receiver and then refresh the main UI</p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: All In Pay</p>
 * @author 		Teddy
 * @date 		2013-12-18
 * @version 	
 */
public class AppBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = "AppBroadcastReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		MainFrameActivity mainFrameInstance = MainFrameActivity.getInstance();
		CategoryMainActivity categoryInstance = CategoryMainActivity.getInstance();
		SoftwareDetailActivity softwareDetailInstance = SoftwareDetailActivity.getInstance();
		SearchActivity searchActivityInstance = SearchActivity.getInstance();
		
		Logger.i(action);
		if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
			Logger.d("app installed");
			Logger.d(intent.getDataString());
			
			if (categoryInstance != null) {
				categoryInstance.refreshLocalSoftData();
			}
			
			if (softwareDetailInstance != null) {
				softwareDetailInstance.refreshLocalSoftData();
			}
			
			if (mainFrameInstance != null) {
				mainFrameInstance.refreshUpdateSoftData();
			}
			
			if (searchActivityInstance != null) {
				searchActivityInstance.refreshUpdateSoftData();
			}
		} else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
			Logger.d("app removed");
			Logger.d(intent.getDataString());
			if (!intent.getDataString().substring(8).equals(Env.getPackageName(context))) {
				if (mainFrameInstance != null) {
					mainFrameInstance.refreshLocalSoftData();
				}
			}
		} else if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
			
		} else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
			Logger.d("app replaced");
			Logger.d(intent.getDataString());
			if (!intent.getDataString().substring(8).equals(Env.getPackageName(context))) {
				if (categoryInstance != null) {
					
					categoryInstance.refreshLocalSoftData();
				}
				if (softwareDetailInstance != null) {
					
					softwareDetailInstance.refreshLocalSoftData();
				}
				if (mainFrameInstance != null) {
					mainFrameInstance.refreshUpdateSoftData();
				}
				if (searchActivityInstance != null) {
					searchActivityInstance.refreshUpdateSoftData();
				}
			}
		} else if (Intent.ACTION_PACKAGE_RESTARTED.equals(action)) {
		}
		
		//delete apk file
		deleteApk(context, intent);
	}
	
	private void deleteApk(Context context, Intent intent) {
		String packageString = intent.getDataString();
		Logger.d("------------>" + packageString);
		Logger.d("=============>" + intent.getPackage());
		boolean isDeleteApk = AppStorePreference.getDeleteApkTag(context);
		if (!TextUtils.isEmpty(packageString) && isDeleteApk) {
			String[] packNameArray = packageString.split(":");
			if (packNameArray != null && packNameArray.length > 1) {
				String path = DownloadDBOperator.getInstance(context).getAppDownloadPathByPackage(packNameArray[1]);
				DataCleanManager.deleteFile(new File(path));
			}
		}
	}

}
