package cn.koolcloud.ipos.appstore;

import java.util.Map;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.widget.ArrayAdapter;
import cn.koolcloud.ipos.appstore.fragment.CategoryLeftFragment;
import cn.koolcloud.ipos.appstore.fragment.CategoryRightFragment;
import cn.koolcloud.ipos.appstore.utils.Env;
import cn.koolcloud.ipos.appstore.utils.MyLog;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.view.MenuItem;

public class CategoryActivity extends BaseActivity {
	private final int INIT_APP_INFOS = 0;
	private ActionBar actionBar;					//action bar
	private boolean useLogo = false;				//action bar logo
    private FragmentManager fragmentManager = null;
    private Bundle mBundle;
    private static CategoryActivity instance;
        
    public static CategoryActivity getInstance() {
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
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayUseLogoEnabled(useLogo);
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.title_bg));
        actionBar.setIcon(R.drawable.ic_launcher_back);
        actionBar.setTitle(" "+getResources().getString(R.string.app_category));
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
		CategoryLeftFragment navFragment = CategoryLeftFragment.getInstance();
		navFragment.setArguments(mBundle);
		
		FragmentTransaction fragTransaction = fragmentManager.beginTransaction();
		fragTransaction.replace(R.id.left_content, (Fragment) navFragment);
		fragTransaction.commit();
	}
	
	public void refreshLocalSoftData() {
		new InitAppsThread().start();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		CategoryRightFragment normalFragment = (CategoryRightFragment) fragmentManager.findFragmentById(R.id.frame_content);
		if (normalFragment != null) {
			normalFragment.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	Handler mHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case INIT_APP_INFOS:
				CategoryRightFragment.refreshDataSet();
				break;
			}
			return false;
		}
	});
	
	//init apps in a new thread
	class InitAppsThread extends Thread {
		@Override
		public void run() {
			MyLog.d("invoke scan local apps");
			Long start = System.currentTimeMillis();
			Map<String, PackageInfo> installedPackage = Env.scanInstalledAppToMap(getApplicationContext());
			Long end = System.currentTimeMillis();
			MyLog.d("total time:" + (end - start));

			//cache installed package
			application.saveInstalledAppsInfo(installedPackage);
			Message msg = mHandler.obtainMessage();
			msg.obj = installedPackage;
			msg.what = INIT_APP_INFOS;
			mHandler.sendMessage(msg);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}
}
