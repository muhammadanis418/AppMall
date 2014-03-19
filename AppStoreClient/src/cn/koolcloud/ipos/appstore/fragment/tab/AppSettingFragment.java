package cn.koolcloud.ipos.appstore.fragment.tab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import cn.koolcloud.ipos.appstore.AppStorePreference;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.cache.DataCleanManager;
import cn.koolcloud.ipos.appstore.fragment.base.BaseFragment;
import cn.koolcloud.ipos.appstore.utils.Logger;
import cn.koolcloud.ipos.appstore.utils.PushUtils;

import com.baidu.android.pushservice.apiproxy.PushManager;

public class AppSettingFragment extends BaseFragment implements View.OnClickListener {
	private static final String TAG = "AppSettingFragment";
	
	private LinearLayout pushButtonlayout;
	private LinearLayout deleteApkButtonlayout;
	private ImageView deleteApkImageView;
	private ImageView pushNotificationImageView;
	
	private boolean deleteApkTag = true;						//tag for delete apk 
	private boolean pushNotificationTag = true;					//tag for push notification switch
	
	public static AppSettingFragment getInstance() {
		AppSettingFragment appSettingFragment = new AppSettingFragment();
		//save params
//		Bundle args = new Bundle();
//		args.putInt("index", index);
//		localSoftFragment.setArguments(args);
		return appSettingFragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		deleteApkTag = AppStorePreference.getDeleteApkTag(application);
		pushNotificationTag = AppStorePreference.getPushNotificationTag(application);
		
	}

	@Override
	public View onCreateView(LayoutInflater inflate, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return inflate.inflate(R.layout.setting_app_main, container, false);
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
	
	private void initViews() {
		deleteApkButtonlayout = (LinearLayout) getActivity().findViewById(R.id.deleteApkButtonlayout);
		deleteApkButtonlayout.setOnClickListener(this);
		deleteApkImageView = (ImageView) getActivity().findViewById(R.id.deleteApkImageView);
		
		pushButtonlayout = (LinearLayout) getActivity().findViewById(R.id.pushButtonlayout);
		pushButtonlayout.setOnClickListener(this);
		pushNotificationImageView = (ImageView) getActivity().findViewById(R.id.pushNotificationImageView);
		switchImageStatus();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.deleteApkButtonlayout:
			deleteApkTag = switchStatus(deleteApkTag);
			AppStorePreference.saveDeleteApkTag(application, deleteApkTag);
			if (deleteApkTag) {
				new ClearAppCacheThread().start();
			}
			break;
		case R.id.pushButtonlayout:
			pushNotificationTag = switchStatus(pushNotificationTag);
			
			AppStorePreference.savePushNotificationTag(application, pushNotificationTag);
			break;

		default:
			break;
		}
		switchImageStatus();
	}
	
	private void switchImageStatus() {
		if (pushNotificationTag) {
			pushNotificationImageView.setImageResource(R.drawable.checkbox_kai);
			PushUtils.logStringCache = PushUtils.getLogText(application);
			Logger.d("logStringCache:" + PushUtils.logStringCache);
			PushUtils.loginBaiduCloud(application);
		} else {
			pushNotificationImageView.setImageResource(R.drawable.checkbox_guan);
			PushManager.stopWork(application);
		}
		
		if (deleteApkTag) {
			deleteApkImageView.setImageResource(R.drawable.checkbox_kai);
		} else {
			deleteApkImageView.setImageResource(R.drawable.checkbox_guan);
		}
	}
	
	//class for clearing internal cache
	class ClearAppCacheThread extends Thread {
		
		@Override
		public void run() {
//			DataCleanManager.cleanDatabases(application);
			DataCleanManager.cleanFiles(application);
			
		}
	}
	
}
