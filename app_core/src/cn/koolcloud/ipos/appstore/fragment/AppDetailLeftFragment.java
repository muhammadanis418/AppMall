package cn.koolcloud.ipos.appstore.fragment;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import cn.koolcloud.ipos.appstore.MainActivity;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.api.ApiService;
import cn.koolcloud.ipos.appstore.cache.ImageDownloader;
import cn.koolcloud.ipos.appstore.constant.Constants;
import cn.koolcloud.ipos.appstore.download.common.DownloadConstants;
import cn.koolcloud.ipos.appstore.download.common.DownloadUtil;
import cn.koolcloud.ipos.appstore.download.database.DownloadDBOperator;
import cn.koolcloud.ipos.appstore.download.entity.DownloadBean;
import cn.koolcloud.ipos.appstore.download.multithread.DownloadContext;
import cn.koolcloud.ipos.appstore.download.multithread.DownloadTaskReceiver;
import cn.koolcloud.ipos.appstore.download.multithread.MultiThreadService;
import cn.koolcloud.ipos.appstore.download.multithread.PauseDownloadState;
import cn.koolcloud.ipos.appstore.download.multithread.StartDownloadState;
import cn.koolcloud.ipos.appstore.download.providers.DownloadEngineCallback;
import cn.koolcloud.ipos.appstore.download.providers.Downloader;
import cn.koolcloud.ipos.appstore.entity.App;
import cn.koolcloud.ipos.appstore.utils.ConvertUtils;
import cn.koolcloud.ipos.appstore.utils.Env;
import cn.koolcloud.ipos.appstore.utils.MyLog;
import cn.koolcloud.ipos.appstore.utils.MySPEdit;
import cn.koolcloud.ipos.appstore.utils.ToastUtil;
import cn.koolcloud.ipos.appstore.utils.Utils;

public class AppDetailLeftFragment extends BaseFragment implements DownloadEngineCallback {
	public static final int SOFTWARE_DETAIL_LEFT_REQUEST = 4;
	private static final int HANDLE_INSTALL_APP = 0;
	private static final int HANDLE_REFRESH_BUTTON_STATUS = 1;
	private static final int HANDLE_DOWNLOAD_PROCESS = 2;
	private static final int HANDLE_DOWNLOAD_PROCESS_DONE = 3;
	
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
	private Map<String, DownloadContext> contaxtMap = new HashMap<String, DownloadContext>();
	//break point download fields
	private Downloader downloader;					//file downloader
	private boolean flag = false;					//refresh progress bar tag
	private long startTime;
	private File downloadedFile;
	private DownloadDBOperator mDBOper;
	protected MyDownloadReceiver myDownlaodReceover;
	private JSONObject paramJson;
	private ProgressDialog myDialog;
	
	public static AppDetailLeftFragment getInstance() {
		AppDetailLeftFragment detailLeftFragment = new AppDetailLeftFragment();
		return detailLeftFragment;
	}
	
    // Container Activity must implement this interface
    public interface OnSoftwareDetailLeftAttachedListener {
        public void onDetailLeftActivityCreated();
    }
    
    @Override
	public void onAttach(Activity activity) {
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
		
        myDownlaodReceover = new MyDownloadReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.ACTION_TASK_STARTED);
		filter.addAction(Constants.ACTION_TASK_PAUSED);
		filter.addAction(Constants.ACTION_TASK_FINISHED);
		filter.addAction(Constants.ACTION_TASK_UPDATED);
		filter.addAction(Constants.ACTION_TASK_ERROR);
		getActivity().registerReceiver(myDownlaodReceover, filter);
		
		myDialog = new ProgressDialog(getActivity());
		myDialog.setTitle(R.string.appstore_list_header_hint_loading);
		myDialog.setCancelable(false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getFragmentManager();
		
		// TODO:initialize software left content and setup software main fragment
		mCallback.onDetailLeftActivityCreated();
		mDBOper = DownloadDBOperator.getInstance(application);
		initViews();
	}


