package cn.koolcloud.ipos.appstore.fragment;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import cn.koolcloud.ipos.appstore.MainActivity;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.adapter.LocalSoftListAdapter;
import cn.koolcloud.ipos.appstore.entity.AppInfo;
import cn.koolcloud.ipos.appstore.utils.Env;
import cn.koolcloud.ipos.appstore.utils.MyLog;

public class LocalSoftwareManagerFragment extends BaseFragment {
	private final int LOCAL_SOFT_LOADED = 0;	
	private GridView appGridView; // the list view of local software
	private List<AppInfo> localSoftDataSource = new ArrayList<AppInfo>(); // local soft data source.
	private LocalSoftListAdapter mSoftListAdapter; // adapter for local soft list view
	
	public static LocalSoftwareManagerFragment getInstance() {
		LocalSoftwareManagerFragment localSoftFragment = new LocalSoftwareManagerFragment();
		return localSoftFragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflate, ViewGroup container,
			Bundle savedInstanceState) {
		return inflate.inflate(R.layout.content_list, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		initViews();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		showLoading();
		new LocalSoftLoading().start();
	}

	private void initViews() {
		appGridView = (GridView) getActivity().findViewById(R.id.appGridView);
	}

	Handler mHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case LOCAL_SOFT_LOADED:
				dismissLoading();
					
				mSoftListAdapter = new LocalSoftListAdapter(getActivity(), localSoftDataSource);
				appGridView.setAdapter(mSoftListAdapter);
				//rescan installed apps to cache
				application.initApps();
				mSoftListAdapter.notifyDataSetChanged();
				MainActivity mainFrameInstance = MainActivity.getInstance();
				mainFrameInstance.refreshInstalledAppNum(localSoftDataSource.size());
				break;
			}
			return false;
		}
	});
	
	public void refreshData(List<AppInfo> dataSource) {
		if (localSoftDataSource != null) {
			localSoftDataSource.clear();
			localSoftDataSource.addAll(dataSource);
		}
		
		if (mSoftListAdapter != null) {
			mSoftListAdapter.notifyDataSetChanged();
			MyLog.e("notifyDataSetChanged");
		}
	}

	//thread for loading local apps
	class LocalSoftLoading extends Thread {
		@Override
		public void run() {
			localSoftDataSource = Env.getInstalledAppsToList(application, false);
			Message msg = mHandler.obtainMessage();
			msg.what = LOCAL_SOFT_LOADED;
			mHandler.sendMessage(msg);
		}
	}
}
