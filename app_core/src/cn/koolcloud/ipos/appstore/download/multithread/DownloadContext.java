package cn.koolcloud.ipos.appstore.download.multithread;

public class DownloadContext {

    private DownloadState state;

	public DownloadContext(DownloadState state)
    {
        this.state = state;
    }

    public void Request(String pkgname, String paramJson, int versionCode)
    {
        state.Handle(this, pkgname, paramJson, versionCode);
    }
    
    public DownloadState getState() {
		return state;
	}

	public void setState(DownloadState state) {
		this.state = state;
	}

}
