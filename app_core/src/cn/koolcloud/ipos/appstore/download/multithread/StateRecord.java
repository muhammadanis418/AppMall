package cn.koolcloud.ipos.appstore.download.multithread;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class StateRecord {

	private StateOpenHelper openHelper;
	private static StateRecord stateDB = null;

	private StateRecord(Context context) {
		openHelper = new StateOpenHelper(context);
	}
	
	public static StateRecord getInstance(Context context){
		if(stateDB == null)
			stateDB = new StateRecord(context);
		return stateDB;
	}

	/**
	 * 获取每个apk的状态、百分比
	 * @param packageName
	 * @return
	 */
	@SuppressLint("UseSparseArrays")
	public HashMap<Integer, Integer> getData(String packageName){
		HashMap<Integer, Integer> data;
		SQLiteDatabase db = openHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select state, percent from filestatelog where packageName=?", new String[]{packageName});
		data = new HashMap<Integer, Integer>();
			
		while(cursor.moveToNext()){
			data.put(cursor.getInt(0), cursor.getInt(1));
		}
			
		cursor.close();
		return data;
	}

	/**
	 * 保存每个packageName的状态、百分比
	 * @param packageName
	 * @param state
	 * @param percent
	 */
	public void insert(String packageName, int state, int percent){//int state, int percent

		SQLiteDatabase db = openHelper.getWritableDatabase();
		db.execSQL("insert into filestatelog(packageName, state, percent) values(?,?,?)",
						new Object[]{packageName, state, percent});

	}

	/**
	 * 实时更新每个apk的状态
	 * @param packageName
	 * @param state
	 */
	public void updateState(String packageName, int state){

		SQLiteDatabase db = openHelper.getWritableDatabase();
		db.execSQL("update filestatelog set state=? where packageName=?",
						new Object[]{state, packageName});
	}
	
	/**
	 * 实时更新每个apk的百分比
	 * @param packageName
	 * @param percent
	 */
	public void updatePercent(String packageName, int percent){

		SQLiteDatabase db = openHelper.getWritableDatabase();
		db.execSQL("update filestatelog set percent=? where packageName=?",
						new Object[]{percent, packageName});
	}
	
	/**
	 * 当apk安装完成后或者取消下载后，删除对应的下载记录
	 * @param packageName
	 */
	public void delete(String packageName){
		SQLiteDatabase db = openHelper.getWritableDatabase();
		db.execSQL("delete from filestatelog where packageName=?", new Object[]{packageName});
	}
	
	/**
	 * 将所有IS_DOWNLOADING状态的记录置为IS_PAUSEING
	 */
	public void initState(){
		SQLiteDatabase db = openHelper.getWritableDatabase();
		db.execSQL("update filestatelog set state=? where state=?",
				new Object[]{MultiThreadService.IS_PAUSEING, MultiThreadService.IS_DOWNLOADING});
	}
	
	public void clear(){
		SQLiteDatabase db = openHelper.getWritableDatabase();
		db.execSQL("delete from filestatelog");
	}
	
	public void close(){
		openHelper.close();
	}
}
