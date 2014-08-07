package cn.koolcloud.ipos.appstore.download.multithread;

import cn.koolcloud.ipos.appstore.utils.MyLog;

public class StartDownloadState extends DownloadState {

	@Override
	public void Handle(DownloadContext context, String pkgname, String paramJson, int versionCode) {
		MyLog.d("The Current State is : Download_StartState");
		//Start -> Pause
		//设置下一个状态
		context.setState(new PauseDownloadState());
		//触发当前行为
		Listener listener = new DownloadListener();
		listener.handle(new StartEvent(), pkgname, paramJson, versionCode);
	}

}
