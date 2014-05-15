package cn.koolcloud.ipos.appstore.utils;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.koolcloud.ipos.appstore.constant.Constants;
import cn.koolcloud.ipos.appstore.entity.App;
import cn.koolcloud.ipos.appstore.entity.Category;
import cn.koolcloud.ipos.appstore.entity.Client;
import cn.koolcloud.ipos.appstore.entity.Comment;
import cn.koolcloud.ipos.appstore.entity.NotificationPromotionInfo;
import cn.koolcloud.ipos.appstore.service.aidl.ParcelableApp;


/**
 * <p>Title: JsonUtils.java</p>
 * <p>Description: be used for parsing json objects.</p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: All In Pay</p>
 * @author 		Teddy
 * @date 		2013-11-5
 * @version 	
 */
public class JsonUtils {
	private static final String TAG = "JsonUtils";
	
	/**
	* @Title: getStringValue
	* @Description: TODO
	* @param @param jsonObject
	* @param @param jsonNameKey
	* @param @return
	* @param @throws JSONException
	* @return String 
	* @throws
	*/
	public static String getStringValue(JSONObject jsonObject, String jsonNameKey) throws JSONException {
		if (null == jsonObject) {
			return null;
		}
		if (!jsonObject.isNull(jsonNameKey)) {
			return jsonObject.getString(jsonNameKey);
		}
		return null;
	}
	
	public static int getIntValue(JSONObject jsonObject, String jsonNameKey) throws JSONException {
		if (null == jsonObject) {
			return -1;
		}
		
		if (!jsonObject.isNull(jsonNameKey)) {
			return jsonObject.getInt(jsonNameKey);
		}
		return -1;
	}
	
	public static long getLongValue(JSONObject jsonObject, String jsonNameKey) throws JSONException {
		if (null == jsonObject) {
			return -1L;
		}
		if (!jsonObject.isNull(jsonNameKey)) {
			return jsonObject.getLong(jsonNameKey);
		}
		return -1L;
	}
	
	public static JSONObject getJSONObject(JSONObject jsonObject, String jsonNameKey) throws JSONException {
		if (null == jsonObject) {
			return null;
		}
		if (!jsonObject.isNull(jsonNameKey)) {
			return jsonObject.getJSONObject(jsonNameKey);
		}
		return null;	
	}
	
	public static JSONArray getJSONArray(JSONObject jsonObject, String jsonNameKey) throws JSONException {
		if (null == jsonObject) {
			return null;
		}
		if (!jsonObject.isNull(jsonNameKey)) {
			return jsonObject.getJSONArray(jsonNameKey);
		}
		return null;	
	}
	
