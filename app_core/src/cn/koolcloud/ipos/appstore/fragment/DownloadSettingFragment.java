package cn.koolcloud.ipos.appstore.fragment;

import java.util.HashMap;
import java.util.Iterator;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.koolcloud.ipos.appstore.MainActivity;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.cache.DataCleanManager;
import cn.koolcloud.ipos.appstore.cache.database.CacheDB;
import cn.koolcloud.ipos.appstore.download.database.DownloadDBOperator;
import cn.koolcloud.ipos.appstore.download.multithread.MultiThreadService;
import cn.koolcloud.ipos.appstore.utils.Env;
import cn.koolcloud.ipos.appstore.utils.MySPEdit;
import cn.koolcloud.ipos.appstore.utils.ToastUtil;

public class DownloadSettingFragment extends BaseFragment implements View.OnClickListener {
	private static final int HANDLE_CLEAR_SD_CARD = 0;
	private static final int HANDLE_CLEAR_INTERNAL_CACHE = 1;
	private View v;
	private ImageButton addThreadImageButton;
	private ImageButton minusThreadImageButton;
	private TextView threadNumText;
	private LinearLayout clearCacheLayout;
	private LinearLayout clearSDCardLayout;
	
	public static DownloadSettingFragment getInstance() {
		DownloadSettingFragment noNetworkFragment = new DownloadSettingFragment();
		return noNetworkFragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflate, ViewGroup container,
			Bundle savedInstanceState) {
		v = inflate.inflate(R.layout.setting_download_main, container, false);
		initViews();
		return v;
	}
	
	Handler mHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLE_CLEAR_SD_CARD:
				dismissLoading();
				ToastUtil.showToast(application, R.string.msg_setting_clear_SDCard_success);
				break;
			case HANDLE_CLEAR_INTERNAL_CACHE:
				dismissLoading();
				ToastUtil.showToast(application, R.string.msg_setting_clear_internal_cache_success);
				break;
			}
			return false;
		}
	});
	
	private void initViews() {
		clearCacheLayout = (LinearLayout) v.findViewById(R.id.clearCachelayout);
		clearCacheLayout.setOnClickListener(this);
		
		clearSDCardLayout = (LinearLayout) v.findViewById(R.id.clearSDLayout);
		clearSDCardLayout.setOnClickListener(this);
		
		addThreadImageButton = (ImageButton) v.findViewById(R.id.addThreadImageButton);
		addThreadImageButton.setOnClickListener(this);
		minusThreadImageButton = (ImageButton) v.findViewById(R.id.minusThreadImageButton);
		minusThreadImageButton.setOnClickListener(this);
		threadNumText = (TextView) v.findViewById(R.id.threadNumText);
		threadNumText.setText("" + MySPEdit.getMultiThreadNums(getActivity()));
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.clearCachelayout:
			showLoading();
			new ClearAppCacheThread().start();
			break;
		case R.id.clearSDLayout:
			showLoading();
			new ClearSDCardThread().start();
			break;
		case R.id.addThreadImageButton:
			if(MySPEdit.getMultiThreadNums(getActivity()) >= 3) {
				ToastUtil.showToast(getActivity(), R.string.multi_down_thread_top_hint);
				return;
			}
			MySPEdit.saveMultiThreadNums(getActivity(), MySPEdit.getMultiThreadNums(getActivity())+1);
			threadNumText.setText("" + MySPEdit.getMultiThreadNums(getActivity()));
			break;
		case R.id.minusThreadImageButton:
			if(MySPEdit.getMultiThreadNums(getActivity()) <= 1) {
				ToastUtil.showToast(getActivity(), R.string.multi_down_thread_low_hint);
				return;
			}
			MySPEdit.saveMultiThreadNums(getActivity(), MySPEdit.getMultiThreadNums(getActivity())-1);
			threadNumText.setText("" + MySPEdit.getMultiThreadNums(getActivity()));
			break;
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
			MultiThreadService.IBinderImple binder = MainActivity.getInstance().getBinder();
			binder.CancelAllTask();
			
			CacheDB.getInstance(application).cleanCacheDBTables();
			DownloadDBOperator.getInstance(application).cleanDownloadDataBaseTables();
			
			DataCleanManager.cleanFiles(application);
			DataCleanManager.cleanInternalCache(application);

			final NotificationManager manager = (NotificationManager)
					getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
			final Iterator<String> iter = MainActivity.notifMap.keySet().iterator();
			while (iter.hasNext()) {
				HashMap<String, String> tmpM = MainActivity.notifMap.get(iter.next());
				int id = Integer.parseInt(tmpM.keySet().iterator().next());
				manager.cancel(id);
			}
			if(MainActivity.notifMap != null && MainActivity.notifMap.size() > 0) {
				MainActivity.notifMap.clear();
			}
			
			mHandler.sendEmptyMessage(HANDLE_CLEAR_INTERNAL_CACHE);
		}
	}
	
}
