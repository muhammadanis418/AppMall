package cn.koolcloud.ipos.appstore.download.multithread;

import cn.koolcloud.ipos.appstore.utils.MyLog;

public class PauseDownloadState extends DownloadState {

	@Override
	public void Handle(DownloadContext context, String pkgname, String paramJson, int versionCode) {
		MyLog.d("The Current State is : Download_PauseState");
		//Pause -> Start
		//设置下一个状态
		context.setState(new StartDownloadState());
		//触发当前行为
		Listener listener = new DownloadListener();
		listener.handle(new PauseEvent(), pkgname, paramJson, versionCode);
	}

}
