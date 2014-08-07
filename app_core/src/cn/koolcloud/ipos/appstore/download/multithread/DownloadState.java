package cn.koolcloud.ipos.appstore.download.multithread;

public abstract class DownloadState {
	
	public abstract void Handle(DownloadContext context, String pkgname, String paramJson, int versionCode);

}
