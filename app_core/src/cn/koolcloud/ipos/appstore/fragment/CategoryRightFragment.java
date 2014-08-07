package cn.koolcloud.ipos.appstore.fragment;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import cn.koolcloud.ipos.appstore.AppDetailActivity;
import cn.koolcloud.ipos.appstore.MyApp;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.adapter.GeneralAppsListAdapter;
import cn.koolcloud.ipos.appstore.api.ApiService;
import cn.koolcloud.ipos.appstore.cache.database.CacheDB;
import cn.koolcloud.ipos.appstore.constant.Constants;
import cn.koolcloud.ipos.appstore.download.multithread.DownloadTaskReceiver;
import cn.koolcloud.ipos.appstore.download.multithread.MultiThreadService;
import cn.koolcloud.ipos.appstore.entity.App;
import cn.koolcloud.ipos.appstore.entity.Category;
import cn.koolcloud.ipos.appstore.entity.ResultSet;
import cn.koolcloud.ipos.appstore.interfaces.CallBack;
import cn.koolcloud.ipos.appstore.utils.Env;
import cn.koolcloud.ipos.appstore.utils.JsonUtils;
import cn.koolcloud.ipos.appstore.utils.MyLog;
import cn.koolcloud.ipos.appstore.utils.MySPEdit;
import cn.koolcloud.ipos.appstore.utils.ToastUtil;

public class CategoryRightFragment extends BaseFragment implements OnItemClickListener {
	public static final int HANDLE_REFRESH_ADAPTER = 0;
	public static final int NORMAL_FRAGMENT_REQUEST = 0;
	public static final int GENERAL_APPS_INSTALL_REQUEST = 1;
	public static final int REFRESH_ADAPTER_DATA_RESOURCE = 2;
	private GridView subCategoryGridView;										//the list view of apps
	private static GeneralAppsListAdapter mAppListAdapter;					//adapter for app list view
	private Category category;								 				//selected app category
	private List<App> appListDataSource = new ArrayList<App>();				//apps data source
	private static MyApp application;
	protected MyDownloadReceiver myDownlaodReceover;

	public static CategoryRightFragment getInstance() {
		CategoryRightFragment normalListFragment = new CategoryRightFragment();
		return normalListFragment;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initViews();
		getAppsByCategory();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		category = (Category) getArguments().getSerializable(Constants.SER_KEY);
		application = (MyApp) getActivity().getApplication();
		
        myDownlaodReceover = new MyDownloadReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.ACTION_TASK_STARTED);
		filter.addAction(Constants.ACTION_TASK_PAUSED);
		filter.addAction(Constants.ACTION_TASK_FINISHED);
		filter.addAction(Constants.ACTION_TASK_UPDATED);
		filter.addAction(Constants.ACTION_TASK_ERROR);
		getActivity().registerReceiver(myDownlaodReceover, filter);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.appstore_content_list, container, false);
	}

	private void initViews() {
		subCategoryGridView = (GridView) getActivity().findViewById(R.id.subCategoryGridView);
		subCategoryGridView.setOnItemClickListener(this);
	}
	
	public void getAppsByCategory() {
		ApiService.getAppsByCategory(getActivity(), MySPEdit.getTerminalID(getActivity()), 
				category.getId(), getAppsCallBack);
	}
	
	Handler mHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLE_REFRESH_ADAPTER:
				application.initApps();
				mAppListAdapter.notifyDataSetChanged();
				break;
			case GENERAL_APPS_INSTALL_REQUEST:
				File file = (File) msg.obj;
				if (file.exists()) {
					//use common installer
					if (isAdded() && getActivity() != null) {
						Env.install(getActivity(), file, GENERAL_APPS_INSTALL_REQUEST);
					}
				} else {
					ToastUtil.showToast(application, R.string.str_apk_download_failure);
				}
				break;
			case REFRESH_ADAPTER_DATA_RESOURCE:
				if(mAppListAdapter != null) {
					mAppListAdapter.notifyDataSetChanged();
				}
				break;
			}
			return false;
		}
	});
	
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
				MyLog.w("onSuccess:\n" + jsonObj.toString());
				
				appListDataSource = JsonUtils.parseJSONApps(jsonObj);
				
				mAppListAdapter = new GeneralAppsListAdapter(getActivity(), appListDataSource, application);
				subCategoryGridView.setAdapter(mAppListAdapter);
				mAppListAdapter.notifyDataSetChanged();
				
				//start thread to cache apps to database
				new CacheAppToDataBaseThread().start();
				dismissLoading();
			} catch (Exception e) {
				onFailure("get apps by category response error!");
				e.printStackTrace();
			}
		}

		@Override
		public void onFailure(String msg) {
			dismissLoading();
			MyLog.w("onFailure:\n" + msg);
			if (msg.contains(ResultSet.NET_ERROR.describe)) {
				ToastUtil.showToast(application, R.string.nonetwork_prompt_server_error);
			}
		}
	};
	
	@Override
	public void onResume() {
		if(mAppListAdapter != null) {
			mAppListAdapter.notifyDataSetChanged();
		}
		super.onResume();
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {
		Intent mIntent = new Intent(getActivity(), AppDetailActivity.class);
		Bundle mBundle = new Bundle();
		mBundle.putSerializable(Constants.SER_KEY, (Serializable) appListDataSource);
		mBundle.putInt(Constants.APP_LIST_POSITION, position);	//ListView header is on first position so to remove one
		mIntent.putExtras(mBundle);
		startActivity(mIntent);
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
	public void onDestroy() {
		try {
			getActivity().unregisterReceiver(myDownlaodReceover);
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onDestroy();
	}
	
	class MyDownloadReceiver extends DownloadTaskReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			super.onReceive(context, intent);
		}

		@Override
		public void downloadStart(String taskPkgName) {
			super.downloadStart(taskPkgName);
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			map.put(taskPkgName, MultiThreadService.IS_DOWNLOADING);
			mAppListAdapter.stateChanged(map);
		}

		@Override
		public void downloadPause(String taskPkgName) {
			super.downloadPause(taskPkgName);
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			map.put(taskPkgName, MultiThreadService.IS_PAUSEING);
			mAppListAdapter.stateChanged(map);
		}

		@Override
		public void downloadResumed(String taskPkgName) {
			super.downloadResumed(taskPkgName);
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			map.put(taskPkgName, MultiThreadService.IS_DOWNLOADING);
			mAppListAdapter.stateChanged(map);
		}

		@Override
		public void downloadFinished(String taskPkgName) {
			super.downloadFinished(taskPkgName);
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			map.put(taskPkgName, MultiThreadService.HAVE_FINISHED);
			mAppListAdapter.stateChanged(map);
		}

		@Override
		public void downloadCanceled(String taskPkgName) {
			super.downloadCanceled(taskPkgName);
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			map.put(taskPkgName, MultiThreadService.IS_PAUSEING);
			mAppListAdapter.stateChanged(map);
		}

		@Override
		public void downloadError(String taskPkgName, String error) {
			super.downloadError(taskPkgName, error);
		}

		@Override
		public void progressChanged(String taskPkgName, int process) {
			super.progressChanged(taskPkgName, process);
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			map.put(taskPkgName, process);
			mAppListAdapter.percentChanged(map);
		}
	}
}
