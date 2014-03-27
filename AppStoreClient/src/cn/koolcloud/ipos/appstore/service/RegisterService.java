package cn.koolcloud.ipos.appstore.service;

import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.Toast;
import cn.koolcloud.ipos.appstore.AppStorePreference;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.api.ApiService;
import cn.koolcloud.ipos.appstore.constant.Constants;
import cn.koolcloud.ipos.appstore.entity.ResultSet;
import cn.koolcloud.ipos.appstore.interfaces.CallBack;
import cn.koolcloud.ipos.appstore.utils.JsonUtils;
import cn.koolcloud.ipos.appstore.utils.Logger;
import cn.koolcloud.ipos.appstore.utils.PushUtils;
import cn.koolcloud.ipos.appstore.utils.ToastUtil;

import com.baidu.android.pushservice.apiproxy.PushManager;


public class RegisterService extends Service {
	private static final String TAG = "RegisterService";
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		Logger.d("onCreate()");
		
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Logger.d("onStart()");
		super.onStart(intent, startId);
		
		String terminalId = AppStorePreference.getTerminalID(getApplicationContext());
		String userId = AppStorePreference.getUserID(getApplicationContext());
		String channelId = AppStorePreference.getChannelId(getApplicationContext());
		
//		if (TextUtils.isEmpty(terminalId)) {
			registerClient(userId, channelId);
//		}
		
		//login baidu cloud
		boolean isOpenPush = AppStorePreference.getPushNotificationTag(getApplicationContext());
		if (isOpenPush) {
			PushUtils.logStringCache = PushUtils.getLogText(getApplicationContext());
			Logger.d("logStringCache:" + PushUtils.logStringCache);
			PushUtils.loginBaiduCloud(getApplicationContext());
		} else {
			PushManager.stopWork(getApplicationContext());
		}
	}

    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
    	Logger.d("onStartCommand()");
    	return super.onStartCommand(intent, flags, startId);
	}
    
    @Override
	public void onDestroy() {
		Logger.d("onDestroy()");
		super.onDestroy();
	}
    
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
				}
				JSONObject dataJson = JsonUtils.getJSONObject(jsonObj, Constants.REQUEST_DATA);
				String terminalId = JsonUtils.getStringValue(dataJson, Constants.JSON_KEY_TERMINAL_ID);
				
				AppStorePreference.saveTerminaID(getApplicationContext(), terminalId);
			} catch (Exception e) {
				onFailure("get client register response error!");
			}
		}

		@Override
		public void onFailure(String msg) {
			Logger.d("describe=" + msg);
			if (msg.contains(ResultSet.NET_ERROR.describe)) {
				ToastUtil.showToast(getApplicationContext(), R.string.nonetwork_prompt_server_error, Toast.LENGTH_LONG);
			}
		}
	};

}
