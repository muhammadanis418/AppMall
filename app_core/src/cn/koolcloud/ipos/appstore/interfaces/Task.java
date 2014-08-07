package cn.koolcloud.ipos.appstore.interfaces;

import android.os.AsyncTask;

public interface Task {
	AsyncTask<?, ?, ?> getTask();
	boolean cancel(boolean mayInterruptIfRunning );
	
}
