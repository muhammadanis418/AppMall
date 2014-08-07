package cn.koolcloud.ipos.appstore.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.json.JSONObject;

import android.annotation.SuppressLint;
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
import cn.koolcloud.ipos.appstore.MainActivity;
import cn.koolcloud.ipos.appstore.MyApp;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.api.ApiService;
import cn.koolcloud.ipos.appstore.cache.ImageDownloader;
import cn.koolcloud.ipos.appstore.constant.Constants;
import cn.koolcloud.ipos.appstore.download.common.DownloadConstants;
import cn.koolcloud.ipos.appstore.download.common.DownloadUtil;
import cn.koolcloud.ipos.appstore.download.common.DownloaderErrorException;
import cn.koolcloud.ipos.appstore.download.database.DownloadDBOperator;
import cn.koolcloud.ipos.appstore.download.entity.DownloadBean;
import cn.koolcloud.ipos.appstore.download.multithread.DownloadContext;
import cn.koolcloud.ipos.appstore.download.multithread.MultiThreadService;
import cn.koolcloud.ipos.appstore.download.multithread.PauseDownloadState;
import cn.koolcloud.ipos.appstore.download.multithread.StartDownloadState;
import cn.koolcloud.ipos.appstore.download.providers.DownloadEngineCallback;
import cn.koolcloud.ipos.appstore.download.providers.Downloader;
import cn.koolcloud.ipos.appstore.entity.App;
import cn.koolcloud.ipos.appstore.fragment.CategoryRightFragment;
import cn.koolcloud.ipos.appstore.utils.ConvertUtils;
import cn.koolcloud.ipos.appstore.utils.Env;
import cn.koolcloud.ipos.appstore.utils.MyLog;
import cn.koolcloud.ipos.appstore.utils.MySPEdit;
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
@SuppressLint("UseSparseArrays")
public class UpdateAppsListAdapter extends BaseAdapter implements DownloadEngineCallback {
	public static final int GENERAL_APPS_INSTALL_REQUEST = 1;
	private static final int HANDLE_DOWNLOAD_PROCESS = 2;
	private static final int HANDLE_DOWNLOAD_PROCESS_DONE = 3;
	private static final int HANDLE_START_NEW_PROCESS = 4;
	
	private List<App> dataList = new ArrayList<App>();
	private Activity ctx;
	private LayoutInflater mInflater;
	private MyApp mApplication;
	private Handler mHandler;
	
	//break point download fields
	private Downloader downloader;					//file downloader
	
	private Map<Integer, AppItemViewHolder> holderMap = new HashMap<Integer, AppItemViewHolder>();			//save every item
	private Map<Integer, DownloadBean> downloadBeanMap = new HashMap<Integer, DownloadBean>();				//save every download bean
	private Map<String, Integer> posMap = new HashMap<String, Integer>();									//save position with download_id key and position value when download finished
	private Queue<DownloadBean> queueTaskList = new LinkedList<DownloadBean>();
	private Map<String, DownloadContext> contaxtMap = new HashMap<String, DownloadContext>();
	
	public UpdateAppsListAdapter(Activity context, List<App> dataSource, Handler handler, MyApp application) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);	
		this.ctx = context;
		dataList = dataSource;
		this.mApplication = application;
		downloader = new Downloader(null, application, null);
		DownloadDBOperator.getInstance(application);
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

		final App app = dataList.get(position);
		
		//save holder in map then to update by thread
		if(holderMap.get(position) == null)
			holderMap.put(position, holder);
		//init download info
		initDownloadComponents(app, position);
