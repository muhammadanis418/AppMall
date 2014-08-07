package cn.koolcloud.ipos.appstore.download.multithread;

public class MultiThreadUnit {
	
	/* 退出开关 */
	private boolean cancel = false;
	
	/* 线程数 */
	public DownloadThread[] threads;
	
	public boolean isCancel() {
		return cancel;
	}

	public void setCancel(boolean cancel) {
		this.cancel = cancel;
	}

}
