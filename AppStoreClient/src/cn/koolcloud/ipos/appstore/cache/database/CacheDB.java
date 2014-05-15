package cn.koolcloud.ipos.appstore.cache.database;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import cn.koolcloud.ipos.appstore.entity.App;
import cn.koolcloud.ipos.appstore.entity.AppInfo;
import cn.koolcloud.ipos.appstore.entity.Category;
import cn.koolcloud.ipos.appstore.entity.NotificationPromotionInfo;

/**
 * <p>Title: CacheDB.java</p>
 * <p>Description: Cache all the categories and apps to database</p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: All In Pay</p>
 * @author 		Teddy
 * @date 		2013-11-18
 * @version 	
 */
public class CacheDB extends BaseSqlAdapter {

	private final static String DATABASE_NAME = "Cache.db";
    private final static int DATABASE_VERSION = 2;
    private final static String CATEGORY_TABLE_NAME = "category_table";
    private final static String CATEGORY_ID = "id";
    private final static String CATEGORY_NAME = "category_name";
    private final static String CATEGORY_HASH = "category_hash";
    private final static String CATEGORY_ICON = "category_icon";
    private final static String CATEGORY_PRIORITY = "category_priority";
    
    private final static String NOTIFICATION_PROMOTION_TABLE_NAME = "notification_promotion_table";
    private final static String NOTIFY_PROMOTION_ID = "id";
    private final static String NOTIFY_PROMOTION_TYPE = "type";
    private final static String NOTIFY_PROMOTION_TITLE = "title";
    private final static String NOTIFY_PROMOTION_DATE = "date";
    private final static String NOTIFY_PROMOTION_IMAGE = "image";
    private final static String NOTIFY_PROMOTION_DESCRIPTION = "description";
    
    private final static String AD_PROMOTION_TABLE_NAME = "ad_promotion_table";
    private final static String APP_TABLE_NAME = "app_table";
    private final static String APP_ID = "id";
    private final static String APP_NAME = "app_name";
    private final static String APP_VERSION = "app_version";
    private final static String APP_SIZE = "app_size";
    private final static String APP_ICON = "app_icon";
    private final static String APP_PACKAGE_NAME = "app_package_name";
    private final static String APP_VERSION_CODE = "app_version_code";
    private final static String APP_CATEGORY_ID = "category_id";
    private final static String APP_DOWNLOAD_ID = "app_download_id";
    private final static String APP_RATING = "app_rating";
    private final static String APP_VENDOR = "app_vendor";
    private final static String APP_DATE = "app_date";
    private final static String AD_TYPE = "ad_type";
    private final static String AD_IMG = "ad_img";
    private final static String AD_URL = "ad_url";
    private final static String AD_APP_ID = "app_id";
    
    private Context context;
    private String dbName;
    
    private static CacheDB cacheDB;
  
    private CacheDB(Context ctx, int version) {
    	this.context = ctx;
    	if (dbName == null) {
    		dbName = context.getFileStreamPath(DATABASE_NAME).getAbsolutePath();
    	}
    	
//		createCacheDB();
		mDbHelper = new CacheHelper(ctx, DATABASE_NAME, null, version);
    } 
    
    public static CacheDB getInstance(Context ctx) {
    	if (cacheDB == null) {
    		cacheDB = new CacheDB(ctx, DATABASE_VERSION);
    	}
    	return cacheDB;
    }
  
