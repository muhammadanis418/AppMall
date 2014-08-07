package cn.koolcloud.ipos.appstore.download.multithread;

import cn.koolcloud.ipos.appstore.utils.MyLog;

public class FinishDownloadState extends DownloadState {

	@Override
	public void Handle(DownloadContext context, String pkgname, String paramJson, int versionCode) {
		MyLog.d("The Current State is : Download_FinishState");
		//触发当前行为
		Listener listener = new DownloadListener();
		listener.handle(new FinishEvent(), pkgname, paramJson, versionCode);
	}

}
