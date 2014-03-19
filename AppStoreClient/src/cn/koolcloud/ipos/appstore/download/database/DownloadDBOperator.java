package cn.koolcloud.ipos.appstore.download.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import cn.koolcloud.ipos.appstore.download.entity.DownloadBean;

/**
 * <p>Title: DownloadDBOperator.java </p>
 * <p>Description: Class for accessing sqlite by download tasks </p>
 * <p> this class include all kinds of operating methods to call database, make sure close the db at the end
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: All In Pay</p>
 * @author 		Teddy
 * @date 		2013-12-5
 * @version 	
 */
public class DownloadDBOperator {
    private final static String TAG = "DownloadDBOperator";
    private static DownloadDBOperator instance = null;
    private DownloadDBHelper dbHelper;

    public static DownloadDBOperator getInstance(Context context) {
        if (instance == null) {
            instance = new DownloadDBOperator(context);
        }
        return instance;
    }

    public DownloadDBOperator(Context context) {
        dbHelper = new DownloadDBHelper(context);
    }

    /**
     * close database
     */
    public void closeDb() {
        dbHelper.close();
    }

    // ========download table operations=========

    /**
     * 1.insert one row information
     * 
     * @param beans
     */
    public void addDownloadTask(ArrayList<DownloadBean> beans) {
        try {
			SQLiteDatabase database = dbHelper.getWritableDatabase();
			for (DownloadBean bean : beans) {
			    String sql = "insert into download_info(url, file_name, total_size, start_pos, end_pos, compelete_size,"
			            + " thread_id, file_version, file_version_code, package_name, icon_url, soft_id, download_id, version_code) values "
			            + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			    Object[] bindArgs = { bean.url, bean.fileName, bean.fileSize,
			            bean.startPosition, bean.endPosition, bean.currentPosition,
			            bean.threadId, bean.fileVersion, bean.fileVersionCode,
			            bean.packageName, bean.iconUrl, bean.fileId, bean.downloadId, bean.versionCode };
			    database.execSQL(sql, bindArgs);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }

    /**
     * 2.delete one row
     * 
     * @param url
     */
    public boolean deleteDownloadTaskByUrl(String url, String downloadId) {
        boolean isSuccess = false;
		try {
			SQLiteDatabase database = dbHelper.getReadableDatabase();
			isSuccess = database.delete("download_info", "url=? and download_id=?", new String[] { url, downloadId }) > 0;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return isSuccess;
    }

    /**
     * 3.search if it is exist
     * 
     * @param urlstr
     * @return
     */
    public boolean isHasDownloadTaskByUrl(String urlstr, String downloadId) {
        int count = 0;
        Cursor cursor = null;
        try {
            SQLiteDatabase database = dbHelper.getReadableDatabase();
            String sql = "select count(*) from download_info where url=? and download_id=?";
            cursor = database.rawQuery(sql, new String[] { urlstr, downloadId });
            cursor.moveToFirst();
            count = cursor.getInt(0);
        } catch (Exception e) {
            Log.e(TAG, "isHasTask Error:" + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return count != 0;
    }

    /**
     * 4.searching by url
     * 
     * @param urlstr
     * @return
     */
    public ArrayList<DownloadBean> getDownloadTaskByUrl(String urlstr, String downloadId) {
        ArrayList<DownloadBean> list = new ArrayList<DownloadBean>();
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String sql = "select url, file_name, total_size, " +
        		"start_pos, end_pos, compelete_size, " +
        		"thread_id, file_version, file_version_code, " +
        		"package_name, icon_url, soft_id, download_id, version_code " +
        		"from download_info " +
        		"where url=? and download_id=?";
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(sql, new String[] { urlstr, downloadId });
            while (cursor.moveToNext()) {
                DownloadBean info = new DownloadBean();
                info.url = cursor.getString(0);
                info.fileName = cursor.getString(1);
                info.fileSize = cursor.getLong(2);
                info.startPosition = cursor.getLong(3);
                info.endPosition = cursor.getLong(4);
                info.currentPosition = cursor.getLong(5);
                info.threadId = cursor.getInt(6);
                info.fileVersion = cursor.getString(7);
                info.fileVersionCode = cursor.getInt(8);
                info.packageName = cursor.getString(9);
                info.iconUrl = cursor.getString(10);
                info.fileId = cursor.getString(11);
                info.downloadId = cursor.getString(12);
                info.versionCode = cursor.getInt(13);
                list.add(info);
            }
        } catch (Exception e) {
            Log.e(TAG, "getDownloadTaskByUrl Error:" + e);
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    /**
     * 5.search all results <url,bean>
     * 
     * @return
     */
    public HashMap<String, ArrayList<DownloadBean>> getAllTasks() {
        HashMap<String, ArrayList<DownloadBean>> map = new HashMap<String, ArrayList<DownloadBean>>();
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String sql = "select url, file_name, " +
        		"total_size, start_pos, end_pos, " +
        		"compelete_size, thread_id, file_version, " +
        		"file_version_code, package_name, " +
        		"icon_url, soft_id, download_id, version_code " +
        		"from download_info";
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                DownloadBean info = new DownloadBean();
                info.url = cursor.getString(0);
                info.fileName = cursor.getString(1);
                info.fileSize = cursor.getLong(2);
                info.startPosition = cursor.getLong(3);
                info.endPosition = cursor.getLong(4);
                info.currentPosition = cursor.getLong(5);
                info.threadId = cursor.getInt(6);
                info.fileVersion = cursor.getString(7);
                info.fileVersionCode = cursor.getInt(8);
                info.packageName = cursor.getString(9);
                info.iconUrl = cursor.getString(10);
                info.fileId = cursor.getString(11);
                info.downloadId = cursor.getString(12);
                info.versionCode = cursor.getInt(13);

                // update the list, contains by Map
                String mapKey = info.url + "_" + info.downloadId;
                if (map.containsKey(mapKey)) {
                    map.get(mapKey).add(info);
                } else {// not exist in the map, create a new one
                    ArrayList<DownloadBean> list = new ArrayList<DownloadBean>();
                    list.add(info);
                    map.put(mapKey, list);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getAllTasks Error:" + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return map;
    }

    /**
     * 6.modify the progress for the thread
     * 
     * @param threadId
     * @param compeleteSize
     * @param urlStr
     */
    public void updateTaskCompleteSize(int threadId, long compeleteSize,
            String urlStr, String downloadId) {
        try {
			SQLiteDatabase database = dbHelper.getReadableDatabase();
			String sql = "update download_info set compelete_size=? " +
					"where thread_id=? and url=? and download_id=?";
			Object[] bindArgs = { compeleteSize, threadId, urlStr, downloadId };
			database.execSQL(sql, bindArgs);
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }

    /**
     * 6.5. modify the size of saved fiel
     * create a new record in database with file size(0), when download task added to list,
     * update the record after got file size from server, begin to start when file size > 0 
     * @param threadId
     * @param compeleteSize
     * @param urlstr
     */
    public void updateTaskCompleteSize(ArrayList<DownloadBean> beans,
            String urlstr) {
        try {
			SQLiteDatabase database = dbHelper.getReadableDatabase();
			for (DownloadBean bean : beans) {
			    String sql = "update download_info set total_size=?, start_pos=?, end_pos=? " +
			    		"where url=? and thread_id=? and download_id=?";
			    Object[] bindArgs = { bean.fileSize, bean.startPosition,
			            bean.endPosition, bean.url, bean.threadId, bean.downloadId };
			    database.execSQL(sql, bindArgs);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }

    /**
     * 7.get download progress 
     * return -1 when it is not exist on searching
     * return -1 when get the file size = 0
     * @param urlStr
     * @return
     */
    public int getTaskProgressByUrl(String urlStr, String downloadId) {
        long tmpCompSize = 0;
        long fileSize = -1;
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String sql = "select compelete_size,total_size from download_info where url=? and download_id=?";
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(sql, new String[] { urlStr, downloadId });
            while (cursor.moveToNext()) {
                tmpCompSize += cursor.getLong(0);

                long tmpFileSize = cursor.getLong(1);
                if (tmpFileSize > fileSize) {
                    fileSize = tmpFileSize;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getTaskProgressByUrl Error:" + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (fileSize < 1) {
            tmpCompSize = -1;// Error
        } else {
            tmpCompSize = tmpCompSize * 100 / fileSize;
        }
        return (int) tmpCompSize;
    }

    // ========Pause table operations=========
    /**
     * 1.insert one record
     * 
     * @param url
     * @param packageName
     * @param softId
     */
    public void addPauseFile(String url, String packageName, String softId, String downloadId) {
        try {
			SQLiteDatabase database = dbHelper.getWritableDatabase();
			String sql = "insert into pause_download_info(url, package_name, soft_id, download_id) " +
					"values (?,?,?,?)";
			Object[] bindArgs = { url, packageName, softId, downloadId };
			database.execSQL(sql, bindArgs);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /**
     * @Title: clearDownloadDataBaseTables
     * @Description: clear download database
     * @return: void
     */
    public void cleanDownloadDataBaseTables() {
    	try {
			SQLiteDatabase database = dbHelper.getWritableDatabase();
			String downloadInfoSql = "delete from download_info";
			String downloadPauseInfoSql = "delete from pause_download_info";
			String downloadEndSql = "delete from end_download_info";
			
			database.execSQL(downloadEndSql);
			database.execSQL(downloadInfoSql);
			database.execSQL(downloadPauseInfoSql);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    }

    /**
     * 2.delete
     * @param url
     */
    public boolean removePauseFileByUrl(String url, String downloadId) {
        boolean isSuccess = false;
		try {
			SQLiteDatabase database = dbHelper.getReadableDatabase();
			isSuccess = database.delete("pause_download_info", "url=? and download_id=?",
			        new String[] { url, downloadId }) > 0;
		} catch (Exception e) {
			e.printStackTrace();
		}
                
        return isSuccess;
    }

    /**
     * 3. search if it is exist
     * @param url
     * @return
     */
    public boolean isPauseFile(String url, String downloadId) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String sql = "select soft_id from pause_download_info where url=? and download_id=?";
        Cursor cursor = null;
        boolean flag = false;
        try {
            cursor = database.rawQuery(sql, new String[] { url, downloadId });
            flag = cursor.moveToNext();
        } catch (Exception e) {
            Log.e(TAG, "removePauseFileByUrl Error:" + e);
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return flag;
    }

    // ========Complete table operations=========
    /**
     * 1.insert records
     * @param info
     */
    public void addCompleteTask(DownloadBean info) {
        try {
			SQLiteDatabase database = dbHelper.getWritableDatabase();
			String sql = "insert into end_download_info(url, file_name," +
					" file_size, done_time, icon_url, file_version," +
					" file_version_code, package_name," +
					" file_path, soft_id, download_id, version_code) values (?,?,?,?,?,?,?,?,?,?,?,?)";
			Object[] bindArgs = { info.url, info.fileName, info.fileSize,
			        info.doneTime, info.iconUrl, info.fileVersion,
			        info.fileVersionCode, info.packageName, info.savePath,
			        info.fileId, info.downloadId, info.versionCode };
			database.execSQL(sql, bindArgs);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /**
     * 2.delete
     * @param url
     */
    public boolean deleteCompleteTaskByUrl(String url, String downloadId) {
        boolean isSuccess = false;
		try {
			SQLiteDatabase database = dbHelper.getReadableDatabase();
			isSuccess = database.delete("end_download_info", "url=? and download_id=?",
			        new String[] { url, downloadId }) > 0;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return isSuccess;
    }

    /**
     * 3.search if it is exist
     * @param url
     * @return
     */
    public boolean isTaskDone(String url, String downloadId) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String sql = "select soft_id from end_download_info where url=? and download_id=?";
        boolean flag = false;
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(sql, new String[] { url, downloadId });
            flag = cursor.moveToNext();
        } catch (Exception e) {
            Log.e(TAG, "isTaskDone Error:" + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return flag;
    }

    /**
     * 4.search by url
     * 
     * @param urlstr
     * @return
     */
    public DownloadBean getCompleteTaskInfoByUrl(String urlstr, String downloadId) {
        DownloadBean info = new DownloadBean();
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String sql = "select url, file_name, file_size, " +
        		"done_time, icon_url, file_version, " +
        		"file_version_code, package_name, " +
        		"file_path, soft_id, download_id, version_code " +
        		"from end_download_info " +
        		"where url=? and download_id=?";
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(sql, new String[] { urlstr, downloadId });
            while (cursor.moveToNext()) {
                info.url = cursor.getString(0);
                info.fileName = cursor.getString(1);
                info.fileSize = cursor.getLong(2);
                info.doneTime = cursor.getLong(3);
                info.iconUrl = cursor.getString(4);
                info.fileVersion = cursor.getString(5);
                info.fileVersionCode = cursor.getInt(6);
                info.packageName = cursor.getString(7);
                info.savePath = cursor.getString(8);
                info.fileId = cursor.getString(9);
                info.downloadId = cursor.getString(10);
                info.versionCode = cursor.getInt(11);
            }
        } catch (Exception e) {
            Log.e(TAG, "getCompleteTaskInfoByUrl Error:" + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return info;
    }

    /**
     * 5.search all
     * 
     * @return
     */
    public List<DownloadBean> getAllCompletedTask() {
        List<DownloadBean> list = new ArrayList<DownloadBean>();
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String sql = "select url, file_name, file_size, " +
        		"done_time, icon_url, file_version, " +
        		"file_version_code, package_name, " +
        		"file_path, soft_id, download_id, version_code " +
        		"from end_download_info order by done_time asc";
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                DownloadBean info = new DownloadBean();
                info.url = cursor.getString(0);
                info.fileName = cursor.getString(1);
                info.fileSize = cursor.getLong(2);
                info.doneTime = cursor.getLong(3);
                info.iconUrl = cursor.getString(4);
                info.fileVersion = cursor.getString(5);
                info.fileVersionCode = cursor.getInt(6);
                info.packageName = cursor.getString(7);
                info.savePath = cursor.getString(8);
                info.fileId = cursor.getString(9);
                info.downloadId = cursor.getString(10);
                info.versionCode = cursor.getInt(11);
                list.add(info);
            }
        } catch (Exception e) {
            Log.e(TAG, "getAllComplieteTask Error:" + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }
    
    /**
     * @Title: getAppDownloadPathByPackage
     * @Description: get path after download the app by package
     * @param packageName
     * @return
     * @return: String
     */
    public String getAppDownloadPathByPackage(String packageName) {
    	SQLiteDatabase database = dbHelper.getReadableDatabase();
    	String path = "";
    	String sql = "select file_path, file_name, " +
    			"done_time, file_version_code, " +
    			"package_name, version_code " +
    			"from end_download_info " +
    			"where package_name = ? order by done_time desc limit 1";
    	Cursor cursor = null;
    	try {
    		cursor = database.rawQuery(sql, new String[] { packageName });
    		while (cursor.moveToNext()) {
    			path = cursor.getString(0);
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    		Log.e(TAG, "getAppDownloadPathByPackage Error:" + e);
    	} finally {
    		if (cursor != null) {
    			cursor.close();
    		}
    	}
    	return path;
    }
}
