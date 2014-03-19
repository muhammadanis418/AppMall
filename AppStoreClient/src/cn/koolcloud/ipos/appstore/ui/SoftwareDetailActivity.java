package cn.koolcloud.ipos.appstore.ui;

import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import cn.koolcloud.ipos.appstore.AppStoreApplication;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.adapter.ActionBarSuggestionsAdapter;
import cn.koolcloud.ipos.appstore.constant.Constants;
import cn.koolcloud.ipos.appstore.fragment.SoftwareDetailCommentFragment;
import cn.koolcloud.ipos.appstore.fragment.SoftwareDetailCommentFragment.OnFragmentActionListener;
import cn.koolcloud.ipos.appstore.fragment.SoftwareDetailLeftFragment;
import cn.koolcloud.ipos.appstore.fragment.SoftwareDetailLeftFragment.OnSoftwareDetailLeftAttachedListener;
import cn.koolcloud.ipos.appstore.fragment.SoftwareDetailMainFragment;
import cn.koolcloud.ipos.appstore.fragment.SoftwareDetailMainFragment.OnSoftwareDetailAttachedListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.widget.SearchView;

public class SoftwareDetailActivity extends BaseActivity implements SearchView.OnQueryTextListener,
	SearchView.OnSuggestionListener, ActionBar.TabListener,
	OnSoftwareDetailLeftAttachedListener, OnSoftwareDetailAttachedListener, OnFragmentActionListener {
	private static final String TAG = "SoftwareDetailActivity";
	
	private SearchView searchView;											//search view in action bar	
	private ActionBar actionBar;											//action bar
	
	private boolean useLogo = false;										//action bar logo
    private boolean showHomeUp = true;										//show home up in action bar
	
	private ActionBarSuggestionsAdapter mSuggestionsAdapter;				//adapter for search view
	private boolean isAddedTabs = false;									//if the tab is added to the action bar.
	private LinearLayout softDetailsContainerLayout;						//contain all the details of app
	private LinearLayout waitingViewContainerLayout;						//contain all the details of waiting view
	private RelativeLayout noNetWorkContainerLayout;						//contain all the details of no network
	private ViewStub viewStub;
	
	private static FragmentManager fragmentManager = null;
//	private List<App> appListDataSource = null;								//apps data source
//	private int currentPosition = 0;
//	private App app = null;
	private Bundle mBundle;
	
	private static SoftwareDetailActivity instance;
	
	public static SoftwareDetailActivity getInstance() {
		return instance;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;
		
		setContentView(R.layout.software_detail);
		fragmentManager = getSupportFragmentManager();
		mBundle = getIntent().getExtras();
		
		/*appListDataSource = (List<App>) getIntent().getExtras().getSerializable(Constants.SER_KEY);
		currentPosition = getIntent().getExtras().getInt(Constants.APP_LIST_POSITION);
		app = appListDataSource.get(currentPosition);*/
		
		activityList.add(this);
		initActionBar();
		
		initFragments();
	}
	
	private void initFragments() {
//		softDetailsContainerLayout = (LinearLayout) findViewById(R.id.software_content);
//		softDetailsContainerLayout.setVisibility(View.VISIBLE);
		viewStub = (ViewStub) findViewById(R.id.viewstub);
		viewStub.setVisibility(View.VISIBLE);
		waitingViewContainerLayout = (LinearLayout) findViewById(R.id.software_loading);
		waitingViewContainerLayout.setVisibility(View.GONE);
		noNetWorkContainerLayout = (RelativeLayout) findViewById(R.id.nonetwork);
		noNetWorkContainerLayout.setVisibility(View.GONE);
		
		
		/*SoftwareDetailLeftFragment detailLeftFragment = (SoftwareDetailLeftFragment) fragmentManager.findFragmentById(R.id.software_detail_left);
		detailLeftFragment.setArguments(mBundle);
		SoftwareDetailMainFragment detailMainFragment = (SoftwareDetailMainFragment) fragmentManager.findFragmentById(R.id.software_detail_main);
		detailMainFragment.setArguments(mBundle);*/
			
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
	
	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
		}
	};
	
	private void showTabsNav() {
        if (actionBar.getNavigationMode() != ActionBar.NAVIGATION_MODE_TABS) {
        	actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }
    }
	
	/*@Override
	protected void onDestroy() {
		activityList.remove(this);
		super.onDestroy();
	}*/

	private void initSearchView() {
		 //Create the search view
		searchView = new SearchView(getSupportActionBar().getThemedContext());
        searchView.setQueryHint("Search for apps¡­");
        searchView.setOnQueryTextListener(this);
        searchView.setOnSuggestionListener(this);
		
        if (mSuggestionsAdapter == null) {
            MatrixCursor cursor = new MatrixCursor(Constants.COLUMNS);
            cursor.addRow(new String[]{"1", "'Murica"});
            cursor.addRow(new String[]{"2", "Canada"});
            cursor.addRow(new String[]{"3", "Denmark"});
            mSuggestionsAdapter = new ActionBarSuggestionsAdapter(getSupportActionBar().getThemedContext(), cursor);
        }

        searchView.setSuggestionsAdapter(mSuggestionsAdapter);
	}
	
	public void refreshLocalSoftData() {
		((AppStoreApplication) application).initApps();
		try {
			//delay execute invoke refresh data set
			Thread.currentThread().sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		/*SoftwareDetailLeftFragment detailLeftFragment = (SoftwareDetailLeftFragment) fragmentManager.findFragmentById(R.id.software_detail_left);
		if (detailLeftFragment != null) {
			
			detailLeftFragment.refreshDataStatus();
		}*/
		SoftwareDetailLeftFragment.refreshDataStatus();
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	//implement these two interfaces to pass argument after the fragments created on the host activity
	//otherwise fragmentManager will find a null fragment.
	@Override
	public void onDetailLeftActivityCreated() {
		SoftwareDetailLeftFragment detailLeftFragment = (SoftwareDetailLeftFragment) fragmentManager.findFragmentById(R.id.software_detail_left);
		detailLeftFragment.setArguments(mBundle);
	}

	@Override
	public void onDetailActivityCreated() {
		SoftwareDetailMainFragment detailLeftFragment = (SoftwareDetailMainFragment) fragmentManager.findFragmentById(R.id.software_detail_main);
		detailLeftFragment.setArguments(mBundle);
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SoftwareDetailLeftFragment.SOFTWARE_DETAIL_LEFT_REQUEST) {
			
			SoftwareDetailLeftFragment detailLeftFragment = (SoftwareDetailLeftFragment) fragmentManager.findFragmentById(R.id.software_detail_left);
			if (detailLeftFragment != null) {
				
				detailLeftFragment.onActivityResult(requestCode, resultCode, data);
			}
		}
		if (requestCode == SoftwareDetailMainFragment.SOFTWARE_DETAIL_MAIN_COMMENT_REQUEST) {
			SoftwareDetailMainFragment detailFragment = (SoftwareDetailMainFragment) fragmentManager.findFragmentById(R.id.software_detail_main);
			if (detailFragment != null) {
				
				detailFragment.onActivityResult(requestCode, resultCode, data);
			}
		}
	}

	@Override
	public void closeCommentFragment() {
		SoftwareDetailCommentFragment commentFragment = (SoftwareDetailCommentFragment) fragmentManager.findFragmentById(R.id.software_detail_main);
		if (commentFragment != null) {
			fragmentManager.beginTransaction().remove(commentFragment).commit();
			
		}
	}
	

}
