package cn.koolcloud.ipos.appstore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import cn.koolcloud.ipos.appstore.api.ApiService;
import cn.koolcloud.ipos.appstore.constant.Constants;
import cn.koolcloud.ipos.appstore.download.multithread.MultiThreadService;
import cn.koolcloud.ipos.appstore.entity.App;
import cn.koolcloud.ipos.appstore.entity.AppInfo;
import cn.koolcloud.ipos.appstore.fragment.LocalSoftwareManagerFragment;
import cn.koolcloud.ipos.appstore.fragment.MainLeftFragment;
import cn.koolcloud.ipos.appstore.fragment.MainLeftFragment.OnTabChangedListener;
import cn.koolcloud.ipos.appstore.fragment.UpdateSoftwareFragment;
import cn.koolcloud.ipos.appstore.interfaces.CallBack;
import cn.koolcloud.ipos.appstore.receiver.MyDownloadReceiver;
import cn.koolcloud.ipos.appstore.utils.Env;
import cn.koolcloud.ipos.appstore.utils.JsonUtils;
import cn.koolcloud.ipos.appstore.utils.MyLog;
import cn.koolcloud.ipos.appstore.utils.MySPEdit;
import cn.koolcloud.ipos.appstore.utils.ToastUtil;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.widget.SearchView;

