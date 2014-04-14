package cn.koolcloud.ipos.appstore.ui;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import cn.koolcloud.ipos.appstore.AppStorePreference;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.adapter.GeneralAppsListAdapter;
import cn.koolcloud.ipos.appstore.api.ApiService;
import cn.koolcloud.ipos.appstore.constant.Constants;
import cn.koolcloud.ipos.appstore.download.database.DownloadDBOperator;
import cn.koolcloud.ipos.appstore.download.providers.Downloader;
import cn.koolcloud.ipos.appstore.entity.App;
import cn.koolcloud.ipos.appstore.entity.ResultSet;
import cn.koolcloud.ipos.appstore.interfaces.CallBack;
import cn.koolcloud.ipos.appstore.utils.Env;
import cn.koolcloud.ipos.appstore.utils.JsonUtils;
import cn.koolcloud.ipos.appstore.utils.Logger;
import cn.koolcloud.ipos.appstore.utils.ToastUtil;
import cn.koolcloud.ipos.appstore.utils.Utils;
import cn.koolcloud.ipos.appstore.views.AppStoreListView;
import cn.koolcloud.ipos.appstore.views.AppStoreListView.AppstoreListViewListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.widget.SearchView;

public class SearchActivity extends BaseActivity implements SearchView.OnQueryTextListener, SearchView.OnSuggestionListener, AppstoreListViewListener, OnItemClickListener {
	private final static String TAG = "SearchActivity";
	
	public final static int SEARCH_ACTIVITY_REQUEST = 3;
	public static final int GENERAL_APPS_INSTALL_REQUEST = 1;
	
	private static final int HANDLE_UPDATE_APP = 0;
	
	private SearchView searchView;					//search view in action bar	
	private ActionBar actionBar;					//action bar
	
	private boolean useLogo = false;				//action bar logo
    private boolean showHomeUp = true;				//show home up in action bar

    private Bundle mBundle;
    
    private AppStoreListView generalListView;								//the list view of apps
    private List<App> appListDataSource = new ArrayList<App>();				//apps data source
    private GeneralAppsListAdapter mAppListAdapter;							//adapter for app list view
    
    private String keyWord = "";
    
    private static SearchActivity instance;
        
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
        actionBar.setTitle("\"" + keyWord + "\"" + Utils.getResourceString(application, R.string.search_activity_result));
        
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
	
	private void initViews() {
		generalListView = (AppStoreListView) findViewById(R.id.normal_list_grid);
		
		generalListView.setOnItemClickListener(this);
		generalListView.setPullLoadEnable(false);
		generalListView.setPullRefreshEnable(false);
		generalListView.setAppStoreListViewListener(this);
	}
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLE_UPDATE_APP:
				/*List<App> appList = (List<App>) msg.obj;
				appListDataSource.clear();
				appListDataSource.addAll(appList);*/
				if (mAppListAdapter != null) {
					
					mAppListAdapter.notifyDataSetChanged();
				}
				break;
			case GENERAL_APPS_INSTALL_REQUEST:
				File file = (File) msg.obj;
//				Env.install(getActivity(), file, GENERAL_APPS_INSTALL_REQUEST);
				
				/*Intent intent = new Intent(Intent.ACTION_VIEW);
				try {
					Runtime.getRuntime().exec("chmod 655 " + file.toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setDataAndType(Uri.fromFile(file),
						"application/vnd.android.package-archive");
				startActivityForResult(intent, GENERAL_APPS_INSTALL_REQUEST);*/
				if (file.exists()) {
					Env.install(instance, file, GENERAL_APPS_INSTALL_REQUEST);
				}
				
				break;

			default:
				break;
			}
		}
	};
	
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
		ApiService.getAppsByKeyWord(application, AppStorePreference.getTerminalID(application), 
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
				String retCode = "";
				String data = "";
				
				Logger.d("-------searching app Info=" + jsonObj.toString());
				
				retCode = JsonUtils.getStringValue(jsonObj, Constants.REQUEST_STATUS);
				data = JsonUtils.getStringValue(jsonObj, Constants.REQUEST_DATA);
				
				/*if (!Constants.REQUEST_STATUS_OK.equals(retCode)) {
					onFailure(data);
				} else {
					if (TextUtils.isEmpty(data)) {
						onFailure(Utils.getResourceString(getActivity(), R.string.nonetwork_prompt_server_error));
					} else {
						client = JsonUtils.parseJSONClient(jsonObj);
					}
				}*/
				
				appListDataSource = JsonUtils.parseSearchingJSONApps(jsonObj);
				if (appListDataSource != null && appListDataSource.size() > 0) {
					
					Downloader downloader = new Downloader(null, application, null);
					DownloadDBOperator mDBOper = DownloadDBOperator.getInstance(application);
					mAppListAdapter = new GeneralAppsListAdapter(SearchActivity.this, appListDataSource, application, mHandler, downloader, mDBOper);
					generalListView.setAdapter(mAppListAdapter);
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
			Logger.d("describe=" + msg);
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
		Logger.d(query);
		
		this.keyWord = query;
		getAppsBySearchWord();
		return true;
	}
	
	@Override
	public boolean onQueryTextChange(String newText) {
		// TODO Auto-generated method stub
		return super.onQueryTextChange(newText);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		mAppListAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onLoadMore() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {
			
		Intent mIntent = new Intent(application, SoftwareDetailActivity.class);
		Bundle mBundle = new Bundle();
		mBundle.putSerializable(Constants.SER_KEY, (Serializable) appListDataSource);
		mBundle.putInt(Constants.APP_LIST_POSITION, --position); //ListView header is on first position so to remove one
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
			
			Logger.d("invoke scan local apps");
			Long start = System.currentTimeMillis();
			Map<String, PackageInfo> installedPackage = Env.scanInstalledAppToMap(getApplicationContext());
			Long end = System.currentTimeMillis();
			Logger.d("total time:" + (end - start));

			//cache installed package
			application.saveInstalledAppsInfo(installedPackage);
			Message msg = mHandler.obtainMessage();
			msg.obj = installedPackage;
			msg.what = HANDLE_UPDATE_APP;
			mHandler.sendMessage(msg);
			
		}
	}
}
