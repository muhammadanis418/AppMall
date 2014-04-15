package cn.koolcloud.ipos.appstore.fragment.tab;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import cn.koolcloud.ipos.appstore.AppStoreApplication;
import cn.koolcloud.ipos.appstore.AppStorePreference;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.adapter.GeneralAppsListAdapter;
import cn.koolcloud.ipos.appstore.api.ApiService;
import cn.koolcloud.ipos.appstore.cache.database.CacheDB;
import cn.koolcloud.ipos.appstore.constant.Constants;
import cn.koolcloud.ipos.appstore.download.database.DownloadDBOperator;
import cn.koolcloud.ipos.appstore.download.providers.Downloader;
import cn.koolcloud.ipos.appstore.entity.App;
import cn.koolcloud.ipos.appstore.entity.Category;
import cn.koolcloud.ipos.appstore.entity.ResultSet;
import cn.koolcloud.ipos.appstore.fragment.base.BaseFragment;
import cn.koolcloud.ipos.appstore.interfaces.CallBack;
import cn.koolcloud.ipos.appstore.ui.SoftwareDetailActivity;
import cn.koolcloud.ipos.appstore.utils.Env;
import cn.koolcloud.ipos.appstore.utils.JsonUtils;
import cn.koolcloud.ipos.appstore.utils.Logger;
import cn.koolcloud.ipos.appstore.utils.ToastUtil;
import cn.koolcloud.ipos.appstore.utils.Utils;
import cn.koolcloud.ipos.appstore.views.AppStoreListView;
import cn.koolcloud.ipos.appstore.views.AppStoreListView.AppstoreListViewListener;

public class NormalListFragment extends BaseFragment implements OnItemClickListener, AppstoreListViewListener {
	private static final String TAG = "NormalListFragment";
	
	public static final int HANDLE_REFRESH_ADAPTER = 0;
	public static final int NORMAL_FRAGMENT_REQUEST = 0;
	public static final int GENERAL_APPS_INSTALL_REQUEST = 1;
	
	private AppStoreListView generalListView;										//the list view of apps
	
	private static GeneralAppsListAdapter mAppListAdapter;					//adapter for app list view
	private Category category;								 				//selected app category
	private List<App> appListDataSource = new ArrayList<App>();				//apps data source
	private static AppStoreApplication application;
	private File file;														//to be installed apk file
	private Downloader downloader;

