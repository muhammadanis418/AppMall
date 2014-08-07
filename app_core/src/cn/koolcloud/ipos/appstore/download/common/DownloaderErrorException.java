package cn.koolcloud.ipos.appstore.download.common;

/**
 * <p>Title: DownloaderErrorException.java </p>
 * <p>Description: downloader exception</p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: All In Pay</p>
 * @author 		Teddy
 * @date 		2013-12-5
 * @version 	
 */
public class DownloaderErrorException extends Exception {
    private static final long serialVersionUID = 1L;

    public DownloaderErrorException(String msg) {
        super(msg);
    }

}
