package cn.koolcloud.ipos.appstore.api;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.http.Header;
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
import org.json.JSONException;
import org.json.JSONObject;

import cn.koolcloud.ipos.appstore.common.AsyncHttpClient;
import cn.koolcloud.ipos.appstore.utils.MyLog;

public class HttpService {

	public HttpService() {

	}
	
	public JSONObject sendRequest(String urlString, JSONObject requestJson)
			throws IOException, JSONException {
		InputStream in = null;
		ByteArrayOutputStream baos = null;
		String str = null;
		
		MyLog.d(requestJson.toString());
		
		HttpResponse response = getResponseResult(urlString, requestJson, "post", null);
		
		if (response != null) {
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				in = new BufferedInputStream(response.getEntity().getContent());
				baos = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int len = 0;
				while ((len = in.read(buffer)) != -1) {
					baos.write(buffer, 0, len);
				}
				str = new String(baos.toByteArray(), "utf8");
			} else if ((statusCode == HttpStatus.SC_MOVED_PERMANENTLY) ||
		            (statusCode == HttpStatus.SC_MOVED_TEMPORARILY) ||
		            (statusCode == HttpStatus.SC_SEE_OTHER) ||
		            (statusCode == HttpStatus.SC_TEMPORARY_REDIRECT)) {
				urlString = response.getLastHeader("Location").getValue();
				HttpResponse newResponse = getResponseResult(urlString, requestJson, "post", null);
				
				if (newResponse != null) {
					statusCode = newResponse.getStatusLine().getStatusCode();
					if (statusCode == HttpStatus.SC_OK) {
						in = new BufferedInputStream(response.getEntity().getContent());
						baos = new ByteArrayOutputStream();
						byte[] buffer = new byte[1024];
						int len = 0;
						while ((len = in.read(buffer)) != -1) {
							baos.write(buffer, 0, len);
						}
						str = new String(baos.toByteArray(), "utf8");
					} else {
						MyLog.d("error:" + response.getStatusLine().getStatusCode());
					}
				} else {
					MyLog.d("error:" + response.getStatusLine().getStatusCode());
				}
				
			} else {
				MyLog.d("error:" + response.getStatusLine().getStatusCode());
			}
		}

		return new JSONObject(str);
	}
	
	public HttpResponse getResponseResult(String url, JSONObject parameters, String requestMethod, Header header) throws ClientProtocolException, IOException {
		HttpRequestBase request = null;
//		checkRequestType(request, url, parameters, requestMethod);
		
		if ("get".equals(requestMethod)) {

			request = new HttpGet(url);
			request.setHeader("Accept", "*/*");
		} else {

			request = new HttpPost(url);
			request.setHeader("Content-Type", "application/json;charset=UTF8");
		}

		if ("post".equals(requestMethod)) {
			StringEntity stringEntity = new StringEntity(parameters.toString(), HTTP.UTF_8);
			// AbstractHttpEntity ent = new UrlEncodedFormEntity(parameters,
			// HTTP.UTF_8);
			// ent.setContentType("application/x-www-form-urlencoded; charset=UTF-8");

			stringEntity.setContentEncoding("UTF-8");
			((HttpPost) request).setEntity(stringEntity);
		}
		
		//this header just for resume download files
		if (header != null) {
			request.addHeader(header);
		}

		MyLog.d("==url==" + url);

		MyLog.d("==parameters==" + parameters.toString());

//		HttpClient httpClient = new DefaultHttpClient(HTTP_PARAMS);
		DefaultHttpClient httpClient = AsyncHttpClient.getDefaultHttpClient(url);//https request
		httpClient.setRedirectHandler(new AppStoreRedirectHandler());
		return httpClient.execute(request);
	}
	
	/**
	 * <p>Description: Manually deal with redirect</p>
	 * <p>Copyright: Copyright (c) 2014</p>
	 * <p>Company: All In Pay</p>
	 * @author 		Teddy
	 * @date 		2014-5-5
	 * @version
	 */
	class AppStoreRedirectHandler implements RedirectHandler {

		public URI getLocationURI(HttpResponse response, HttpContext context) throws ProtocolException {
			return null;
		}
		
		public boolean isRedirectRequested(HttpResponse response,
				HttpContext context) {
			// Manually deal with redirect, so return false
			return false;
		}

	}

}
