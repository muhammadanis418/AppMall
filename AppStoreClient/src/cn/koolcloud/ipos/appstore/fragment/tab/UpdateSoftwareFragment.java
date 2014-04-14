package cn.koolcloud.ipos.appstore.fragment.tab;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.adapter.UpdateAppsListAdapter;
import cn.koolcloud.ipos.appstore.cache.database.CacheDB;
import cn.koolcloud.ipos.appstore.entity.App;
import cn.koolcloud.ipos.appstore.entity.AppInfo;
import cn.koolcloud.ipos.appstore.fragment.base.BaseFragment;
import cn.koolcloud.ipos.appstore.utils.Env;
import cn.koolcloud.ipos.appstore.utils.Logger;
import cn.koolcloud.ipos.appstore.utils.ToastUtil;
import cn.koolcloud.ipos.appstore.views.AppStoreListView;

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
	private static final String TAG = "UpdateSoftwareFragment";
	
	private final int HANDLE_UPDATE_APP = 0;
	private final int GENERAL_APPS_INSTALL_REQUEST = 1;
	
	private FragmentManager fragManager;
	
	private AppStoreListView mListView;
	private TextView groupTextView;
	private TextView updateAllTextView;
	private TextView ignoreTextView;
	private RelativeLayout updateAllLayout;
	private static UpdateAppsListAdapter updateAppListAdapter = null;
	private static List<App> appListDataSource = new ArrayList<App>();				//apps data source
	
	public static UpdateSoftwareFragment getInstance() {
		UpdateSoftwareFragment updateSoftwareFragment = new UpdateSoftwareFragment();
		return updateSoftwareFragment;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		fragManager = getFragmentManager();
		initViews();
		if (isAdded() && getActivity() != null) {
			new CheckUpdateSoftwareThread().start();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.update_list_grid, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();
		showLoading();
	}
	
	private void initViews() {
		mListView = (AppStoreListView) getActivity().findViewById(R.id.normal_list_grid);
		mListView.setPullRefreshEnable(false);
		mListView.setPullLoadEnable(false);
		
		groupTextView = (TextView) getActivity().findViewById(R.id.group_name);
		updateAllLayout = (RelativeLayout) getActivity().findViewById(R.id.update_all_layout);
		updateAllTextView = (TextView) getActivity().findViewById(R.id.update_all);
		ignoreTextView = (TextView) getActivity().findViewById(R.id.view_ignore);
	}
	
	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLE_UPDATE_APP:
				updateAppListAdapter = new UpdateAppsListAdapter(getActivity(), appListDataSource, mHandler, application);
				mListView.setAdapter(updateAppListAdapter);
				updateAppListAdapter.notifyDataSetChanged();
				dismissLoading();
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
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == UpdateAppsListAdapter.GENERAL_APPS_INSTALL_REQUEST &&
				resultCode == Activity.RESULT_CANCELED) {
			mListView.setAdapter(updateAppListAdapter);
			updateAppListAdapter.notifyDataSetChanged();
		}
	}

	//check out the installed software and compare the version with the cache in the database
	class CheckUpdateSoftwareThread extends Thread {

		@Override
		public void run() {
			List<AppInfo> installedSoftList = Env.getInstalledAppsToList(application, false);
			
			CacheDB cacheDB = CacheDB.getInstance(application);
			appListDataSource = cacheDB.getUpdatedSoft(installedSoftList);
			
			Message msg = mHandler.obtainMessage();
			msg.what = HANDLE_UPDATE_APP;
			mHandler.sendMessage(msg);
			
		}
	}
	
	public static void refreshDataSet(List<App> appList) {
		if (appList != null ) {
			
			appListDataSource.clear();
			appListDataSource.addAll(appList);
		}
		
		if (updateAppListAdapter != null) {
			updateAppListAdapter.notifyDataSetChanged();
		}
		Logger.d("refresh update software successfull!");
	}
	
}
