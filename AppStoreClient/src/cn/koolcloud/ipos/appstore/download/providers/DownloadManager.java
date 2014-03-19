package cn.koolcloud.ipos.appstore.download.providers;

import android.content.Context;

import cn.koolcloud.ipos.appstore.download.common.DownloadConstants;
import cn.koolcloud.ipos.appstore.download.common.DownloadVariable;

public class DownloadManager {
    private static DownloadManager dm;

    public static DownloadManager getInstance(Context context) {
        if (dm == null) {
            dm = new DownloadManager(context);
        }
        return dm;
    }

    public DownloadManager(Context context) {
        DownloadVariable.g_Context = context;
    }

    /**
     * 支持最大任务数为3个任务
     * 
     * @param count
     */
    public void setMaxTaskCount(int count) {
        if (count > 3) {
            DownloadVariable.MAX_TASK_COUNT = 3;
        } else if (count < 0) {
            DownloadVariable.MAX_TASK_COUNT = 0;
        } else {
            DownloadVariable.MAX_TASK_COUNT = count;
        }
    }

    /**
     * 设置网络状态，默认是任何网络都可以下载。0：任何网络都可以下载，1：仅使用wifi网络下载
     */
    public void setDownloadNetwork(int networkType) {
        switch (networkType) {
        case DownloadConstants.DOWNLOAD_NETWORK_ALL:
            DownloadVariable.SUPPORT_NETWORK_TYPE = DownloadConstants.DOWNLOAD_NETWORK_ALL;
            break;
        case DownloadConstants.DOWNLOAD_NETWORK_ONLYWIFI:
            DownloadVariable.SUPPORT_NETWORK_TYPE = DownloadConstants.DOWNLOAD_NETWORK_ONLYWIFI;
            break;
        default:
            DownloadVariable.SUPPORT_NETWORK_TYPE = DownloadConstants.DOWNLOAD_NETWORK_ALL;
        }
    }

    /**
     * 加入任务
     */
    public void addDownloadTask() {

    }

    /**
     * 删除任务
     */
    public void deleteDownloadTask() {

    }

    /**
     * 暂停任务
     */
    public void pauseDownloadTask() {

    }

}
