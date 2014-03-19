package cn.koolcloud.ipos.appstore.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.koolcloud.ipos.appstore.AppStoreApplication;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.api.ApiService;
import cn.koolcloud.ipos.appstore.cache.ImageDownloader;
import cn.koolcloud.ipos.appstore.constant.Constants;
import cn.koolcloud.ipos.appstore.download.common.DownloadConstants;
import cn.koolcloud.ipos.appstore.download.common.DownloadUtil;
import cn.koolcloud.ipos.appstore.download.common.DownloaderErrorException;
import cn.koolcloud.ipos.appstore.download.database.DownloadDBOperator;
import cn.koolcloud.ipos.appstore.download.entity.DownloadBean;
import cn.koolcloud.ipos.appstore.download.providers.DownloadEngineCallback;
import cn.koolcloud.ipos.appstore.download.providers.Downloader;
import cn.koolcloud.ipos.appstore.entity.App;
import cn.koolcloud.ipos.appstore.fragment.tab.NormalListFragment;
import cn.koolcloud.ipos.appstore.utils.ConvertUtils;
import cn.koolcloud.ipos.appstore.utils.Env;
import cn.koolcloud.ipos.appstore.utils.Logger;
import cn.koolcloud.ipos.appstore.utils.Utils;


/**
 * <p>Title: GeneralAppsListAdapter.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: All In Pay</p>
 * @author 		Teddy
 * @date 		2013-11-8
 * @version 	
 */
public class UpdateAppsListAdapter extends BaseAdapter implements DownloadEngineCallback {
	private static final String TAG = "GeneralAppsListAdapter";
	public static final int GENERAL_APPS_INSTALL_REQUEST = 1;
	private static final int HANDLE_DOWNLOAD_PROCESS = 2;
	private static final int HANDLE_DOWNLOAD_PROCESS_DONE = 3;
	private static final int HANDLE_START_NEW_PROCESS = 4;
	
	private List<App> dataList = new ArrayList<App>();
	private Activity ctx;
	private LayoutInflater mInflater;
	private AppStoreApplication mApplication;
	private Handler mHandler;
	
	private String savePath;
	
	//break point download fields
	private Downloader downloader;					//file downloader
	
	private DownloadDBOperator mDBOper;
	private Map<Integer, AppItemViewHolder> holderMap = new HashMap<Integer, AppItemViewHolder>();			//save every item
	private Map<Integer, DownloadBean> downloadBeanMap = new HashMap<Integer, DownloadBean>();				//save every download bean
	private Map<String, Integer> posMap = new HashMap<String, Integer>();									//save position with download_id key and position value when download finished
	private Queue<DownloadBean> queueTaskList = new LinkedList<DownloadBean>();
	private boolean isRunningDownload = false;
	
