package cn.koolcloud.ipos.appstore.api;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import cn.koolcloud.ipos.appstore.AppStorePreference;
import cn.koolcloud.ipos.appstore.cache.FileManager;
import cn.koolcloud.ipos.appstore.common.AsyncHttpClient;
import cn.koolcloud.ipos.appstore.constant.Constants;
import cn.koolcloud.ipos.appstore.entity.ResultSet;
import cn.koolcloud.ipos.appstore.interfaces.CallBack;
import cn.koolcloud.ipos.appstore.interfaces.Task;
import cn.koolcloud.ipos.appstore.utils.Logger;

public class ApiService {
	private static final String TAG = "ApiService";

//	private static final String HOST = "http://192.168.101.35:8080";
//	private static final String HOST = "http://192.168.101.34:8080";//UAT
//	private static final String HOST = "https://192.168.101.34";//UAT
//	private static final String HOST = "https://116.236.252.102:30068";//static ip
//	private static final String HOST = "http://10.0.5.124:8080";//TEST
//	private static final String HOST = "http://192.168.1.100:8080";
	private static final String HOST = "https://aipstore.allinpay.com";//production env
	
	private static final String VERSION_VAL = "1.0";
	private static final String REGISTER_TERMINAL_ACTION = "register";
	private static final String GET_CATEGORIES_ACTION = "getCategories";
	private static final String GET_APPS_ACTION = "getApps";
	private static final String SEARCH_APP_ACTION = "search";
	private static final String GET_APP_DETAILS_ACTION = "getAppDetails";
	private static final String DOWNLOAD_APP_ACTION = "downloadApp";
	private static final String DOWNLOAD_PIC_ACTION = "downloadIcon";
	private static final String SELF_UPDATE_ACTION = "selfUpdate";
	private static final String POST_COMMENT_ACTION = "postComment";
	private static final String GET_COMMENTS_ACTION = "getComments";
	private static final String GET_COMMENT_SUMMARY_ACTION = "getCommentSummary";
	private static final String CHECK_APP_UPDATE_ACTION = "checkUpdate";
	private static final String GET_PROMOTION_ACTION = "getPromotions";

	private static final String REQUEST_CONTEXT = "/asapi";
	private static final String REGISTER_TERMINAL_PATH = "/appstore/terminal/register";
	private static final String GET_CATEGORIES_PATH = "/appstore/app/getCategories";
	private static final String SEARCH_APPS_PATH = "/appstore/app/search";
	private static final String GET_APPS_PATH = "/appstore/app/getApps";
	private static final String GET_APP_DETAILS_PATH = "/appstore/app/getAppDetails";
	private static final String SELF_UPDATE_PATH = "/appstore/client/selfUpdate";
	private static final String POST_COMMENT_PATH = "/appstore/app/postComment";
	private static final String GET_COMMENTS_PATH = "/appstore/app/getComments";
	private static final String GET_COMMENT_SUMMARY_PATH = "/appstore/app/getCommentSummary";
	private static final String CHECK_APP_UPDATE_PATH = "/appstore/app/checkUpdate";
	private static final String DOWNLOAD_APP_PATH = "/appstore/download/downloadApp";
	private static final String DOWNLOAD_PICTURE_PATH = "/appstore/download/downloadPic";
	private static final String CONTRACT_LINK_PATH = "/asweb/agreement";
//	private static final String DOWNLOAD_APP_PATH = "/appstore/download/download";
//	private static final String DOWNLOAD_PICTURE_PATH = "/appstore/download/download";
	private static final String GET_PROMOTION_PATH = "/appstore/promotion/getPromotions";

