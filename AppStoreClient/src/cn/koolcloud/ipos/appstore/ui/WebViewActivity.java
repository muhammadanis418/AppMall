package cn.koolcloud.ipos.appstore.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import cn.koolcloud.ipos.appstore.R;

public class WebViewActivity extends BaseActivity {

	private WebView webView;

	private String url = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview_main);
		url = getIntent().getStringExtra("url");

		findViews();
		readHtmlFromAssets();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// CookieSyncManager cookieSyncManager =
		// CookieSyncManager.createInstance(context);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeSessionCookie();// remove session.
	}

	private void findViews() {
		// get webview components
		webView = (WebView) findViewById(R.id.webview);
		// allow javascript
		webView.getSettings().setJavaScriptEnabled(true);
	}

	private void readHtmlFromAssets() {
		WebSettings webSettings = webView.getSettings();

		webSettings.setSupportZoom(false);
		webSettings.setUseWideViewPort(true);
		webSettings.setSavePassword(false);
		webSettings.setSaveFormData(false);
		webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
//		webSettings.setJavaScriptEnabled(true);
//		webSettings.setLoadsImagesAutomatically(true);
		
		//webView.setBackgroundColor(Color.TRANSPARENT); // set WebView background
														// color TRANSPARENT
		webView.setWebViewClient(new MyWebView());
		webView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				// Activity and webview decide progression, the progress
				// disappear when 100% loaded
				WebViewActivity.this.setProgress(progress * 100);
			}
		});

		webView.addJavascriptInterface(new JavaScriptInterface(), "android");
		// load html files from assets
		// webView.loadUrl("file:///android_asset/html/index.html");
		// webView.loadUrl(url + ":" + port);
		// webView.loadUrl("http://blog.csdn.net");
		webView.loadUrl(url);
	}
	
	Handler mHandler = new Handler();

	/*@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
			// webView.goBack();
//			showExitDialog();
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}*/

	/*public void showExitDialog() {
		new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.msg_cofirm_exit))
				.setIcon(android.R.drawable.ic_dialog_info)
				.setPositiveButton(getResources().getString(R.string.btn_cofirm_msg), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// clicking confirm button
						WebViewActivity.this.finish();
					}
				}).setNegativeButton(getResources().getString(R.string.btn_back_msg), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// nothing to do here
					}
				}).show();
		// super.onBackPressed();
	}*/

	/**
	 * @author Administrator deal with clicking hyper link
	 */
	class MyWebView extends WebViewClient {

		// override shouldOverrideUrlLoading method, avoid to open other
		// browsers when click the hyper link

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {

			view.loadUrl(url);
			// return true if you don't want to deal the click event on link,
			// otherwise return false
			return true;
		}
	}
	
	final class JavaScriptInterface {
        JavaScriptInterface() {}
  
        /** 
         * this method is invoked by javascript 
         */  
        public void print(final String jsonObj) {
            mHandler.post(new Runnable() {
                public void run() {
//                	Toast.makeText(getApplicationContext(), "Invoked by JS", Toast.LENGTH_LONG).show();
                	//TODO: call print method
                	//Invoke onJsAndroid method in js by java
//                	webView.loadUrl("javascript:onJsAndroid()");
                }
            });
        }
        
        public void printQueueNumber(final String jsonObj) {
        	mHandler.post(new Runnable() {
        		public void run() {
//                	Toast.makeText(getApplicationContext(), "Invoked by JS", Toast.LENGTH_LONG).show();
        			//TODO: call print method
        		}
        	});
        }
	}
}
