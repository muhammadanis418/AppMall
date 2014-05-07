package cn.koolcloud.ipos.appstore.download.providers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.json.JSONObject;

import android.content.Context;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;

import cn.koolcloud.ipos.appstore.AppStorePreference;
import cn.koolcloud.ipos.appstore.api.ApiService;
import cn.koolcloud.ipos.appstore.api.HttpService;
import cn.koolcloud.ipos.appstore.common.AsyncHttpClient;
import cn.koolcloud.ipos.appstore.download.common.DownloadConstants;
import cn.koolcloud.ipos.appstore.download.common.DownloadUtil;
import cn.koolcloud.ipos.appstore.download.common.DownloadVariable;
import cn.koolcloud.ipos.appstore.download.common.DownloaderErrorException;
import cn.koolcloud.ipos.appstore.download.database.DownloadDBOperator;
import cn.koolcloud.ipos.appstore.download.entity.DownloadBean;
import cn.koolcloud.ipos.appstore.utils.Logger;

public class Downloader {
    private final static String TAG = "Downloader";
    private final static byte[] lock_getFileSize = new byte[1];
    private final static byte[] lock_refresh_progress = new byte[1];

    private int mThreadCount = 4;				//default sub thread num = 4
    private int bufferSize = 1024 * 16; 		//one block is 16K

    private DownloadBean mBean;					// this is downloader's bean not sub bean
    private Context mContext;
    private DownloadEngineCallback mCallback;
    private DownloadDBOperator mDBOper;
    private int mDoneThreadCount = 0;			// finished thread num
    private int mState = DownloadConstants.DOWNLOAD_STATE_INIT;		// downloader status
    private ArrayList<DownloadBean> mBeans;

    public Downloader(DownloadBean bean, Context context,
            DownloadEngineCallback callback) {
        this.mBean = bean;
        this.mContext = context;
        this.mCallback = callback;
        this.mDBOper = DownloadDBOperator.getInstance(context);
        this.mBeans = new ArrayList<DownloadBean>(mThreadCount);

    }
    
    public void setDownloadBean(DownloadBean bean) {
    	this.mBean = bean;
    }
    