	/**
	* @Title: parseJSONClient
	* @Description: TODO
	* @param @param jsonObject
	* @param @return
	* @return Client 
	* @throws
	*/
	public static Client parseJSONClient(JSONObject jsonObject) {
		Client updateVersion = null;
		try {
			JSONObject dataJson = getJSONObject(jsonObject,
					Constants.REQUEST_DATA);
			
			JSONObject updateJson = getJSONObject(dataJson,
					Constants.JSON_KEY_CLIENT);
			
			String strategy = getStringValue(dataJson,
					Constants.JSON_KEY_STRATEGY);
			if (Constants.STRATEGY_UPDATE_NONE.equals(strategy)) {
				return null;
			}
			
			updateVersion = new Client(strategy, 
					getStringValue(updateJson, Constants.JSON_KEY_ID), 
					getStringValue(updateJson, Constants.JSON_KEY_VERSION), 
					getStringValue(updateJson, Constants.JSON_KEY_SIZE));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return updateVersion;
	}
	
	/**
	* @Title: parseJSONCategories
	* @Description: TODO
	* @param @param jsonObject
	* @param @return
	* @return List<Category> 
	* @throws
	*/
	public static List<Category> parseJSONCategories(JSONObject jsonObject) {
		List<Category> categoryList = new ArrayList<Category>();
		
		try {
			JSONObject dataJson = getJSONObject(jsonObject, Constants.REQUEST_DATA);
			JSONArray categoriesJson = getJSONArray(dataJson, Constants.JSON_KEY_CATEGORIES);
			
			if (categoriesJson != null && categoriesJson.length() > 0) {
				
				for (int i = 0; i < categoriesJson.length(); i++) {
					JSONObject categoryJson = categoriesJson.getJSONObject(i);
					Category category = new Category(getStringValue(categoryJson, Constants.JSON_KEY_ID),
							getStringValue(categoryJson, Constants.JSON_KEY_NAME),
							getStringValue(categoryJson, Constants.REQUEST_HASH),
							getStringValue(categoryJson, Constants.JSON_KEY_ICON),
							getStringValue(categoryJson, Constants.JSON_KEY_PRIORITY));
					categoryList.add(category);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return categoryList;
	}
	
	/**
	* @Title: parseJSONApps
	* @Description: parse json to list apps
	* @param @param jsonObject
	* @param @return
	* @return List<App> 
	* @throws
	*/
	public static List<App> parseJSONApps(JSONObject jsonObject) {
		List<App> appsList = null;
		try {
			JSONArray categoriesJson = getJSONObject(jsonObject,
					Constants.REQUEST_DATA).getJSONArray(
					Constants.JSON_KEY_CATEGORIES);
			Long categoryId = null;
			
			if (categoriesJson != null && categoriesJson.length() > 0) {
				
				appsList = new ArrayList<App>();
				for (int i = 0; i < categoriesJson.length(); i++) {
					
					JSONObject categoryJson = categoriesJson.getJSONObject(i);
					categoryId = getLongValue(categoryJson, Constants.JSON_KEY_ID);
					JSONArray appsJson = getJSONArray(categoryJson,
							Constants.JSON_KEY_APPS);
					int appLength = appsJson.length();
					Logger.d("applen:" + appLength);
					for (int j = 0; j < appLength; j++) {
						JSONObject appJson = appsJson.getJSONObject(j);
						App app = new App(getStringValue(appJson,
								Constants.JSON_KEY_ID), 
								getStringValue(appJson, Constants.JSON_KEY_NAME), 
								getStringValue(appJson, Constants.JSON_KEY_VERSION), 
								getStringValue(appJson, Constants.JSON_KEY_SIZE), 
								getStringValue(appJson, Constants.JSON_KEY_ICON), 
								getStringValue(appJson, Constants.JSON_KEY_DOWNLOAD_ID));
						app.setRating(getStringValue(appJson, Constants.JSON_KEY_SCORE));
						app.setDate(getLongValue(appJson, Constants.JSON_KEY_DATE));
						app.setVendor(getStringValue(appJson, Constants.JSON_KEY_VENDOR));
						app.setVersionCode(getIntValue(appJson, Constants.JSON_KEY_VERSION_CODE));
						app.setPackageName(getStringValue(appJson, Constants.JSON_KEY_PACKAGE_NAME));
						appsList.add(app);
					}
					//			mapping.put(categoryId, apps);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return appsList;
	}
	
	/**
	 * @Title: parseJSONAdPromotionApps
	 * @Description: parse json to apps list
	 * @param jsonObject
	 * @return
	 * @return: List<App>
	 */
	public static List<App> parseJSONAdPromotionApps(JSONObject jsonObject) {
		List<App> appsList = null;
		try {
			JSONObject dataJson = getJSONObject(jsonObject, Constants.REQUEST_DATA);
			JSONArray promotionArray = getJSONArray(dataJson, Constants.JSON_KEY_PROMOTIONS);
			
			if (promotionArray != null && promotionArray.length() > 0) {
				
				appsList = new ArrayList<App>();
				for (int i = 0; i < promotionArray.length(); i++) {
					JSONObject appJson = promotionArray.getJSONObject(i);
					App app = new App(getStringValue(appJson,
							Constants.JSON_KEY_ID), 
							getStringValue(appJson, Constants.JSON_KEY_NAME), 
							getStringValue(appJson, Constants.JSON_KEY_VERSION), 
							getStringValue(appJson, Constants.JSON_KEY_SIZE), 
							getStringValue(appJson, Constants.JSON_KEY_ICON), 
							getStringValue(appJson, Constants.JSON_KEY_DOWNLOAD_ID));
					app.setRating(getStringValue(appJson, Constants.JSON_KEY_SCORE));
					app.setDate(getLongValue(appJson, Constants.JSON_KEY_DATE));
					app.setVendor(getStringValue(appJson, Constants.JSON_KEY_VENDOR));
					app.setVersionCode(getIntValue(appJson, Constants.JSON_KEY_VERSION_CODE));
					app.setPackageName(getStringValue(appJson, Constants.JSON_KEY_PACKAGE_NAME));
					app.setType(getIntValue(appJson, Constants.JSON_KEY_TYPE));
					app.setImg(getStringValue(appJson, Constants.JSON_KEY_IMG));
					app.setUrl(getStringValue(appJson, Constants.JSON_KEY_URL));
					appsList.add(app);
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return appsList;
	}
	
	public static List<App> parsePushJSONApp(JSONObject jsonObject) {
		List<App> appsList = null;
		try {
			appsList = new ArrayList<App>();
			App app = new App(getStringValue(jsonObject,
					Constants.JSON_KEY_ID), 
					getStringValue(jsonObject, Constants.JSON_KEY_NAME), 
					getStringValue(jsonObject, Constants.JSON_KEY_VERSION), 
					getStringValue(jsonObject, Constants.JSON_KEY_SIZE), 
					getStringValue(jsonObject, Constants.JSON_KEY_ICON), 
					getStringValue(jsonObject, Constants.JSON_KEY_DOWNLOAD_ID));
			app.setRating(getStringValue(jsonObject, Constants.JSON_KEY_SCORE));
			app.setDate(getLongValue(jsonObject, Constants.JSON_KEY_DATE));
			app.setVendor(getStringValue(jsonObject, Constants.JSON_KEY_VENDOR));
			app.setVersionCode(getIntValue(jsonObject, Constants.JSON_KEY_VERSION_CODE));
			app.setPackageName(getStringValue(jsonObject, Constants.JSON_KEY_PACKAGE_NAME));
			appsList.add(app);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return appsList;
	}
	
	/**
	 * @Title: parsePushPromotion
	 * @Description: TODO parse notification promotions
	 * @param jsonObject
	 * @return
	 * @return: List<NotificationPromotionInfo>
	 */
	public static List<NotificationPromotionInfo> parsePushPromotion(JSONObject jsonObject) {
		List<NotificationPromotionInfo> promotionList = null;
		try {
			promotionList = new ArrayList<NotificationPromotionInfo>();
			NotificationPromotionInfo promotionInfo = new NotificationPromotionInfo();
			promotionInfo.setType(getIntValue(jsonObject, Constants.JSON_KEY_TYPE));
			promotionInfo.setId(getIntValue(jsonObject, Constants.JSON_KEY_ID));
			promotionInfo.setTitle(getStringValue(jsonObject, Constants.JSON_KEY_TITLE));
			promotionInfo.setDate(getStringValue(jsonObject, Constants.JSON_KEY_DATE));
			promotionInfo.setImageId(getStringValue(jsonObject, Constants.JSON_KEY_IMAGE));
			promotionInfo.setDescription(getStringValue(jsonObject, Constants.JSON_KEY_DESCRIPTION));
			promotionList.add(promotionInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return promotionList;
	}
	
	/**
	 * @Title: parseSearchingJSONApps
	 * @Description: parse json object to apps
	 * @param jsonObject
	 * @return
	 * @return: List<App>
	 */
	public static List<App> parseSearchingJSONApps(JSONObject jsonObject) {
		List<App> appsList = null;
		try {
			JSONObject dataJson = getJSONObject(jsonObject, Constants.REQUEST_DATA);
			JSONArray appArray = getJSONArray(dataJson, Constants.JSON_KEY_APPS);
			
			if (appArray != null && appArray.length() > 0) {
				
				appsList = new ArrayList<App>();
				for (int i = 0; i < appArray.length(); i++) {
					
					JSONObject appJson = appArray.getJSONObject(i);
					App app = new App();
					
					app.setId(getStringValue(appJson, Constants.JSON_KEY_ID));
					app.setName(getStringValue(appJson, Constants.JSON_KEY_NAME));
					app.setIcon(getStringValue(appJson, Constants.JSON_KEY_ICON));
					app.setDownloadId(getStringValue(appJson, Constants.JSON_KEY_DOWNLOAD_ID));
					app.setVersion(getStringValue(appJson, Constants.JSON_KEY_VERSION));
					app.setSize(getStringValue(appJson, Constants.JSON_KEY_SIZE));
					app.setRating(getStringValue(appJson, Constants.JSON_KEY_SCORE));
					app.setVendor(getStringValue(appJson, Constants.JSON_KEY_VENDOR));
					app.setDate(getLongValue(appJson, Constants.JSON_KEY_DATE));
					app.setVersionCode(getIntValue(appJson, Constants.JSON_KEY_VERSION_CODE));
					app.setPackageName(getStringValue(appJson, Constants.JSON_KEY_PACKAGE_NAME));
					appsList.add(app);
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return appsList;
	}
	
	/**
	 * @Title: parseJSONParcelApp
	 * @Description: TODO
	 * @param jsonObject
	 * @return
	 * @return: ParcelableApp
	 */
	public static ParcelableApp parseJSONParcelApp(JSONObject jsonObject) {
		ParcelableApp app = null;
		try {
			
			JSONObject dataObj = getJSONObject(jsonObject, Constants.REQUEST_DATA);			
			JSONArray appArray = getJSONArray(dataObj, Constants.JSON_KEY_APPS);
			
			if (appArray != null && appArray.length() > 0) {
				
				JSONObject appJson = appArray.getJSONObject(0);
				app = new ParcelableApp();
				app.setId(getStringValue(appJson, Constants.JSON_KEY_ID));
				app.setName(getStringValue(appJson, Constants.JSON_KEY_NAME));
				app.setIcon(getStringValue(appJson, Constants.JSON_KEY_ICON));
				app.setDownloadId(getStringValue(appJson, Constants.JSON_KEY_DOWNLOAD_ID));
				app.setVersion(getStringValue(appJson, Constants.JSON_KEY_VERSION));
				app.setSize(getStringValue(appJson, Constants.JSON_KEY_SIZE));
				app.setRating(getStringValue(appJson, Constants.JSON_KEY_SCORE));
				app.setVender(getStringValue(appJson, Constants.JSON_KEY_VENDOR));
				app.setDate(getLongValue(appJson, Constants.JSON_KEY_DATE));
				app.setVersionCode(getIntValue(appJson, Constants.JSON_KEY_VERSION_CODE));
				app.setPackageName(getStringValue(appJson, Constants.JSON_KEY_PACKAGE_NAME));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return app;
	}
	
	public static List<Comment> parseJSONComments(JSONObject jsonObject) {
		List<Comment> commentsList = null;
		try {
			JSONObject dataJson = getJSONObject(jsonObject, Constants.REQUEST_DATA);
			JSONArray commentsJson = getJSONArray(dataJson, Constants.JSON_KEY_COMMENTS);
			/*JSONArray commentsJson = getJSONObject(jsonObject,
					Constants.REQUEST_DATA).getJSONArray(
							Constants.JSON_KEY_CATEGORIES);*/
			
			if (commentsJson != null && commentsJson.length() > 0) {
				
				commentsList = new ArrayList<Comment>();
				for (int i = 0; i < commentsJson.length(); i++) {					
					JSONObject commentJson = commentsJson.getJSONObject(i);
					Comment comment = new Comment();
					comment.setUser(getStringValue(commentJson, Constants.JSON_KEY_USER));
					comment.setRating(getStringValue(commentJson, Constants.JSON_KEY_SCORE));
					comment.setDate(getLongValue(commentJson, Constants.JSON_KEY_DATE));
					comment.setComment(getStringValue(commentJson, Constants.JSON_KEY_COMMENT));
					commentsList.add(comment);
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return commentsList;
	}
}
