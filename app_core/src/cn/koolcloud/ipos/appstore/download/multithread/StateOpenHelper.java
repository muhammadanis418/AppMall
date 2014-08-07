package cn.koolcloud.ipos.appstore.download.multithread;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StateOpenHelper extends SQLiteOpenHelper {

	private static final String state = "state.db";
	private static final int VERSION = 1;

	/**
	 * 构造器
	 * @param context
	 */
	public StateOpenHelper(Context context) {
		super(context, state, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS filestatelog (id integer primary key autoincrement, packageName varchar(100), state INTEGER, percent INTEGER)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS filestatelog");
		onCreate(db);
	}

}
