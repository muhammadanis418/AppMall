package cn.koolcloud.ipos.appstore.download.common;

import java.io.File;

import cn.koolcloud.ipos.appstore.cache.FileManager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

public class DownloadUtil {
    private final static String TAG = "DownloadUtil";
    private static DownloadUtil eu;

    public static DownloadUtil getInstance() {
        if (eu == null) {
            eu = new DownloadUtil();
        }
        return eu;
    }

    /**
     * @Title: getNetworkType
     * @Description: get network type
     * @return -1:error, 0:not connected, 1:wifi, 2:3G
     * @return: int
     */
    public int getNetworkType() {
        try {
            ConnectivityManager connectivity = (ConnectivityManager) DownloadVariable.g_Context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null && info.isConnected()) {
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                            return DownloadConstants.NETWORK_STATE_WIFI;
                        } else {
                            return DownloadConstants.NETWORK_STATE_3G;
                        }
                    } else {
                        return DownloadConstants.NETWORK_STATE_NO_CONNECTION;
                    }
                } else {
                    return DownloadConstants.NETWORK_STATE_NO_CONNECTION;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getNetworkType ERROR:" + e);
            return DownloadConstants.NETWORK_STATE_ERROR;
        }
        return DownloadConstants.NETWORK_STATE_ERROR;
    }

    /**
     * @Title: getFreeSpaceAtDirectory
     * @Description: get free space at directory
     * @param directoryPath
     * @return
     * @return: long
     */
    public long getFreeSpaceAtDirectory(String directoryPath) {
        File path = new File(directoryPath);
        if (!path.exists()) {
            return -1;
        }

        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }
    
    public static String getAbsoluteFilePath(Context ctx, String fileName) {
    	String absFileName = "";
    	
    	//don't download apk file to sd from 2014-01-28
		/*if (FileManager.canWriteSD()) {
				
			absFileName = Environment.getExternalStorageDirectory() + "/download/" + fileName;
		} else {*/
			absFileName = ctx.getFileStreamPath(fileName).getAbsolutePath();
		//}
		
		return absFileName;
    }
}
