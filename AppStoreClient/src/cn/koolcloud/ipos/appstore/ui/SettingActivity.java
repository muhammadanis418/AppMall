package cn.koolcloud.ipos.appstore.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.ArrayAdapter;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.constant.Constants;
import cn.koolcloud.ipos.appstore.fragment.LeftNavigationFragment;
import cn.koolcloud.ipos.appstore.fragment.LeftNavigationFragment.OnTabChangedListener;
import cn.koolcloud.ipos.appstore.utils.Utils;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;

public class SettingActivity extends BaseActivity implements OnTabChangedListener {
	private static final String TAG = "SettingActivity";
	
	private static FragmentManager fragmentManager = null;
	
	private ActionBar actionBar;					//action bar
	
	private boolean useLogo = false;				//action bar logo
    private boolean showHomeUp = true;				//show home up in action bar
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main_framework);
		fragmentManager = getSupportFragmentManager();
		activityList.add(this);
		initActionBar();
		
		initFragments();
	}
	
	/**
	* @Title: initActionBar
	* @Description: Initialize Action Bar
	* @param 
	* @return void 
	* @throws
	*/
	private void initActionBar() {
		
		actionBar = getSupportActionBar();

        // set defaults for logo & home up
        actionBar.setDisplayHomeAsUpEnabled(showHomeUp);
        actionBar.setDisplayUseLogoEnabled(useLogo);
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.title_bg));
        actionBar.setIcon(R.drawable.logo);
        actionBar.setTitle(Utils.getResourceString(application, R.string.action_label_setting_settings));
        showTabsNav();
		// set up list nav
        actionBar.setListNavigationCallbacks(ArrayAdapter.createFromResource(this, R.array.sections,
                        R.layout.sherlock_spinner_dropdown_item),
                new OnNavigationListener() {
                    public boolean onNavigationItemSelected(int itemPosition,
                            long itemId) {
                        // FIXME add proper implementation
                        return false;
                    }
                });
		
	}
	
	private void initFragments() {
		//navigation bar
		LeftNavigationFragment navFragment = LeftNavigationFragment.getInstance();
		
		//replace FrameLayout DetailFragment 
		FragmentTransaction fragTransaction = fragmentManager.beginTransaction();
		fragTransaction.replace(R.id.left_content, navFragment);
		Bundle bundle = new Bundle();
		bundle.putBoolean(Constants.IS_SETTING_KEY, true);
		navFragment.setArguments(bundle);
		fragTransaction.commit();
			
	}
	
	private void showTabsNav() {
        if (actionBar.getNavigationMode() != ActionBar.NAVIGATION_MODE_TABS) {
        	actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }
    }

	@Override
	public void disableAllTabs() {
		// TODO Auto-generated method stub
		actionBar.removeAllTabs();
	}

	@Override
	public void enableAllTabs() {
		// TODO Auto-generated method stub
	}
}
