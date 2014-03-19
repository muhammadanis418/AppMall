package cn.koolcloud.ipos.appstore.ui;

import java.util.Map;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.ArrayAdapter;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.fragment.LeftCategoryFragment;
import cn.koolcloud.ipos.appstore.fragment.tab.NormalListFragment;
import cn.koolcloud.ipos.appstore.utils.Env;
import cn.koolcloud.ipos.appstore.utils.Logger;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.widget.SearchView;

public class CategoryMainActivity extends BaseActivity {
	private final static String TAG = "CategoryMainActivity";
	
	private final int INIT_APP_INFOS = 0;
	private SearchView searchView;					//search view in action bar	
	private ActionBar actionBar;					//action bar
	
	private boolean useLogo = false;				//action bar logo
    private boolean showHomeUp = true;				//show home up in action bar

    private FragmentManager fragmentManager = null;
    
    private Bundle mBundle;
    private static CategoryMainActivity instance;
        
    public static CategoryMainActivity getInstance() {
		return instance;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;
		
		setContentView(R.layout.main_framework);
		fragmentManager = getSupportFragmentManager();
		mBundle = getIntent().getExtras();
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
		LeftCategoryFragment navFragment = LeftCategoryFragment.getInstance();
		navFragment.setArguments(mBundle);
		
		//replace FrameLayout with left category fragment and home frame with apps fragment 
		FragmentTransaction fragTransaction = fragmentManager.beginTransaction();
		fragTransaction.replace(R.id.left_content, (Fragment) navFragment);
		fragTransaction.commit();
			
	}
	
	public void refreshLocalSoftData() {
		/*((AppStoreApplication) application).initApps();
		try {
			//delay execute invoke refresh data set
			Thread.currentThread().sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
//		NormalListFragment.refreshDataSet();
		new InitAppsThread().start();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
		NormalListFragment normalFragment = (NormalListFragment) fragmentManager.findFragmentById(R.id.frame_content);
		if (normalFragment != null) {
			
			normalFragment.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	/*@Override
	protected void onDestroy() {
		activityList.remove(this);
		super.onDestroy();
	}*/
	
	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case INIT_APP_INFOS:
				NormalListFragment.refreshDataSet();
				break;

			default:
				break;
			}
		}
		
	};
	
	//init apps in a new thread
	class InitAppsThread extends Thread {

		@Override
		public void run() {
			Logger.d("invoke scan local apps");
			Long start = System.currentTimeMillis();
			Map<String, PackageInfo> installedPackage = Env.scanInstalledAppToMap(getApplicationContext());
			Long end = System.currentTimeMillis();
			Logger.d("total time:" + (end - start));

			//cache installed package
			application.saveInstalledAppsInfo(installedPackage);
			Message msg = mHandler.obtainMessage();
			msg.obj = installedPackage;
			msg.what = INIT_APP_INFOS;
			mHandler.sendMessage(msg);
		}
		
	}
	
}
