package cn.koolcloud.ipos.appstore.download.providers;

import cn.koolcloud.ipos.appstore.download.entity.DownloadBean;

public interface DownloadEngineCallback {
    /**
     * information call back
     * @param state
     * @param bean
     * @param info
     */
    public void callbackWhenDownloadTaskListener(int state, DownloadBean bean, String info);

}
