package cn.koolcloud.ipos.appstore.ui;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.adapter.NotificationPromotionAdapter;
import cn.koolcloud.ipos.appstore.cache.database.CacheDB;
import cn.koolcloud.ipos.appstore.constant.Constants;
import cn.koolcloud.ipos.appstore.entity.NotificationPromotionInfo;
import cn.koolcloud.ipos.appstore.utils.JsonUtils;
import cn.koolcloud.ipos.appstore.utils.ToastUtil;
import cn.koolcloud.ipos.appstore.utils.Utils;
import cn.koolcloud.ipos.appstore.views.AppStoreListView;
import cn.koolcloud.ipos.appstore.views.AppStoreListView.AppstoreListViewListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.widget.SearchView;

public class NotificationPromotionActivity extends BaseActivity implements AppstoreListViewListener {
	
	private static final int HANDLE_NOTIFY_PROMOTION = 0;
	private AppStoreListView contentListView;
	private SearchView searchView;
	private ActionBar actionBar;					//action bar
	
	private boolean useLogo = false;				//action bar logo
	private boolean showHomeUp = false;				//show home up in action bar
	private JSONObject dataJson = null;				//data from push message.
	private List<NotificationPromotionInfo> dataSource = new ArrayList<NotificationPromotionInfo>();
	
	private NotificationPromotionAdapter mAdapter;	//promotion adapter
	
	private Bundle mBundle;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.appstore_content_list);
		activityList.add(this);
		mBundle = getIntent().getExtras();
		try {
			dataJson = new JSONObject(mBundle.getString(Constants.SER_KEY));
		} catch (JSONException e) {
			e.printStackTrace();
			ToastUtil.showToast(application, R.string.notification_trans_param_format_error);
			finish();
		}
		
		initViews();
		initViewStatus();
	}
	
	private void initViews() {
		initActionBar();
		contentListView = (AppStoreListView) findViewById(R.id.contentListView);
		contentListView.setPullLoadEnable(false);
		contentListView.setPullRefreshEnable(true);
		contentListView.setAppStoreListViewListener(this);
	}
	
	private void initViewStatus() {
		if (null != dataJson) {
			dataSource = JsonUtils.parsePushPromotion(dataJson);
			mAdapter = new NotificationPromotionAdapter(dataSource, application);
			contentListView.setAdapter(mAdapter);
			mAdapter.notifyDataSetChanged();
			
			if (null != dataSource && dataSource.size() > 0) {
				new SaveNotificationPromotion().start();
			}
		}
	}
	
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLE_NOTIFY_PROMOTION:
				List<NotificationPromotionInfo> tmpDataList = (List<NotificationPromotionInfo>) msg.obj;
				if (tmpDataList != null && tmpDataList.size() > 0) {
					dataSource.clear();
					dataSource.addAll(tmpDataList);
					
					mAdapter = new NotificationPromotionAdapter(dataSource, application);
					contentListView.setAdapter(mAdapter);
					mAdapter.notifyDataSetChanged();
				}
				break;

			default:
				break;
			}
		}
		
	};
	
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
        actionBar.setTitle(Utils.getResourceString(application, R.string.msg_setting_install_push_switch));
        
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
    public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
        
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setVisibility(View.INVISIBLE);
        
        return true;
    }

	@Override
	public void onRefresh() {
		new LoadNotifyPromotions().start();
		onFinishLoading();
	}

	@Override
	public void onLoadMore() {
		onFinishLoading();
	}
	
	private void onFinishLoading() {
		contentListView.stopRefresh();
		contentListView.stopLoadMore();
		contentListView.setRefreshTime(Utils.getResourceString(application, R.string.appstore_list_header_hint_just_now));
	}
	
	/**
	 * <p>Title: NotificationPromotionActivity.java </p>
	 * <p>Description: class for save promotion to sqlite database</p>
	 * <p>Copyright: Copyright (c) 2014</p>
	 * <p>Company: All In Pay</p>
	 * @author 		Teddy
	 * @date 		2014-5-14
	 * @version 	
	 */
	class SaveNotificationPromotion extends Thread {

		@Override
		public void run() {
			CacheDB cacheDB = CacheDB.getInstance(application);
			cacheDB.insertNotificatinoPromotion(dataSource);
		}
	}
	
	/**
	 * <p>Title: NotificationPromotionActivity.java </p>
	 * <p>Description: load local notify promotions</p>
	 * <p>Copyright: Copyright (c) 2014</p>
	 * <p>Company: All In Pay</p>
	 * @author 		Teddy
	 * @date 		2014-5-14
	 * @version 	
	 */
	class LoadNotifyPromotions extends Thread {

		@Override
		public void run() {
			CacheDB cacheDB = CacheDB.getInstance(application);
			List<NotificationPromotionInfo> promotionList = cacheDB.selectAllNotifyPromotions();
			Message msg = mHandler.obtainMessage();
			msg.obj = promotionList;
			msg.what = HANDLE_NOTIFY_PROMOTION;
			mHandler.sendMessage(msg);
		}
	}
	
}
