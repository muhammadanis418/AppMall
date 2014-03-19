package cn.koolcloud.ipos.appstore.dialogs;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.api.ApiService;
import cn.koolcloud.ipos.appstore.ui.BaseActivity;
import cn.koolcloud.ipos.appstore.ui.WebViewActivity;
import cn.koolcloud.ipos.appstore.utils.Env;
import cn.koolcloud.ipos.appstore.utils.Utils;

public class AboutDialog extends BaseActivity implements View.OnClickListener {

	private TextView titleTextView;
	private TextView msgBodyTextView;
	private TextView linkTextView;
	private Button okButton;
	
	private SpannableString spString = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.dialog_about_layout);
		activityList.add(this);
		initViews();
	}

	private void initViews() {
		
		titleTextView = (TextView) findViewById(R.id.dialog_title_text);
		titleTextView.setText(Utils.getResourceString(getApplicationContext(), R.string.action_label_menu_setting_abount));
		msgBodyTextView = (TextView) findViewById(R.id.dialog_common_text);
		/*okButton = (Button) findViewById(R.id.ok);
		okButton.setVisibility(View.VISIBLE);
		okButton.setOnClickListener(this);*/
		
		StringBuffer strBuffer = new StringBuffer();
//		strBuffer.append(Utils.getResourceString(getApplicationContext(), R.string.app_version_code));
		strBuffer.append(Utils.getResourceString(getApplicationContext(), R.string.app_name) + "\n");
		strBuffer.append(Utils.getResourceString(getApplicationContext(), R.string.app_version_code));
		strBuffer.append(Env.getVersionName(getApplicationContext()) + "\n");
		strBuffer.append(Utils.getResourceString(getApplicationContext(), R.string.about_warn) + "\n");
		strBuffer.append(Utils.getResourceString(getApplicationContext(), R.string.about_warninfo));
		
		msgBodyTextView.setText(strBuffer.toString());
		
		linkTextView = (TextView) findViewById(R.id.dialog_link_text);
		spString = new SpannableString(getResources().getString(R.string.about_link));
		spString.setSpan(new StyleSpan(Typeface.BOLD), 0, getResources().getString(R.string.about_link).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		spString.setSpan(new URLSpan(getResources().getString(R.string.about_link_http)), 0,
				getResources().getString(R.string.about_link).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		linkTextView.setText(spString);
		linkTextView.setOnClickListener(this);
//		linkTextView.setMovementMethod(LinkMovementMethod.getInstance());
	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {
		case R.id.ok:
			finish();
			break;
		case R.id.dialog_link_text:
			Intent mIntent = new Intent();
			mIntent.setClass(this, WebViewActivity.class);
			mIntent.putExtra("url", ApiService.getContractLink());
			startActivity(mIntent);
			finish();
			break;

		default:
			break;
		}
		
	}
	
}
