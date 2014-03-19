package cn.koolcloud.ipos.appstore;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * <p>Title: AppStorePreference.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: All In Pay</p>
 * @author 		Teddy
 * @date 		2013-11-7
 * @version 	
 */
public class AppStorePreference {

	private static final int DEFAULT_PROMOTION_DATA_ID					= 0;
	public static final String PREFS_NAME 								= "msc_preference_file";
	public static final String TERMINAL_ID 								= "terminal_id";
	public static final String CATEGORY_HASH 							= "category_hash";
	public static final String BAIDU_PUSH_USER_ID						= "userId";
	public static final String BAIDU_PUSH_CHANNEL_ID					= "channelId";
	public static final String DELETE_APK_BTN_TAG						= "delete_apk_tag";
	public static final String PUSH_NOTIFICATION_TAG					= "push_notification_tag";
	public static final String WIFI_SWITCH_TAG							= "wifi_switch_tag";
	public static final String PROMOTION_DATA_ID						= "promotion_data_id";
	
	// load terminal id
	public static String getTerminalID(Context ctx) {
		SharedPreferences prefer = ctx.getSharedPreferences(PREFS_NAME,
				Context.MODE_PRIVATE);
		return prefer.getString(TERMINAL_ID, "");
	}

	// save terminal id
	public static void saveTerminaID(Context ctx, String terminalID) {
		SharedPreferences prefer = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		prefer.edit().putString(TERMINAL_ID, terminalID).commit();
	}
	
	// load promotion data id
	public static int getPromotinDataID(Context ctx) {
		SharedPreferences prefer = ctx.getSharedPreferences(PREFS_NAME,
				Context.MODE_PRIVATE);
		return prefer.getInt(PROMOTION_DATA_ID, DEFAULT_PROMOTION_DATA_ID);
	}
	
	// save promotion data id
	public static void savePromotinDataID(Context ctx, int promotinDataID) {
		SharedPreferences prefer = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		prefer.edit().putInt(PROMOTION_DATA_ID, promotinDataID).commit();
	}
	
	// load delete apk tag
	public static boolean getDeleteApkTag(Context ctx) {
		SharedPreferences prefer = ctx.getSharedPreferences(PREFS_NAME,
				Context.MODE_PRIVATE);
		return prefer.getBoolean(DELETE_APK_BTN_TAG, true);
	}
	
	// save delete apk tag
	public static void saveDeleteApkTag(Context ctx, boolean isDelete) {
		SharedPreferences prefer = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		prefer.edit().putBoolean(DELETE_APK_BTN_TAG, isDelete).commit();
	}
	
	// load push notification tag
	public static boolean getPushNotificationTag(Context ctx) {
		SharedPreferences prefer = ctx.getSharedPreferences(PREFS_NAME,
				Context.MODE_PRIVATE);
		return prefer.getBoolean(PUSH_NOTIFICATION_TAG, true);
	}
	
	// save push notification tag
	public static void savePushNotificationTag(Context ctx, boolean isOpen) {
		SharedPreferences prefer = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		prefer.edit().putBoolean(PUSH_NOTIFICATION_TAG, isOpen).commit();
	}
	
	// load wifi switch tag
	public static boolean getWifiSwitchTag(Context ctx) {
		SharedPreferences prefer = ctx.getSharedPreferences(PREFS_NAME,
				Context.MODE_PRIVATE);
		return prefer.getBoolean(WIFI_SWITCH_TAG, false);
	}
	
	// save wifi switch tag
	public static void saveWifiSwitchTag(Context ctx, boolean isOpen) {
		SharedPreferences prefer = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		prefer.edit().putBoolean(WIFI_SWITCH_TAG, isOpen).commit();
	}
	
	public static void saveUserId(Context ctx, String userId) {
		SharedPreferences prefer = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		prefer.edit().putString(BAIDU_PUSH_USER_ID, userId).commit();
	}
	
	public static String getUserID(Context ctx) {
		SharedPreferences prefer = ctx.getSharedPreferences(PREFS_NAME,
				Context.MODE_PRIVATE);
		return prefer.getString(BAIDU_PUSH_USER_ID, "");
	}
	
	public static void saveChannelId(Context ctx, String channelId) {
		SharedPreferences prefer = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		prefer.edit().putString(BAIDU_PUSH_CHANNEL_ID, channelId).commit();
	}
	
	public static String getChannelId(Context ctx) {
		SharedPreferences prefer = ctx.getSharedPreferences(PREFS_NAME,
				Context.MODE_PRIVATE);
		return prefer.getString(BAIDU_PUSH_CHANNEL_ID, "");
	}
	
	// load category hash
	public static String getCategoryHash(Context ctx) {
		SharedPreferences prefer = ctx.getSharedPreferences(PREFS_NAME,
				Context.MODE_PRIVATE);
		return prefer.getString(CATEGORY_HASH, "");
	}

	// save category hash
	public static void saveCategoryHash(Context ctx, String categoryHash) {
		SharedPreferences prefer = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		prefer.edit().putString(CATEGORY_HASH, categoryHash).commit();
	}
}
