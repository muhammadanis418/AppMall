package cn.koolcloud.ipos.appstore.receiver;

import java.io.File;

import cn.koolcloud.ipos.appstore.CategoryActivity;
import cn.koolcloud.ipos.appstore.MainActivity;
import cn.koolcloud.ipos.appstore.SearchActivity;
import cn.koolcloud.ipos.appstore.AppDetailActivity;
import cn.koolcloud.ipos.appstore.cache.DataCleanManager;
import cn.koolcloud.ipos.appstore.download.database.DownloadDBOperator;
import cn.koolcloud.ipos.appstore.utils.MySPEdit;
import cn.koolcloud.ipos.appstore.utils.Env;
import cn.koolcloud.ipos.appstore.utils.MyLog;

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

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		MainActivity mainFrameInstance = MainActivity.getInstance();
		CategoryActivity categoryInstance = CategoryActivity.getInstance();
		AppDetailActivity softwareDetailInstance = AppDetailActivity.getInstance();
		SearchActivity searchActivityInstance = SearchActivity.getInstance();
		
		MyLog.i(action);
		if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
			MyLog.d("app installed");
			MyLog.d(intent.getDataString());
			
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
			MyLog.d("app removed");
			MyLog.d(intent.getDataString());
			if (!intent.getDataString().substring(8).equals(Env.getPackageName(context))) {
				if (mainFrameInstance != null) {
					mainFrameInstance.refreshLocalSoftData();
				}
			}
		} else if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
			
		} else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
			MyLog.d("app replaced");
			MyLog.d(intent.getDataString());
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
		MyLog.d("------------>" + packageString);
		MyLog.d("=============>" + intent.getPackage());
		boolean isDeleteApk = MySPEdit.getDeleteApkTag(context);
		if (!TextUtils.isEmpty(packageString) && isDeleteApk) {
			String[] packNameArray = packageString.split(":");
			if (packNameArray != null && packNameArray.length > 1) {
//				String path = DownloadDBOperator.getInstance(context).getAppDownloadPathByPackage(packNameArray[1]);
				String path = packNameArray[1]+".apk";
				DataCleanManager.deleteFile(new File(context.getFilesDir(), path));
			}
		}
	}

}