    public void initBeanInDataBase() {
    	try {
			if (this.mDBOper != null) {
				//if this task is exist in database
				if (this.mDBOper.isHasDownloadTaskByUrl(mBean.url,
						mBean.downloadId)) {
					getDownloaderInfoFromDB(mBean);
				} else {	// insert information to database
					addDownloaderInfoToDB(mBean);
				}
			} else {
				callBackError("Downloader error, DBOperator may be null.");
				throw new DownloaderErrorException(
						"Downloader error, DBOperator may be null.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public void setDownloadEngineCallback(DownloadEngineCallback callBack) {
    	this.mCallback = callBack;
    }

    public DownloadBean getDownloaderInfo() {
        return mBean;
    }

    public int getDownloaderState() {
        return mState;
    }

    /**
     * request initialization
     * @param state
     */
    protected void setDownloaderState(int state) {
        mState = state;
        if (state == DownloadConstants.DOWNLOAD_STATE_INIT) {
            mBean.currentPosition = 0;
        }
    }

    /**
     * Add download information to database, 
     * used for just initialing downloader and this task not exist in database
     * 
     * @param bean
     * @throws DownloaderErrorException
     */
    private void addDownloaderInfoToDB(DownloadBean bean)
            throws DownloaderErrorException {
        /*if (mState != DownloadConstants.DOWNLOAD_STATE_INIT
                && mState != DownloadConstants.DOWNLOAD_STATE_STOP
                && mState != DownloadConstants.DOWNLOAD_STATE_ERROR) {
            callBackError("This task is already in database");
            throw new DownloaderErrorException("This task is already in database");
        }*/

        if (mDBOper != null) {
            long fileSize = bean.fileSize;
            if (mBeans.size() > 0) {
                mBeans.clear();
            }

            try {
            	//check file size. no need to access the network and init sub threads directly when size > 0
                if (fileSize > 0) {
                    if (!hasSpaceInSDCard()) {
                        return;
                    }
                    
                    //split file to sub blocks
                    long range = fileSize / mThreadCount;
                    for (int i = 0; i < mThreadCount - 1; i++) {
                        DownloadBean subBean = (DownloadBean) bean.clone();
                        subBean.threadId = i;
                        subBean.startPosition = i * range;
                        subBean.endPosition = (i + 1) * range - 1;
                        mBeans.add(subBean);
                    }

                    DownloadBean subBean = (DownloadBean) bean.clone();
                    subBean.threadId = mThreadCount - 1;
                    subBean.startPosition = (mThreadCount - 1) * range;
                    subBean.endPosition = fileSize - 1;
                    mBeans.add(subBean);
                } else {// init N sub downloaders directly with size 0, when file size = 0
                    for (int n = 0; n < mThreadCount - 1; n++) {
                        DownloadBean subBean = (DownloadBean) bean.clone();
                        subBean.threadId = n;
                        mBeans.add(subBean);
                    }

                    DownloadBean subBean = (DownloadBean) bean.clone();
                    subBean.threadId = mThreadCount - 1;
                    mBeans.add(subBean);
                }

                mDBOper.addDownloadTask(mBeans);
              //set to waiting status when the file size already got.
                if (bean.fileSize > 0) {
                    mState = DownloadConstants.DOWNLOAD_STATE_WAITTING;	// downloader is waiting
                } else {// to get file size from network and update sub downloaders when file size not got yet
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            boolean flag = false;
                            synchronized (lock_getFileSize) {
                                flag = getFileSizeByNetwork(mBean);
                            }
                            if (flag) {
                                mState = DownloadConstants.DOWNLOAD_STATE_WAITTING;
                            } else {
                                Log.e(TAG, "get file size error from network 1");
                            }
                        }
                    }).start();

                }
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        } else {
            callBackError("add Downloader Info To DB error, DBOperator maybe null");
            throw new DownloaderErrorException(
                    "add Downloader Info To DB error, DBOperator maybe null");
        }
    }

    /**
     * get downloader information from database
     * 
     * @param bean
     * @throws DownloaderErrorException
     */
    private void getDownloaderInfoFromDB(DownloadBean bean)
            throws DownloaderErrorException {
        if (mDBOper != null) {
            mBeans.clear();
            mBeans = mDBOper.getDownloadTaskByUrl(bean.url, bean.downloadId);

            mBean.currentPosition = 0;
            mBean.fileSize = 0;
            mThreadCount = mBeans.size();
            for (DownloadBean subBean : mBeans) {
                mBean.currentPosition += subBean.currentPosition;
                if (subBean.fileSize > mBean.fileSize) {
                    mBean.fileSize = subBean.fileSize;
                }
            }

            if (mBean.fileSize < 1) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean flag = false;
                        synchronized (lock_getFileSize) {
                            flag = getFileSizeByNetwork(mBean);
                        }
                        if (flag) {
                            mState = DownloadConstants.DOWNLOAD_STATE_WAITTING;
                        } else {
                            Log.e(TAG, "get file size from network error 2");
                        }
                    }
                }).start();
            } else {
                mState = DownloadConstants.DOWNLOAD_STATE_WAITTING;
            }
        } else {
            callBackError("getDownloaderInfoFromDB Error,May be EngineDBOperator is Null.");
            throw new DownloaderErrorException(
                    "getDownloaderInfoFromDB Error,May be EngineDBOperator is Null.");
        }
    }

    /**
     * @Title: getFileSizeByNetwork
     * @Description: get file size from network and update listBeans
     * @param bean
     * @return
     * @return: boolean
     */
    private boolean getFileSizeByNetwork(DownloadBean bean) {
//        HttpURLConnection connection = null;
        long fileSize = bean.fileSize;
        try {
        	//resolve android.os.NetworkOnMainThreadException
        	if (android.os.Build.VERSION.SDK_INT > 9) {
        	    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        	    StrictMode.setThreadPolicy(policy);
        	}
        	
        	String terminalId = AppStorePreference.getTerminalID(mContext);
        	JSONObject downloadJson = ApiService.getDownloadFileJson(terminalId, bean.downloadId);
        	//get file size from network if file size is not initialized
            if (fileSize <= 0) {
    			HttpService httpService = new HttpService();
    			int statusCode = -1;
    			HttpResponse response = httpService.getResponseResult(bean.url, downloadJson, "post", null);
    			
    			if (response != null) {
    				statusCode = response.getStatusLine().getStatusCode();
    				Logger.d("==statusCode==" + statusCode);
    				if (statusCode == 200) {
    					return checkFileSize(response.getEntity().getContentLength());
    				} else if ((statusCode == HttpStatus.SC_MOVED_PERMANENTLY) ||
    			            (statusCode == HttpStatus.SC_MOVED_TEMPORARILY) ||
    			            (statusCode == HttpStatus.SC_SEE_OTHER) ||
    			            (statusCode == HttpStatus.SC_TEMPORARY_REDIRECT)) {
    					String newUrl = response.getLastHeader("Location").getValue();
    					HttpResponse newResponse = httpService.getResponseResult(newUrl, downloadJson, "post", null);
    					statusCode = newResponse.getStatusLine().getStatusCode();
                    	if (newResponse != null) {
                    		if (statusCode == 200) {
                    			return checkFileSize(newResponse.getEntity().getContentLength());
                    		}
                    	}
                    }
    			} 

                callBackError("http return code error:" + statusCode);
                return false;
                
            } else {// exit directly when file size > 0
                return true;
            }
        } catch (Exception e) {
            callBackError("Time out when get file size from server");
            e.printStackTrace();
        }
        return false;
    }
    
    private boolean checkFileSize(long fileSize) throws DownloaderErrorException {
    	// get file size
//        fileSize = response.getEntity().getContentLength();
        mBean.fileSize = fileSize;

        if (fileSize <= 0) {
            callBackError("Can't get file size from server:" + fileSize);
            return false;
        }

        //there is no free space both in sdcard and internal
        if (!hasSpaceInSDCard()) {
            return false;
        }

        long range = fileSize / mThreadCount;
        // update listBean
        for (int i = 0; i < mThreadCount - 1; i++) {
            DownloadBean subBean = mBeans.get(i);
            subBean.fileSize = fileSize;
            subBean.startPosition = i * range;
            subBean.endPosition = (i + 1) * range - 1;
        }

        DownloadBean subBean = mBeans.get(mThreadCount - 1);
        subBean.fileSize = fileSize;
        subBean.startPosition = (mThreadCount - 1) * range;
        subBean.endPosition = fileSize - 1;

        // update database
        if (mDBOper != null) {
            mDBOper.updateTaskCompleteSize(mBeans, mBean.url);
        } else {
            callBackError("getFileSizeByNetwork error£¬Maybe EngineDBOperator is Null.");
            throw new DownloaderErrorException(
                    "getFileSizeByNetwork error£¬Maybe EngineDBOperator is Null.");
        }
        return true;
    }

    /**
     * @Title: startDownloader
     * @Description: start download, can invoke more than one time
     * @throws DownloaderErrorException
     * @return: void
     */
    public void startDownloader() throws DownloaderErrorException {
    	
    	//exit when is downloading
        if (mState == DownloadConstants.DOWNLOAD_STATE_DOWNLOADING) {
            return;
        }

        if (mBean == null) {
            callBackError("Downloader is not initialized.");
            return;
        }

        File file = new File(mBean.savePath);
        File parentDirectory = file.getParentFile();
        if (!parentDirectory.exists()) {
            parentDirectory.mkdirs();
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        initBeanInDataBase();
        
        //Forbade for clearing the mbeans list, when error occurred, but restart the task, so to reinitialize mBeans
        if (mBeans.size() < 1) {
            try {
                addDownloaderInfoToDB(mBean);
            } catch (DownloaderErrorException e) {
                e.printStackTrace();
                return;
            }
        }

        /**
         * start to download only when got file size
         */
        synchronized (lock_getFileSize) {
        	//get file size error, try it again.
            if (mState == DownloadConstants.DOWNLOAD_STATE_INIT) {
                boolean flag = getFileSizeByNetwork(mBean);
                if (!flag) {
                    callBackError("get file size error");
                    return;
                }
            }
            
            //add new by teddy on 6th December start
            if (mState == DownloadConstants.DOWNLOAD_STATE_PAUSE) {
            	if (mDBOper.isHasDownloadTaskByUrl(mBean.url, mBean.downloadId)) {
            		try {
						getDownloaderInfoFromDB(mBean);
					} catch (DownloaderErrorException e) {
						e.printStackTrace();
					}
            	}
            }
            
          //add new by teddy at 6th December end
        }

        mState = DownloadConstants.DOWNLOAD_STATE_DOWNLOADING;
        mDBOper.removePauseFileByUrl(mBean.url, mBean.downloadId);// remove from pause table
        mDoneThreadCount = 0;// init thread number

        for (DownloadBean bean : mBeans) {
        	//the thread belongs to unfinished task
            if (bean.currentPosition < (bean.endPosition - bean.startPosition)) {
                HamalThread hamalThread = new HamalThread(bean);
                hamalThread.start();
            } else {// no need to recreate thread when it is finished
                mDoneThreadCount++;
            }
        }
        
        //complete download
        if (mDoneThreadCount == mThreadCount) {
            downloaderDone();
        }
    }

    private class HamalThread extends Thread {
        private int threadId;
        private long startPos;
        private long endPos;
        private long compeleteSize;
        private String urlStr;
        private String downloadId;
        private JSONObject downloadJson;

        public HamalThread(DownloadBean bean) {
            this.threadId = bean.threadId;
            this.startPos = bean.startPosition;
            this.endPos = bean.endPosition;
            this.compeleteSize = bean.currentPosition;
            this.urlStr = bean.url;
            this.downloadId = bean.downloadId;
            
            String terminalId = AppStorePreference.getTerminalID(mContext);
            this.downloadJson = ApiService.getDownloadFileJson(terminalId, downloadId);
        }

        @Override
        public void run() {
            RandomAccessFile randomAccessFile = null;
            InputStream is = null;
            try {
            	HttpService httpService = new HttpService();
            	
    			//multi threads download
    			Header headerSize = null;
                if (mThreadCount > 1) {
                    // set range (Range£ºbytes x-y)
                	headerSize = new BasicHeader("Range", "bytes=" + (startPos + compeleteSize) + "-" + endPos);
                	Logger.d(headerSize.getValue());
                }
                HttpResponse response = httpService.getResponseResult(urlStr, downloadJson, "post", headerSize);

                randomAccessFile = new RandomAccessFile(mBean.savePath, "rwd");
                randomAccessFile.seek(startPos + compeleteSize);
                
                if (response != null) {
                	int statusCode = response.getStatusLine().getStatusCode();
    				Logger.d("==statusCode==" + statusCode);
    				
    				if (statusCode == 200 || statusCode == 206) {
    					 // write downloaded file to the folder
    	                is = new BufferedInputStream(response.getEntity().getContent());
    	                Logger.d("sub thread get block size:" + response.getEntity().getContentLength());
    	                byte[] buffer = new byte[bufferSize];
    	                int length = -1;

    	                while ((length = is.read(buffer)) != -1) {

    	                    randomAccessFile.write(buffer, 0, length);
    	                    compeleteSize += length;
    	                    synchronized (lock_refresh_progress) {
    	                        mBean.currentPosition += length;
    	                    }
    	                    // update download information to database
    	                    mDBOper.updateTaskCompleteSize(threadId, compeleteSize,
    	                            urlStr, downloadId);
    	                    // stop
    	                    if (mState == DownloadConstants.DOWNLOAD_STATE_PAUSE
    	                            || mState == DownloadConstants.DOWNLOAD_STATE_INTERRUPT
    	                            || mState == DownloadConstants.DOWNLOAD_STATE_STOP
    	                            || mState == DownloadConstants.DOWNLOAD_STATE_ERROR) {
    	                        return;
    	                    }
    	                }

    	                // sub thread downloading finished
    	                mDoneThreadCount++;
    				} else if ((statusCode == HttpStatus.SC_MOVED_PERMANENTLY) ||
    			            (statusCode == HttpStatus.SC_MOVED_TEMPORARILY) ||
    			            (statusCode == HttpStatus.SC_SEE_OTHER) ||
    			            (statusCode == HttpStatus.SC_TEMPORARY_REDIRECT)) {
    					urlStr = response.getLastHeader("Location").getValue();
    					Logger.d("==redirect download url==" + urlStr);
    					HttpResponse newResponse = httpService.getResponseResult(urlStr, downloadJson, "post", headerSize);
    					
    					if (newResponse != null) {
    						statusCode = newResponse.getStatusLine().getStatusCode();
    						Logger.d("==statusCode==" + statusCode);
    						if (statusCode == 200 || statusCode ==206) {
    							 // write downloaded file to the folder
    	    	                is = new BufferedInputStream(newResponse.getEntity().getContent());
    	    	                Logger.d("sub thread get block size:" + newResponse.getEntity().getContentLength());
    	    	                byte[] buffer = new byte[bufferSize];
    	    	                int length = -1;

    	    	                while ((length = is.read(buffer)) != -1) {

    	    	                    randomAccessFile.write(buffer, 0, length);
    	    	                    compeleteSize += length;
    	    	                    synchronized (lock_refresh_progress) {
    	    	                        mBean.currentPosition += length;
    	    	                    }
    	    	                    // update download information to database
    	    	                    mDBOper.updateTaskCompleteSize(threadId, compeleteSize,
    	    	                            urlStr, downloadId);
    	    	                    // stop
    	    	                    if (mState == DownloadConstants.DOWNLOAD_STATE_PAUSE
    	    	                            || mState == DownloadConstants.DOWNLOAD_STATE_INTERRUPT
    	    	                            || mState == DownloadConstants.DOWNLOAD_STATE_STOP
    	    	                            || mState == DownloadConstants.DOWNLOAD_STATE_ERROR) {
    	    	                        return;
    	    	                    }
    	    	                }

    	    	                // sub thread downloading finished
    	    	                mDoneThreadCount++;
    						} else {
    							Log.e(TAG, "Connection is interrupted while downloading...");
    			                interruptDownloader();
    						}
    					} else {
    						Log.e(TAG, "Connection is interrupted while downloading...");
    		                interruptDownloader();
    					}
    				}
                }
               
            } catch (Exception e) {
                Log.e(TAG, "Connection is interrupted while downloading...");
                interruptDownloader();
                e.printStackTrace();
            } finally {
                try {
                	if (is != null) {
                		is.close();
                	}
                    randomAccessFile.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (mDoneThreadCount == mThreadCount) {
                downloaderDone();
            }
        }
    }

    /**
     * @Title: getProgress
     * @Description: get download progress
     * @return
     * @return: int
     */
    public int getProgress() {
        if (mBean == null || mBean.fileSize < 1) {
            return 0;
        }
        return (int) (mBean.currentPosition * 100 / mBean.fileSize);
    }

    /**
     * pause download
     */
    public void pauseDownloader() {
    	
        try {
        	new Thread() {

				@Override
				public void run() {
					mState = DownloadConstants.DOWNLOAD_STATE_PAUSE;
					if (null != mBean) {
						mDBOper.addPauseFile(mBean.url, mBean.packageName, mBean.fileId, mBean.downloadId);
					}
				}
        		
        	}.start();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
     * pause download£¨not by user£©
     */
    private void interruptDownloader() {
        mState = DownloadConstants.DOWNLOAD_STATE_INTERRUPT;
    }

    /**
     * finish download
     */
    public void stopDownloader() {
        mState = DownloadConstants.DOWNLOAD_STATE_STOP;
        mBean.currentPosition = 0;
        removeDownloaderInfo(mBean.url, mBean.downloadId);
    }

    /**
     * remove download info
     * 
     * @param urlstr
     */
    private void removeDownloaderInfo(String urlstr, String downloadId) {
        mDBOper.deleteDownloadTaskByUrl(urlstr, downloadId);
        mDBOper.removePauseFileByUrl(urlstr, downloadId);
        mBeans.clear();
    }

    /**
     * download done
     */
    private void downloaderDone() {
        mState = DownloadConstants.DOWNLOAD_STATE_DONE;
        mBean.doneTime = System.currentTimeMillis();
        mCallback.callbackWhenDownloadTaskListener(mState, mBean,
                mBean.fileName + "download successfull");

        removeDownloaderInfo(mBean.url, mBean.downloadId);
        //save finished info save to database
        mDBOper.addCompleteTask(mBean);
    }

    /**
     * call back when error occurred
     * 
     * @param info
     */
    private void callBackError(String info) {
        mState = DownloadConstants.DOWNLOAD_STATE_ERROR;
        mCallback.callbackWhenDownloadTaskListener(mState, mBean, info);
        removeDownloaderInfo(mBean.url, mBean.downloadId);
    }

    /**
     * Check if there is enough space
     */
    private boolean hasSpaceInSDCard() {
        /*if (mBean.fileSize > DownloadUtil.getInstance().getFreeSpaceAtDirectory(
                Environment.getExternalStorageDirectory().getAbsolutePath())) {*/
        if (mBean.fileSize > DownloadUtil.getInstance().getFreeSpaceAtDirectory(mBean.savePath)) {
            callBackError("There is no enough space.");
            return false;
        }
        return true;
    }
}