    private void createCacheDB() {
    	try {
    		File file = new File(dbName);
    		
    		//create file
			if (!file.exists()) {
				file.createNewFile();
			}
    		
			SQLiteDatabase sdbVersion = SQLiteDatabase.openOrCreateDatabase(file, null);
			String createCategorySql = "CREATE TABLE IF NOT EXISTS " + CATEGORY_TABLE_NAME + " (" + CATEGORY_ID 
		        + " INTEGER primary key, " 
		        + CATEGORY_HASH + " varchar, " 
		        + CATEGORY_ICON + " varchar, " 
		        + CATEGORY_PRIORITY + " varchar, " 
		        + CATEGORY_NAME + " varchar);";
	        
	        String createAppSql = "CREATE TABLE IF NOT EXISTS " + APP_TABLE_NAME + " (" + APP_ID 
		        + " INTEGER primary key, " 
		        + APP_NAME + " varchar, " 
		        + APP_VERSION + " varchar, " 
		        + APP_SIZE + " varchar, " 
		        + APP_ICON + " varchar, " 
		        + APP_CATEGORY_ID + " varchar, " 
		        + APP_DOWNLOAD_ID + " varchar);"; 
			sdbVersion.execSQL(createCategorySql);
			sdbVersion.execSQL(createAppSql);
			sdbVersion.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
  
    public List<Category> selectAllCategories() {
//        Cursor cursor = db.query(CATEGORY_TABLE_NAME, null, null, null, null, null, null);
        String sql = "select * from " + CATEGORY_TABLE_NAME + " order by category_priority asc";
        Cursor cursor = getCursor(sql, null);
        List<Category> categoryList = new ArrayList<Category>();
        while (cursor.moveToNext()) {
        	String id = cursor.getString(cursor.getColumnIndex(CATEGORY_ID));
        	String name = cursor.getString(cursor.getColumnIndex(CATEGORY_NAME));
        	String hash = cursor.getString(cursor.getColumnIndex(CATEGORY_HASH));
        	String icon = cursor.getString(cursor.getColumnIndex(CATEGORY_ICON));
        	String priority = cursor.getString(cursor.getColumnIndex(CATEGORY_PRIORITY));
        	
        	Category category = new Category(id, name, hash, icon, priority);
        	categoryList.add(category);
        }
        cursor.close();
//        closeDB();
        return categoryList;
    }
    
    public List<App> selectAllApps() {
//    	SQLiteDatabase db = this.getReadableDatabase();
//    	Cursor cursor = db.query(APP_TABLE_NAME, null, null, null, null, null, null);
    	String sql = "select * from " + APP_TABLE_NAME;
    	Cursor cursor = getCursor(sql, null);
    	List<App> appList = new ArrayList<App>();
    	while (cursor.moveToNext()) {
    		String id = cursor.getString(cursor.getColumnIndex(APP_ID));
    		String name = cursor.getString(cursor.getColumnIndex(APP_NAME));
    		String version = cursor.getString(cursor.getColumnIndex(APP_VERSION));
    		String icon = cursor.getString(cursor.getColumnIndex(APP_ICON));
    		String size = cursor.getString(cursor.getColumnIndex(APP_SIZE));
    		String downloadId = cursor.getString(cursor.getColumnIndex(APP_DOWNLOAD_ID));
    		
    		App app = new App(id, name, version, size, icon, downloadId);
    		appList.add(app);
    	}
    	return appList;
    }
    
    /**
     * @Title: selectAllNotifyPromotions
     * @Description: TODO get all notify promotions
     * @return
     * @return: List<NotificationPromotionInfo>
     */
    public List<NotificationPromotionInfo> selectAllNotifyPromotions() {
    	String sql = "select * from " + NOTIFICATION_PROMOTION_TABLE_NAME;
    	Cursor cursor = getCursor(sql, null);
    	List<NotificationPromotionInfo> notifyPromotionList = new ArrayList<NotificationPromotionInfo>();
    	while (cursor.moveToNext()) {
    		String id = cursor.getString(cursor.getColumnIndex(NOTIFY_PROMOTION_ID));
    		String type = cursor.getString(cursor.getColumnIndex(NOTIFY_PROMOTION_TYPE));
    		String date = cursor.getString(cursor.getColumnIndex(NOTIFY_PROMOTION_DATE));
    		String imageId = cursor.getString(cursor.getColumnIndex(NOTIFY_PROMOTION_IMAGE));
    		String title = cursor.getString(cursor.getColumnIndex(NOTIFY_PROMOTION_TITLE));
    		String description = cursor.getString(cursor.getColumnIndex(NOTIFY_PROMOTION_DESCRIPTION));
    		
    		NotificationPromotionInfo notifyPromotion = new NotificationPromotionInfo();
    		notifyPromotion.setId(Integer.parseInt(id));
    		notifyPromotion.setType(Integer.parseInt(type));
    		notifyPromotion.setDate(date);
    		notifyPromotion.setImageId(imageId);
    		notifyPromotion.setDescription(description);
    		notifyPromotion.setTitle(title);
    		notifyPromotionList.add(notifyPromotion);
    	}
    	return notifyPromotionList;
    }
    
    public List<App> selectAllAdPromotion() {
    	List<App> appList = null;
    	Cursor cursor = null;
		try {
			String sql = "select app.*, ad.* from ad_promotion_table ad " +
					"left outer join app_table app " +
					"on app.id = ad.app_id";
			cursor = getCursor(sql, null);
			appList = new ArrayList<App>();
			while (cursor.moveToNext()) {
				String id = cursor.getString(cursor.getColumnIndex(APP_ID));
				String name = cursor.getString(cursor.getColumnIndex(APP_NAME));
				String version = cursor.getString(cursor.getColumnIndex(APP_VERSION));
				String icon = cursor.getString(cursor.getColumnIndex(APP_ICON));
				String size = cursor.getString(cursor.getColumnIndex(APP_SIZE));
				String downloadId = cursor.getString(cursor.getColumnIndex(APP_DOWNLOAD_ID));
				
				int versionCode = cursor.getInt(cursor.getColumnIndex(APP_VERSION_CODE));
				long date = cursor.getLong(cursor.getColumnIndex(APP_DATE));
				String rating = cursor.getString(cursor.getColumnIndex(APP_RATING));
				String vendor = cursor.getString(cursor.getColumnIndex(APP_VENDOR));
				String img = cursor.getString(cursor.getColumnIndex(AD_IMG));
				String url = cursor.getString(cursor.getColumnIndex(AD_URL));
				int type = cursor.getInt(cursor.getColumnIndex(AD_TYPE));
				String packageName = cursor.getString(cursor.getColumnIndex(APP_PACKAGE_NAME));
				
				App app = new App(id, name, version, size, icon, downloadId);
				app.setType(type);
				app.setVersionCode(versionCode);
				app.setDate(date);
				app.setRating(rating);
				app.setVendor(vendor);
				app.setImg(img);
				app.setUrl(url);
				app.setPackageName(packageName);
				appList.add(app);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
//			closeDB();
		}
    	return appList;
    }
    
    public Cursor selectCategoryById(String categoryId) { 
//    	SQLiteDatabase db = this.getReadableDatabase();
//    	Cursor cursor = db.query(CATEGORY_TABLE_NAME, null, CATEGORY_ID + "=?", new String[] {categoryId}, null, null, null);
    	String sql = "select * from " + CATEGORY_TABLE_NAME + " where " + CATEGORY_ID + " = " + categoryId;
    	Cursor cursor = getCursor(sql, null);
    	return cursor; 
    }
    
    public Cursor selectAppById(String appId) { 
    	String sql = "select * from " + APP_TABLE_NAME + " where " + APP_ID + " = " + appId;
    	Cursor cursor = getCursor(sql, null);
    	return cursor; 
    }
    
    public Cursor selectNotifyPromotionById(int promotionId) { 
    	String sql = "select * from " + NOTIFICATION_PROMOTION_TABLE_NAME + " where " + NOTIFY_PROMOTION_ID + " = " + promotionId;
    	Cursor cursor = getCursor(sql, null);
    	return cursor; 
    }
  
    /**
    * @Title: insertCategories
    * @Description: Insert categories
    * @param @param categoryList
    * @param @return
    * @return long 
    * @throws
    */
    public void insertCategories(List<Category> categoryList) {
        
        ArrayList<SQLEntity> sqlList = new ArrayList<SQLEntity>();
        Cursor cursor = null;
		try {
			String sql = "INSERT INTO "+ CATEGORY_TABLE_NAME +"(" +
					CATEGORY_ID + ", " +
					CATEGORY_NAME + ", " +
					CATEGORY_HASH + ", " +
					CATEGORY_ICON + ", " +
					CATEGORY_PRIORITY + ") VALUES(?, ?, ?, ?, ?)";
			
			if (categoryList != null && categoryList.size() > 0) {
				for (int i = 0; i < categoryList.size(); i++) {
					Category category = categoryList.get(i);
					cursor = selectCategoryById(category.getId());
					if (cursor.getCount() > 0) {
						cursor.moveToFirst();
						if (!cursor.getString(cursor.getColumnIndex(CATEGORY_HASH)).equals(category.getHash())) {
							updateCategoryById(category.getId(), category);
						}
						continue;
					}
					cursor.close();
					String[] params = new String[] { category.getId(), category.getName(),
							category.getHash(), category.getIcon(), category.getPriority() };
					sqlList.add(new SQLEntity(sql, params));
				}
				excuteSql(sqlList);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			closeDB();
		}
    }
    
    /**
    * @Title: insertApps
    * @Description: TODO
    * @param @param appList
    * @return void 
    * @throws
    */
    public void insertApps(List<App> appList, String categoryId) {
        
        ArrayList<SQLEntity> sqlList = new ArrayList<SQLEntity>();
        Cursor cursor = null;
        Set<String> appSet = null;
		try {
			
			String sql = "INSERT INTO "+ APP_TABLE_NAME +"(" +
					APP_ID + ", " +
					APP_NAME + ", " +
					APP_SIZE + ", " +
					APP_ICON + ", " +
					APP_VERSION + ", " +
					APP_CATEGORY_ID + ", " +
					APP_PACKAGE_NAME + ", " +
					APP_VERSION_CODE + ", " +
					APP_RATING + ", " +
					APP_VENDOR + ", " +
					APP_DATE + ", " +
					APP_DOWNLOAD_ID + ") VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			
			if (appList != null && appList.size() > 0) {
				appSet = new HashSet<String>();
				
				for (int i = 0; i < appList.size(); i++) {
					App app = appList.get(i);
					cursor = selectAppById(app.getId());
					if (cursor.getCount() > 0) {
						cursor.moveToFirst();
						if (!cursor.getString(cursor.getColumnIndex(APP_VERSION)).equals(app.getVersion())) {
							updateAppById(app.getId(), app, categoryId);
						}
						continue;
					}
					cursor.close();
					if (appSet.contains(app.getPackageName())) {
						continue;
					} else {
						
						String[] params = new String[] { app.getId(), app.getName(),
								app.getSize(), app.getIcon(), app.getVersion(), categoryId, app.getPackageName(),
								String.valueOf(app.getVersionCode()), app.getRating(), app.getVendor(), String.valueOf(app.getDate()),
								app.getDownloadId() };
						sqlList.add(new SQLEntity(sql, params));
						appSet.add(app.getPackageName());
					}
				}
				excuteSql(sqlList);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				
				cursor.close();
			}
			closeDB();
		}
    }
    
    /**
     * @Title: insertNotificatinoPromotion
     * @Description: TODO inser promotion data
     * @param promotionList
     * @return: void
     */
    public void insertNotificatinoPromotion(List<NotificationPromotionInfo> promotionList) {
    	
    	ArrayList<SQLEntity> sqlList = new ArrayList<SQLEntity>();
    	Cursor cursor = null;
    	Set<Integer> appSet = null;
    	try {
    		
    		String sql = "INSERT INTO "+ NOTIFICATION_PROMOTION_TABLE_NAME +"(" +
    				NOTIFY_PROMOTION_ID + ", " +
    				NOTIFY_PROMOTION_TYPE + ", " +
    				NOTIFY_PROMOTION_TITLE + ", " +
    				NOTIFY_PROMOTION_DATE + ", " +
    				NOTIFY_PROMOTION_IMAGE + ", " +
    				NOTIFY_PROMOTION_DESCRIPTION  + ") VALUES(?, ?, ?, ?, ?, ?)";
    		
    		if (promotionList != null && promotionList.size() > 0) {
    			appSet = new HashSet<Integer>();
    			
    			for (int i = 0; i < promotionList.size(); i++) {
    				NotificationPromotionInfo promotionInfo = promotionList.get(i);
    				cursor = selectNotifyPromotionById(promotionInfo.getId());
    				if (cursor.getCount() > 0) {
    					continue;
    				}
    				cursor.close();
    				if (appSet.contains(promotionInfo.getId())) {
    					continue;
    				} else {
    					
    					String[] params = new String[] { String.valueOf(promotionInfo.getId()), 
    							String.valueOf(promotionInfo.getType()),
    							promotionInfo.getTitle(), promotionInfo.getDate(), 
    							promotionInfo.getImageId(), promotionInfo.getDescription() };
    					sqlList.add(new SQLEntity(sql, params));
    					appSet.add(promotionInfo.getId());
    				}
    			}
    			excuteSql(sqlList);
    		}
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	} finally {
    		if (cursor != null) {
    			
    			cursor.close();
    		}
    		closeDB();
    	}
    }
    
    /**
     * @Title: insertAdPromotions
     * @Description: insert AD promotion to table
     * @param appList
     * @return: void
     */
    public void insertAdPromotions(List<App> appList) {
    	
    	ArrayList<SQLEntity> sqlList = new ArrayList<SQLEntity>();
    	try {
    		//clear table first
    		clearAdPromotionTableData();
    		String sql = "INSERT INTO "+ AD_PROMOTION_TABLE_NAME +"(" +
    				AD_APP_ID + ", " +
    				/*APP_NAME + ", " +
    				APP_SIZE + ", " +
    				APP_ICON + ", " +
    				APP_VERSION + ", " +
    				APP_PACKAGE_NAME + ", " +
    				APP_VERSION_CODE + ", " +
    				APP_RATING + ", " +
    				APP_VENDOR + ", " +
    				APP_DATE + ", " +*/
    				AD_IMG + ", " +
    				AD_TYPE + ", " +
    				AD_URL + ") VALUES(?, ?, ?, ?)";
//    				APP_DOWNLOAD_ID + ") VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    		
    		if (appList != null && appList.size() > 0) {
    			for (int i = 0; i < appList.size(); i++) {
    				App app = appList.get(i);
    				String[] params = new String[] { app.getId(),
    						app.getImg(), String.valueOf(app.getType()), app.getUrl() };
    				sqlList.add(new SQLEntity(sql, params));
    				
    			}
    			excuteSql(sqlList);
    		}
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	} finally {
    		closeDB();
    	}
    }
    
    public void clearAppTableData() {
    	String sql = "delete from " + APP_TABLE_NAME;
    	
    	try {
			excuteWriteAbleSql(sql);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		closeDB();
    }
    
    public void clearAdPromotionTableData() {
    	String sql = "delete from " + AD_PROMOTION_TABLE_NAME;
    	
    	try {
    		excuteWriteAbleSql(sql);
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    	
    	closeDB();
    }
  
    // delete operations by id
    public void deleteCategoryById(int id) { 
        
        String sql = "delete from " + CATEGORY_TABLE_NAME + " where " + CATEGORY_ID + "='" + id + "'";
		try {
			excuteSql(sql);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		closeDB();
    } 
    
    public void cleanCacheDBTables() {
//    	String appTableSql = "delete from app_table";
    	String categorySql = "delete from category_table";
    	
    	try {
//    		excuteSql(appTableSql);
    		excuteSql(categorySql);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	closeDB();
    }
    
    /**
     * delete item by name
     * @param categoryName
     */
    public void deleteCategoryByName(String categoryName) { 
    	String sql = "delete from " + CATEGORY_TABLE_NAME + " where " + CATEGORY_TABLE_NAME + "='" + categoryName + "'";
		try {
			excuteSql(sql);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		closeDB();
    }
  
    // update oerations 
    public void updateCategoryById(String id, Category category) { 
        
        String updateSql = "update " + CATEGORY_TABLE_NAME + " set " +
        	CATEGORY_NAME + " = '" + category.getName() + "', " +
        	CATEGORY_HASH + " = '" + category.getHash() + "', " +
        	CATEGORY_ICON + " = '" + category.getIcon() + "', " +
        	CATEGORY_PRIORITY + " = '" + category.getPriority() + "' " +
        	"where " + CATEGORY_ID + " = " + id;
        excuteSql(updateSql);
    }
    
    // update oerations 
    public void updateAppById(String id, App app, String categoryId) { 
    	
    	String updateSql = "update " + APP_TABLE_NAME + " set " +
    		APP_NAME + " = '" + app.getName() + "', " +
	    	APP_SIZE + " = '" + app.getSize() + "', " +
	    	APP_ICON + " = '" + app.getIcon() + "', " +
	    	APP_VERSION + " = '" + app.getVersion() + "', " +
	    	APP_CATEGORY_ID + " = '" + categoryId + "', " +
	    	APP_DOWNLOAD_ID + " = '" + app.getDownloadId() + "'," +
	    	APP_PACKAGE_NAME + " = '" + app.getPackageName() + "'," +
	    	APP_VENDOR + " = '" + app.getVendor() + "'," +
	    	APP_RATING + " = " + app.getRating() + "," +
	    	APP_DATE + " = " + app.getDate() + "," +
	    	APP_VERSION_CODE + " = " + app.getVersionCode() + 
	    	" where " + APP_ID + " = " + id;
    	excuteSql(updateSql);
    }
    
    public List<App> getUpdatedSoft(List<AppInfo> appInfoList) {
    	List<App> appList = new ArrayList<App>();
    	
    	String sql = "select * from " + APP_TABLE_NAME;
    	Cursor cursor = getCursor(sql, null);
    	Map<String, App> map = new HashMap<String, App>();
    	
    	while (cursor.moveToNext()) {
    		String id = cursor.getString(cursor.getColumnIndex(APP_ID));
    		String name = cursor.getString(cursor.getColumnIndex(APP_NAME));
    		String version = cursor.getString(cursor.getColumnIndex(APP_VERSION));
    		String icon = cursor.getString(cursor.getColumnIndex(APP_ICON));
    		String size = cursor.getString(cursor.getColumnIndex(APP_SIZE));
    		String downloadId = cursor.getString(cursor.getColumnIndex(APP_DOWNLOAD_ID));
    		
    		App app = new App(id, name, version, size, icon, downloadId);
    		map.put(name, app);
    	}
    	
    	if (appInfoList != null && appInfoList.size() > 0) {
    		for (int i = 0; i < appInfoList.size(); i++) {
    			AppInfo localAppInfo = appInfoList.get(i);
    			App cloudApp = map.get(localAppInfo.getName());
    			if (cloudApp != null) {
    				if (localAppInfo.getVersionName().compareTo(cloudApp.getVersion())  < 0) {
    					appList.add(cloudApp);
    				}
    			} 
    			continue;
    		}
    	}
    	
    	return appList;
    }
    
    class CacheHelper extends SQLiteOpenHelper {
    	Context ctx;
		public CacheHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
			ctx = context;
		}

		@Override
		
		public void onCreate(SQLiteDatabase db) {
			createTables(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if (oldVersion == 1 && newVersion == 2) {
				// Drop tables  
		        db.execSQL("DROP TABLE IF EXISTS " + CATEGORY_TABLE_NAME);
		        db.execSQL("DROP TABLE IF EXISTS " + APP_TABLE_NAME);
		        db.execSQL("DROP TABLE IF EXISTS " + AD_PROMOTION_TABLE_NAME);
		        db.execSQL("DROP TABLE IF EXISTS " + NOTIFICATION_PROMOTION_TABLE_NAME);
		        // Create tables  
		        onCreate(db); 
			}
		}
		
		private void createTables(SQLiteDatabase db) {
			String createCategorySql = "CREATE TABLE IF NOT EXISTS " + CATEGORY_TABLE_NAME + " (" + CATEGORY_ID 
		        + " INTEGER primary key, " 
		        + CATEGORY_HASH + " varchar, " 
		        + CATEGORY_ICON + " varchar, " 
		        + CATEGORY_PRIORITY + " varchar, " 
		        + CATEGORY_NAME + " varchar);";	
	        
	        String createAppSql = "CREATE TABLE IF NOT EXISTS " + APP_TABLE_NAME + " (" + APP_ID 
		        + " INTEGER primary key, " 
		        + APP_NAME + " varchar, " 
		        + APP_VERSION + " varchar, " 
		        + APP_SIZE + " varchar, " 
		        + APP_ICON + " varchar, " 
		        + APP_PACKAGE_NAME + " varchar, " 
		        + APP_VERSION_CODE + " INTEGER, " 
		        + APP_CATEGORY_ID + " varchar, " 
		        + APP_RATING + " INTEGER, " 
		        + APP_VENDOR + " varchar, " 
		        + APP_DATE + " double, " 
		        + APP_DOWNLOAD_ID + " varchar);";
	        
	        String createAdPromotionSql = "CREATE TABLE IF NOT EXISTS " + AD_PROMOTION_TABLE_NAME + " (" + AD_APP_ID 
	        		+ " INTEGER, " 
	        		/*+ APP_NAME + " varchar, " 
	        		+ APP_VERSION + " varchar, " 
	        		+ APP_SIZE + " varchar, " 
	        		+ APP_ICON + " varchar, " 
	        		+ APP_PACKAGE_NAME + " varchar, " 
	        		+ APP_VERSION_CODE + " INTEGER, " 
	        		+ APP_RATING + " INTEGER, " 
	    	        + APP_VENDOR + " varchar, " 
	    	        + APP_DATE + " double, "*/
	    	        + AD_TYPE + " INTEGER, "
	    	        + AD_IMG + " varchar, "
	    	        + AD_URL + " varchar);";
/*	        + AD_URL + " varchar, "
	        + APP_DOWNLOAD_ID + " varchar);";
*/	        
	        String createNotificatinoPromotionSql = "CREATE TABLE IF NOT EXISTS " + NOTIFICATION_PROMOTION_TABLE_NAME + " ("
	        		+ NOTIFY_PROMOTION_ID + " INTEGER, "
	        		+ NOTIFY_PROMOTION_TYPE + " INTEGER, "
	        		+ NOTIFY_PROMOTION_TITLE + " VARCHAR, "
	        		+ NOTIFY_PROMOTION_DATE + " VARCHAR, "
	        		+ NOTIFY_PROMOTION_IMAGE + " VARCHAR, "
	        		+ NOTIFY_PROMOTION_DESCRIPTION + " VARCHAR"
	        		+ ");";
	        
	        db.execSQL(createCategorySql);
			db.execSQL(createAppSql);
			db.execSQL(createAdPromotionSql);
			db.execSQL(createNotificatinoPromotionSql);
			setmDb(db);
		}
	}
}
