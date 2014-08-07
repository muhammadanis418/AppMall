package cn.koolcloud.ipos.appstore.download.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DownloadDBHelper extends SQLiteOpenHelper {
    private final static String DATABASE_NAME = "download.db";
    private final static int DATABASE_VERSION = 2;
    /**
     * create download database helper
     * @param context
     */
    public DownloadDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
       createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	/*if (oldVersion == 1 && newVersion == 2) {
			// Drop tables  
	        db.execSQL("DROP TABLE IF EXISTS download_info");
	        db.execSQL("DROP TABLE IF EXISTS end_download_info");
	        db.execSQL("DROP TABLE IF EXISTS pause_download_info");
	        // Create tables  
	        onCreate(db); 
		}*/
    }
    
    private void createTables(SQLiteDatabase db) {
    	 // file download table
        db.execSQL("create table if not exists download_info(" +
        		"_id integer PRIMARY KEY AUTOINCREMENT, " +
        		"url char, " +
        		"download_id char, " +
        		"file_name char, " +
        		"total_size bigint, " +
        		"start_pos bigint, " +
                "end_pos bigint, " +
                "compelete_size bigint, " +
                "thread_id integer, " +
                "file_version char, " +
                "file_version_code int, " +
                "version_code int, " +
                "package_name char, " +
                "icon_url char, " +
                "soft_id char" +
                ")");
        
        // create index
        db.execSQL("create index if not exists idownload on download_info(url, download_id)");

        // paused files table
        db.execSQL("create table if not exists pause_download_info(" +
        		"_id integer PRIMARY KEY AUTOINCREMENT, " +
        		"url char, " + 
        		"download_id char, " + 
        		"package_name char, " +
        		"soft_id char" +
        		")");
        
        // create index
        db.execSQL("create index if not exists ipdownload on pause_download_info(" +
        		"url, " +
        		"download_id, " +
        		"package_name, " +
        		"soft_id" +
        		")");

        // download completed files table
        db.execSQL("create table if not exists end_download_info(" +
        		"_id integer PRIMARY KEY AUTOINCREMENT, " +
        		"url char, " +
        		"download_id char, " +
        		"file_name char, " +
        		"file_size bigint, " +
        		"done_time bigint, " +
        		"icon_url char, " +
        		"file_version char, " +
        		"file_version_code int, " +
        		"version_code int, " +
        		"package_name char, " +
        		"file_path char, " +
        		"soft_id char" +
        		")");
        
        // create index
        db.execSQL("create index if not exists iedownload on end_download_info(" +
        		"url, " +
        		"download_id, " +
        		"package_name, " +
        		"done_time, " +
        		"soft_id)");
    }
}
