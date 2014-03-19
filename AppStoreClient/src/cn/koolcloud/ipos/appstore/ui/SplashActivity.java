package cn.koolcloud.ipos.appstore.ui;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.RelativeLayout;
import android.widget.Toast;
import cn.koolcloud.ipos.appstore.AppStorePreference;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.api.ApiService;
import cn.koolcloud.ipos.appstore.constant.Constants;
import cn.koolcloud.ipos.appstore.entity.ResultSet;
import cn.koolcloud.ipos.appstore.interfaces.CallBack;
import cn.koolcloud.ipos.appstore.utils.JsonUtils;
import cn.koolcloud.ipos.appstore.utils.Logger;
import cn.koolcloud.ipos.appstore.utils.NetUtil;
import cn.koolcloud.ipos.appstore.utils.PushUtils;
import cn.koolcloud.ipos.appstore.utils.ToastUtil;

import com.baidu.android.pushservice.PushManager;

public class SplashActivity extends BaseActivity {
	
	public static final String NETWORK_AVAILABLE_KEY = "is_network_available";
	
	private final int CURRENT_PAGE_SLEEP_TIME = 2500;
	private RelativeLayout rootLayout;
	private long delayMillis = 2000;
	private static final int SHOW_TOAST = 1;
	private static final int GO_TO_MAIN_PAGE = 2;
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Logger.d("-----onCreate-------");
		activityList.add(this);
		setContentView(R.layout.loading);
		checkNetwork();
		initViews();
		
		boolean isOpenPush = AppStorePreference.getPushNotificationTag(application);
		if (isOpenPush) {
			
			PushUtils.logStringCache = PushUtils.getLogText(getApplicationContext());
			PushUtils.loginBaiduCloud(getApplicationContext());
		} else {
			PushManager.stopWork(getApplicationContext());
		}
	}
	
	private void initViews() {
		rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);
	}
	
	/**
	* @Title: checkNetwork
	* @Description: check network
	* @param 
	* @return void 
	* @throws
	*/
	private void checkNetwork() {
		//network is ok
		if (NetUtil.isAvailable(getApplicationContext())) {
			String terminalId = AppStorePreference.getTerminalID(getApplicationContext());
			if (TextUtils.isEmpty(terminalId)) {
				String userId = AppStorePreference.getUserID(getApplicationContext());
				String channelId = AppStorePreference.getChannelId(getApplicationContext());
				registerClient(userId, channelId);
			} else {
				new JumpToMainActivity().start();
			}
		} else {
			ToastUtil.showToast(getApplicationContext(), R.string.dialog_network_not_available, Toast.LENGTH_LONG);
			new JumpToMainActivity().start();
		}
	}
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SHOW_TOAST:
				Toast.makeText(SplashActivity.this, msg.arg1, Toast.LENGTH_SHORT).show();
				break;
			case GO_TO_MAIN_PAGE:
				//clear apps table cache
				/*CacheDB cacheDB = CacheDB.getInstance(getApplicationContext());
				cacheDB.clearAppTableData();
				
				AppStorePreference.saveTerminaID(getApplicationContext(), String.valueOf(msg.obj));*/
				gotoMainActivity();
				break;
			default:
				break;
			}
		}
	};
	
	public void registerClient(String userId, String channelId) {
		ApiService.register(this, userId, channelId, registerCallBack);
	}
	
	private CallBack registerCallBack = new CallBack() {
		@Override
		public void onCancelled() {
		}

		@Override
		public void onStart() {
		}

		@Override
		public void onSuccess(JSONObject jsonObj) {
			try {
				String retCode = "";
				String data = "";
				
				Logger.d("-------getRegisterInfo=" + jsonObj.toString());
				
				retCode = JsonUtils.getStringValue(jsonObj, Constants.REQUEST_STATUS);
				data = JsonUtils.getStringValue(jsonObj, Constants.REQUEST_DATA);
				
				/*if (!Constants.REQUEST_STATUS_OK.equals(retCode)) {
					onFailure(data);
				} else {
					if (TextUtils.isEmpty(data)) {
						onFailure(Utils.getResourceString(getApplicationContext(), R.string.nonetwork_prompt_server_error));
					} else {
						client = JsonUtils.parseJSONClient(jsonObj);
					}
				}*/
				if (retCode.equals(Constants.REQUEST_STATUS_FORBIDDEN)) {
					ToastUtil.showToast(getApplicationContext(), R.string.msg_pos_forbiden, Toast.LENGTH_LONG);
					exit();
				}
				JSONObject dataJson = JsonUtils.getJSONObject(jsonObj, Constants.REQUEST_DATA);
				String terminalId = JsonUtils.getStringValue(dataJson, Constants.JSON_KEY_TERMINAL_ID);
				
				AppStorePreference.saveTerminaID(getApplicationContext(), terminalId);
				new JumpToMainActivity().start();
			} catch (Exception e) {
				onFailure("get client register response error!");
			}
		}

		@Override
		public void onFailure(String msg) {
			Logger.d("describe=" + msg);
			if (msg.contains(ResultSet.NET_ERROR.describe)) {
				new JumpToMainActivity().start();
				ToastUtil.showToast(getApplicationContext(), R.string.nonetwork_prompt_server_error, Toast.LENGTH_LONG);
			}
		}
	};


	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {   
//            ((AppStoreApplication) application).exit();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
  
        return super.onKeyDown(keyCode, event);
	}
	
	/*@Override
	protected void onDestroy() {
		activityList.remove(this);
		super.onDestroy();
	    System.gc();
	}*/

	@Override
	public void finish() {
		super.finish();
	}

	/**
	* @Title: gotoMainActivity
	* @Description: TODO go to main page
	* @param 
	* @return void 
	* @throws
	*/
	private void gotoMainActivity() {
		/*try {
			//delay execute invoke refresh data set
			Thread.currentThread().sleep(CURRENT_PAGE_SLEEP_TIME);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
		Intent intent = new Intent(SplashActivity.this, MainFrameActivity.class);
		startActivity(intent);
		finish();
	}
	
	class JumpToMainActivity extends Thread {

		@Override
		public void run() {
			mHandler.sendEmptyMessageDelayed(GO_TO_MAIN_PAGE, CURRENT_PAGE_SLEEP_TIME);
		}
	}
}
