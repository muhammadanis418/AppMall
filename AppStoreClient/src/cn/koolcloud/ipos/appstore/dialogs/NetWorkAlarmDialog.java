package cn.koolcloud.ipos.appstore.dialogs;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.ui.BaseActivity;
import cn.koolcloud.ipos.appstore.ui.SplashActivity;
import cn.koolcloud.ipos.appstore.utils.Utils;

public class NetWorkAlarmDialog extends BaseActivity implements View.OnClickListener {

	private TextView titleTextView;
	private TextView msgBodyTextView;
	private Button okBtn;
	
	private boolean isNetAvailable = false;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.dialog_common_layout);
		isNetAvailable = getIntent().getBooleanExtra(SplashActivity.NETWORK_AVAILABLE_KEY, false);
		initViews();
		activityList.add(this);
	}

	private void initViews() {
		
		titleTextView = (TextView) findViewById(R.id.dialog_title_text);
		titleTextView.setText(Utils.getResourceString(getApplicationContext(), R.string.dialog_network_alarm_title));
		msgBodyTextView = (TextView) findViewById(R.id.dialog_common_text);
		okBtn = (Button) findViewById(R.id.ok);
		okBtn.setOnClickListener(this);
		okBtn.setVisibility(View.VISIBLE);
		
		StringBuffer strBuffer = new StringBuffer();
		//append dialog body message
		if (!isNetAvailable) {
			strBuffer.append(Utils.getResourceString(getApplicationContext(), R.string.dialog_network_not_available));
		}
		msgBodyTextView.setText(strBuffer.toString());
	}
	
	

	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}

	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		case R.id.ok:
			finish();
			break;

		default:
			break;
		}
	}
	
}
