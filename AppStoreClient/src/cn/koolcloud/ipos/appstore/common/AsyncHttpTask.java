package cn.koolcloud.ipos.appstore.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import cn.koolcloud.ipos.appstore.api.HttpService;
import cn.koolcloud.ipos.appstore.entity.ResultSet;
import cn.koolcloud.ipos.appstore.interfaces.CallBack;
import cn.koolcloud.ipos.appstore.utils.Logger;
import cn.koolcloud.ipos.appstore.utils.NetUtil;

/**
 * <p>Title: AsyncHttpTask.java</p>
 * <p>Description: custom http Async task</p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: All In Pay</p>
 * @author 		Teddy
 * @date 		2013-11-5
 * @version 	
 */
public class AsyncHttpTask extends AsyncTask<Object, Object, Object> {
	static final String LOG_TAG = "AsyncHttpTask";

	/*public static int CONNECTION_TIMEOUT = 30 * 1000;// 1;
	public static int SO_TIMEOUT = 30 * 1000;*/

	Context context;
	String url;
	JSONObject parameters;
	CallBack callBack;
	int responseType;
	String requestMethod = "post";

	/*private static final String USER_AGENT = "AllInPay-Network";
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
	}*/

	public AsyncHttpTask(Context context, String url,
			JSONObject parameters, CallBack callBack, int responseType) {
		this.context = context;
		this.url = url;
		this.parameters = parameters;
		this.callBack = callBack;
		this.responseType = responseType;
	}

	public AsyncHttpTask(Context context, String method, String url,
			JSONObject parameters, CallBack callBack, int responseType) {
		this(context, url, parameters, callBack, responseType);

		requestMethod = method;
	}

