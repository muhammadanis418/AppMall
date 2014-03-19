package cn.koolcloud.ipos.appstore.download.entity;

import java.io.File;

import android.content.Context;

import cn.koolcloud.ipos.appstore.download.database.DownloadDBOperator;

/**
 * <p>Title: DownloadBean.java </p>
 * <p>Description: downloader entity</p>
 * <p>describe one file or sub file</p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: All In Pay</p>
 * @author 		Teddy
 * @date 		2013-12-5
 * @version 	
 */
public class DownloadBean implements Cloneable {
    public String url; 				// download address
    public String fileName; 		// file name
    public String savePath; 		// save path
    public long fileSize; 			// file size
    public long startPosition; 		// file block start position
    public long endPosition; 		// file block end position
    public long currentPosition; 	// block current progress
    public int threadId; 			// block id
    public int versionCode; 		// software version code
    public String fileVersion; 		// file version
    public int fileVersionCode; 	// file version code
    public String packageName; 		// package name
    public String iconUrl; 			// icon url
    public String fileId; 			// unique tag
    public String downloadId; 		// file download id
    public long doneTime; 			// finished time
    
    //DownloadBean status fields
    public boolean flag = false;					//refresh progress bar tag
    public long startTime;
    public boolean isAPKDownloading = false;		//the tag for checking if the file is downloading
    public File downloadedFile;
    public int installedStatus;

    /**
     * @Title: deleteFromDoneDB
     * @Description: delete from complete table
     * @param context
     * @return
     * @return: boolean
     */
    public boolean deleteFromDoneDB(Context context) {
        return DownloadDBOperator.getInstance(context).deleteCompleteTaskByUrl(url, downloadId);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "url:" + url + "," + "download_id:" + downloadId + ","
        		+ " fileName:" + fileName + "," + " savePath:" + savePath + ","
                + " fileSize:" + fileSize + "," + " startP:" + startPosition + ","
                + " endP:" + endPosition + "," + " cuP:" + currentPosition + ","
                + " threadId:" + threadId  + "," + " v:" + fileVersion + "," + " vc:"
                + fileVersionCode + "," + " pn:" + packageName + "," + " iu:" + iconUrl + ","
                + " id:" + fileId + "," + " dtime:" + doneTime;
    }
}
