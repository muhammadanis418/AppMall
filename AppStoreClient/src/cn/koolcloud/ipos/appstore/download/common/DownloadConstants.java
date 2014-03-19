package cn.koolcloud.ipos.appstore.download.common;

/**
 * <p>Title: EngineConstants.java </p>
 * <p>Description: Global constant, including file status</p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: All In Pay</p>
 * @author 		Teddy
 * @date 		2013-12-5
 * @version 	
 */
public class DownloadConstants {
    public static final int DOWNLOAD_STATE_INIT = -1;             // initialized status
    public static final int DOWNLOAD_STATE_ERROR = -2;            // error
    public static final int DOWNLOAD_STATE_WAITTING = 21;         // waiting list 
    public static final int DOWNLOAD_STATE_DOWNLOADING = 22;      // downloading
    public static final int DOWNLOAD_STATE_PAUSE = 23;            // user pause the download
    public static final int DOWNLOAD_STATE_INTERRUPT = 24;        // paused by special reasons (bad network)
    public static final int DOWNLOAD_STATE_STOP = 25;             // pause
    public static final int DOWNLOAD_STATE_DONE = 26;             // finish
    
    public static final int DOWNLOAD_NETWORK_ALL = 0;             // any network allow to download
    public static final int DOWNLOAD_NETWORK_ONLYWIFI = 1;        // only allowed in with wifi
    
    public static final int NETWORK_STATE_ERROR = -1;             // get network status error
    public static final int NETWORK_STATE_NO_CONNECTION = 0;      // network not connected
    public static final int NETWORK_STATE_WIFI = 1;               // current network is wifi
    public static final int NETWORK_STATE_3G = 2;                 // current network is 3g
}
