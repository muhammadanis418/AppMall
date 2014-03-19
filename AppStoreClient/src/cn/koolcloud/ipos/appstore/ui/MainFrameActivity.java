package cn.koolcloud.ipos.appstore.ui;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;
import cn.koolcloud.ipos.appstore.AppStoreApplication;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.adapter.ActionBarSuggestionsAdapter;
import cn.koolcloud.ipos.appstore.cache.database.CacheDB;
import cn.koolcloud.ipos.appstore.entity.App;
import cn.koolcloud.ipos.appstore.entity.AppInfo;
import cn.koolcloud.ipos.appstore.fragment.LeftNavigationFragment;
import cn.koolcloud.ipos.appstore.fragment.LeftNavigationFragment.OnTabChangedListener;
import cn.koolcloud.ipos.appstore.fragment.tab.LocalSoftwareManagerFragment;
import cn.koolcloud.ipos.appstore.fragment.tab.UpdateSoftwareFragment;
import cn.koolcloud.ipos.appstore.utils.Env;
import cn.koolcloud.ipos.appstore.utils.ToastUtil;
import cn.koolcloud.ipos.appstore.utils.Utils;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.widget.SearchView;

public class MainFrameActivity extends BaseActivity implements SearchView.OnQueryTextListener,
	SearchView.OnSuggestionListener, OnTabChangedListener {
	private static final String TAG = "MainFrameActivity";
	
	private static final int HANDLE_UPDATE_APP = 0;
	private long exitTime = 0;
	private static final int EXIT_LAST_TIME = 2000;
	
	private SearchView searchView;					//search view in action bar	
	private ActionBar actionBar;					//action bar
	
	private boolean useLogo = false;				//action bar logo
    private boolean showHomeUp = false;				//show home up in action bar
	
	private ActionBarSuggestionsAdapter mSuggestionsAdapter;		//adapter for search view
	private static FragmentManager fragmentManager = null;
	
	private final String installedTabStr = "installed_tab";
	private final String canUpdateTabStr = "can_update_tab";
	
	private static MainFrameActivity instance;
	public static MainFrameActivity getInstance() {
		return instance;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;
		
		setContentView(R.layout.main_framework);
		fragmentManager = getSupportFragmentManager();
		activityList.add(this);
		initActionBar();
		
		initFragments();
		getClientVersion();
	}
	
	private void initFragments() {
		//navigation bar
		LeftNavigationFragment navFragment = LeftNavigationFragment.getInstance();
		
		//replace FrameLayout DetailFragment 
		FragmentTransaction fragTransaction = fragmentManager.beginTransaction();
		fragTransaction.replace(R.id.left_content, navFragment);
		fragTransaction.commit();
			
	}
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLE_UPDATE_APP:
				List<App> appList = (List<App>) msg.obj;
				UpdateSoftwareFragment.refreshDataSet(appList);
				break;

			default:
				break;
			}
		}
	};
	
	public void refreshLocalSoftData() {
		/*LocalSoftwareManagerFragment localSoft = LocalSoftwareManagerFragment.getInstance();
		android.app.FragmentTransaction fragTransaction = fragmentManager.beginTransaction();
		fragTransaction.replace(R.id.frame_content, localSoft);
		fragTransaction.commit();*/
		((AppStoreApplication) application).initApps();
		LocalSoftwareManagerFragment.notifyDataSetChanged(Env.getInstalledAppsToList(application, false));
	}
	
	/**
	 * @Title: refreshUpdateSoftData
	 * @Description: update the status of UpdateSoftwareFragement
	 * @return: void
	 */
	public void refreshUpdateSoftData() {
		new CheckUpdateSoftwareThread().start();
	}
	
	//check out the installed software and compare the version with the cache in the database
	class CheckUpdateSoftwareThread extends Thread {

		@Override
		public void run() {
			List<AppInfo> installedSoftList = Env.getInstalledAppsToList(application, false);
			
			CacheDB cacheDB = CacheDB.getInstance(application);
			List<App> appListDataSource = cacheDB.getUpdatedSoft(installedSoftList);
			
			Message msg = mHandler.obtainMessage();
			msg.what = HANDLE_UPDATE_APP;
			msg.obj = appListDataSource;
			mHandler.sendMessage(msg);
			
		}
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
	
	private void setupTabs() {
		Tab installedTab = actionBar.newTab();
		installedTab.setText(Utils.getResourceString(getApplicationContext(), R.string.installed));
		installedTab.setTag(installedTabStr);
		installedTab.setTabListener(new TabSelectListener());
		actionBar.addTab(installedTab);
		
		Tab canUpdatedTab = actionBar.newTab();
		canUpdatedTab.setText(Utils.getResourceString(getApplicationContext(), R.string.can_update));
		canUpdatedTab.setTag(canUpdateTabStr);
		canUpdatedTab.setTabListener(new TabSelectListener());
		actionBar.addTab(canUpdatedTab);
	}
	
	private void showTabsNav() {
        if (actionBar.getNavigationMode() != ActionBar.NAVIGATION_MODE_TABS) {
        	actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }
    }
	
	public class TabSelectListener implements ActionBar.TabListener {
		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			String selectedTabTag = String.valueOf(tab.getTag());
			
			//TODO: must get child transaction and can't use the argument ft,
			//otherwise the programe will throw "commit already called" exception when add the tabs
			FragmentTransaction tran = fragmentManager.beginTransaction();
			if  (selectedTabTag.equals(installedTabStr))  {
				LocalSoftwareManagerFragment localSoftFragment = LocalSoftwareManagerFragment.getInstance();
				tran.replace(R.id.frame_content, localSoftFragment);
				tran.commit();
			}  else if  (selectedTabTag.equals(canUpdateTabStr))  {
				UpdateSoftwareFragment updateSoftFragment = UpdateSoftwareFragment.getInstance();
				tran.replace(R.id.frame_content, updateSoftFragment);
				tran.commit();
			}
			
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		UpdateSoftwareFragment updateSoftFragment = (UpdateSoftwareFragment) fragmentManager.findFragmentById(R.id.frame_content);
		if (updateSoftFragment != null) {
			updateSoftFragment.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN){
		    if ((System.currentTimeMillis() - exitTime) > EXIT_LAST_TIME) {
		    	ToastUtil.showToast(getApplicationContext(), R.string.msg_exist_toast);
		        exitTime = System.currentTimeMillis();
			} else {
				exit();
			}
		    return true;
	    }
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void disableAllTabs() {
		actionBar.removeAllTabs();
	}

	@Override
	public void enableAllTabs() {
		setupTabs();
	}

}
