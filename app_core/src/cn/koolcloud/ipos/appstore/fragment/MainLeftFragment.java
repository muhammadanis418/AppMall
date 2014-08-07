package cn.koolcloud.ipos.appstore.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import cn.koolcloud.ipos.appstore.AboutDialogActivity;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.SettingActivity;
import cn.koolcloud.ipos.appstore.adapter.MainLeftNaviAdapter;
import cn.koolcloud.ipos.appstore.constant.Constants;
import cn.koolcloud.ipos.appstore.utils.NetUtil;

@SuppressLint("CutPasteId")
public class MainLeftFragment extends BaseFragment implements OnItemClickListener {
	public static int currentNavItem = Constants.NAV_ITEM_CATEGORY;				//current navigation item selected tag
	private boolean isAddedTabs = false;									//if the tab is added to the action bar.
	private FragmentManager fragManager;
	private OnTabChangedListener mCallback;
	private ListView mainLeftListView;
	private MainLeftNaviAdapter mainLeftNaviAdapter;

	// Container Activity must implement this interface
    public interface OnTabChangedListener {
        public void disableAllTabs();
        public void enableAllTabs();
    }
	
	public static MainLeftFragment getInstance() {
		MainLeftFragment navFragment = new MainLeftFragment();
		//save params
		Bundle args = new Bundle();
		navFragment.setArguments(args);
		return navFragment;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnTabChangedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnTabChangedListener");
        }
	}

	@Override
	public View onCreateView(LayoutInflater inflate, ViewGroup container,
			Bundle savedInstanceState) {
		return inflate.inflate(R.layout.main_left_nav, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null) {
            // Restore last state for checked position.
            currentNavItem = savedInstanceState.getInt("curChoice", 0);
        }
		
		fragManager = getFragmentManager();
		initLeftNavItem();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		showNavigationContent();
	}
	
	@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("curChoice", currentNavItem);
    }

	private void initLeftNavItem() {
		currentNavItem = Constants.NAV_ITEM_CATEGORY;
		mainLeftListView = (ListView) getActivity().findViewById(R.id.mainLeftListView);
		mainLeftNaviAdapter = new MainLeftNaviAdapter(getActivity());
		mainLeftListView.setAdapter(mainLeftNaviAdapter);
		mainLeftListView.setOnItemClickListener(this);
	}
	
	private void showNavigationContent() {
		mCallback.disableAllTabs();
		if (currentNavItem == Constants.NAV_ITEM_CATEGORY) {
			showCategoryFragment(); 
		} else if (currentNavItem == Constants.NAV_ITEM_MANAGEMENT) {
			if (!isAddedTabs) {
				mCallback.enableAllTabs();
			}
			
			showLocalSoft();
		}
	}
	
	//show local soft fragment
	private void showLocalSoft() {
		//fragment management
		LocalSoftwareManagerFragment localSoftFragment = LocalSoftwareManagerFragment.getInstance();
		FragmentTransaction fragTransaction = fragManager.beginTransaction();
//		fragTransaction.setCustomAnimations(R.anim.activity_open_in_anim,
//                R.anim.activity_open_out_anim);
		fragTransaction.replace(R.id.frame_content, localSoftFragment);
		fragTransaction.commit();
	}
	
	//show local soft fragment
	private void showCategoryFragment() {
		//fragment management
		FragmentTransaction fragTransaction = fragManager.beginTransaction();
//		fragTransaction.setCustomAnimations(R.anim.activity_open_in_anim,
//                R.anim.activity_open_out_anim);
		if (NetUtil.isAvailable(getActivity())) {
			MainRightFragment categoryFragment = MainRightFragment.getInstance();
			fragTransaction.replace(R.id.frame_content, categoryFragment);
		} else {
			NoNetworkFragment noNetFragment = NoNetworkFragment.getInstance();
			fragTransaction.replace(R.id.frame_content, noNetFragment);
		}
		fragTransaction.commit();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		switch (position) {
		case 0: // category
			mCallback.disableAllTabs();
			currentNavItem = Constants.NAV_ITEM_CATEGORY;
			showCategoryFragment();
			mainLeftNaviAdapter.notifyDataSetChanged();
			break;
		case 1: // management
			mCallback.disableAllTabs();
			if (!isAddedTabs) {
				mCallback.enableAllTabs();
			}
			
			showLocalSoft();
			currentNavItem = Constants.NAV_ITEM_MANAGEMENT;
			mainLeftNaviAdapter.notifyDataSetChanged();
			break;
		case 2:
			Intent intent = new Intent(getActivity(), SettingActivity.class);
			getActivity().startActivity(intent);
			break;
		case 3:
			getActivity().startActivity(new Intent(getActivity(), AboutDialogActivity.class));
			break;
		}
	}
}