	@Override
	public void onStart() {
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
			e.printStackTrace();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.soft_detail_left, container, false);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SOFTWARE_DETAIL_LEFT_REQUEST && 
				resultCode == Activity.RESULT_CANCELED || resultCode == Activity.RESULT_OK) {
			setDownloadButtonStatus();
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
//		File saveF = getActivity().getFilesDir();
//		if(saveF.listFiles().length == 0) {
//			changeButtonState(app, downloadButton, downloadRelativeLayout, downloadProgressBar,
//					downloadProcessTextView, true, false);
//		}
		initComponents();
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

		String terminalId = MySPEdit.getTerminalID(application);
        paramJson = ApiService.getDownloadFileJson(terminalId, app.getDownloadId());
	}
	
	@SuppressWarnings("unchecked")
	public void setArguments(Bundle bundle) {
		appListDataSource = (List<App>) bundle.getSerializable(Constants.SER_KEY);
		currentPosition = bundle.getInt(Constants.APP_LIST_POSITION);
		app = appListDataSource.get(currentPosition);
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
		
		installedStatus = Env.isAppInstalled(application, app.getPackageName(), app.getVersionCode(),
				application.getInstalledAppsInfo());
		downloadRelativeLayout.setVisibility(View.INVISIBLE);
		// 初始化整个view的下载状态
		if (installedStatus == Constants.APP_NO_INSTALLED_DOWNLOAD) {
			// app正在下载时，清除内部缓存，会遇到一个bug：下载已停止，但是MainActivity.downMap中还保留着下载的记录
			// 这句话就是用来解决此bug的
			if(MainActivity.downMap.containsKey(app.getPackageName())) {
				MainActivity.downMap.remove(app.getPackageName());
			}
			// 还没有安装这个App
			changeButtonState(app, downloadButton, downloadRelativeLayout,
					downloadProgressBar, downloadProcessTextView, true, false);
		} else if (installedStatus == Constants.APP_NEW_VERSION_UPDATE) {
			// app正在下载时，清除内部缓存，会遇到一个bug：下载已停止，但是MainActivity.downMap中还保留着下载的记录
			// 这句话就是用来解决此bug的
			if(MainActivity.downMap.containsKey(app.getPackageName())) {
				MainActivity.downMap.remove(app.getPackageName());
			}
			// 还没有安装这个App
			changeButtonState(app, downloadButton, downloadRelativeLayout,
					downloadProgressBar, downloadProcessTextView, true, true);
		} else if (installedStatus == Constants.APP_INSTALLED_OPEN) {
			// 已安装，无更新
			downloadButton.setText(Utils.getResourceString(application, R.string.open));
			
			downloadButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Intent intent = Env.getLaunchIntent(application, app.getPackageName(),
							application.getInstalledAppsInfo());
					application.startActivity(intent);
				}
			});
		}
		
		if (null != app.getRating()) {
			ratingBar.setRating(Float.parseFloat(app.getRating()));
		} else {
			ratingBar.setRating(0f);
		}
		releaseDateTextView.setText(ConvertUtils.longToString(app.getDate(), "yyyy-MM-dd"));
		vendorTextView.setText(app.getVendor());
	}
	
	private void changeButtonState(final App appInfo, Button downloadButton,
			RelativeLayout downloadRelativeLayout2, ProgressBar progressBar,
			TextView downloadText, final boolean isInitState, boolean isUpdateState) {
		MultiThreadService.IBinderImple binder = MainActivity.getInstance().getBinder();
		HashMap<Integer, Integer> downState = binder.GetStateData(appInfo.getPackageName(),
				appInfo.getVersionCode());
		String terminalId = MySPEdit.getTerminalID(getActivity());
        final JSONObject paramJson = ApiService.getDownloadFileJson(terminalId, appInfo.getDownloadId());
		if(downState != null && downState.size() == 1) {
			if(downState.containsKey(MultiThreadService.IS_DOWNLOADING)) {
				MyLog.e("正在下载："+appInfo.getName());
				// 后台正在下载
				if(!isUpdateState) {
					downloadButton.setText(Utils.getResourceString(application,
							R.string.pause));
				} else {
					downloadButton.setText(Utils.getResourceString(application,
							R.string.pause));
				}

				downloadButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						DownloadContext downloadContext;
						if(!isInitState && contaxtMap.containsKey(appInfo.getPackageName())) {
							downloadContext = contaxtMap.get(appInfo.getPackageName());
						} else {
							downloadContext = new DownloadContext(new PauseDownloadState());
							contaxtMap.put(appInfo.getPackageName(), downloadContext);
						}
						downloadContext.Request(appInfo.getPackageName(), paramJson.toString(),
								appInfo.getVersionCode());
						myDialog.show();
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								initComponents();
								myDialog.dismiss();
							}
						}, 1000);
					}
				});
				
				downloadRelativeLayout2.setVisibility(View.VISIBLE);
				if(MainActivity.downMap.get(appInfo.getPackageName()) == null) {
					progressBar.setProgress(0);
					downloadText.setText("0");
				} else {
					progressBar.setProgress(MainActivity.downMap.get(appInfo.getPackageName()));
					downloadText.setText(""+MainActivity.downMap.get(appInfo.getPackageName()));
				}
			} else if(downState.containsKey(MultiThreadService.IS_PAUSEING)) {
				MyLog.e("正在暂停："+appInfo.getName());
				// 后台已暂停
				if(!isUpdateState) {
					downloadButton.setText(Utils.getResourceString(application,
							R.string.go_continue));
				} else {
					downloadButton.setText(Utils.getResourceString(application,
							R.string.go_continue));
				}

				downloadButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						DownloadContext downloadContext;
						if(!isInitState && contaxtMap.containsKey(appInfo.getPackageName())) {
							downloadContext = contaxtMap.get(appInfo.getPackageName());
						} else {
							downloadContext = new DownloadContext(new StartDownloadState());
							contaxtMap.put(appInfo.getPackageName(), downloadContext);
						}
						downloadContext.Request(appInfo.getPackageName(), paramJson.toString(),
								appInfo.getVersionCode());
						myDialog.show();
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								initComponents();
								myDialog.dismiss();
							}
						}, 1500);
					}
				});
				downloadRelativeLayout2.setVisibility(View.VISIBLE);
				progressBar.setProgress(downState.get(MultiThreadService.IS_PAUSEING));
				downloadText.setText(""+downState.get(MultiThreadService.IS_PAUSEING));
			} else if(downState.containsKey(MultiThreadService.HAVE_FINISHED)) {
				MyLog.e("已经结束："+appInfo.getName());
				// 已下载
				final File f = new File(getActivity().getFilesDir(), appInfo.getPackageName() + ".apk");
				if(f.exists()) {
					if(!isUpdateState) {
						downloadButton.setText(Utils.getResourceString(application,
								R.string.install));
					} else {
						downloadButton.setText(Utils.getResourceString(application,
								R.string.install));
					}
					downloadButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View arg0) {
							Env.install(getActivity(), f, CategoryRightFragment.GENERAL_APPS_INSTALL_REQUEST);
						}
					});
				} else {
					if(!isUpdateState) {
						downloadButton.setText(Utils.getResourceString(application,
								R.string.download));
					} else {
						downloadButton.setText(Utils.getResourceString(application,
								R.string.str_update));
					}

					downloadButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View arg0) {
							DownloadContext downloadContext;
							if(!isInitState && contaxtMap.containsKey(appInfo.getPackageName())) {
								downloadContext = contaxtMap.get(appInfo.getPackageName());
							} else {
								downloadContext = new DownloadContext(new PauseDownloadState());
								contaxtMap.put(appInfo.getPackageName(), downloadContext);
							}
							downloadContext.Request(appInfo.getPackageName(), paramJson.toString(),
									appInfo.getVersionCode());
							myDialog.show();
							new Handler().postDelayed(new Runnable() {
								@Override
								public void run() {
									initComponents();
									myDialog.dismiss();
								}
							}, 1000);
						}
					});
				}
			}
		} else {
			// 没有安装，后台也没有在下载这个App
			progressBar.setProgress(0);
			if(!isUpdateState) {
				downloadButton.setText(Utils.getResourceString(application, R.string.download));
			} else {
				downloadButton.setText(Utils.getResourceString(application, R.string.str_update));
			}
			downloadButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					DownloadContext downloadContext;
					if(!isInitState && contaxtMap.containsKey(appInfo.getPackageName())) {
						downloadContext = contaxtMap.get(appInfo.getPackageName());
					} else {
						downloadContext = new DownloadContext(new StartDownloadState());
						contaxtMap.put(appInfo.getPackageName(), downloadContext);
					}
					downloadContext.Request(appInfo.getPackageName(), paramJson.toString(),
							appInfo.getVersionCode());
					myDialog.show();
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							initComponents();
							myDialog.dismiss();
						}
					}, 1500);
				}
			});
		}
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
	
	@Override
	public void onDestroy() {
		downloader.pauseDownloader();
		installedStatus = Constants.APP_NO_INSTALLED_DOWNLOAD;
		try {
			getActivity().unregisterReceiver(myDownlaodReceover);
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onDestroy();
	}

	Handler mHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
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
			}
			return false;
		}
	});
	
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
		MyLog.d("state:" + state + " info:" + info);
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
        MyLog.d("total time:" + DateFormat.format("mm:ss", endTime - startTime));
	}
	
	class MyDownloadReceiver extends DownloadTaskReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			super.onReceive(context, intent);
		}

		@Override
		public void downloadStart(String taskPkgName) {
			super.downloadStart(taskPkgName);
			if(app.getPackageName().equals(taskPkgName)) {
				downloadButton.setText(Utils.getResourceString(application, R.string.pause));
			}
		}

		@Override
		public void downloadPause(String taskPkgName) {
			super.downloadPause(taskPkgName);
			if(app.getPackageName().equals(taskPkgName)) {
				downloadButton.setText(Utils.getResourceString(application, R.string.go_continue));
			}
		}

		@Override
		public void downloadResumed(String taskPkgName) {
			super.downloadResumed(taskPkgName);
			if(app.getPackageName().equals(taskPkgName)) {
				downloadButton.setText(Utils.getResourceString(application, R.string.pause));
			}
		}

		@Override
		public void downloadFinished(String taskPkgName) {
			super.downloadFinished(taskPkgName);
			if(app.getPackageName().equals(taskPkgName)) {
				downloadRelativeLayout.setVisibility(View.INVISIBLE);
				if(installedStatus == Constants.APP_INSTALLED_OPEN) {
					downloadButton.setText(Utils.getResourceString(application, R.string.open));
					
					downloadButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View arg0) {
							Intent intent = Env.getLaunchIntent(application, app.getPackageName(),
									application.getInstalledAppsInfo());
							application.startActivity(intent);
						}
					});
				} else {
					final File f = new File(application.getFilesDir(), app.getPackageName() + ".apk");
					if(f.exists()) {
						downloadButton.setText(Utils.getResourceString(application,
								R.string.install));
						downloadButton.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View arg0) {
								Env.install(application, f, CategoryRightFragment.GENERAL_APPS_INSTALL_REQUEST);
							}
						});
					} else {
						downloadButton.setText(Utils.getResourceString(application,
								R.string.download));
	
						downloadButton.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View arg0) {
								DownloadContext downloadContext;
								if(contaxtMap.containsKey(app.getPackageName())) {
									downloadContext = contaxtMap.get(app.getPackageName());
								} else {
									downloadContext = new DownloadContext(new PauseDownloadState());
									contaxtMap.put(app.getPackageName(), downloadContext);
								}
								downloadContext.Request(app.getPackageName(), paramJson.toString(),
										app.getVersionCode());
							}
						});
					}
				}
			}
		}

		@Override
		public void downloadCanceled(String taskPkgName) {
			super.downloadCanceled(taskPkgName);
			if(app.getPackageName().equals(taskPkgName)) {
				downloadButton.setText(Utils.getResourceString(application,
						R.string.download));
			}
		}

		@Override
		public void downloadError(String taskPkgName, String error) {
			super.downloadError(taskPkgName, error);
		}

		@Override
		public void progressChanged(String taskPkgName, int process) {
			super.progressChanged(taskPkgName, process);
			if(app.getPackageName().equals(taskPkgName)) {
				downloadRelativeLayout.setVisibility(View.VISIBLE);
				downloadProcessTextView.setText(process + "%");
				downloadProgressBar.setProgress(process);
			}
		}
	}
}
