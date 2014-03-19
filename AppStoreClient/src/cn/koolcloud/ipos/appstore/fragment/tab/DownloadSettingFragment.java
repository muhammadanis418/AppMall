package cn.koolcloud.ipos.appstore.fragment.tab;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import cn.koolcloud.ipos.appstore.AppStorePreference;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.cache.DataCleanManager;
import cn.koolcloud.ipos.appstore.cache.database.CacheDB;
import cn.koolcloud.ipos.appstore.download.database.DownloadDBOperator;
import cn.koolcloud.ipos.appstore.fragment.base.BaseFragment;
import cn.koolcloud.ipos.appstore.utils.Env;
import cn.koolcloud.ipos.appstore.utils.ToastUtil;

public class DownloadSettingFragment extends BaseFragment implements View.OnClickListener {
	private static final String TAG = "DownloadSettingFragment";
	
	private static final int HANDLE_CLEAR_SD_CARD = 0;
	private static final int HANDLE_CLEAR_INTERNAL_CACHE = 1;
	
	private LinearLayout clearCacheLayout;
	private LinearLayout clearSDCardLayout;
	private LinearLayout wifiSwitchLayout;
	private ImageView wifiSwitchImageView;
	
	private boolean wifiSwitchTag = false;
	
	public static DownloadSettingFragment getInstance() {
		DownloadSettingFragment noNetworkFragment = new DownloadSettingFragment();
		//save params
//		Bundle args = new Bundle();
//		args.putInt("index", index);
//		localSoftFragment.setArguments(args);
		return noNetworkFragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.wifiSwitchTag = AppStorePreference.getWifiSwitchTag(application);
	}

	@Override
	public View onCreateView(LayoutInflater inflate, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return inflate.inflate(R.layout.setting_download_main, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		initViews();
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}
	
	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLE_CLEAR_SD_CARD:
				dismissLoading();
				ToastUtil.showToast(application, R.string.msg_setting_clear_SDCard_success);
				break;
			case HANDLE_CLEAR_INTERNAL_CACHE:
				dismissLoading();
				ToastUtil.showToast(application, R.string.msg_setting_clear_internal_cache_success);
				break;

			default:
				break;
			}
		}
		
	};
	
	private void initViews() {
		clearCacheLayout = (LinearLayout) getActivity().findViewById(R.id.clearCachelayout);
		clearCacheLayout.setOnClickListener(this);
		
		clearSDCardLayout = (LinearLayout) getActivity().findViewById(R.id.clearSDLayout);
		clearSDCardLayout.setOnClickListener(this);
		
		wifiSwitchLayout = (LinearLayout) getActivity().findViewById(R.id.wifiSwitchLayout);
		wifiSwitchLayout.setOnClickListener(this);
		wifiSwitchImageView = (ImageView) getActivity().findViewById(R.id.wifiSwitchImageView);
		
		switchImageStatus();
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.clearCachelayout:
			showLoading();
			new ClearAppCacheThread().start();
			break;
		case R.id.clearSDLayout:
			showLoading();
			new ClearSDCardThread().start();
			break;
		case R.id.wifiSwitchLayout:
			wifiSwitchTag = switchStatus(wifiSwitchTag);
			AppStorePreference.saveWifiSwitchTag(application, wifiSwitchTag);
			switchImageStatus();
			break;

		default:
			break;
		}
	}
	
	private void switchImageStatus() {
		if (wifiSwitchTag) {
			wifiSwitchImageView.setImageResource(R.drawable.checkbox_kai);
		} else {
			wifiSwitchImageView.setImageResource(R.drawable.checkbox_guan);
		}
	}
	
	//class for clearing SDCard cache
	class ClearSDCardThread extends Thread {

		@Override
		public void run() {
			DataCleanManager.cleanCustomCache(Env.SD_CARD_APK_CACHE_DIR);
			DataCleanManager.cleanCustomCache(Env.SD_CARD_IMAGE_CACHE_DIR);
			
			mHandler.sendEmptyMessage(HANDLE_CLEAR_SD_CARD);
		}
		
	}
	
	//class for clearing internal cache
	class ClearAppCacheThread extends Thread {
		
		@Override
		public void run() {
//			DataCleanManager.cleanDatabases(application);
			CacheDB.getInstance(application).cleanCacheDBTables();
			DownloadDBOperator.getInstance(application).cleanDownloadDataBaseTables();
			
			DataCleanManager.cleanFiles(application);
			DataCleanManager.cleanInternalCache(application);
			
			mHandler.sendEmptyMessage(HANDLE_CLEAR_INTERNAL_CACHE);
		}
	}
	
}
