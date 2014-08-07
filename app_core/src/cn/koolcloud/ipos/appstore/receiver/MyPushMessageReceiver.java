package cn.koolcloud.ipos.appstore.receiver;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import cn.koolcloud.ipos.appstore.NotificationPromotionActivity;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.AppDetailActivity;
import cn.koolcloud.ipos.appstore.api.ApiService;
import cn.koolcloud.ipos.appstore.constant.Constants;
import cn.koolcloud.ipos.appstore.entity.App;
import cn.koolcloud.ipos.appstore.entity.ResultSet;
import cn.koolcloud.ipos.appstore.interfaces.CallBack;
import cn.koolcloud.ipos.appstore.utils.MySPEdit;
import cn.koolcloud.ipos.appstore.utils.JsonUtils;
import cn.koolcloud.ipos.appstore.utils.MyLog;
import cn.koolcloud.ipos.appstore.utils.PushUtils;
import cn.koolcloud.ipos.appstore.utils.ToastUtil;

import com.baidu.frontia.api.FrontiaPushMessageReceiver;

/**
 * Push message handle receiver��please overwrite your needed callback methods,
 * generally��
 * onBind must��to deal with startWork callback result;
 * onMessage receive messages;
 * onSetTags��onDelTags��onListTags for tag operation callback;
 * onNotificationClicked when notification clicking callback;
 * onUnbind is stopWork interface callback;
 * 
 * return errorCode description:
 * 0 - Success
 * 10001 - Network Problem
 * 30600 - Internal Server Error
 * 30601 - Method Not Allowed
 * 30602 - Request Params Not Valid
 * 30603 - Authentication Failed
 * 30604 - Quota Use Up Payment Required
 * 30605 - Data Required Not Found
 * 30606 - Request Time Expires Timeout
 * 30607 - Channel Token Timeout
 * 30608 - Bind Relation Not Found
 * 30609 - Bind Number Too Many
 * 
 */
public class MyPushMessageReceiver extends FrontiaPushMessageReceiver {
	/** TAG to Log */
	public static final String TAG = MyPushMessageReceiver.class.getSimpleName();
	
	private Context ctx;

	/**
	 * after calling PushManager.startWork,sdk will start binding request to push server, it is sync, and return binding result in onBind.
	 * if you need unicast push, you need to upload channel id and user id to server, then call server interface with channel id and user id to the phone or client.
	 * 
	 * @param context
	 *          BroadcastReceiver running Context
	 * @param errorCode
     *          binding interface callback value, 0 - success
     * @param appid 
     *          app id. it is null when errorCode is not 0
	 * @param userId
	 *          app user id��it is null when errorCode is not 0.
	 * @param channelId
	 *          app channel id��it is null when errorCode is not 0
	 * @param requestId
	 *          request id to server. for tracking problems.
	 * @return
	 *     none
	 */
	@Override
	public void onBind(Context context, int errorCode, String appid, 
				String userId, String channelId, String requestId) {
		String responseString = "onBind errorCode=" + errorCode + " appid="
				+ appid + " userId=" + userId + " channelId=" + channelId
				+ " requestId=" + requestId;
		MyLog.d(responseString);
		
		// binding success, set binded flag, decrease unneccesary binding request.
		if (errorCode == 0) {
			PushUtils.setBind(context, true);
			MySPEdit.saveUserId(context, userId);
			MySPEdit.saveChannelId(context, channelId);
			
			ctx = context;
			registerClient(ctx, userId, channelId);
		}
		// update UI for Demo program.
		updateContent(context, responseString);
	}

