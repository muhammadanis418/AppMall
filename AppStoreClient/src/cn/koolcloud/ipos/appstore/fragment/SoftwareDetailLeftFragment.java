package cn.koolcloud.ipos.appstore.fragment;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import cn.koolcloud.ipos.appstore.fragment.base.BaseFragment;
import cn.koolcloud.ipos.appstore.utils.ConvertUtils;
import cn.koolcloud.ipos.appstore.utils.Env;
import cn.koolcloud.ipos.appstore.utils.Logger;
import cn.koolcloud.ipos.appstore.utils.ToastUtil;
import cn.koolcloud.ipos.appstore.utils.Utils;

public class SoftwareDetailLeftFragment extends BaseFragment implements DownloadEngineCallback {
	private static final String TAG = "SoftwareDetailLeftFragment";
	
	public static final int SOFTWARE_DETAIL_LEFT_REQUEST = 4;
	private static final int HANDLE_INSTALL_APP = 0;
	private static final int HANDLE_REFRESH_BUTTON_STATUS = 1;
	private static final int HANDLE_DOWNLOAD_PROCESS = 2;
	private static final int HANDLE_DOWNLOAD_PROCESS_DONE = 3;
	
	private FragmentManager fragmentManager;
	
	private TextView softNameTextView;
	private RelativeLayout downloadRelativeLayout;
	private TextView downloadProcessTextView;
	private ProgressBar downloadProgressBar;
	private TextView softSizeTextView;
	private TextView softVersionTextView;
	private ImageView softIconImageView;
	private static Button downloadButton;
	private RatingBar ratingBar;
	private TextView releaseDateTextView;
	private TextView vendorTextView;
	
	private List<App> appListDataSource = null;								//apps data source
	private int currentPosition = 0;
	private App app = null;
	private OnSoftwareDetailLeftAttachedListener mCallback;
	private static int installedStatus;
	
	//break point download fields
	private Downloader downloader;					//file downloader
	private boolean flag = false;					//refresh progress bar tag
	private long startTime;
	private boolean isAPKDownloading = false;		//the tag for checking if the file is downloading
	private boolean canInstallAPK = false;			//the tag for checking if the file download completed
	private File downloadedFile;
	private DownloadDBOperator mDBOper;
	
	public static SoftwareDetailLeftFragment getInstance() {
		SoftwareDetailLeftFragment detailLeftFragment = new SoftwareDetailLeftFragment();
		return detailLeftFragment;
	}
	
    // Container Activity must implement this interface
    public interface OnSoftwareDetailLeftAttachedListener {
        public void onDetailLeftActivityCreated();
    }
    
