package cn.koolcloud.ipos.appstore;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import cn.koolcloud.ipos.appstore.fragment.SettingLeftFragment;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;

public class SettingActivity extends BaseActivity {
	private static FragmentManager fragmentManager = null;
	private ActionBar actionBar;					//action bar
	
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
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.title_bg));
        actionBar.setIcon(R.drawable.ic_launcher_back);
        actionBar.setTitle(" "+getResources().getString(R.string.action_label_setting_settings));
	}
	
	private void initFragments() {
		//navigation bar
		SettingLeftFragment navFragment = SettingLeftFragment.getInstance();
		
		//replace FrameLayout DetailFragment 
		FragmentTransaction fragTransaction = fragmentManager.beginTransaction();
		fragTransaction.replace(R.id.left_content, navFragment);
		Bundle bundle = new Bundle();
		navFragment.setArguments(bundle);
		fragTransaction.commit();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}
}
