package cn.koolcloud.ipos.appstore.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import cn.koolcloud.ipos.appstore.BuildingConfig;
import cn.koolcloud.ipos.appstore.api.ssl.AppStoreSSLSocketFactory;
import cn.koolcloud.ipos.appstore.constant.Constants;
import cn.koolcloud.ipos.appstore.interfaces.CallBack;
import cn.koolcloud.ipos.appstore.interfaces.Task;

public class AsyncHttpClient {
	private static final String TAG = "AsyncHttpClient";
	
	public static int CONNECTION_TIMEOUT = 30 * 1000;// 1;
	public static int SO_TIMEOUT = 30 * 1000;
	private static final String USER_AGENT = "AllInPay-Network";
	public static final HttpParams HTTP_PARAMS;
	static {
		// Prepare HTTP parameters.
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setStaleCheckingEnabled(params, true);
		HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, CONNECTION_TIMEOUT);
		HttpClientParams.setRedirecting(params, true);
		HttpProtocolParams.setUserAgent(params, USER_AGENT);
		HttpProtocolParams.setUseExpectContinue(params, false);
		HTTP_PARAMS = params;
	}
	
	public static DefaultHttpClient getDefaultHttpClient(String url) {
		//add ssl param start
		Pattern pattern = Pattern.compile(Constants.REG_PACKAGE_MATCH);
		Matcher matcher = pattern.matcher(url);
		if (!matcher.matches()) {
			return new DefaultHttpClient();
		}
		SchemeRegistry schemeRegistry = new SchemeRegistry();  
		schemeRegistry.register(new Scheme("https",  
		                    new AppStoreSSLSocketFactory(), 443));  
		ClientConnectionManager connManager = new ThreadSafeClientConnManager(HTTP_PARAMS, schemeRegistry);  
		//add ssl param end
		return new DefaultHttpClient(connManager, HTTP_PARAMS);
	}

	public static Task request(Context context, String url,
			JSONObject parameters, CallBack callBack, int responseType) {
		class TaskImpl implements Task {
			public AsyncTask<?, ?, ?> asynTask;

			public TaskImpl(AsyncTask<?, ?, ?> asynTask) {
				this.asynTask = asynTask;
			}

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return asynTask.cancel(mayInterruptIfRunning);
			}

			@Override
			public AsyncTask<?, ?, ?> getTask() {
				return asynTask;
			}

		}

		try {
			return new TaskImpl(new AsyncHttpTask(context, url, parameters,
					callBack, responseType).execute());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// Get method
	public static Task requestGet(Context context, String url,
			JSONObject parameters, CallBack callBack, int responseType) {
		class TaskImpl implements Task {
			public AsyncTask<?, ?, ?> asynTask;

			public TaskImpl(AsyncTask<?, ?, ?> asynTask) {
				this.asynTask = asynTask;
			}

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return asynTask.cancel(mayInterruptIfRunning);
			}

			@Override
			public AsyncTask<?, ?, ?> getTask() {
				return asynTask;
			}

		}

		try {
			return new TaskImpl(new AsyncHttpTask(context, "get", url,
					parameters, callBack, responseType).execute());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