	@Override
	protected Object doInBackground(Object... params) {

		Map<String, Object> results = new HashMap<String, Object>();

		try {

			// first check network allowed or not
			if (!NetUtil.isAvailable(context)) {
				results.put(ResultSet.Response.RETCODE,
						ResultSet.CURRENT_NET_NOT_ALLOWED.retcode);
				results.put(ResultSet.Response.DESCRIBE,
						ResultSet.CURRENT_NET_NOT_ALLOWED.describe);
				return results;
			}

			HttpService httpService = new HttpService();
			HttpResponse response = httpService.getResponseResult(url, parameters, requestMethod, null);

			if (response != null) {
				int statusCode = response.getStatusLine().getStatusCode();
				Logger.d("==statusCode==" + statusCode);
				HttpEntity entity = response.getEntity();
				if (statusCode == 200) {

					onResponse(results, entity);

				} else if ((statusCode == HttpStatus.SC_MOVED_PERMANENTLY) ||
			            (statusCode == HttpStatus.SC_MOVED_TEMPORARILY) ||
			            (statusCode == HttpStatus.SC_SEE_OTHER) ||
			            (statusCode == HttpStatus.SC_TEMPORARY_REDIRECT)) {	//Manually deal with new request
					url = response.getLastHeader("Location").getValue();
					
					HttpResponse newResponse = httpService.getResponseResult(url, parameters, requestMethod, null);
					statusCode = newResponse.getStatusLine().getStatusCode();
					Logger.d("==statusCode==" + statusCode);
					entity = newResponse.getEntity();
					if (statusCode == 200) {
						onResponse(results, entity);
					} else {
						Logger.d("==response.getEntity()=="
								+ EntityUtils.toString(entity, HTTP.UTF_8));
						results.put(ResultSet.Response.RETCODE,
								ResultSet.FAIL.retcode);
						results.put(ResultSet.Response.DESCRIBE,
								ResultSet.SERVER_ERROR.describe + statusCode);
					}
				} else {
					Logger.d("==response.getEntity()=="
							+ EntityUtils.toString(entity, HTTP.UTF_8));
					results.put(ResultSet.Response.RETCODE,
							ResultSet.FAIL.retcode);
					results.put(ResultSet.Response.DESCRIBE,
							ResultSet.SERVER_ERROR.describe + statusCode);
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			Logger.d("UnsupportedEncodingException");
			results.put(ResultSet.Response.RETCODE, ResultSet.FAIL.retcode);
			results.put(ResultSet.Response.DESCRIBE,
					ResultSet.NET_ENCODING_ERROR.describe);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			Logger.d("ClientProtocolException");
			results.put(ResultSet.Response.RETCODE, ResultSet.FAIL.retcode);
			results.put(ResultSet.Response.DESCRIBE,
					ResultSet.NET_ERROR.describe);
		} catch (IOException e) {
			e.printStackTrace();
			Logger.d("IOException");
			results.put(ResultSet.Response.RETCODE, ResultSet.FAIL.retcode);
			results.put(ResultSet.Response.DESCRIBE,
					ResultSet.NET_ERROR.describe);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			Logger.d("OutOfMemoryError");
			results.put(ResultSet.Response.RETCODE, ResultSet.FAIL.retcode);
			results.put(ResultSet.Response.DESCRIBE, ResultSet.FAIL.describe);
		}

		return results;
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();

		if (callBack != null) {
			callBack.onCancelled();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onPostExecute(Object result) {
		super.onPostExecute(result);
		if (isCancelled()) {
			return;
		}
		Map<String, Object> res = (Map<String, Object>) result;

		int retcode = (Integer) res.get(ResultSet.Response.RETCODE);

		if (ResultSet.SUCCESS.retcode == retcode) {
			Object content = res.get(ResultSet.Response.CONTENT);

			if (callBack != null) {
				parseResponse(content);
			}

		} else {
			if (callBack != null) {
				callBack.onFailure(ResultSet.NET_ERROR.describe);
			}
		}

	}

	private void parseResponse(Object content) {
		switch (responseType) {
		case ResultSet.RESPONSETYPE.RESPONSE_TYPE_JSON_ARRAY:
			try {
				callBack.onSuccess((JSONArray) content);
			} catch (Exception e) {
				e.printStackTrace();
			}

			break;

		case ResultSet.RESPONSETYPE.RESPONSE_TYPE_JSON_OBJECT:
			try {
				callBack.onSuccess((JSONObject) content);
			} catch (Exception e) {
				e.printStackTrace();
			}

			break;

		case ResultSet.RESPONSETYPE.RESPONSE_TYPE_STREAM:
			try {
				callBack.onSuccess((InputStream) content);
			} catch (Exception e) {
				e.printStackTrace();
			}

			break;

		}
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		if (callBack != null) {
			callBack.onStart();
		}
	}

	@Override
	protected void onProgressUpdate(Object... values) {
		super.onProgressUpdate(values);
	}

	public void onResponse(Map<String, Object> results, HttpEntity entity) {
		Logger.d("****************onResponse******************");

		results.put(ResultSet.Response.RETCODE, ResultSet.SUCCESS.retcode);
		results.put(ResultSet.Response.RESPONSE_TYPE, responseType);
		results.put(ResultSet.Response.DESCRIBE, ResultSet.SUCCESS.describe);

		switch (responseType) {
		case ResultSet.RESPONSETYPE.RESPONSE_TYPE_JSON_ARRAY:
			try {
				String entityContent = EntityUtils.toString(entity, HTTP.UTF_8);
				Logger.d("response type= RESPONSE_TYPE_JSON_ARRAY , entityContent ="
						+ entityContent);
				results.put(ResultSet.Response.CONTENT, new JSONArray(
						entityContent));

			} catch (Exception e) {
				e.printStackTrace();
			}

			break;

		case ResultSet.RESPONSETYPE.RESPONSE_TYPE_JSON_OBJECT:
			try {
				
				String entityContent = EntityUtils.toString(entity, HTTP.UTF_8);
				Logger.d("response type= RESPONSE_TYPE_JSON_OBJECT , entityContent ="
						+ entityContent);
				results.put(ResultSet.Response.CONTENT, new JSONObject(
						entityContent));
				
			} catch (Exception e) {
				e.printStackTrace();
			}

			break;

		case ResultSet.RESPONSETYPE.RESPONSE_TYPE_STREAM:

			InputStream is;
			try {

				is = entity.getContent();
				results.put(ResultSet.Response.CONTENT, is);

				// Logger.debug(this,
				// "response type= RESPONSE_TYPE_STREAM , entityContent ="+FileManager.convertStreamToString((InputStream)
				// results.get(ResultSet.Response.CONTENT)));

			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			break;

		}

	}

}