	public static NormalListFragment getInstance() {
		NormalListFragment normalListFragment = new NormalListFragment();
		//save params
//		Bundle args = new Bundle();
//		args.putInt("index", index);
//		normalListFragment.setArguments(args);
		return normalListFragment;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initViews();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		category = (Category) getArguments().getSerializable(Constants.SER_KEY);
		application = (AppStoreApplication) getActivity().getApplication();
		downloader = new Downloader(null, application, null);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.appstore_content_list, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();
		getAppsByCategory();
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

	private void initViews() {
		generalListView = (AppStoreListView) getActivity().findViewById(R.id.contentListView);
		generalListView.setOnItemClickListener(this);
		generalListView.setPullRefreshEnable(true);
		generalListView.setPullLoadEnable(false);
		generalListView.setAppStoreListViewListener(this);
	}
	
	public void getAppsByCategory() {
		ApiService.getAppsByCategory(getActivity(), AppStorePreference.getTerminalID(getActivity()), 
				category.getId(), getAppsCallBack);
	}
	
	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLE_REFRESH_ADAPTER:
				file = (File) msg.obj;
				application.initApps();
				mAppListAdapter.notifyDataSetChanged();
				break;
			case GENERAL_APPS_INSTALL_REQUEST:
				File file = (File) msg.obj;
//				Env.install(getActivity(), file, GENERAL_APPS_INSTALL_REQUEST);
				if (file.exists()) {
					
					/*Intent intent = new Intent(Intent.ACTION_VIEW);
					try {
						Runtime.getRuntime().exec("chmod 655 " + file.toString());
					} catch (IOException e) {
						e.printStackTrace();
					}
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.setDataAndType(Uri.fromFile(file),
							"application/vnd.android.package-archive");
					if (isAdded() && getActivity() != null) {
						
						getActivity().startActivityForResult(intent, GENERAL_APPS_INSTALL_REQUEST);
					}*/
					//use common installer
					if (isAdded() && getActivity() != null) {
						Env.install(getActivity(), file, GENERAL_APPS_INSTALL_REQUEST);
					}
				} else {
					ToastUtil.showToast(application, R.string.str_apk_download_failure);
				}
				
				break;

			default:
				break;
			}
		}
		
	};
	
	public static void refreshDataSet() {
		if (mAppListAdapter != null) {
			
			mAppListAdapter.notifyDataSetChanged();
		}
	}
	
	private CallBack getAppsCallBack = new CallBack() {
		
		@Override
		public void onCancelled() {
			dismissLoading();
		}

		@Override
		public void onStart() {
			showLoading();
		}

		@Override
		public void onSuccess(JSONObject jsonObj) {
			try {
				String retCode = "";
				String data = "";
				
				Logger.d("-------getAppsInfo=" + jsonObj.toString());
				
				retCode = JsonUtils.getStringValue(jsonObj, Constants.REQUEST_STATUS);
				data = JsonUtils.getStringValue(jsonObj, Constants.REQUEST_DATA);
				
				/*if (!Constants.REQUEST_STATUS_OK.equals(retCode)) {
					onFailure(data);
				} else {
					if (TextUtils.isEmpty(data)) {
						onFailure(Utils.getResourceString(getActivity(), R.string.nonetwork_prompt_server_error));
					} else {
						client = JsonUtils.parseJSONClient(jsonObj);
					}
				}*/
				
				appListDataSource = JsonUtils.parseJSONApps(jsonObj);
				
				DownloadDBOperator mDBOper = DownloadDBOperator.getInstance(application);
				mAppListAdapter = new GeneralAppsListAdapter(getActivity(), appListDataSource, application, mHandler, downloader, mDBOper);
				generalListView.setAdapter(mAppListAdapter);
				mAppListAdapter.notifyDataSetChanged();
				
				//start thread to cache apps to database
				new CacheAppToDataBaseThread().start();
				dismissLoading();
			} catch (Exception e) {
				onFailure("get apps by category response error!");
			}
		}

		@Override
		public void onFailure(String msg) {
			dismissLoading();
			Logger.d("describe=" + msg);
			if (msg.contains(ResultSet.NET_ERROR.describe)) {
				
				ToastUtil.showToast(application, R.string.nonetwork_prompt_server_error);
			}
		}
	};

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {
		if (position > 0) {
			
			Intent mIntent = new Intent(getActivity(), SoftwareDetailActivity.class);
			Bundle mBundle = new Bundle();
			mBundle.putSerializable(Constants.SER_KEY, (Serializable) appListDataSource);
			mBundle.putInt(Constants.APP_LIST_POSITION, --position);	//ListView header is on first position so to remove one
			mIntent.putExtras(mBundle);
			startActivity(mIntent);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == GENERAL_APPS_INSTALL_REQUEST && resultCode == Activity.RESULT_CANCELED) {
			mAppListAdapter.notifyDataSetChanged();
			Logger.d("install cancelled");
		}
	}
	
	//insert cache to database
	class CacheAppToDataBaseThread extends Thread {

		@Override
		public void run() {
			CacheDB cacheDB = CacheDB.getInstance(application);
			cacheDB.insertApps(appListDataSource, category.getId());
		}
	}

	@Override
	public void onRefresh() {
		getAppsByCategory();
		onFinishLoading();
	}

	@Override
	public void onLoadMore() {
		onFinishLoading();
	}
	
	private void onFinishLoading() {
		generalListView.stopRefresh();
		generalListView.stopLoadMore();
		generalListView.setRefreshTime(Utils.getResourceString(application, R.string.appstore_list_header_hint_just_now));
	}
}
