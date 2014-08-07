package cn.koolcloud.ipos.appstore.fragment;

import java.io.File;
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
import android.widget.GridView;
import cn.koolcloud.ipos.appstore.MainActivity;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.adapter.UpdateAppsListAdapter;
import cn.koolcloud.ipos.appstore.api.ApiService;
import cn.koolcloud.ipos.appstore.constant.Constants;
import cn.koolcloud.ipos.appstore.download.multithread.DownloadTaskReceiver;
import cn.koolcloud.ipos.appstore.download.multithread.MultiThreadService;
import cn.koolcloud.ipos.appstore.entity.AppInfo;
import cn.koolcloud.ipos.appstore.interfaces.CallBack;
import cn.koolcloud.ipos.appstore.utils.Env;
import cn.koolcloud.ipos.appstore.utils.JsonUtils;
import cn.koolcloud.ipos.appstore.utils.MyLog;
import cn.koolcloud.ipos.appstore.utils.MySPEdit;
import cn.koolcloud.ipos.appstore.utils.ToastUtil;

/**
 * <p>Title: UpdateSoftwareFragment.java </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: All In Pay</p>
 * @author 		Teddy
 * @date 		2013-11-26
 * @version 	
 */
public class UpdateSoftwareFragment extends BaseFragment {
	private final int GENERAL_APPS_INSTALL_REQUEST = 1;
	private GridView mListView;
	private static UpdateAppsListAdapter mAppListAdapter = null;
	protected MyDownloadReceiver myDownlaodReceover;
	
	public static UpdateSoftwareFragment getInstance() {
		UpdateSoftwareFragment updateSoftwareFragment = new UpdateSoftwareFragment();
		return updateSoftwareFragment;
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
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getFragmentManager();
		initViews();
		
		if(MainActivity.updateAppLits == null || MainActivity.updateAppLits.size() == 0) {
			List<AppInfo> localSoftDataSource = Env.getInstalledAppsToList(application, false);
			ApiService.checkAllAppUpdate(getActivity(), MySPEdit.getTerminalID(getActivity()),
					localSoftDataSource, getUpdateCallBack);
		}
	}
	
	private CallBack getUpdateCallBack = new CallBack() {
		@Override
		public void onCancelled() {
		}

		@Override
		public void onStart() {
		}

		@Override
		public void onSuccess(JSONObject jsonObj) {
			try {
				MainActivity.updateAppLits = JsonUtils.parseSearchingJSONApps(jsonObj);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onFailure(String msg) {
			MyLog.d("describe=" + msg);
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.update_list_grid, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();
	}
	
	private void initViews() {
		mListView = (GridView) getActivity().findViewById(R.id.normal_list_grid);
		mAppListAdapter = new UpdateAppsListAdapter(getActivity(), MainActivity.updateAppLits,
				mHandler, application);
		mListView.setAdapter(mAppListAdapter);
		mAppListAdapter.notifyDataSetChanged();
	}
	
	Handler mHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case GENERAL_APPS_INSTALL_REQUEST:
				File file = (File) msg.obj;
				if (file.exists()) {
					if (isAdded() && getActivity() != null) {
						Env.install(getActivity(), file, GENERAL_APPS_INSTALL_REQUEST);
					}
				} else {
					ToastUtil.showToast(application, R.string.str_apk_download_failure);
				}
				break;
			}
			return false;
		}
	});
	
	@Override
	public void onResume() {
		if(mAppListAdapter != null) {
			mAppListAdapter.notifyDataSetChanged();
		}
		super.onResume();
	}
	
	public void refreshDataSet() {
		if (mAppListAdapter != null) {
			mAppListAdapter.refreshData(MainActivity.updateAppLits);
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
			MyLog.e("onReceive了--------");
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
			MyLog.e("暂停--------");
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
