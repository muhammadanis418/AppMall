package cn.koolcloud.ipos.appstore.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.constant.Constants;
import cn.koolcloud.ipos.appstore.fragment.base.BaseFragment;
import cn.koolcloud.ipos.appstore.fragment.tab.AppSettingFragment;
import cn.koolcloud.ipos.appstore.fragment.tab.CategoryFragment;
import cn.koolcloud.ipos.appstore.fragment.tab.DownloadSettingFragment;
import cn.koolcloud.ipos.appstore.fragment.tab.LocalSoftwareManagerFragment;
import cn.koolcloud.ipos.appstore.fragment.tab.NoNetworkFragment;
import cn.koolcloud.ipos.appstore.utils.NetUtil;
import cn.koolcloud.ipos.appstore.utils.ViewUtils;

public class LeftNavigationFragment extends BaseFragment implements View.OnClickListener {

	private LinearLayout leftNavLinearLayout;		//obtain every item bar
	private ImageView indicatorImageView;			//navigation indicator
	
	//left navigation items
	private View navfirstPageView;
	private View navRankingPageView;
	private View navCategoryPageView;
	private View navTopicPageView;
	private View navManagementPageView;
	
	//left navigation items in setting page
	private View navAppSettingPageView;
	private View navDownloadSettingPageView;
	
	private LayoutInflater inflater;										//view inflater
	
	private int currentNavItem = Constants.NAV_ITEM_FIRST_PAGE;				//current navigation item selected tag
	private boolean isAddedTabs = false;									//if the tab is added to the action bar.
	
	private static Handler mHandler;
	
	private FragmentManager fragManager;
	
	private OnTabChangedListener mCallback;
	
	// Container Activity must implement this interface
    public interface OnTabChangedListener {
        public void disableAllTabs();
        public void enableAllTabs();
    }
	
	public static LeftNavigationFragment getInstance() {
		LeftNavigationFragment navFragment = new LeftNavigationFragment();
		//save params
		Bundle args = new Bundle();
//		args.putInt("index", index);
		navFragment.setArguments(args);
		return navFragment;
	}
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		this.inflater = inflate;
		return inflate.inflate(R.layout.left_nav, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null) {
            // Restore last state for checked position.
            currentNavItem = savedInstanceState.getInt("curChoice", 0);
        }
		
		leftNavLinearLayout = (LinearLayout) getActivity().findViewById(R.id.left_nav_layout);
		indicatorImageView = (ImageView) getActivity().findViewById(R.id.indicator);
		fragManager = getFragmentManager();
		boolean isSettingFragment = getArguments().getBoolean(Constants.IS_SETTING_KEY);
		initLeftNavItem(isSettingFragment);
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Boolean isSettingFragment = getArguments().getBoolean(Constants.IS_SETTING_KEY);
		if (isSettingFragment) {
			if (currentNavItem == Constants.NAV_ITEM_APP_SETTING) {
				showAppSettingFragment();
			} else {
				showDownloadSettingFragment();
			}
		} else {
			//fix back bug from search or setting activity
//			showCategoryFragment();
			showNavigationContent();
		}
	}
	
	@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("curChoice", currentNavItem);
    }

	private void initLeftNavItem(Boolean isSettingFragment) {
		
		if (isSettingFragment) {
			navAppSettingPageView = ViewUtils.inflateNavigationBarItem(getActivity(), inflater, 
					R.drawable.left_app_setting, R.string.msg_setting_app_setting_title, 
					Constants.NAV_ITEM_APP_SETTING, leftNavLinearLayout);
			navAppSettingPageView.setOnClickListener(this);
			navAppSettingPageView.setSelected(true);
			this.currentNavItem = Constants.NAV_ITEM_APP_SETTING;
			
			navDownloadSettingPageView = ViewUtils.inflateNavigationBarItem(getActivity(), inflater,
					R.drawable.left_install_selected, R.string.msg_setting_download_title, 
					Constants.NAV_ITEM_DOWNLOAD_SETTING, leftNavLinearLayout);
			navDownloadSettingPageView.setOnClickListener(this);
		} else {
			
			/*
			navfirstPageView = ActivityViewUtils.inflateNavigationBarItem(getApplicationContext(), inflater, 
					R.drawable.left_tuijian, R.string.home, 
					Constants.NAV_ITEM_FIRST_PAGE, leftNavLinearLayout);
			navfirstPageView.setOnClickListener(this);
			navfirstPageView.setSelected(true);
			this.currentNavItem = Constants.NAV_ITEM_FIRST_PAGE;
			
			navRankingPageView = ActivityViewUtils.inflateNavigationBarItem(getApplicationContext(), inflater, 
					R.drawable.left_paihang, R.string.ranking, 
					Constants.NAV_ITEM_RANKING, leftNavLinearLayout);
			navRankingPageView.setOnClickListener(this);
			 */
			navCategoryPageView = ViewUtils.inflateNavigationBarItem(getActivity(), inflater, 
					R.drawable.left_feilei, R.string.category, 
					Constants.NAV_ITEM_CATEGORY, leftNavLinearLayout);
			navCategoryPageView.setOnClickListener(this);
			navCategoryPageView.setSelected(true);
			this.currentNavItem = Constants.NAV_ITEM_CATEGORY;
			/*
			navTopicPageView = ActivityViewUtils.inflateNavigationBarItem(getApplicationContext(), inflater, 
					R.drawable.left_zhuanti, R.string.topic, 
					Constants.NAV_ITEM_TOPIC, leftNavLinearLayout);
			navTopicPageView.setOnClickListener(this);
			 */
			navManagementPageView = ViewUtils.inflateNavigationBarItem(getActivity(), inflater,
					R.drawable.left_guanli, R.string.management, 
					Constants.NAV_ITEM_MANAGEMENT, leftNavLinearLayout);
			navManagementPageView.setOnClickListener(this);
		}
	}
	
	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		indicatorImageView.setVisibility(View.INVISIBLE);
		leftNavLinearLayout.findViewWithTag(currentNavItem).setSelected(false);
		ImageView indicator = (ImageView) leftNavLinearLayout.findViewWithTag(currentNavItem).findViewById(R.id.indicator);
		indicator.setVisibility(View.INVISIBLE);
		view.setSelected(true);
		
		//tab show or not on action bar
		mCallback.disableAllTabs();
		
		if (view.getTag() == Constants.NAV_ITEM_FIRST_PAGE) {
			
			this.currentNavItem = Constants.NAV_ITEM_FIRST_PAGE;
		} else if (view.getTag() == Constants.NAV_ITEM_RANKING) {
			
			this.currentNavItem = Constants.NAV_ITEM_RANKING;
		} else if (view.getTag() == Constants.NAV_ITEM_CATEGORY) {
			this.currentNavItem = Constants.NAV_ITEM_CATEGORY;
			
			showCategoryFragment();
		} else if (view.getTag() == Constants.NAV_ITEM_TOPIC) {
			
			this.currentNavItem = Constants.NAV_ITEM_TOPIC;
		} else if (view.getTag() == Constants.NAV_ITEM_MANAGEMENT) {
			if (!isAddedTabs) {
				mCallback.enableAllTabs();
			}
			
			showLocalSoft();
			this.currentNavItem = Constants.NAV_ITEM_MANAGEMENT;
		} else if (view.getTag() == Constants.NAV_ITEM_APP_SETTING) {
			//TODO: show app setting fragment
			showAppSettingFragment();
			this.currentNavItem = Constants.NAV_ITEM_APP_SETTING;
		} else if (view.getTag() == Constants.NAV_ITEM_DOWNLOAD_SETTING) {
			//TODO: show app installing fragment
			showDownloadSettingFragment();
			this.currentNavItem = Constants.NAV_ITEM_DOWNLOAD_SETTING;
		}
		
		ImageView selectedIndicator = (ImageView) leftNavLinearLayout.findViewWithTag(currentNavItem).findViewById(R.id.indicator);
		selectedIndicator.setVisibility(View.VISIBLE);
