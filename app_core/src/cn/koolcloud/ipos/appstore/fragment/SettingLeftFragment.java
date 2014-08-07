package cn.koolcloud.ipos.appstore.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.adapter.SettingLeftAdapter;
import cn.koolcloud.ipos.appstore.utils.MyLog;

@SuppressLint("CutPasteId")
public class SettingLeftFragment extends BaseFragment implements OnItemClickListener {
	private FragmentManager fragManager;
	private ListView categoryListView;
	private SettingLeftAdapter adapter = null;

	public static SettingLeftFragment getInstance() {
		SettingLeftFragment navFragment = new SettingLeftFragment();
		// save params
		Bundle args = new Bundle();
		navFragment.setArguments(args);
		return navFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflate, ViewGroup container,
			Bundle savedInstanceState) {
		fragManager = getActivity().getSupportFragmentManager();
		return inflate.inflate(R.layout.setting_left, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		categoryListView = (ListView) getActivity().findViewById(R.id.listView);
		MyLog.d("onActivityCreated");
		adapter = new SettingLeftAdapter(getActivity(), 0);
		categoryListView.setAdapter(adapter);
		categoryListView.setOnItemClickListener(this);
		
		//init status
		showAppSettingFragment();
	}

	// show download setting fragment
	private void showDownloadSettingFragment() {
		DownloadSettingFragment downloadSettingFragment = DownloadSettingFragment.getInstance();
		FragmentTransaction fragTransaction = fragManager.beginTransaction();
		fragTransaction.setCustomAnimations(R.anim.activity_open_in_anim,
                R.anim.activity_open_out_anim);
		fragTransaction.replace(R.id.frame_content, downloadSettingFragment);
		fragTransaction.commit();
	}

	// show download setting fragment
	private void showAppSettingFragment() {
		AppSettingFragment appSettingFragment = AppSettingFragment.getInstance();
		FragmentTransaction fragTransaction = fragManager.beginTransaction();
		fragTransaction.setCustomAnimations(R.anim.activity_open_in_anim,
                R.anim.activity_open_out_anim);
		fragTransaction.replace(R.id.frame_content, appSettingFragment);
		fragTransaction.commit();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
		View rootView = categoryListView.getChildAt(position);
		View selectedView = rootView.findViewById(R.id.left_nav_item_root);
		selectedView.setSelected(false);
		rootView.findViewById(R.id.indicator).setVisibility(View.GONE);
		
		//set selected status on the clicking item
//		view.findViewById(R.id.left_nav_item_root).setBackgroundColor(
//				getResources().getColor(R.color.category_select_bg));
		view.findViewById(R.id.indicator).setVisibility(View.VISIBLE);
		adapter.updateSelectedPosition(position);
		
		if (position == 0) {
			showAppSettingFragment();
		} else if (position == 1) {
			showDownloadSettingFragment();
		}
	}
}