public class MainActivity extends BaseActivity implements SearchView.OnQueryTextListener,
		SearchView.OnSuggestionListener, OnTabChangedListener {
	private long exitTime = 0;
	private static final int EXIT_LAST_TIME = 2000;
	private ActionBar actionBar;					//action bar
	private boolean useLogo = false;				//action bar logo
    private boolean showHomeUp = false;				//show home up in action bar
	private static FragmentManager fragmentManager = null;
	private final String installedTabStr = "installed_tab";
	private final String canUpdateTabStr = "can_update_tab";
	private static MainActivity instance;
	private MyDownloadReceiver myDownlaodReceover;
	private MultiThreadService.IBinderImple binder;
	public static HashMap<String, Integer> downMap = new HashMap<String, Integer>();
	public static List<App> updateAppLits = new ArrayList<App>();
	public static HashMap<String, HashMap<String, String>> notifMap = new HashMap<String, HashMap<String, String>>();

    public MultiThreadService.IBinderImple getBinder() {
		return binder;
	}

	private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
        	MyLog.d("onServiceConnected");
            binder = (MultiThreadService.IBinderImple) service;
        }

        public void onServiceDisconnected(ComponentName name) {
        	MyLog.d("onServiceDisconnected");
        }
    };
	
	public static MainActivity getInstance() {
		return instance;
	}
	
	private CallBack getUpdateCallBack = new CallBack() {
		@Override
		public void onCancelled() {
		}

		@Override
		public void onStart() {
		}

		@Override
		public void onSuccess(JSONObject jsonObj) {
			MyLog.e("可更新app：\n"+jsonObj);
			try {
				updateAppLits = JsonUtils.parseSearchingJSONApps(jsonObj);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onFailure(String msg) {
			MyLog.d("describe=" + msg);
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;
		
		myDownlaodReceover = new MyDownloadReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.ACTION_TASK_STARTED);
		filter.addAction(Constants.ACTION_TASK_PAUSED);
		filter.addAction(Constants.ACTION_TASK_FINISHED);
		filter.addAction(Constants.ACTION_TASK_UPDATED);
		filter.addAction(Constants.ACTION_TASK_ERROR);
		registerReceiver(myDownlaodReceover, filter);
		
		setContentView(R.layout.main);
		fragmentManager = getSupportFragmentManager();
		activityList.add(this);
		initActionBar();
		
		initFragments();
		getClientVersion();
		
		Intent intent = new Intent(this, MultiThreadService.class);
		bindService(intent, conn, Service.BIND_AUTO_CREATE);
		
		List<AppInfo> localSoftDataSource = Env.getInstalledAppsToList(application, false);
		ApiService.checkAllAppUpdate(this, MySPEdit.getTerminalID(this), localSoftDataSource, getUpdateCallBack);
		Runnable r = new Runnable() {
			@Override
			public void run() {
			// TODO Auto-generated method stub
				Bundle bundle = getIntent().getExtras(); 
				if(bundle != null) {
					String name = bundle.getString("NAME");
					if(name !=null && name.equals("MSCService")) {
			    		Intent mIntent = new Intent(getApplicationContext(), AppDetailActivity.class);
						mIntent.putExtras(bundle);
						startActivity(mIntent);	
					}
				}
			}
		};
		new Handler().postDelayed(r, 500);
	}
	
	private void initFragments() {
		//navigation bar
		MainLeftFragment navFragment = MainLeftFragment.getInstance();
		
		FragmentTransaction fragTransaction = fragmentManager.beginTransaction();
		fragTransaction.replace(R.id.left_content, navFragment);
		fragTransaction.commitAllowingStateLoss();
	}
	
	public void refreshLocalSoftData() {
		((MyApp) application).initApps();
		
		List<AppInfo> list = Env.getInstalledAppsToList(application, false);
		try {
			Fragment f = fragmentManager.findFragmentById(R.id.frame_content);
			if(f instanceof LocalSoftwareManagerFragment) {
				((LocalSoftwareManagerFragment)f).refreshData(list);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		try {
			((TextView) (actionBar.getSelectedTab().getCustomView().findViewById(R.id.titleImage)))
					.setText(list.size() + "");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void refreshInstalledAppNum(int num) {
		try {
			((TextView) (actionBar.getSelectedTab().getCustomView().findViewById(R.id.titleImage)))
					.setText(num + "");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void initActionBar() {
		actionBar = getSupportActionBar();

        // set defaults for logo & home up
        actionBar.setDisplayHomeAsUpEnabled(showHomeUp);
        actionBar.setDisplayUseLogoEnabled(useLogo);
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.title_bg));
        actionBar.setIcon(R.drawable.logo);
        actionBar.setTitle(" "+getResources().getString(R.string.app_name));
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
		installedTab.setTag(installedTabStr);
		installedTab.setTabListener(new TabSelectListener());
		View view1 = LayoutInflater.from(MainActivity.this).inflate(R.layout.actionbar_tab, null);
		((TextView) view1.findViewById(R.id.titleText)).setText(R.string.installed);
		((TextView) view1.findViewById(R.id.titleImage)).setText("0");
		((TextView) view1.findViewById(R.id.titleImage)).setBackgroundResource(R.drawable.install_tab);
		installedTab.setCustomView(view1);
		actionBar.addTab(installedTab);
		
		Tab canUpdatedTab = actionBar.newTab();
		canUpdatedTab.setTag(canUpdateTabStr);
		canUpdatedTab.setTabListener(new TabSelectListener());
		View view2 = LayoutInflater.from(MainActivity.this).inflate(R.layout.actionbar_tab, null);
		((TextView) view2.findViewById(R.id.titleText)).setText(R.string.can_update);
		if(updateAppLits == null) {
			updateAppLits = new ArrayList<App>();
		}
		((TextView) view2.findViewById(R.id.titleImage)).setText(updateAppLits.size() + "");
		((TextView) view2.findViewById(R.id.titleImage)).setBackgroundResource(R.drawable.update_tab);
		canUpdatedTab.setCustomView(view2);
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
				tran.commitAllowingStateLoss();
			}  else if  (selectedTabTag.equals(canUpdateTabStr))  {
				UpdateSoftwareFragment updateSoftFragment = UpdateSoftwareFragment.getInstance();
				tran.replace(R.id.frame_content, updateSoftFragment);
				tran.commitAllowingStateLoss();
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
		UpdateSoftwareFragment updateSoftFragment = (UpdateSoftwareFragment)
				fragmentManager.findFragmentById(R.id.frame_content);
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

	@Override
	protected void onDestroy() {
		unbindService(conn);
		if(downMap != null && downMap.size() > 0) {
			downMap.clear();
		}
		if(updateAppLits != null && updateAppLits.size() > 0) {
			updateAppLits.clear();
		}
		try {
			unregisterReceiver(myDownlaodReceover);
		} catch (Exception e) {
			e.printStackTrace();
		}
		final NotificationManager manager = (NotificationManager)
				getSystemService(Context.NOTIFICATION_SERVICE);
		final Iterator<String> iter = notifMap.keySet().iterator();

		while (iter.hasNext()) {
			HashMap<String, String> tmpM = notifMap.get(iter.next());
			int id = Integer.parseInt(tmpM.keySet().iterator().next());
			manager.cancel(id);
		}
		if(notifMap != null && notifMap.size() > 0) {
			notifMap.clear();
		}
		super.onDestroy();
	}

	public void refreshUpdateSoftData() {
		if(updateAppLits != null && updateAppLits.size() > 0) {
			List<App> tmpList = new ArrayList<App>();
			List<AppInfo> list = Env.getInstalledAppsToList(application, false);
			for (AppInfo appInfo : list) {
				for (App app : updateAppLits) {
					if(app.getPackageName().equals(appInfo.getPackageName()) &&
							app.getVersionCode() == appInfo.getVersionCode()) {
						tmpList.add(app);
					}
				}
			}
			updateAppLits.removeAll(tmpList);
			
			try {
				((TextView) (actionBar.getSelectedTab().getCustomView().findViewById(R.id.titleImage)))
						.setText(updateAppLits.size() + "");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		Fragment f = fragmentManager.findFragmentById(R.id.frame_content);
		if(f instanceof UpdateSoftwareFragment) {
			((UpdateSoftwareFragment)f).refreshDataSet();
		}
	}
}