	/**
	* @Title: getDownloadPicJson
	* @Description: TODO
	* @param @param picId
	* @param @param ctx
	* @param @return
	* @return JSONObject 
	* @throws
	*/
	public static JSONObject getDownloadPicJson(String picId, Context ctx) {
		JSONObject jsonObj = null;
		try {
			jsonObj = generateReq(DOWNLOAD_PIC_ACTION);
			jsonObj.put(Constants.JSON_KEY_TERMINAL_ID, AppStorePreference.getTerminalID(ctx));
			jsonObj.getJSONObject(Constants.REQUEST_DATA).put(Constants.JSON_KEY_ID, picId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonObj;
	}
	
	public static String getDownloadPictureUrl() {
		return HOST + REQUEST_CONTEXT + DOWNLOAD_PICTURE_PATH;
	}
	
	public static String getDownloadAppUrl() {
		return HOST + REQUEST_CONTEXT + DOWNLOAD_APP_PATH;
	}
	
	public static String getContractLink() {
		return HOST + CONTRACT_LINK_PATH;
	}

	/**
	* @Title: generateReq
	* @Description: generate common JSON Object
	* @param @param action
	* @param @return
	* @param @throws JSONException
	* @return JSONObject 
	* @throws
	*/
	private static JSONObject generateReq(String action) throws JSONException {
		JSONObject req = new JSONObject();
		req.put(Constants.JSON_KEY_VERSION, VERSION_VAL);
		req.put(Constants.JSON_KEY_ACTION, action);
		JSONObject data = new JSONObject();
		req.put(Constants.REQUEST_DATA, data);
		return req;
	}
	
	/**
	* @Title: getUpdateVersion
	* @Description: TODO
	* @param @param context
	* @param @param terminalID
	* @param @param versionName
	* @param @param callBack
	* @param @return
	* @return Task 
	* @throws
	*/
	public static Task getUpdateVersion(Context context, String terminalID, String versionName, CallBack callBack) {
		if (TextUtils.isEmpty(terminalID)) {
			return null;
		}
		
		String url = HOST + REQUEST_CONTEXT + SELF_UPDATE_PATH;
		
		JSONObject reqestJson = null;
		try {
			reqestJson = generateReq(SELF_UPDATE_ACTION);
			reqestJson.put(Constants.JSON_KEY_TERMINAL_ID, terminalID);
			reqestJson.getJSONObject(Constants.REQUEST_DATA).put(Constants.JSON_KEY_VERSION, versionName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return AsyncHttpClient.request(context, url, reqestJson, callBack,
				ResultSet.RESPONSETYPE.RESPONSE_TYPE_JSON_OBJECT);
	}
	
	/**
	* @Title: getAppCategories
	* @Description: TODO get app categories by terminal id
	* @param @param context
	* @param @param terminalID
	* @param @param categoryHash
	* @param @param callBack
	* @param @return
	* @return Task 
	* @throws
	*/
	public static Task getAppCategories(Context context, String terminalID, String categoryHash, CallBack callBack) {
		
		if (TextUtils.isEmpty(terminalID)) {
			return null;
		}
		
		JSONObject reqestJson = null;
		String url = HOST + REQUEST_CONTEXT + GET_CATEGORIES_PATH;
		try {
			reqestJson = generateReq(GET_CATEGORIES_ACTION);
			reqestJson.put(Constants.JSON_KEY_TERMINAL_ID, terminalID);
			if (!TextUtils.isEmpty(categoryHash)) {
				reqestJson.getJSONObject(Constants.REQUEST_DATA).put(Constants.REQUEST_HASH, "0");
			} else {
				reqestJson.getJSONObject(Constants.REQUEST_DATA).put(Constants.REQUEST_HASH, Constants.CATEGORY_DEFAULT_HASH);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return AsyncHttpClient.request(context, url, reqestJson, callBack,
				ResultSet.RESPONSETYPE.RESPONSE_TYPE_JSON_OBJECT);
	}
	
	
	/**
	* @Title: getAppsByCategory
	* @Description: TODO get apps by category
	* @param @param context
	* @param @param terminalID
	* @param @param categoryHash
	* @param @param callBack
	* @param @return
	* @return Task 
	* @throws
	*/
	public static Task getAppsByCategory(Context context, String terminalID, String categoryId, CallBack callBack) {
		
		if (TextUtils.isEmpty(terminalID)) {
			return null;
		}
		
		JSONObject reqestJson = null;
		String url = HOST + REQUEST_CONTEXT + GET_APPS_PATH;
		try {
			reqestJson = generateReq(GET_APPS_ACTION);
			reqestJson.put(Constants.JSON_KEY_TERMINAL_ID, terminalID);
			JSONArray categories = new JSONArray();
			categories.put(categoryId);
			reqestJson.getJSONObject(Constants.REQUEST_DATA).put(
					Constants.JSON_KEY_CATEGORIES, categories);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return AsyncHttpClient.request(context, url, reqestJson, callBack,
				ResultSet.RESPONSETYPE.RESPONSE_TYPE_JSON_OBJECT);
	}
	
	public static Task getAppDetailsByIds(Context context, String terminalID, String appId, CallBack callBack) {
		
		if (TextUtils.isEmpty(terminalID)) {
			return null;
		}
		
		JSONObject reqestJson = null;
		String url = HOST + REQUEST_CONTEXT + GET_APP_DETAILS_PATH;
		try {
			reqestJson = generateReq(GET_APP_DETAILS_ACTION);
			reqestJson.put(Constants.JSON_KEY_TERMINAL_ID, terminalID);
			JSONArray apps = new JSONArray();
			apps.put(appId);
			reqestJson.getJSONObject(Constants.REQUEST_DATA).put(
					Constants.JSON_KEY_APP_IDS, apps);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return AsyncHttpClient.request(context, url, reqestJson, callBack,
				ResultSet.RESPONSETYPE.RESPONSE_TYPE_JSON_OBJECT);
	}
	
	/**
	* @Title: getAppsBySearchWord
	* @Description: TODO
	* @param @param context
	* @param @param terminalID
	* @param @param categoryId
	* @param @param callBack
	* @param @return
	* @return Task 
	* @throws
	*/
	public static Task getAppsByKeyWord(Context context, String terminalID, String keyWord, CallBack callBack) {
		
		if (TextUtils.isEmpty(terminalID)) {
			return null;
		}
		
		JSONObject reqestJson = null;
		// FIXME mod url, request action and arguments
		String url = HOST + REQUEST_CONTEXT + SEARCH_APPS_PATH;
		try {
			reqestJson = generateReq(SEARCH_APP_ACTION);
			reqestJson.put(Constants.JSON_KEY_TERMINAL_ID, terminalID);
			reqestJson.getJSONObject(Constants.REQUEST_DATA).put(
					Constants.JSON_KEY_NAME, keyWord);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return AsyncHttpClient.request(context, url, reqestJson, callBack,
				ResultSet.RESPONSETYPE.RESPONSE_TYPE_JSON_OBJECT);
	}
	
	/**
	 * @Title: getPromotions
	 * @Description: get promotions
	 * @param context
	 * @param terminalID
	 * @param callBack
	 * @return
	 * @return: Task
	 */
	public static Task getPromotions(Context context, String terminalID, CallBack callBack) {
		
		if (TextUtils.isEmpty(terminalID)) {
			return null;
		}
		
		JSONObject reqestJson = null;
		// FIXME mod url, request action and arguments
		String url = HOST + REQUEST_CONTEXT + GET_PROMOTION_PATH;
		try {
			reqestJson = generateReq(GET_PROMOTION_ACTION);
			reqestJson.put(Constants.JSON_KEY_TERMINAL_ID, terminalID);
			reqestJson.getJSONObject(Constants.REQUEST_DATA).put(
					Constants.JSON_KEY_ID, AppStorePreference.getPromotinDataID(context));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return AsyncHttpClient.request(context, url, reqestJson, callBack,
				ResultSet.RESPONSETYPE.RESPONSE_TYPE_JSON_OBJECT);
	}
	
	/**
	 * @Title: commitComment
	 * @Description: commit comments
	 * @param context
	 * @param terminalID
	 * @param appId
	 * @param comment
	 * @param rating
	 * @param callBack
	 * @return
	 * @return: Task
	 */
	public static Task commitComment(Context context, String terminalID, String appId, String comment, int rating, CallBack callBack) {
		
		if (TextUtils.isEmpty(terminalID)) {
			return null;
		}
		
		JSONObject reqestJson = null;
		// FIXME mod url, request action and arguments
		String url = HOST + REQUEST_CONTEXT + POST_COMMENT_PATH;
		try {
			reqestJson = generateReq(POST_COMMENT_ACTION);
			reqestJson.put(Constants.JSON_KEY_TERMINAL_ID, terminalID);
		
			reqestJson.getJSONObject(Constants.REQUEST_DATA).put(
					Constants.JSON_KEY_APP_ID, appId);
			reqestJson.getJSONObject(Constants.REQUEST_DATA).put(
					Constants.JSON_KEY_COMMENT, comment);
			reqestJson.getJSONObject(Constants.REQUEST_DATA).put(
					Constants.JSON_KEY_SCORE, rating);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return AsyncHttpClient.request(context, url, reqestJson, callBack,
				ResultSet.RESPONSETYPE.RESPONSE_TYPE_JSON_OBJECT);
	}
	
	/**
	 * @Title: getComments
	 * @Description: get comment by appId
	 * @param context
	 * @param terminalID
	 * @param appId
	 * @param callBack
	 * @return
	 * @return: Task
	 */
	public static Task getComments(Context context, String terminalID, String appId, CallBack callBack) {
		
		if (TextUtils.isEmpty(terminalID)) {
			return null;
		}
		
		JSONObject reqestJson = null;
		// FIXME mod url, request action and arguments
		String url = HOST + REQUEST_CONTEXT + GET_COMMENTS_PATH;
		try {
			reqestJson = generateReq(GET_COMMENTS_ACTION);
			reqestJson.put(Constants.JSON_KEY_TERMINAL_ID, terminalID);
			
			reqestJson.getJSONObject(Constants.REQUEST_DATA).put(
					Constants.JSON_KEY_APP_ID, appId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return AsyncHttpClient.request(context, url, reqestJson, callBack,
				ResultSet.RESPONSETYPE.RESPONSE_TYPE_JSON_OBJECT);
	}
	
	/**
	 * @Title: getCommentSummary
	 * @Description: get comment summary
	 * @param context
	 * @param terminalID
	 * @param appId
	 * @param callBack
	 * @return
	 * @return: Task
	 */
	public static Task getCommentSummary(Context context, String terminalID, String appId, CallBack callBack) {
		
		if (TextUtils.isEmpty(terminalID)) {
			return null;
		}
		
		JSONObject reqestJson = null;
		// FIXME mod url, request action and arguments
		String url = HOST + REQUEST_CONTEXT + GET_COMMENT_SUMMARY_PATH;
		try {
			reqestJson = generateReq(GET_COMMENT_SUMMARY_ACTION);
			reqestJson.put(Constants.JSON_KEY_TERMINAL_ID, terminalID);
			
			reqestJson.getJSONObject(Constants.REQUEST_DATA).put(
					Constants.JSON_KEY_APP_ID, appId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return AsyncHttpClient.request(context, url, reqestJson, callBack,
				ResultSet.RESPONSETYPE.RESPONSE_TYPE_JSON_OBJECT);
	}
	
	/**
	 * @Title: checkSoftUpdate
	 * @Description: check app for update
	 * @param context
	 * @param softName
	 * @param version
	 * @param callBack
	 * @return
	 * @return: Task
	 */
	public static Task checkSoftUpdate(Context context, String softName, String version, CallBack callBack) {
		
		if (TextUtils.isEmpty(softName) || TextUtils.isEmpty(version)) {
			return null;
		}
		
		JSONObject reqestJson = null;
		// FIXME mod url, request action and arguments
		String url = HOST + REQUEST_CONTEXT + CHECK_APP_UPDATE_PATH;
		try {
			reqestJson = generateReq(CHECK_APP_UPDATE_ACTION);
			
			JSONObject appObj = new JSONObject();
			appObj.put(Constants.JSON_KEY_NAME, softName);
			appObj.put(Constants.JSON_KEY_VERSION, version);
			
			JSONArray appArray = new JSONArray();
			appArray.put(appObj);
			
			reqestJson.getJSONObject(Constants.REQUEST_DATA).put(
					Constants.JSON_KEY_APPS, appArray);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return AsyncHttpClient.request(context, url, reqestJson, callBack,
				ResultSet.RESPONSETYPE.RESPONSE_TYPE_JSON_OBJECT);
	}
	
	public static JSONObject checkSoftUpdate(String terminalId, String packageName, int versionCode) {
		JSONObject reponseJson = null;
		
		HttpService httpService = new HttpService();
		String url = HOST + REQUEST_CONTEXT + CHECK_APP_UPDATE_PATH;
	
		JSONObject dataJson = null;
		try {
			JSONObject reqestJson = generateReq(CHECK_APP_UPDATE_ACTION);
			reqestJson.put(Constants.JSON_KEY_TERMINAL_ID, terminalId);
						
			JSONObject appObj = new JSONObject();
			appObj.put(Constants.JSON_KEY_PACKAGE_NAME, packageName);
			appObj.put(Constants.JSON_KEY_VERSION_CODE, versionCode);
			
			JSONArray appArray = new JSONArray();
			appArray.put(appObj);
			
			reqestJson.getJSONObject(Constants.REQUEST_DATA).put(
					Constants.JSON_KEY_APPS, appArray);
			
			reponseJson = httpService.sendRequest(url, reqestJson);
			Logger.d(reponseJson.toString());
//			dataJson = reponseJson.getJSONObject(Constants.REQUEST_DATA);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return reponseJson;
	
	}
	
	
	/**
	* @Title: register
	* @Description: register client
	* @param @param context
	* @param @param callBack
	* @param @return
	* @return Task 
	* @throws
	*/
	public static Task register(Context context, String userId, String channelId, CallBack callBack) {
		
		JSONObject reqestJson = null;
		String url = HOST + REQUEST_CONTEXT + REGISTER_TERMINAL_PATH;
		try {
			reqestJson = generateReq(REGISTER_TERMINAL_ACTION);
			JSONObject dataJson = reqestJson.getJSONObject(Constants.REQUEST_DATA);
			dataJson.put(Constants.JSON_KEY_SN, android.os.Build.SERIAL);
			dataJson.put(Constants.JSON_KEY_MANUFACTURER, android.os.Build.MANUFACTURER);
			JSONObject modelJson = new JSONObject();
			dataJson.put(Constants.JSON_KEY_TERMINAL_MODEL, modelJson);
			modelJson.put(Constants.JSON_KEY_ANDROID_VERSION, android.os.Build.VERSION.RELEASE);
			modelJson.put(Constants.JSON_KEY_NAME, android.os.Build.MODEL);
			modelJson.put(Constants.JSON_KEY_CPU_INFO, android.os.Build.CPU_ABI);
			modelJson.put(Constants.JSON_KEY_DISPLAY, android.os.Build.DISPLAY);
			modelJson.put(Constants.JSON_KEY_MEM_INFO, "");
			modelJson.put(Constants.JSON_KEY_PRINTER_INFO, "");
			modelJson.put(Constants.JSON_KEY_RESOLUTION_INFO, "");
			modelJson.put(Constants.JSON_KEY_CARD_READER_INFO, "");
			
			if (!TextUtils.isEmpty(userId) && !TextUtils.isEmpty(channelId)) {
				JSONObject baiduPushJson = new JSONObject();
				dataJson.put(Constants.JSON_KEY_BAIDU_PUSH, baiduPushJson);
				baiduPushJson.put(Constants.JSON_KEY_USER_ID, userId);
				baiduPushJson.put(Constants.JSON_KEY_CHANNEL_ID, channelId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return AsyncHttpClient.request(context, url, reqestJson, callBack,
				ResultSet.RESPONSETYPE.RESPONSE_TYPE_JSON_OBJECT);
	}
	
	/**
	* @Title: downloadApp
	* @Description: TODO
	* @param @param context
	* @param @param terminalId
	* @param @param id
	* @param @param callBack
	* @param @return
	* @return Task 
	* @throws
	*/
	public static Task downloadApp(Context context, String terminalId, String id, CallBack callBack) {
		
		JSONObject reqestJson = null;
		String url = HOST + REQUEST_CONTEXT + DOWNLOAD_APP_PATH;
		try {
			reqestJson = generateReq(DOWNLOAD_APP_ACTION);

			reqestJson.put(Constants.JSON_KEY_TERMINAL_ID, terminalId);
			reqestJson.getJSONObject(Constants.REQUEST_DATA).put(Constants.JSON_KEY_ID, id);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return AsyncHttpClient.request(context, url, reqestJson, callBack,
				ResultSet.RESPONSETYPE.RESPONSE_TYPE_STREAM);
	}
	
	
	public static File downloadFile(Context context, String terminalId, String id, String fileName) {
		InputStream in = null;
		FileOutputStream fos = null;
		String sdPath = "";
		File file = null;

		//don't download apk file to sd from 2014-01-28
		/*if (FileManager.canWriteSD()) {
			
			sdPath = Environment.getExternalStorageDirectory() + "/";
			File tmpFile = new File(sdPath + "download");
			if (!tmpFile.exists()) {
				tmpFile.mkdir();
			}
			file = new File(sdPath + "download/" + fileName);
		} else {*/
			sdPath =  context.getFileStreamPath(fileName).getAbsolutePath();
			file = new File(sdPath);
		//}
		
		try {
			JSONObject jsonObj = generateReq(DOWNLOAD_APP_ACTION);
			
			jsonObj.put(Constants.JSON_KEY_TERMINAL_ID, terminalId);
			jsonObj.getJSONObject(Constants.REQUEST_DATA).put(Constants.JSON_KEY_ID, id);
			String httpUrl = HOST + REQUEST_CONTEXT + DOWNLOAD_APP_PATH;
			
//			HttpClient client = new DefaultHttpClient();
			HttpClient client = AsyncHttpClient.getDefaultHttpClient();
			HttpPost request = new HttpPost(httpUrl);
			request.setHeader("Content-Type", "application/json;charset=UTF8");
			request.setEntity(new StringEntity(jsonObj.toString()));
			HttpResponse response = client.execute(request);
			
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				in = new BufferedInputStream(response.getEntity().getContent());
				fos = new FileOutputStream(file);
				byte[] buf = new byte[1024];
				while (true) {
					if (in != null) {
						int numRead = in.read(buf);
						if (numRead <= 0) {
							break;
						} else {
							fos.write(buf, 0, numRead);
						}

					} else {
						break;
					}
				}
			} else {
				Logger.d(TAG + "_" + "error: "
						+ response.getStatusLine().getStatusCode());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file;
	}
	
	public static JSONObject getDownloadFileJson(String terminalId, String id) {
		JSONObject jsonObj = null;
		
		try {
			jsonObj = generateReq(DOWNLOAD_APP_ACTION);
			jsonObj.put(Constants.JSON_KEY_TERMINAL_ID, terminalId);
			jsonObj.getJSONObject(Constants.REQUEST_DATA).put(
					Constants.JSON_KEY_ID, id);
//			String httpUrl = HOST + REQUEST_CONTEXT + DOWNLOAD_PATH;
//			jsonObj.put(Constants.JSON_KEY_DOWNLOAD_APK_URL, httpUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonObj;
	}
}