//		setDownloadButtonStatus(holder, appInfo);
		
		holder.softwareName.setText(app.getName());
		holder.softwareVersionNameTextView.setText(app.getVersion());
		holder.softwareSizeTextView.setText(ConvertUtils.bytes2kb(Long.parseLong(app.getSize())));
		
		//app icon
		Bitmap defaultBitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.moren_icon);
		ImageDownloader.getInstance(ctx).download(app.getIconFileName(), defaultBitmap, holder.shadeImageView);
		
		final int installedStatus = Env.isAppInstalled(ctx, app.getPackageName(),
				app.getVersionCode(), mApplication.getInstalledAppsInfo());
		String terminalId = MySPEdit.getTerminalID(ctx);
        final JSONObject paramJson = ApiService.getDownloadFileJson(terminalId, app.getDownloadId());
        
        File saveF = ctx.getFilesDir();
		if((percentMap == null || percentMap.size() == 0) &&
				(stateMap == null || stateMap.size() == 0) ||
				saveF.list().length == 0) {
			holder.progressLinearLayout.setVisibility(View.INVISIBLE);
			// 初始化整个view的下载状态
			if (installedStatus == Constants.APP_NO_INSTALLED_DOWNLOAD) {
				// app正在下载时，清除内部缓存，会遇到一个bug：下载已停止，但是MainActivity.downMap中还保留着下载的记录
				// 这句话就是用来解决此bug的
				if(MainActivity.downMap.containsKey(app.getPackageName())) {
					MainActivity.downMap.remove(app.getPackageName());
				}
				// 还没有安装这个App
				changeButtonState(app, holder.downloadButton, holder.progressLinearLayout,
						holder.progressBar, true, false);
			} else if (installedStatus == Constants.APP_NEW_VERSION_UPDATE) {
				// app正在下载时，清除内部缓存，会遇到一个bug：下载已停止，但是MainActivity.downMap中还保留着下载的记录
				// 这句话就是用来解决此bug的
				if(MainActivity.downMap.containsKey(app.getPackageName())) {
					MainActivity.downMap.remove(app.getPackageName());
				}
				// 已安装，但是有更新
				changeButtonState(app, holder.downloadButton, holder.progressLinearLayout,
						holder.progressBar, true, true);
			} else if (installedStatus == Constants.APP_INSTALLED_OPEN) {
				// 已安装，无更新
				holder.downloadButton.setText(Utils.getResourceString(mApplication, R.string.open));
				
				holder.downloadButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Intent intent = Env.getLaunchIntent(mApplication, app.getPackageName(),
								mApplication.getInstalledAppsInfo());
						ctx.startActivity(intent);
					}
				});
			}
		}
		if(percentMap != null && percentMap.size() > 0) {
			if(percentMap.containsKey(app.getPackageName())) {
				if(percentMap.get(app.getPackageName()) < 100)
					holder.progressLinearLayout.setVisibility(View.VISIBLE);
				holder.progressBar.setProgress(percentMap.get(app.getPackageName()));
			}
		}
		if(stateMap != null && stateMap.size() > 0) {
			if(stateMap.containsKey(app.getPackageName())) {
				changeButtonState(app, holder.downloadButton, holder.progressLinearLayout,
						holder.progressBar, false, false);
				if(stateMap.get(app.getPackageName()) == MultiThreadService.IS_DOWNLOADING) {
					holder.downloadButton.setText(Utils.getResourceString(mApplication, R.string.pause));
				} else if(stateMap.get(app.getPackageName()) == MultiThreadService.IS_PAUSEING) {
					holder.downloadButton.setText(Utils.getResourceString(mApplication, R.string.go_continue));
				} else if(stateMap.get(app.getPackageName()) == MultiThreadService.HAVE_FINISHED) {
					holder.progressLinearLayout.setVisibility(View.INVISIBLE);
					if(installedStatus == Constants.APP_INSTALLED_OPEN) {
						holder.downloadButton.setText(Utils.getResourceString(mApplication, R.string.open));
						
						holder.downloadButton.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View arg0) {
								Intent intent = Env.getLaunchIntent(mApplication, app.getPackageName(),
										mApplication.getInstalledAppsInfo());
								ctx.startActivity(intent);
							}
						});
					} else {
						final File f = new File(ctx.getFilesDir(), app.getPackageName() + ".apk");
						if(f.exists()) {
							holder.downloadButton.setText(Utils.getResourceString(mApplication,
									R.string.install));
							holder.downloadButton.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View arg0) {
									Env.install(ctx, f, CategoryRightFragment.GENERAL_APPS_INSTALL_REQUEST);
								}
							});
						} else {
							holder.downloadButton.setText(Utils.getResourceString(mApplication,
									R.string.download));
	
							holder.downloadButton.setOnClickListener(new View.OnClickListener() {
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
		}
		return convertView;
	}
	
	private void changeButtonState(final App appInfo, Button downloadButton,
			LinearLayout progressLinearLayout, ProgressBar progressBar, final boolean isInitState,
			boolean isUpdateState) {
		MultiThreadService.IBinderImple binder = MainActivity.getInstance().getBinder();
		HashMap<Integer, Integer> downState = binder.GetStateData(appInfo.getPackageName(),
				appInfo.getVersionCode());
		String terminalId = MySPEdit.getTerminalID(ctx);
        final JSONObject paramJson = ApiService.getDownloadFileJson(terminalId, appInfo.getDownloadId());
		if(downState != null && downState.size() == 1) {
			if(downState.containsKey(MultiThreadService.IS_DOWNLOADING)) {
				MyLog.e("正在下载："+appInfo.getName());
				// 后台正在下载
				if(!isUpdateState) {
					downloadButton.setText(Utils.getResourceString(mApplication,
							R.string.pause));
				} else {
					downloadButton.setText(Utils.getResourceString(mApplication,
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
					}
				});
				
				progressLinearLayout.setVisibility(View.VISIBLE);
				if(MainActivity.downMap.get(appInfo.getPackageName()) == null) {
					progressBar.setProgress(0);
				} else {
					progressBar.setProgress(MainActivity.downMap.get(appInfo.getPackageName()));
				}
			} else if(downState.containsKey(MultiThreadService.IS_PAUSEING)) {
				MyLog.e("正在暂停："+appInfo.getName());
				// 后台已暂停
				if(!isUpdateState) {
					downloadButton.setText(Utils.getResourceString(mApplication,
							R.string.go_continue));
				} else {
					downloadButton.setText(Utils.getResourceString(mApplication,
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
					}
				});
				progressLinearLayout.setVisibility(View.VISIBLE);
				progressBar.setProgress(downState.get(MultiThreadService.IS_PAUSEING));
			} else if(downState.containsKey(MultiThreadService.HAVE_FINISHED)) {
				MyLog.e("已经结束："+appInfo.getName());
				// 已下载
				final File f = new File(ctx.getFilesDir(), appInfo.getPackageName() + ".apk");
				if(f.exists()) {
					if(!isUpdateState) {
						downloadButton.setText(Utils.getResourceString(mApplication,
								R.string.install));
					} else {
						downloadButton.setText(Utils.getResourceString(mApplication,
								R.string.install));
					}
					downloadButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View arg0) {
							Env.install(ctx, f, CategoryRightFragment.GENERAL_APPS_INSTALL_REQUEST);
						}
					});
				} else {
					if(!isUpdateState) {
						downloadButton.setText(Utils.getResourceString(mApplication,
								R.string.download));
					} else {
						downloadButton.setText(Utils.getResourceString(mApplication,
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
						}
					});
				}
			}
		} else {
			// 没有安装，后台也没有在下载这个App
			progressBar.setProgress(0);
			if(!isUpdateState) {
				downloadButton.setText(Utils.getResourceString(mApplication, R.string.download));
			} else {
				downloadButton.setText(Utils.getResourceString(mApplication, R.string.str_update));
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
				}
			});
		}
	}
	
	Handler handler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			File file = (File) msg.obj;
			Env.install(ctx, file, GENERAL_APPS_INSTALL_REQUEST);
			return false;
		}
	});
	
	private void initDownloadComponents(App app, int pos) {
		DownloadBean downloadBean = generateDownloadBean(app);
		if(posMap.get(downloadBean.downloadId) == null)
			posMap.put(downloadBean.downloadId, pos);
		if(downloadBeanMap.get(pos) == null)
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

                 if(nowProgress >= 100) {
                	 break;
                 }

                 try {
                     Thread.sleep(refreshNum);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
             }
         }
	}
	
	Handler myHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			AppItemViewHolder holder = holderMap.get(msg.arg2);
			switch (msg.what) {
			case HANDLE_DOWNLOAD_PROCESS:
				int progress = 0;
				if (msg.arg1 >= 100) {
					progress = 99;
				} else {
					progress = msg.arg1;
				}
				holder.progressLinearLayout.setVisibility(View.VISIBLE);
				holder.downloadButton.setClickable(true);
				holder.progressBar.setProgress(progress);
				break;
			case HANDLE_DOWNLOAD_PROCESS_DONE:
				int progressDone = msg.arg1;
				holder.progressBar.setProgress(progressDone);
				holder.progressLinearLayout.setVisibility(View.INVISIBLE);
				
				//change download button status
				holder.downloadButton.setText(Utils.getResourceString(mApplication, R.string.install));
				
				DownloadBean downloadedBean = (DownloadBean) msg.obj;
				downloadedBean.installedStatus = Constants.APP_DOWNLOADED_INSTALL;
				downloadedBean.downloadedFile = new File(downloadedBean.savePath);
				if(downloadBeanMap.get(msg.arg2) == null)
					downloadBeanMap.put(msg.arg2, downloadedBean);
				
				//install the app then refresh adapter
				File downloadedFile = new File(downloadedBean.savePath);
				Message message = mHandler.obtainMessage();
				message.obj = downloadedFile;
				message.what = CategoryRightFragment.GENERAL_APPS_INSTALL_REQUEST;
				mHandler.sendMessage(message);
				
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
			}
			return false;
		}
	});
	
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
			MyLog.d("start to download :" + downloadBean.fileName);
			downloader.setDownloadBean(downloadBean);
			downloader.startDownloader();
		} catch (DownloaderErrorException e) {
			MyLog.d(e.getLocalizedMessage());
			downloadBean.flag = false;
			e.printStackTrace();
		}
		new RefreshDownloadProcessThread(position).start();
		if(downloadBeanMap.get(position) == null)
			downloadBeanMap.put(position, downloadBean);
		AppItemViewHolder holder = holderMap.get(position);
		holder.downloadButton.setText(Utils.getResourceString(mApplication, R.string.pause));
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
		MyLog.d("state:" + state + " info:" + info);
        
        long endTime = System.currentTimeMillis();
        Message msg = myHandler.obtainMessage();
        msg.what = HANDLE_DOWNLOAD_PROCESS_DONE;
        
        if (state == DownloadConstants.DOWNLOAD_STATE_DONE) {
            msg.arg1 = 100;
        } else {
        	msg.arg1 = downloader.getProgress();
        }
        bean.flag = false;
        bean.isAPKDownloading = false;
        msg.arg2 = posMap.get(bean.downloadId);
        msg.obj = bean;
        myHandler.sendMessage(msg);
        MyLog.d("total time:" + DateFormat.format("mm:ss", endTime - bean.startTime) + "->position:" + msg.arg2);
	}

	private HashMap<String, Integer> stateMap = new HashMap<String, Integer>();
	public void stateChanged(HashMap<String, Integer> map) {
		this.stateMap = map;
		notifyDataSetChanged();
	}
	private HashMap<String, Integer> percentMap = new HashMap<String, Integer>();
	public void percentChanged(HashMap<String, Integer> map) {
		this.percentMap = map;
		notifyDataSetChanged();
	}

	public void refreshData(List<App> updateAppLits) {
		this.dataList = updateAppLits;
		notifyDataSetChanged();
	}
}