//		runIndicatorAnimation(currentNavItem, indicatorImageView);
	}
	
	private void runIndicatorAnimation(int navItemNo, final View view) {
		// Calculate ActionBar height
		int actionBarHeight = 0;
		TypedValue tv = new TypedValue();
		if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
		    actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
		}
		
		int itemHeight = getResources().getDimensionPixelSize(R.dimen.left_nav_item_height);
		
		final int endPosition = navItemNo * itemHeight + itemHeight / 2 + actionBarHeight;
		
		int viewXPosition = ViewUtils.getViewLocationScreenX(view);
		int viewYPosition = ViewUtils.getViewLocationScreenY(view);
		
		AnimationSet animationSet = new AnimationSet(true);
		TranslateAnimation transAnimation = new TranslateAnimation(
				 Animation.RELATIVE_TO_SELF, viewXPosition,
				 Animation.RELATIVE_TO_SELF, viewXPosition,
				 Animation.RELATIVE_TO_SELF, viewYPosition,
				 Animation.RELATIVE_TO_SELF, endPosition);
		transAnimation.setDuration(2000);
		animationSet.addAnimation(transAnimation);
		view.startAnimation(animationSet);
		
		transAnimation.setAnimationListener(new Animation.AnimationListener() {  
		    @Override  
		    public void onAnimationStart(Animation animation) {  
		    }  
		      
		    @Override  
		    public void onAnimationRepeat(Animation animation) {  
		    }  
		      
		    @Override  
		    public void onAnimationEnd(Animation animation) {  
		        int left = view.getLeft();  
		        int top = view.getTop();  
		        int width = view.getWidth();  
		        int height = view.getHeight();  
		        view.clearAnimation();  
		        view.layout(left, endPosition, left + width, top + height);  
		    }  
		});  
		
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
		fragTransaction.replace(R.id.frame_content, localSoftFragment);
		fragTransaction.commit();
			
	}
	
	//show download setting fragment
	private void showDownloadSettingFragment() {
		DownloadSettingFragment downloadSettingFragment = DownloadSettingFragment.getInstance();
		FragmentTransaction fragTransaction = fragManager.beginTransaction();
		fragTransaction.replace(R.id.frame_content, downloadSettingFragment);
		fragTransaction.commit();
	}
	
	//show download setting fragment
	private void showAppSettingFragment() {
		AppSettingFragment appSettingFragment = AppSettingFragment.getInstance();
		FragmentTransaction fragTransaction = fragManager.beginTransaction();
		fragTransaction.replace(R.id.frame_content, appSettingFragment);
		fragTransaction.commit();
	}
	
	//show local soft fragment
	private void showCategoryFragment() {
		//fragment management
		FragmentTransaction fragTransaction = fragManager.beginTransaction();
		if (NetUtil.isAvailable(getActivity())) {
			CategoryFragment categoryFragment = CategoryFragment.getInstance();
			fragTransaction.replace(R.id.frame_content, categoryFragment);
		} else {
			NoNetworkFragment noNetFragment = NoNetworkFragment.getInstance();
			fragTransaction.replace(R.id.frame_content, noNetFragment);
		}
		fragTransaction.commit();
			
	}
	
}
