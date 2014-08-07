package cn.koolcloud.ipos.appstore.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.koolcloud.ipos.appstore.MainActivity;
import cn.koolcloud.ipos.appstore.MyApp;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.api.ApiService;
import cn.koolcloud.ipos.appstore.cache.ImageDownloader;
import cn.koolcloud.ipos.appstore.constant.Constants;
import cn.koolcloud.ipos.appstore.download.common.DownloadUtil;
import cn.koolcloud.ipos.appstore.download.entity.DownloadBean;
import cn.koolcloud.ipos.appstore.download.multithread.DownloadContext;
import cn.koolcloud.ipos.appstore.download.multithread.MultiThreadService;
import cn.koolcloud.ipos.appstore.download.multithread.PauseDownloadState;
import cn.koolcloud.ipos.appstore.download.multithread.StartDownloadState;
import cn.koolcloud.ipos.appstore.entity.App;
import cn.koolcloud.ipos.appstore.fragment.CategoryRightFragment;
import cn.koolcloud.ipos.appstore.utils.ConvertUtils;
import cn.koolcloud.ipos.appstore.utils.Env;
import cn.koolcloud.ipos.appstore.utils.MyLog;
import cn.koolcloud.ipos.appstore.utils.MySPEdit;
import cn.koolcloud.ipos.appstore.utils.Utils;
import cn.koolcloud.ipos.appstore.views.ShadeImageView;


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
public class GeneralAppsListAdapter extends BaseAdapter {
	private List<App> dataList = new ArrayList<App>();
	private Activity ctx;
	private LayoutInflater mInflater;
	private MyApp mApplication;
	private Map<Integer, AppItemViewHolder> holderMap = new HashMap<Integer, AppItemViewHolder>();//save every item
	private Map<Integer, DownloadBean> downloadBeanMap = new HashMap<Integer, DownloadBean>();//save every download bean
	private Map<String, Integer> posMap = new HashMap<String, Integer>();//save position with download_id key and position value when download finished
	private Map<String, DownloadContext> contaxtMap = new HashMap<String, DownloadContext>();
	
	public GeneralAppsListAdapter(Activity context, List<App> dataSource,
			MyApp application) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);	
		this.ctx = context;
		dataList = dataSource;
		mApplication = application;
	}

	@Override
	public int getCount() {
		if (null != dataList) {
			return dataList.size();
		}
		return 0;
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
			convertView = mInflater.inflate(R.layout.general_software_list_item, null);
			holder = new AppItemViewHolder();
			holder.rootRelativeLayout = (RelativeLayout) convertView.findViewById(R.id.left_nav_item_root);
			holder.progressLinearLayout = (LinearLayout) convertView.findViewById(R.id.process_bar_layout);
			holder.progressBar = (ProgressBar) convertView.findViewById(R.id.processbar);
			holder.shadeImageView = (ShadeImageView) convertView.findViewById(R.id.software_icon);
			holder.hdImageView = (ImageView) convertView.findViewById(R.id.hd_tag);
			holder.firstRelImageView = (ImageView) convertView.findViewById(R.id.first_rel);
			holder.ratingBar = (RatingBar) convertView.findViewById(R.id.RatingBar01);
			holder.downloadButton = (Button) convertView.findViewById(R.id.download_bt);
			holder.softwareName = (TextView) convertView.findViewById(R.id.software_item_name);							
			holder.downloadNumTextView = (TextView) convertView.findViewById(R.id.software_downloadtimes);						
			holder.softwareSizeTextView = (TextView) convertView.findViewById(R.id.software_size);					
			holder.softwarePatchSizeTextView = (TextView) convertView.findViewById(R.id.software_patch_size);					
			holder.mergeApkTextView = (TextView) convertView.findViewById(R.id.mergeapk_tv);				
			convertView.setTag(holder);
		} else {
			holder = (AppItemViewHolder) convertView.getTag();
		}

		final App app = dataList.get(position);

		//save holder in map then to update by thread
		if(holderMap.get(position) == null) {
			holderMap.put(position, holder);
		}
		//init download info
		initDownloadComponents(app, position);
		
		holder.softwareName.setText(app.getName());
		holder.downloadNumTextView.setText(app.getVersion());
		holder.softwareSizeTextView.setText(ConvertUtils.bytes2kb(Long.parseLong(app.getSize())));

		//app icon
		Bitmap defaultBitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.moren_icon);
		ImageDownloader.getInstance(ctx).download(app.getIconFileName(), defaultBitmap, holder.shadeImageView);
		
		holder.ratingBar.setRating(Float.parseFloat(app.getRating()));
		
		final int installedStatus = Env.isAppInstalled(ctx, app.getPackageName(),
				app.getVersionCode(), mApplication.getInstalledAppsInfo());
		MyLog.e(app.getName()+" installedStatus----:"+installedStatus);
		
		String terminalId = MySPEdit.getTerminalID(ctx);
        final JSONObject paramJson = ApiService.getDownloadFileJson(terminalId, app.getDownloadId());
		
        File saveF = ctx.getFilesDir();
        MyLog.e("saveF.list().length:"+saveF.list().length);
		if((percentMap == null || percentMap.size() == 0) &&
				(stateMap == null || stateMap.size() == 0) ||
				saveF.list().length == 0) {
			if(percentMap != null && percentMap.size() > 0)
				percentMap.clear();
			if(stateMap != null && stateMap.size() > 0)
				stateMap.clear();
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
				
//				percentMap.clear();
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
	
	/**
	 * @param appInfo - app info java bean
	 * @param downloadButton - download button
	 * @param progressLinearLayout - progress bar layout
	 * @param progressBar - progress bar
	 * @param isInitState - is init state or not
	 * @param isUpdateState - is update state or not
	 */
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
		
		String fileName = app.getVersion() + "_" + app.getPackageName() + ".apk";		
		downloadBean.savePath = DownloadUtil.getAbsoluteFilePath(mApplication, fileName);
		return downloadBean;
	}
	
	private void initDownloadComponents(App app, int pos) {
		DownloadBean downloadBean = generateDownloadBean(app);
		if(posMap.get(downloadBean.downloadId) == null) {
			posMap.put(downloadBean.downloadId, pos);
		}
		if(downloadBeanMap.get(pos) == null) {
			downloadBeanMap.put(pos, downloadBean);
		}
	}
	
	class AppItemViewHolder {
		RelativeLayout rootRelativeLayout;
		LinearLayout progressLinearLayout;
		ProgressBar progressBar;
		ShadeImageView shadeImageView;
		ImageView hdImageView;
		ImageView firstRelImageView;
		RatingBar ratingBar;
		Button downloadButton;
		TextView softwareName;						//app name
		TextView downloadNumTextView;				//download times
		TextView softwareSizeTextView;				//app size
		TextView softwarePatchSizeTextView;
		TextView mergeApkTextView;
	}

	private HashMap<String, Integer> stateMap = new HashMap<String, Integer>();
	public void stateChanged(HashMap<String, Integer> map) {
		this.stateMap = map;
//		if(percentMap != null && percentMap.size() > 0) {
//			percentMap.clear();
//		}
		notifyDataSetChanged();
	}
	private HashMap<String, Integer> percentMap = new HashMap<String, Integer>();
	public void percentChanged(HashMap<String, Integer> map) {
		this.percentMap = map;
//		if(stateMap != null && stateMap.size() > 0) {
//			stateMap.clear();
//		}
		notifyDataSetChanged();
	}
}
