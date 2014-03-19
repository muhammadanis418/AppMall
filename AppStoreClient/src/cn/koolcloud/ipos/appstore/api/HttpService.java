package cn.koolcloud.ipos.appstore.api;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import cn.koolcloud.ipos.appstore.api.ssl.AppStoreSSLSocketFactory;
import cn.koolcloud.ipos.appstore.common.AsyncHttpClient;
import cn.koolcloud.ipos.appstore.utils.Logger;

import android.content.Context;

public class HttpService {
	private static final String TAG = "HttpService";

	public HttpService() {

	}
	
	public JSONObject sendRequest(String urlString, JSONObject request)
			throws IOException, JSONException {
		InputStream in = null;
		ByteArrayOutputStream baos = null;
		String str = null;

		HttpClient client = AsyncHttpClient.getDefaultHttpClient();
		HttpPost req = new HttpPost(urlString);
		req.setHeader("Content-Type", "application/json;charset=UTF8");
		
		StringEntity ent = new StringEntity(request.toString(), HTTP.UTF_8);

		ent.setContentEncoding("UTF-8");
		
		req.setEntity(ent);
		
		Logger.d(request.toString());
		
		HttpResponse response = client.execute(req);
		
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			in = new BufferedInputStream(response.getEntity().getContent());
			baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = in.read(buffer)) != -1) {
				baos.write(buffer, 0, len);
			}
			str = new String(baos.toByteArray(), "utf8");
		} else {
			Logger.d("error:" + response.getStatusLine().getStatusCode());
		}

		return new JSONObject(str);
	}

}
