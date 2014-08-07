package cn.koolcloud.ipos.appstore.download.multithread;

import cn.koolcloud.ipos.appstore.constant.Constants;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 接收下载状态的receiver
 */
public class DownloadTaskReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        String taskPkgName = intent.getExtras().getString(Constants.TASK_PACKAGE_NAME);
        int process = intent.getExtras().getInt(Constants.TASK_COMPLETE_PROCESS);
        String errorMes = intent.getExtras().getString(Constants.TASK_ERROR_MES);
        
        if (action.equals(Constants.ACTION_TASK_STARTED)) {
            downloadStart(taskPkgName);
        } else if (action.equals(Constants.ACTION_TASK_PAUSED)) {
            downloadPause(taskPkgName);
        } else if (action.equals(Constants.ACTION_TASK_FINISHED)) {
            downloadFinished(taskPkgName);
        } else if (action.equals(Constants.ACTION_TASK_CANCEL)) {
            downloadCanceled(taskPkgName);
        } else if (action.equals(Constants.ACTION_TASK_UPDATED)) {
            progressChanged(taskPkgName, process);
        } else if (action.equals(Constants.ACTION_TASK_ERROR)) {
            downloadError(taskPkgName, errorMes);
        }
    }

    /**
     * 下载开始
     *
     * @param taskId
     */
    public void downloadStart(String taskPkgName) {
    }

    /**
     * 通知下载暂停
     */
    public void downloadPause(String taskPkgName) {
    }

    /**
     * 通知下载继续
     */
    public void downloadResumed(String taskPkgName) {
    }

    /**
     * 通知下载完成
     */
    public void downloadFinished(String taskPkgName) {
    }

    /**
     * 通知下载取消
     */
    public void downloadCanceled(String taskPkgName) {
    }

    /**
     * 通知下载进度变化
     *
     * @param downloadedBytes
     */
    public void progressChanged(String taskPkgName, int process) {
    }

    /**
     * 通知下载错误
     *
     * @param error
     */
    public void downloadError(String taskPkgName, String error) {
    }
}