	public UpdateAppsListAdapter(Activity context, List<App> dataSource, Handler handler, AppStoreApplication application) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);	
		this.ctx = context;
		dataList = dataSource;
		this.mApplication = application;
		downloader = new Downloader(null, application, null);
		mDBOper = DownloadDBOperator.getInstance(application);
		mHandler = handler;
		downloader.setDownloadEngineCallback(this);
	}

	@Override
	public int getCount() {
		return dataList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return dataList.get(arg0);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		AppItemViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.update_software_list_item, null);
			holder = new AppItemViewHolder();
			holder.rootRelativeLayout = (RelativeLayout) convertView.findViewById(R.id.general_software_list_item);
			holder.progressLinearLayout = (LinearLayout) convertView.findViewById(R.id.process_bar_layout);
			holder.progressBar = (ProgressBar) convertView.findViewById(R.id.processbar);
			holder.shadeImageView = (ImageView) convertView.findViewById(R.id.software_icon);
			holder.hdImageView = (ImageView) convertView.findViewById(R.id.hd_tag);
			holder.firstRelImageView = (ImageView) convertView.findViewById(R.id.first_rel);
			holder.downloadButton = (Button) convertView.findViewById(R.id.download_bt);
			holder.ignoreTextView = (TextView) convertView.findViewById(R.id.ignore_bt);							
			holder.softwareName = (TextView) convertView.findViewById(R.id.software_item_name);							
			holder.softwareVersionTextView = (TextView) convertView.findViewById(R.id.software_version);						
			holder.softwareVersionNameTextView = (TextView) convertView.findViewById(R.id.software_version_name);						
			holder.dividerTextView = (TextView) convertView.findViewById(R.id.software_update_divider);					
			holder.releaseDateTextView = (TextView) convertView.findViewById(R.id.software_release_date);					
			holder.softwareSizeTextView = (TextView) convertView.findViewById(R.id.software_size);					
			holder.softwarePatchSizeTextView = (TextView) convertView.findViewById(R.id.software_patch_size);					
			holder.mergeApkTextView = (TextView) convertView.findViewById(R.id.mergeapk_tv);				
			holder.downloadProcessTextView = (TextView) convertView.findViewById(R.id.download_process);			
			holder.newFeatureTextView = (TextView) convertView.findViewById(R.id.new_feature);	
			holder.newFeatureDetailTextView = (TextView) convertView.findViewById(R.id.new_feature_detail);
			convertView.setTag(holder);
		} else {
			holder = (AppItemViewHolder) convertView.getTag();
		}

		final App appInfo = dataList.get(position);
		
		//save holder in map then to update by thread
		holderMap.put(position, holder);
		//init download info
		initDownloadComponents(appInfo, position);
		setDownloadButtonStatus(holder, appInfo);
		
		holder.softwareName.setText(appInfo.getName());
		holder.softwareVersionNameTextView.setText(appInfo.getName());
		holder.softwareSizeTextView.setText(ConvertUtils.bytes2kb(Long.parseLong(appInfo.getSize())));
		
		//app icon
		Bitmap defaultBitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.moren_icon);
		ImageDownloader.getInstance(ctx).download(appInfo.getIconFileName(), defaultBitmap, holder.shadeImageView);
		
		
		holder.downloadButton.setOnClickListener(new DownloadButtonOnClickListener(position));
		return convertView;
	}
	
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			File file = (File) msg.obj;
			Env.install(ctx, file, GENERAL_APPS_INSTALL_REQUEST);
		}
		
	};
	
	private void setDownloadButtonStatus(AppItemViewHolder holder, App app) {
		int installedStatus = Env.isAppInstalled(ctx, app.getPackageName(), app.getVersionCode(), mApplication.getInstalledAppsInfo());
		boolean hasDownloadTask = mDBOper.isHasDownloadTaskByUrl(ApiService.getDownloadAppUrl(), app.getDownloadId());
				
		//need to continue download
		if (hasDownloadTask) {
			if (isRunningDownload) {
				holder.downloadButton.setText(Utils.getResourceString(mApplication, R.string.pause));
			} else {
				holder.downloadButton.setText(Utils.getResourceString(mApplication, R.string.go_continue));
			}
			holder.progressLinearLayout.setVisibility(View.VISIBLE);
			holder.progressBar.setProgress(downloader.getProgress());
		} else {
			if (installedStatus == Constants.APP_NO_INSTALLED_DOWNLOAD) {
				holder.downloadButton.setText(Utils.getResourceString(mApplication, R.string.download));
			} else if (installedStatus == Constants.APP_NEW_VERSION_UPDATE) {
				holder.downloadButton.setText(Utils.getResourceString(mApplication, R.string.str_update));
			} else if (installedStatus == Constants.APP_INSTALLED_OPEN) {
				holder.downloadButton.setText(Utils.getResourceString(mApplication, R.string.open));
			}
		}
	}
	
	private void initDownloadComponents(App app, int pos) {
		DownloadBean downloadBean = generateDownloadBean(app);
		posMap.put(downloadBean.downloadId, pos);
		
		downloadBeanMap.put(pos, downloadBean);
	}
	
	private DownloadBean generateDownloadBean(App app) {
		DownloadBean downloadBean = new DownloadBean();
		downloadBean.url = ApiService.getDownloadAppUrl();
		downloadBean.fileId = app.getId();
		downloadBean.fileName = app.getName();
		downloadBean.fileVersion = app.getVersion();
		downloadBean.downloadId = app.getDownloadId();
		downloadBean.versionCode = app.getVersionCode();
		downloadBean.packageName = app.getPackageName();
		downloadBean.fileSize = Long.parseLong(app.getSize());
		
		int installedStatus = Env.isAppInstalled(ctx, app.getPackageName(), app.getVersionCode(), mApplication.getInstalledAppsInfo());
		downloadBean.installedStatus = installedStatus;
		
		String fileName = app.getVersion() + "_" + app.getName() + ".apk";		
		downloadBean.savePath = DownloadUtil.getAbsoluteFilePath(mApplication, fileName);
		return downloadBean;
	}
	
	class DownloadButtonOnClickListener implements View.OnClickListener {
		private App app;
		private int position;
		
		public DownloadButtonOnClickListener(int pos) {
			this.app = dataList.get(pos);
			this.position = pos;
		}

		@Override
		public void onClick(View view) {
			Button button = (Button) view;
			DownloadBean downloadBean = downloadBeanMap.get(position);
			File file = new File(downloadBean.savePath);	
			boolean hasDownloadTask = mDBOper.isHasDownloadTaskByUrl(ApiService.getDownloadAppUrl(), app.getDownloadId());
			if (hasDownloadTask ||
					downloadBean.installedStatus == Constants.APP_NO_INSTALLED_DOWNLOAD ||
							downloadBean.installedStatus == Constants.APP_NEW_VERSION_UPDATE) {
				//start download
				if (downloadBtnSwitcher(downloadBean, downloadBean.isAPKDownloading)) {
					if (!isRunningDownload) {
						button.setText(Utils.getResourceString(mApplication, R.string.pause));
						downloadBean.startTime = System.currentTimeMillis();
						downloadBean.flag = true;
						downloadBean.isAPKDownloading = true;
						try {
							Logger.d("click to download :" + downloadBean.fileName);
							downloader.setDownloadBean(downloadBean);
							downloader.startDownloader();
						} catch (DownloaderErrorException e) {
							Logger.d(e.getLocalizedMessage());
							downloadBean.flag = false;
							e.printStackTrace();
						}
						new RefreshDownloadProcessThread(position).start();
						isRunningDownload = true;
					} else {
						button.setClickable(false);
						button.setText(Utils.getResourceString(mApplication, R.string.waiting));
						queueTaskList.add(downloadBean);
					}
					
				} else {//stop download
					button.setText(Utils.getResourceString(mApplication, R.string.go_continue));
					isRunningDownload = false;
					downloadBean.flag = false;
					downloadBean.isAPKDownloading = false;
		            downloader.pauseDownloader();
				}				
	            
			} else if (downloadBean.installedStatus == Constants.APP_DOWNLOADED_INSTALL) {
				Env.install(ctx, file, GENERAL_APPS_INSTALL_REQUEST);
			} else if (downloadBean.installedStatus == Constants.APP_INSTALLED_OPEN) {
				Intent intent = Env.getLaunchIntent(mApplication, app.getPackageName(), mApplication.getInstalledAppsInfo());
				ctx.startActivity(intent);
			}
			downloadBeanMap.put(position, downloadBean);
		}
		
	}
	
	//class for update ui components
	class RefreshDownloadProcessThread extends Thread {
		private int position;
		
		public RefreshDownloadProcessThread(int pos) {
			this.position = pos;
		}

		@Override
         public void run() {
             int refreshNum = 1000;// refresh after 1s
             DownloadBean downloadBean = downloadBeanMap.get(position);
             while (downloadBean.flag) {
                 int nowProgress = downloader.getProgress();

                 Message msg = myHandler.obtainMessage();
                 msg.what = HANDLE_DOWNLOAD_PROCESS;
                 msg.obj = nowProgress;
                 msg.arg1 = nowProgress;
                 msg.arg2 = position;
                 myHandler.sendMessage(msg);

                 try {
                     Thread.sleep(refreshNum);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
             }
         }
	}
	
	Handler myHandler = new Handler() {
		AppItemViewHolder holder;
		
		@Override
		public void handleMessage(Message msg) {
			holder = holderMap.get(msg.arg2);
			switch (msg.what) {
			case HANDLE_DOWNLOAD_PROCESS:
				int progress = 0;
				if (msg.arg1 >= 100) {
					progress = 99;
				} else {
					progress = msg.arg1;
				}
				holder.progressLinearLayout.setVisibility(View.VISIBLE);
//				holder.downloadProcessTextView.setVisibility(View.VISIBLE);
//				holder.downloadProcessTextView.setText(progress + "%");
				holder.downloadButton.setClickable(true);
				holder.progressBar.setProgress(progress);
				break;
			case HANDLE_DOWNLOAD_PROCESS_DONE:
				int progressDone = msg.arg1;
//				holder.downloadProcessTextView.setText(progressDone + "%");
//				holder.downloadProcessTextView.setVisibility(View.GONE);
				holder.progressBar.setProgress(progressDone);
				holder.progressLinearLayout.setVisibility(View.INVISIBLE);
				
				//change download button status
				holder.downloadButton.setText(Utils.getResourceString(mApplication, R.string.install));
				
				DownloadBean downloadedBean = (DownloadBean) msg.obj;
				downloadedBean.installedStatus = Constants.APP_DOWNLOADED_INSTALL;
				downloadedBean.downloadedFile = new File(downloadedBean.savePath);
				
				downloadBeanMap.put(msg.arg2, downloadedBean);
				
				//install the app then refresh adapter
				File downloadedFile = new File(downloadedBean.savePath);
				Message message = mHandler.obtainMessage();
				message.obj = downloadedFile;
				message.what = NormalListFragment.GENERAL_APPS_INSTALL_REQUEST;
				mHandler.sendMessage(message);
				
//				Env.install(ctx, downloadedFile, SOFTWARE_DETAIL_LEFT_REQUEST);
				myHandler.sendEmptyMessageDelayed(HANDLE_START_NEW_PROCESS, 2000);
				
				break;
			case HANDLE_START_NEW_PROCESS:
				//get a new task to run
				if (queueTaskList != null && queueTaskList.size() > 0) {
					
					DownloadBean newBean = queueTaskList.poll();
					int pos = posMap.get(newBean.downloadId);
					holder = holderMap.get(pos);
					holder.progressLinearLayout.setVisibility(View.VISIBLE);
					runNewTask(newBean, pos);
				}
				break;
			default:
				break;
			}
			
		}
	};
	
	/**
	 * @Title: runNewTask
	 * @Description: get a new task from queue then start to download
	 * @param downloadBean
	 * @param position
	 * @return: void
	 */
	private void runNewTask(DownloadBean downloadBean, int position) {
		
		downloader.setDownloadEngineCallback(this);
		downloadBean.startTime = System.currentTimeMillis();
		downloadBean.flag = true;
		downloadBean.isAPKDownloading = true;
		try {
			Logger.d("start to download :" + downloadBean.fileName);
			downloader.setDownloadBean(downloadBean);
			downloader.startDownloader();
		} catch (DownloaderErrorException e) {
			Logger.d(e.getLocalizedMessage());
			downloadBean.flag = false;
			e.printStackTrace();
		}
		new RefreshDownloadProcessThread(position).start();
		isRunningDownload = true;
		downloadBeanMap.put(position, downloadBean);
		AppItemViewHolder holder = holderMap.get(position);
		holder.downloadButton.setText(Utils.getResourceString(mApplication, R.string.pause));
	}
	
	private boolean downloadBtnSwitcher(DownloadBean downloadBean, boolean currentStatus) {
		downloadBean.isAPKDownloading = !currentStatus;
		downloadBean.flag = !currentStatus;
		return !currentStatus;
	}
	
	class AppItemViewHolder {
		RelativeLayout rootRelativeLayout;
		LinearLayout progressLinearLayout;
		ProgressBar progressBar;
		ImageView shadeImageView;
		ImageView hdImageView;
		ImageView firstRelImageView;
		Button downloadButton;
		TextView ignoreTextView;					//ignore text view
		TextView softwareName;						//app name
		TextView softwareVersionTextView;			//msg text view
		TextView softwareVersionNameTextView;		//version name
		TextView softwareSizeTextView;				//app size
		TextView dividerTextView;					//divider
		TextView releaseDateTextView;
		TextView softwarePatchSizeTextView;
		TextView mergeApkTextView;
		TextView downloadProcessTextView;
		TextView newFeatureTextView;
		TextView newFeatureDetailTextView;
	}
	
	
	@Override
	public void callbackWhenDownloadTaskListener(int state, DownloadBean bean,
			String info) {
		// TODO Auto-generated method stub
		Logger.d("state:" + state + " info:" + info);
        
        long endTime = System.currentTimeMillis();
        Message msg = myHandler.obtainMessage();
        msg.what = HANDLE_DOWNLOAD_PROCESS_DONE;
        
        if (state == DownloadConstants.DOWNLOAD_STATE_DONE) {
            msg.arg1 = 100;
        } else {
        	msg.arg1 = downloader.getProgress();
        }
        isRunningDownload = false;
        bean.flag = false;
        bean.isAPKDownloading = false;
        msg.arg2 = posMap.get(bean.downloadId);
        msg.obj = bean;
        myHandler.sendMessage(msg);
        Logger.d("total time:" + DateFormat.format("mm:ss", endTime - bean.startTime) + "->position:" + msg.arg2);
	}
}
