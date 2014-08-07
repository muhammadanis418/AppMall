package cn.koolcloud.ipos.appstore.receiver;

import java.io.File;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import cn.koolcloud.ipos.appstore.MainActivity;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.download.multithread.DownloadTaskReceiver;
import cn.koolcloud.ipos.appstore.fragment.CategoryRightFragment;
import cn.koolcloud.ipos.appstore.utils.Env;
import cn.koolcloud.ipos.appstore.utils.MyLog;
import cn.koolcloud.ipos.appstore.utils.ToastUtil;

public class MyDownloadReceiver extends DownloadTaskReceiver {
	private Context context;
	private NotificationManager manager;
	private NotificationCompat.Builder builder;

	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
		manager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		super.onReceive(context, intent);
	}

	@Override
	public void downloadStart(String taskPkgName) {
        MyLog.e("downloadStart:");
		super.downloadStart(taskPkgName);
	}

	@Override
	public void downloadPause(String taskPkgName) {
        MyLog.e("downloadPause:");
		super.downloadPause(taskPkgName);
	}

	@Override
	public void downloadResumed(String taskPkgName) {
        MyLog.e("downloadResumed:");
		super.downloadResumed(taskPkgName);
	}

	@Override
	public void downloadFinished(String taskPkgName) {
        MyLog.e("downloadFinished:");
		// 下载完成
		File file = new File(context.getFilesDir(), taskPkgName + ".apk");
		if (file.exists()) {
			//use common installer
			Env.install(context, file, CategoryRightFragment.GENERAL_APPS_INSTALL_REQUEST);
		} else {
			ToastUtil.showToast(context, R.string.str_apk_download_failure);
		}
		MainActivity.downMap.remove(taskPkgName);
		MainActivity.downMap.put(taskPkgName, 100);
		super.downloadFinished(taskPkgName);
		
		try {
			final int mId = Integer.parseInt(MainActivity.notifMap.get(taskPkgName).keySet().iterator().next());
			manager.cancel(mId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void downloadCanceled(String taskPkgName) {
        MyLog.e("downloadCanceled:");
		super.downloadCanceled(taskPkgName);
	}

	@Override
	public void downloadError(String taskPkgName, String error) {
        MyLog.e("downloadError:");
		super.downloadError(taskPkgName, error);
	}

	@Override
	public void progressChanged(final String taskPkgName, final int process) {
		MainActivity.downMap.put(taskPkgName, process);
        MyLog.w(taskPkgName+" percent:"+process);
		super.progressChanged(taskPkgName, process);
		// mId allows you to update the notification later on.
		try {
			final int mId = Integer.parseInt(MainActivity.notifMap.get(taskPkgName).keySet().iterator().next());
			builder = new NotificationCompat.Builder(context)
					.setSmallIcon(R.drawable.guanli_on)
					.setContentTitle(MainActivity.notifMap.get(taskPkgName).get(mId + ""))
					.setContentText(context.getResources().getText(R.string.start_download));
			builder.setAutoCancel(true);
			builder.setProgress(100, process, false).setContentText(process+"%");
			manager.notify(mId, builder.build());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
