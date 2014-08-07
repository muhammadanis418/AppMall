package cn.koolcloud.ipos.appstore.download.multithread;


public abstract class Listener {

	public Listener() {
		
	}

	public abstract void handle(Event myEvent, String pkgName, String paramJson, int versionCode);
}
