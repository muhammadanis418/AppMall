package cn.koolcloud.ipos.appstore.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cn.koolcloud.ipos.appstore.R;

public class NoNetworkFragment extends BaseFragment {
	

	
	public static NoNetworkFragment getInstance() {
		NoNetworkFragment noNetworkFragment = new NoNetworkFragment();
		//save params
//		Bundle args = new Bundle();
//		args.putInt("index", index);
//		localSoftFragment.setArguments(args);
		return noNetworkFragment;
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
		return inflate.inflate(R.layout.no_network_view, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}
	
}
