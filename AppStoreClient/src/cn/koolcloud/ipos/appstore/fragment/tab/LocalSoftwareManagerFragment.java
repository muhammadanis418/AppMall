package cn.koolcloud.ipos.appstore.fragment.tab;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.adapter.LocalSoftListAdapter;
import cn.koolcloud.ipos.appstore.entity.AppInfo;
import cn.koolcloud.ipos.appstore.fragment.base.BaseFragment;
import cn.koolcloud.ipos.appstore.utils.Env;

public class LocalSoftwareManagerFragment extends BaseFragment implements OnItemClickListener {
	
	private final int LOCAL_SOFT_LOADED = 0;	

	private LayoutInflater inflater;										//view inflater
	private ListView localSoftListView;										//the list view of local software
	
	private static List<AppInfo> localSoftDataSource = new ArrayList<AppInfo>();	//local soft data source.
	private static LocalSoftListAdapter mSoftListAdapter;							//adapter for local soft list view
	
	public static LocalSoftwareManagerFragment getInstance() {
		LocalSoftwareManagerFragment localSoftFragment = new LocalSoftwareManagerFragment();
		//save params
//		Bundle args = new Bundle();
//		args.putInt("index", index);
//		localSoftFragment.setArguments(args);
		return localSoftFragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflate, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		this.inflater = inflate;
		return inflate.inflate(R.layout.content_list, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
		initViews();
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		showLoading();
		new LocalSoftLoading().start();
	}

	private void initViews() {
		localSoftListView = (ListView) getActivity().findViewById(R.id.contentListView);
		localSoftListView.setOnItemClickListener(this);
	}

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case LOCAL_SOFT_LOADED:
				dismissLoading();
					
				mSoftListAdapter = new LocalSoftListAdapter(application, localSoftDataSource);
				localSoftListView.setAdapter(mSoftListAdapter);
				//rescan installed apps to cache
				application.initApps();
				mSoftListAdapter.notifyDataSetChanged();
				break;
			default:
				break;
			}
		}
		
	};
	
	public static void notifyDataSetChanged(List<AppInfo> dataSource) {
		if (localSoftDataSource != null) {
			
			localSoftDataSource.clear();
			localSoftDataSource.addAll(dataSource);
		}
		
		if (mSoftListAdapter != null) {
			
			mSoftListAdapter.notifyDataSetChanged();
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

	@Override
	public void onItemClick(AdapterView<?> adapterViwe, View view, int position, long arg3) {
		AppInfo appInfo = localSoftDataSource.get(position);
	}
	
}
