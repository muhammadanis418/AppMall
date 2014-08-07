package cn.koolcloud.ipos.appstore.download.multithread;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DownloadRecord {

	private DownloadOpenHelper openHelper;
	private static DownloadRecord downloadDB = null;

	private DownloadRecord(Context context) {
		openHelper = new DownloadOpenHelper(context);
	}
	
	public static DownloadRecord getInstance(Context context) {
		if(downloadDB == null)
			downloadDB = new DownloadRecord(context);
		return downloadDB;
	}
	
	public static DownloadRecord getInstance() {
		return downloadDB;
	}
	
	/**
	 * 获取每条线程已经下载的文件长度
	 * @param path
	 * @return
	 */
	@SuppressLint("UseSparseArrays")
	public Map<Integer, Integer> getData(String path){
		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select threadid, downlength from filedownlog where downpath=?", new String[]{path});
		Map<Integer, Integer> data = new HashMap<Integer, Integer>();
		
		while(cursor.moveToNext()){
			data.put(cursor.getInt(0), cursor.getInt(1));
		}
		
		cursor.close();
		return data;
	}
	
	/**
	 * 保存每条线程已经下载的文件长度
	 * @param path
	 * @param map
	 */
	public void insert(String path,  Map<Integer, Integer> map){//int threadid, int position
		SQLiteDatabase db = openHelper.getWritableDatabase();
		db.beginTransaction();
		
		try{
			for(Map.Entry<Integer, Integer> entry : map.entrySet()){
				db.execSQL("insert into filedownlog(downpath, threadid, downlength) values(?,?,?)",
						new Object[]{path, entry.getKey(), entry.getValue()});
			}
			
			db.setTransactionSuccessful();
		}finally{
			db.endTransaction();
		}
		
	}
	
	/**
	 * 实时更新每条线程已经下载的文件长度
	 * @param path
	 * @param map
	 */
	public void update(String path, Map<Integer, Integer> map){
		SQLiteDatabase db = openHelper.getWritableDatabase();
		db.beginTransaction();
		
		try{
			for(Map.Entry<Integer, Integer> entry : map.entrySet()){
				db.execSQL("update filedownlog set downlength=? where downpath=? and threadid=?",
						new Object[]{entry.getValue(), path, entry.getKey()});
			}
			
			db.setTransactionSuccessful();
		}finally{
			db.endTransaction();
		}
		
	}
	
	/**
	 * 当文件下载完成后，删除对应的下载记录
	 * @param path
	 */
	public void delete(String path){
		SQLiteDatabase db = openHelper.getWritableDatabase();
		db.execSQL("delete from filedownlog where downpath=?", new Object[]{path});
	}
	
	public void clear(){
		SQLiteDatabase db = openHelper.getWritableDatabase();
		db.execSQL("delete from filedownlog");
	}
	
	public void close(){
		openHelper.close();
	}
}
