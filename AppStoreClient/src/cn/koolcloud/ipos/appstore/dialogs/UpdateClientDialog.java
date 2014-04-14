package cn.koolcloud.ipos.appstore.dialogs;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.koolcloud.ipos.appstore.AppStorePreference;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.api.ApiService;
import cn.koolcloud.ipos.appstore.cache.DataCleanManager;
import cn.koolcloud.ipos.appstore.constant.Constants;
import cn.koolcloud.ipos.appstore.entity.Client;
import cn.koolcloud.ipos.appstore.ui.BaseActivity;
import cn.koolcloud.ipos.appstore.utils.ConvertUtils;
import cn.koolcloud.ipos.appstore.utils.Env;
import cn.koolcloud.ipos.appstore.utils.NetUtil;
import cn.koolcloud.ipos.appstore.utils.Utils;

public class UpdateClientDialog extends BaseActivity implements View.OnClickListener {
	private static final String TAG = "UpdateClientDialog";

	public static final int UPDATE_CLIENT_DIALOG_REQUEST = 1;
	private TextView titleTextView;
	private TextView msgBodyTextView;
	private Button okButton;
	private Button cancelButton;
	private RelativeLayout progressLayout;
	private RelativeLayout buttonLayout;
	
	private Client client = null;
	
	private String savePath;
	private static final String APK_NAME = "newVersion.apk";
	private File file;					//downloaded file
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.dialog_soft_update_layout);
		client = (Client) getIntent().getSerializableExtra(Constants.SER_KEY);
		initViews();
		
//		if (Constants.STRATEGY_UPDATE_NONE.equals(client.getStrategy()) || Constants.STRATEGY_UPDATE_FORCE.equals(client.getStrategy())) {
		if (client == null) {
			
			cancelButton.setVisibility(View.GONE);
		} else if (Constants.STRATEGY_UPDATE_FORCE.equals(client.getStrategy())) {
			//TODO:force download apk and install
			cancelButton.setVisibility(View.GONE);
			new DownloadThread().start();
			progressLayout.setVisibility(View.VISIBLE);
			buttonLayout.setVisibility(View.GONE);
			//clean appstore data
			DataCleanManager.cleanApplicationData(application);
		} else {
			buttonLayout.setVisibility(View.VISIBLE);
			cancelButton.setVisibility(View.VISIBLE);
		}
	}

	private void initViews() {
		
		progressLayout = (RelativeLayout) findViewById(R.id.progressLayout);
		buttonLayout = (RelativeLayout) findViewById(R.id.okLayout);
		
		titleTextView = (TextView) findViewById(R.id.dialog_title_text);
		titleTextView.setText(Utils.getResourceString(getApplicationContext(), R.string.dialog_software_update_title));
		msgBodyTextView = (TextView) findViewById(R.id.dialog_common_text);
		okButton = (Button) findViewById(R.id.ok);
		okButton.setVisibility(View.VISIBLE);
		okButton.setOnClickListener(this);
		
		cancelButton = (Button) findViewById(R.id.cancel);
		cancelButton.setText(Utils.getResourceString(getApplicationContext(), R.string.str_update_later));
		cancelButton.setOnClickListener(this);
		
		StringBuffer strBuffer = new StringBuffer();
		
		//network alarm
		/*if (NetUtil.isWifiConnected(getApplicationContext())) {
			
			strBuffer.append(Utils.getResourceString(getApplicationContext(), R.string.dialog_network_wifi_on));
		} else if (NetUtil.isEthernetDataEnable(getApplicationContext())) {
			
			strBuffer.append(Utils.getResourceString(getApplicationContext(), R.string.dialog_network_ethernet_on));
		} else if (NetUtil.isMobileConnected(getApplicationContext())) {
			
			strBuffer.append(Utils.getResourceString(getApplicationContext(), R.string.dialog_network_3g_on));
		}*/
		
		if (client == null) {
			
			strBuffer.append(Utils.getResourceString(getApplicationContext(), R.string.dialog_software_latest_version) + "\n");
			
		} else {
			
			strBuffer.append(Utils.getResourceString(getApplicationContext(), R.string.dialog_software_update_or_not) + "\n");
		}
		
		strBuffer.append(Utils.getResourceString(getApplicationContext(), R.string.dialog_software_current_version));
		strBuffer.append(Env.getVersionName(getApplicationContext()) + "\n");
		strBuffer.append(Utils.getResourceString(getApplicationContext(), R.string.dialog_software_found_version));
		if (client != null) {
			
			strBuffer.append(client.getVersion() + "\n");
		} else {
			strBuffer.append(Env.getVersionName(getApplicationContext()) + "\n");
		}
		if (client != null) {
			strBuffer.append(Utils.getResourceString(getApplicationContext(), R.string.dialog_software_size));
			strBuffer.append(ConvertUtils.bytes2kb(Long.parseLong(client.getSize())) + "\n");
		}
		
		//check app when no update
//		if (Constants.STRATEGY_UPDATE_NONE.equals(client.getStrategy())) {
		if (client == null) {
			
			okButton.setText(Utils.getResourceString(getApplicationContext(), R.string.str_ok));
			
		} else {
			
			okButton.setText(Utils.getResourceString(getApplicationContext(), R.string.str_update));
		}
		msgBodyTextView.setText(strBuffer.toString());
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK && client.getStrategy().equals(Constants.STRATEGY_UPDATE_FORCE)) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		case R.id.ok:
			//TODO download apk then install it
//			if (Constants.STRATEGY_UPDATE_NONE.equals(client.getStrategy())) {
			if (client == null) {
				finish();
			} else {
				
				if (file != null && file.exists()) {
					Env.install(UpdateClientDialog.this, file, UPDATE_CLIENT_DIALOG_REQUEST);
				} else {
					
					new DownloadThread().start();
					progressLayout.setVisibility(View.VISIBLE);
					buttonLayout.setVisibility(View.GONE);
					cancelButton.setVisibility(View.GONE);
					okButton.setText(Utils.getResourceString(getApplicationContext(), R.string.str_apk_downloading));
					okButton.setClickable(false);
					
				}
				//clean appstore data
				DataCleanManager.cleanApplicationData(application);
			}
			break;
		case R.id.cancel:
			finish();
			break;
		default:
			break;
		}
	}
	
	final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			file = (File) msg.obj;
			finish();
			Env.install(UpdateClientDialog.this, file, UPDATE_CLIENT_DIALOG_REQUEST);
		}
		
	};
	
	class DownloadThread extends Thread {

		public void run() {
			String fileName = client.getVersion() + "_" + client.getId() + "_" + APK_NAME;
			String terminalId = AppStorePreference.getTerminalID(getApplicationContext());
			File downloadFile = ApiService.downloadFile(getApplicationContext(), terminalId, client.getId(), fileName);
			
			Message msg = mHandler.obtainMessage();
			msg.obj = downloadFile;
			mHandler.sendMessage(msg);
		}
	}

	/**
	 * deal with not responding on clicking out side of dialog
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;  
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == UPDATE_CLIENT_DIALOG_REQUEST && resultCode == Activity.RESULT_CANCELED) {
			okButton.setClickable(true);
			if (file.exists()) {
				okButton.setText(Utils.getResourceString(getApplicationContext(), R.string.install));
			} else {
				okButton.setText(Utils.getResourceString(getApplicationContext(), R.string.str_update));
				
			}
		}
	}
	
}
