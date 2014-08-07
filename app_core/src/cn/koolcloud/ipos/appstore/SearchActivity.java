package cn.koolcloud.ipos.appstore;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import cn.koolcloud.ipos.appstore.adapter.GeneralAppsListAdapter;
import cn.koolcloud.ipos.appstore.api.ApiService;
import cn.koolcloud.ipos.appstore.constant.Constants;
import cn.koolcloud.ipos.appstore.download.multithread.DownloadTaskReceiver;
import cn.koolcloud.ipos.appstore.download.multithread.MultiThreadService;
import cn.koolcloud.ipos.appstore.entity.App;
import cn.koolcloud.ipos.appstore.entity.ResultSet;
import cn.koolcloud.ipos.appstore.interfaces.CallBack;
import cn.koolcloud.ipos.appstore.utils.Env;
import cn.koolcloud.ipos.appstore.utils.JsonUtils;
import cn.koolcloud.ipos.appstore.utils.MyLog;
import cn.koolcloud.ipos.appstore.utils.MySPEdit;
import cn.koolcloud.ipos.appstore.utils.ToastUtil;
import cn.koolcloud.ipos.appstore.utils.Utils;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;

public class SearchActivity extends BaseActivity implements SearchView.OnQueryTextListener,
		SearchView.OnSuggestionListener, OnItemClickListener {
	public final static int SEARCH_ACTIVITY_REQUEST = 3;
	public static final int GENERAL_APPS_INSTALL_REQUEST = 1;
	private static final int HANDLE_UPDATE_APP = 0;
	private SearchView searchView;					//search view in action bar	
	private ActionBar actionBar;					//action bar
	private boolean useLogo = false;				//action bar logo
    private Bundle mBundle;
    private GridView searchResultGridView;								//the list view of apps
    private List<App> appListDataSource = new ArrayList<App>();				//apps data source
    private GeneralAppsListAdapter mAppListAdapter;							//adapter for app list view
    private String keyWord = "";
    private static SearchActivity instance;
	protected MyDownloadReceiver myDownlaodReceover;
        
    public static SearchActivity getInstance() {
		return instance;
	}
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;
		
		setContentView(R.layout.search_list_grid);
		
		mBundle = getIntent().getExtras();
		keyWord = mBundle.getString(Constants.SEARCH_WORD_KEY);
		activityList.add(this);
		initActionBar();
		initViews();
			
		getAppsBySearchWord();
        
        myDownlaodReceover = new MyDownloadReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.ACTION_TASK_STARTED);
		filter.addAction(Constants.ACTION_TASK_PAUSED);
		filter.addAction(Constants.ACTION_TASK_FINISHED);
		filter.addAction(Constants.ACTION_TASK_UPDATED);
		filter.addAction(Constants.ACTION_TASK_ERROR);
		registerReceiver(myDownlaodReceover, filter);
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
        actionBar.setTitle("\"" + keyWord + "\"" +
        		Utils.getResourceString(application, R.string.search_activity_result));
        
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
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void initViews() {
		searchResultGridView = (GridView) findViewById(R.id.searchResultGridView);
		
		searchResultGridView.setOnItemClickListener(this);
	}
	
	private Handler mHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLE_UPDATE_APP:
				if (mAppListAdapter != null) {
					mAppListAdapter.notifyDataSetChanged();
				}
				break;
			case GENERAL_APPS_INSTALL_REQUEST:
				File file = (File) msg.obj;
				if (file.exists()) {
					Env.install(instance, file, GENERAL_APPS_INSTALL_REQUEST);
				}
				
				break;
			}
			return false;
		}
	});
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
        
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setQueryHint(Utils.getResourceString(application, R.string.search_message_hint));
        searchView.setOnQueryTextListener(this);
        searchView.setSubmitButtonEnabled(true);
        searchView.setIconifiedByDefault(true);
        
        return true;
    }
	
	public void getAppsBySearchWord() {
		ApiService.getAppsByKeyWord(application, MySPEdit.getTerminalID(application), 
				keyWord, getAppsCallBack);
	}
	
	private CallBack getAppsCallBack = new CallBack() {
		@Override
		public void onCancelled() {
			dismissLoading();
		}

		@Override
		public void onStart() {
			showLoading();
		}

		@Override
		public void onSuccess(JSONObject jsonObj) {
			try {
				MyLog.d("-------searching app Info=" + jsonObj.toString());
				
				appListDataSource = JsonUtils.parseSearchingJSONApps(jsonObj);
				if (appListDataSource != null && appListDataSource.size() > 0) {
					mAppListAdapter = new GeneralAppsListAdapter(SearchActivity.this, appListDataSource, application);
					searchResultGridView.setAdapter(mAppListAdapter);
					mAppListAdapter.notifyDataSetChanged();
				}
				
				dismissLoading();
			} catch (Exception e) {
				e.printStackTrace();
				onFailure("searching app response error!");
			}
		}

		@Override
		public void onFailure(String msg) {
			dismissLoading();
			MyLog.d("describe=" + msg);
			if (msg.contains(ResultSet.NET_ERROR.describe)) {
				
				ToastUtil.showToast(application, R.string.nonetwork_prompt_server_error);
			}
		}
	};
	
	@Override
	public boolean onQueryTextSubmit(String query) {
		actionBar.setTitle("\"" + query + "\"" + Utils.getResourceString(application, R.string.search_activity_result));
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
		MyLog.d(query);
		
		this.keyWord = query;
		getAppsBySearchWord();
		return true;
	}
	
	@Override
	public boolean onQueryTextChange(String newText) {
		return super.onQueryTextChange(newText);
	}
	
	@Override
	protected void onResume() {
		if(mAppListAdapter != null)
			mAppListAdapter.notifyDataSetChanged();
		super.onResume();
	}
	
	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {
			
		Intent mIntent = new Intent(application, AppDetailActivity.class);
		Bundle mBundle = new Bundle();
		mBundle.putSerializable(Constants.SER_KEY, (Serializable) appListDataSource);
		mBundle.putInt(Constants.APP_LIST_POSITION, position); //ListView header is on first position so to remove one
		mIntent.putExtras(mBundle);
		startActivity(mIntent);
	}
	
	public void refreshUpdateSoftData() {
		new CheckUpdateSoftwareThread().start();
	}
	
	//check out the installed software and compare the version with the cache in the database
	class CheckUpdateSoftwareThread extends Thread {
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
			msg.what = HANDLE_UPDATE_APP;
			mHandler.sendMessage(msg);
		}
	}
	
	@Override
	protected void onDestroy() {
		try {
			unregisterReceiver(myDownlaodReceover);
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onDestroy();
	}
	
	class MyDownloadReceiver extends DownloadTaskReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			super.onReceive(context, intent);
		}

		@Override
		public void downloadStart(String taskPkgName) {
			super.downloadStart(taskPkgName);
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			map.put(taskPkgName, MultiThreadService.IS_DOWNLOADING);
			mAppListAdapter.stateChanged(map);
		}

		@Override
		public void downloadPause(String taskPkgName) {
			super.downloadPause(taskPkgName);
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			map.put(taskPkgName, MultiThreadService.IS_PAUSEING);
			mAppListAdapter.stateChanged(map);
		}

		@Override
		public void downloadResumed(String taskPkgName) {
			super.downloadResumed(taskPkgName);
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			map.put(taskPkgName, MultiThreadService.IS_DOWNLOADING);
			mAppListAdapter.stateChanged(map);
		}

		@Override
		public void downloadFinished(String taskPkgName) {
			super.downloadFinished(taskPkgName);
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			map.put(taskPkgName, MultiThreadService.HAVE_FINISHED);
			mAppListAdapter.stateChanged(map);
		}

		@Override
		public void downloadCanceled(String taskPkgName) {
			super.downloadCanceled(taskPkgName);
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			map.put(taskPkgName, MultiThreadService.IS_PAUSEING);
			mAppListAdapter.stateChanged(map);
		}

		@Override
		public void downloadError(String taskPkgName, String error) {
			super.downloadError(taskPkgName, error);
		}

		@Override
		public void progressChanged(String taskPkgName, int process) {
			super.progressChanged(taskPkgName, process);
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			map.put(taskPkgName, process);
			mAppListAdapter.percentChanged(map);
		}
	}
}