    @Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		// This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnSoftwareDetailLeftAttachedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnSoftwareDetailLeftAttachedListener");
        }
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		fragmentManager = getFragmentManager();
		
		// TODO:initialize software left content and setup software main fragment
		mCallback.onDetailLeftActivityCreated();
		mDBOper = DownloadDBOperator.getInstance(application);
		initViews();
	}


	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		initDownloadComponents();
		initComponents();
	}

	@Override
	public void onStop() {
		try {
			downloader.pauseDownloader();
			super.onStop();
		} catch (Exception e) {
			Logger.e(e.getMessage());
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return inflater.inflate(R.layout.soft_detail_left, container, false);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SOFTWARE_DETAIL_LEFT_REQUEST && 
				resultCode == Activity.RESULT_CANCELED || resultCode == Activity.RESULT_OK) {
			setDownloadButtonStatus();
		}
	}

	private void initViews() {
		softNameTextView = (TextView) getActivity().findViewById(R.id.sw_left_name);
		softSizeTextView = (TextView) getActivity().findViewById(R.id.sw_left_size);
		softVersionTextView = (TextView) getActivity().findViewById(R.id.sw_left_version);
		softIconImageView = (ImageView) getActivity().findViewById(R.id.sw_left_icon);
		downloadButton = (Button) getActivity().findViewById(R.id.sw_left_download_bt);
		downloadRelativeLayout = (RelativeLayout) getActivity().findViewById(R.id.sw_left_download_layout);
		downloadProcessTextView = (TextView) getActivity().findViewById(R.id.sw_left_download_process);
		downloadProgressBar = (ProgressBar) getActivity().findViewById(R.id.sw_left_processbar);
		ratingBar = (RatingBar) getActivity().findViewById(R.id.sw_left_rating);
		releaseDateTextView = (TextView) getActivity().findViewById(R.id.sw_left_release_date);
		vendorTextView = (TextView) getActivity().findViewById(R.id.sw_left_developer);
	}
	
	/**
	 * @Title: initDownloadComponents
	 * @Description: init all the components before downloading
	 * @return: void
	 */
	private void initDownloadComponents() {
		DownloadBean downloadBean = new DownloadBean();
		downloadBean.url = ApiService.getDownloadAppUrl();
		downloadBean.fileId = app.getId();
		downloadBean.fileName = app.getName();
		downloadBean.fileVersion = app.getVersion();
		downloadBean.downloadId = app.getDownloadId();
		downloadBean.versionCode = app.getVersionCode();
		downloadBean.packageName = app.getPackageName();
		if (null != app.getSize()) {
			downloadBean.fileSize = Long.parseLong(app.getSize());
		}
		
		String fileName = app.getVersion() + "_" + app.getPackageName() + ".apk";		
		downloadBean.savePath = DownloadUtil.getAbsoluteFilePath(application, fileName);
		
		//init downloader
		if (downloader == null) {
			downloader = new Downloader(downloadBean, application, this);
		}
		/*try {
			if (downloader == null) {
				downloader = new Downloader(downloadBean, application, this);
			}
            
        } catch (DownloaderErrorException e) {
            Logger.debug(TAG, e.getLocalizedMessage());
            flag = false;
            e.printStackTrace();
        }*/
	}
	
	public void setArguments(Bundle bundle) {
		appListDataSource = (List<App>) bundle.getSerializable(Constants.SER_KEY);
		currentPosition = bundle.getInt(Constants.APP_LIST_POSITION);
		app = appListDataSource.get(currentPosition);
		Logger.d("set app succucessfull, position -->:" + currentPosition);
		
	}

	private void initComponents() {
		softNameTextView.setText(app.getName());
		if (null != app.getSize()) {
			softSizeTextView.setText(ConvertUtils.bytes2kb(Long.parseLong(app.getSize())));
		} else {
			softSizeTextView.setText("0");
		}
		softVersionTextView.setText(app.getVersion());
		Bitmap defaultBitmap = BitmapFactory.decodeResource(application.getResources(), R.drawable.moren_icon);
		ImageDownloader.getInstance(application).download(app.getIconFileName(), defaultBitmap, softIconImageView);
		
		setDownloadButtonStatus();
		downloadButton.setOnClickListener(new DownloadButtonOnClickListener());
		
		if (null != app.getRating()) {
			ratingBar.setRating(Float.parseFloat(app.getRating()));
		} else {
			ratingBar.setRating(0f);
		}
		releaseDateTextView.setText(ConvertUtils.longToString(app.getDate(), "yyyy-MM-dd"));
		vendorTextView.setText(app.getVendor());
	}
	
	private void setDownloadButtonStatus() {
		boolean hasDownloadTask = mDBOper.isHasDownloadTaskByUrl(ApiService.getDownloadAppUrl(), app.getDownloadId());
		installedStatus = Env.isAppInstalled(application, app.getPackageName(), app.getVersionCode(), application.getInstalledAppsInfo());
		//need to continue download
		if (hasDownloadTask) {
			downloader.initBeanInDataBase();
			
			downloadButton.setText(Utils.getResourceString(application, R.string.go_continue));
			downloadRelativeLayout.setVisibility(View.VISIBLE);
			
			downloadProcessTextView.setText(downloader.getProgress() + "%");
			downloadProgressBar.setProgress(downloader.getProgress());
		} else if (installedStatus == Constants.APP_DOWNLOADED_INSTALL) {
			downloadButton.setText(Utils.getResourceString(application, R.string.install));
		} else {
			
			if (installedStatus == Constants.APP_NO_INSTALLED_DOWNLOAD) {
				downloadButton.setText(Utils.getResourceString(application, R.string.download));
			} else if (installedStatus == Constants.APP_NEW_VERSION_UPDATE) {
				downloadButton.setText(Utils.getResourceString(application, R.string.str_update));
			} else if (installedStatus == Constants.APP_INSTALLED_OPEN) {
				downloadButton.setText(Utils.getResourceString(application, R.string.open));
			}
		}
	}
	
	class DownloadButtonOnClickListener implements View.OnClickListener {
		
		@Override
		public void onClick(View view) {
			if ((downloader.getProgress() < 100 && downloader.getProgress() > 0) || installedStatus == Constants.APP_NO_INSTALLED_DOWNLOAD || installedStatus == Constants.APP_NEW_VERSION_UPDATE) {
				//start download
				if (downloadBtnSwitcher(isAPKDownloading)) {
					downloadButton.setText(Utils.getResourceString(application, R.string.pause));
					startTime = System.currentTimeMillis();
		            flag = true;
		            isAPKDownloading = true;
		            try {
						downloader.startDownloader();
					} catch (DownloaderErrorException e) {
			            Logger.d(e.getLocalizedMessage());
			            flag = false;
			            e.printStackTrace();
			        }
					new RefreshDownloadProcessThread().start();
				} else {//stop download
					downloadButton.setText(Utils.getResourceString(application, R.string.go_continue));
					flag = false;
		            isAPKDownloading = false;
		            downloader.pauseDownloader();
				}				
	            
			} else if (installedStatus == Constants.APP_DOWNLOADED_INSTALL) {
				Env.install(getActivity(), downloadedFile, SOFTWARE_DETAIL_LEFT_REQUEST);
			} else if (installedStatus == Constants.APP_INSTALLED_OPEN) {
				Intent intent = Env.getLaunchIntent(application, app.getPackageName(), application.getInstalledAppsInfo());
				getActivity().startActivity(intent);
			}
		}
		
	}
	
	private boolean downloadBtnSwitcher(boolean currentStatus) {
		isAPKDownloading = !currentStatus;
		flag = !currentStatus;
		return !currentStatus;
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		downloader.pauseDownloader();
		installedStatus = Constants.APP_NO_INSTALLED_DOWNLOAD;
		super.onDestroy();
	}

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLE_INSTALL_APP:
				File file = (File) msg.obj;
				Env.install(getActivity(), file, SOFTWARE_DETAIL_LEFT_REQUEST);
				break;
			case HANDLE_REFRESH_BUTTON_STATUS:
				setDownloadButtonStatus();
				break;
			case HANDLE_DOWNLOAD_PROCESS:
				int progress = 0;
				if (msg.arg1 >= 100) {
					progress = 99;
				} else {
					progress = msg.arg1;
				}
				downloadRelativeLayout.setVisibility(View.VISIBLE);
				downloadProcessTextView.setText(progress + "%");
				downloadProgressBar.setProgress(progress);
				break;
			case HANDLE_DOWNLOAD_PROCESS_DONE:
				int progressDone = msg.arg1;
				downloadProcessTextView.setText(progressDone + "%");
				downloadProgressBar.setProgress(progressDone);
				downloadRelativeLayout.setVisibility(View.INVISIBLE);
				
				//change download button status
				downloadButton.setText(Utils.getResourceString(application, R.string.install));
				installedStatus = Constants.APP_DOWNLOADED_INSTALL;
				DownloadBean downloadedBean = (DownloadBean) msg.obj;
				downloadedFile = new File(downloadedBean.savePath);
				if (downloadedFile.exists()) {
					Env.install(getActivity(), downloadedFile, SOFTWARE_DETAIL_LEFT_REQUEST);
				} else {
					ToastUtil.showToast(application, R.string.str_apk_download_failure);
				}
				break;

			default:
				break;
			}
			
		}
		
	};
	
	/**
	 * @Title: refreshDataStatus
	 * @Description: refresh download button status after installed the app
	 * @return: void
	 */
	public static void refreshDataStatus() {
		installedStatus = Constants.APP_INSTALLED_OPEN;
		if (downloadButton != null) {
			downloadButton.setText(Utils.getResourceString(application, R.string.open));
		}
	}
	
	//class for update ui components
	class RefreshDownloadProcessThread extends Thread {
		 @Override
         public void run() {
             int refreshNum = 1000;// refresh after 1s

             while (flag) {
                 int nowProgress = downloader.getProgress();

                 Message msg = mHandler.obtainMessage();
                 msg.what = HANDLE_DOWNLOAD_PROCESS;
                 msg.obj = nowProgress;
                 msg.arg1 = nowProgress;
                 mHandler.sendMessage(msg);

                 try {
                     Thread.sleep(refreshNum);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
             }
         }
	}

	//method for call back from file is download completed
	@Override
	public void callbackWhenDownloadTaskListener(int state, DownloadBean bean,
			String info) {
		Logger.d("state:" + state + " info:" + info);
        flag = false;
        long endTime = System.currentTimeMillis();
        Message msg = mHandler.obtainMessage();
        msg.what = HANDLE_DOWNLOAD_PROCESS_DONE;
        
        if (state == DownloadConstants.DOWNLOAD_STATE_DONE) {
            msg.arg1 = 100;
        } else {
        	msg.arg1 = downloader.getProgress();
        }
        msg.arg2 = state;
        msg.obj = bean;
        mHandler.sendMessage(msg);
        Logger.d("total time:" + DateFormat.format("mm:ss", endTime - startTime));
	}
}