	/**
	 * receive passthrough message.
	 * 
	 * @param context 
	 * @param message pushed message
	 * @param customContentString custom content, null or json string
	 */
	@Override
	public void onMessage(Context context, String message, String customContentString) {
		String messageString = "Passthrough message=\"" + message + "\" customContentString="
				+ customContentString;
		MyLog.d(messageString);
		
		// get custom content,mykey and myvalue face to the kay and value in custom content.
		if (customContentString != null & TextUtils.isEmpty(customContentString)) {
			JSONObject customJson = null;
			try {
				customJson = new JSONObject(customContentString);
				if (customJson.isNull("mykey")) {
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		updateContent(context, messageString);
	}
	
	/**
	 * Method for receiving notification clicking. Warn:app can't get notification content before clicking notification.
	 * 
	 * @param context 
	 * @param title The title of push notification
	 * @param description The description of push notification.
	 * @param customContentString Custom content string, null or json string.
	 */
	@Override
	public void onNotificationClicked(Context context, String title, 
				String description, String customContentString) {
		String notifyString = "Notification clicking title=\"" + title + "\" description=\""
				+ description + "\" customContent=" + customContentString;
		MyLog.d(notifyString);
		
		// get custom content
		if (customContentString != null && !TextUtils.isEmpty(customContentString)) {
			JSONObject customJson = null;
			try {
				customJson = new JSONObject(customContentString);
				if (customJson != null && !customJson.isNull(Constants.JSON_KEY_TYPE)) {
					int type = JsonUtils.getIntValue(customJson, Constants.JSON_KEY_TYPE);
					if (type == Constants.TYPE_APP_UPDATE) {
						MyLog.d("type:" + type);
						List<App> appListDataSource = JsonUtils.parsePushJSONApp(customJson);
						
						Intent mIntent = new Intent(context, AppDetailActivity.class);
						mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						Bundle mBundle = new Bundle();
						mBundle.putSerializable(Constants.SER_KEY, (Serializable) appListDataSource);
						mBundle.putInt(Constants.APP_LIST_POSITION, 0);
						mIntent.putExtras(mBundle);
						context.startActivity(mIntent);
					} else if (type == Constants.TYPE_NOTIFICATION_PROMOTION) {
						Intent mIntent = new Intent(context, NotificationPromotionActivity.class);
						mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						Bundle mBundle = new Bundle();
						mBundle.putSerializable(Constants.SER_KEY, (Serializable) customJson);
						mIntent.putExtras(mBundle);
						context.startActivity(mIntent);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * setTags() callback method.
	 * 
	 * @param context 
	 * @param errorCode Error Code. 0 some tag set success; tags' setting are all failure.
	 * @param successTags Setting success tags.
	 * @param failTags Setting failure tags.
	 * @param requestId Assign to push service request id.
	 */
	@Override
	public void onSetTags(Context context, int errorCode, 
				List<String> sucessTags, List<String> failTags, String requestId) {
		String responseString = "onSetTags errorCode=" + errorCode + " sucessTags="
				+ sucessTags + " failTags=" + failTags + " requestId="
				+ requestId;
		MyLog.d(responseString);
		
		updateContent(context, responseString);
	}

	/**
	 * delTags() callback method.
	 * 
	 * @param context 
	 * @param errorCode Error Code. 0 some tag delete success; !0 All the tags are deleting failure.
	 * @param successTags The Tags deleted are successfull.
	 * @param failTags The tags deleting failure.
	 * @param requestId Assign to push service request id.
	 */
	@Override
	public void onDelTags(Context context, int errorCode, 
				List<String> sucessTags, List<String> failTags, String requestId) {
		String responseString = "onDelTags errorCode=" + errorCode + " sucessTags="
				+ sucessTags + " failTags=" + failTags + " requestId="
				+ requestId;
		MyLog.d(responseString);
		
		updateContent(context, responseString);
	}

	/**
	 * listTags() callback method.
	 * 
	 * @param context 
	 * @param errorCode  Error Code. 0 list tags success. !0 failure.
	 * @param tags Current app setted all the tags.
	 * @param requestId Assign to push service request id.
	 */
	@Override
	public void onListTags(Context context, int errorCode, 
				List<String> tags, String requestId) {
		String responseString = "onListTags errorCode=" + errorCode + " tags=" + tags;
		MyLog.d(responseString);
		
		updateContent(context, responseString);
	}

	/**
	 * PushManager.stopWork() call back method.
	 * 
	 * @param context 
	 * @param errorCode Error code. 0 unbind from cloud push service. !0 failure.
	 * @param requestId Assign to push service request id.
	 */
	@Override
	public void onUnbind(Context context, int errorCode, String requestId) {
		String responseString = "onUnbind errorCode=" + errorCode
				+ " requestId = " + requestId;
		MyLog.d(responseString);
		
		// unbind success, and set bind flag.
		if (errorCode == 0) {
			PushUtils.setBind(context, false);
		}
		updateContent(context, responseString);
	}
	
	private void updateContent(Context context, String content) {
		MyLog.d("updateContent");
		String logText = "" + PushUtils.logStringCache;
		
		if (!logText.equals("")) {
			logText += "\n";
		}
		
		SimpleDateFormat sDateFormat = new SimpleDateFormat("HH-mm-ss", Locale.getDefault());
		logText += sDateFormat.format(new Date()) + ": ";
		logText += content;
		
		PushUtils.logStringCache = logText;
		
		/*Intent intent = new Intent();
        intent.setClass(context.getApplicationContext(), PushDemoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.getApplicationContext().startActivity(intent);*/
	}
	
	public void registerClient(Context ctx, String userId, String channelId) {
		ApiService.register(ctx, userId, channelId, registerCallBack);
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
					ToastUtil.showToast(ctx, R.string.msg_pos_forbiden, Toast.LENGTH_LONG);
				}
				JSONObject dataJson = JsonUtils.getJSONObject(jsonObj, Constants.REQUEST_DATA);
				String terminalId = JsonUtils.getStringValue(dataJson, Constants.JSON_KEY_TERMINAL_ID);
				
				MySPEdit.saveTerminaID(ctx, terminalId);
			} catch (Exception e) {
				onFailure("get client register response error!");
			}
		}

		@Override
		public void onFailure(String msg) {
			MyLog.d("describe=" + msg);
			if (msg.contains(ResultSet.NET_ERROR.describe)) {
				ToastUtil.showToast(ctx, R.string.nonetwork_prompt_server_error, Toast.LENGTH_LONG);
			}
		}
	};

}