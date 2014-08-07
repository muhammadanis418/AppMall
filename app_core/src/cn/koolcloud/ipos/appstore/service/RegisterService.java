package cn.koolcloud.ipos.appstore.service;

import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.api.ApiService;
import cn.koolcloud.ipos.appstore.constant.Constants;
import cn.koolcloud.ipos.appstore.entity.ResultSet;
import cn.koolcloud.ipos.appstore.interfaces.CallBack;
import cn.koolcloud.ipos.appstore.utils.MySPEdit;
import cn.koolcloud.ipos.appstore.utils.JsonUtils;
import cn.koolcloud.ipos.appstore.utils.MyLog;
import cn.koolcloud.ipos.appstore.utils.PushUtils;
import cn.koolcloud.ipos.appstore.utils.ToastUtil;

import com.baidu.android.pushservice.apiproxy.PushManager;


public class RegisterService extends Service {
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		String userId = MySPEdit.getUserID(getApplicationContext());
		String channelId = MySPEdit.getChannelId(getApplicationContext());
		
//		if (TextUtils.isEmpty(terminalId)) {
			registerClient(userId, channelId);
//		}
		
		//login baidu cloud
		boolean isOpenPush = MySPEdit.getPushNotificationTag(getApplicationContext());
		if (isOpenPush) {
			PushUtils.logStringCache = PushUtils.getLogText(getApplicationContext());
			MyLog.d("logStringCache:" + PushUtils.logStringCache);
			PushUtils.loginBaiduCloud(getApplicationContext());
		} else {
			PushManager.stopWork(getApplicationContext());
		}
	}

    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
    	MyLog.d("onStartCommand()");
    	return super.onStartCommand(intent, flags, startId);
	}
    
    @Override
	public void onDestroy() {
		MyLog.d("onDestroy()");
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
				MyLog.d("-------getRegisterInfo=" + jsonObj.toString());
				
				retCode = JsonUtils.getStringValue(jsonObj, Constants.REQUEST_STATUS);
				
				if (retCode.equals(Constants.REQUEST_STATUS_FORBIDDEN)) {
					ToastUtil.showToast(getApplicationContext(), R.string.msg_pos_forbiden, Toast.LENGTH_LONG);
				}
				JSONObject dataJson = JsonUtils.getJSONObject(jsonObj, Constants.REQUEST_DATA);
				String terminalId = JsonUtils.getStringValue(dataJson, Constants.JSON_KEY_TERMINAL_ID);
				
				MySPEdit.saveTerminaID(getApplicationContext(), terminalId);
			} catch (Exception e) {
				onFailure("get client register response error!");
			}
		}

		@Override
		public void onFailure(String msg) {
			MyLog.d("describe=" + msg);
			if (msg.contains(ResultSet.NET_ERROR.describe)) {
				ToastUtil.showToast(getApplicationContext(), R.string.nonetwork_prompt_server_error, Toast.LENGTH_LONG);
			}
		}
	};

}
