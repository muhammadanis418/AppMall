package cn.koolcloud.ipos.appstore.dialogs;

import org.json.JSONObject;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import cn.koolcloud.ipos.appstore.AppStorePreference;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.api.ApiService;
import cn.koolcloud.ipos.appstore.constant.Constants;
import cn.koolcloud.ipos.appstore.entity.App;
import cn.koolcloud.ipos.appstore.entity.ResultSet;
import cn.koolcloud.ipos.appstore.interfaces.CallBack;
import cn.koolcloud.ipos.appstore.ui.BaseActivity;
import cn.koolcloud.ipos.appstore.utils.JsonUtils;
import cn.koolcloud.ipos.appstore.utils.Logger;
import cn.koolcloud.ipos.appstore.utils.ToastUtil;
import cn.koolcloud.ipos.appstore.utils.Utils;

public class SoftwareCommentDialog extends BaseActivity implements View.OnClickListener {
	private static final String TAG = "SoftwareCommentDialog";

	public static final int SOFTWARE_COMMNET_DIALOG_REQUEST = 1;
	private TextView titleTextView;
	private EditText commentEditText;
	private Button okButton;
	private Button cancelButton;
	private RatingBar ratingBar;
	
	private App app = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.software_detail_remark);
		app = (App) getIntent().getSerializableExtra(Constants.SER_KEY);
		initViews();
		
	}

	private void initViews() {
		
		titleTextView = (TextView) findViewById(R.id.dialog_title_text);
		titleTextView.setText(Utils.getResourceString(getApplicationContext(), R.string.dialog_software_comment_title));
		okButton = (Button) findViewById(R.id.ok);
		okButton.setOnClickListener(this);
		
		cancelButton = (Button) findViewById(R.id.cancel);
		cancelButton.setOnClickListener(this);
		
		commentEditText = (EditText) findViewById(R.id.sw_mark_edit);
		ratingBar = (RatingBar) findViewById(R.id.edit_ratingBar);
	}
	
	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		case R.id.ok:
			String comment = commentEditText.getText().toString();
			int rating = (int) ratingBar.getRating();
			if (!isCommentsEmpty(comment, rating)) {
				commitComment(comment, rating);
			} else {
				ToastUtil.showToast(application, R.string.comment_rating_not_null);
			}
			break;
		case R.id.cancel:
			finish();
			break;
		default:
			break;
		}
	}
	
	private void commitComment(String comment, int rating) {
		ApiService.commitComment(application, AppStorePreference.getTerminalID(application), 
				app.getId(), comment, rating, commitCommentCallBack);
	}
	
	//get categories call back
	private CallBack commitCommentCallBack = new CallBack() {
		@Override
		public void onCancelled() {
			dismissLoading();
		}

		@Override
		public void onStart() {
			showLoading();
		}

		@Override
		public void onSuccess(JSONObject jsonObj) {
			try {
				String retCode = "";
				String data = "";
				
				Logger.d("-------commit Comment=" + jsonObj.toString());
				
				retCode = JsonUtils.getStringValue(jsonObj, Constants.REQUEST_STATUS);
				data = JsonUtils.getStringValue(jsonObj, Constants.REQUEST_DATA);
				
				/*if (!Constants.REQUEST_STATUS_OK.equals(retCode)) {
					onFailure(data);
				} else {
					if (TextUtils.isEmpty(data)) {
						onFailure(Utils.getResourceString(getApplicationContext(), R.string.nonetwork_prompt_server_error));
					} else {
						
					}
				}*/
				
				if (retCode.equals(Constants.REQUEST_STATUS_OK)) {
					ToastUtil.showToast(application, R.string.remark_app_success);
				} else {
					ToastUtil.showToast(application, R.string.remark_app_failure);
				}
				
				finish();
				
				dismissLoading();
			} catch (Exception e) {
				onFailure("commit comment response error!");
			}
		}
		
		@Override
		public void onFailure(String msg) {
			dismissLoading();
			if (msg.contains(ResultSet.NET_ERROR.describe)) {
				
				ToastUtil.showToast(application, R.string.nonetwork_prompt_server_error);
			}
		}
	};

	/**
	 * deal with not responding on clicking out side of dialog
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;  
	}
	
	private boolean isCommentsEmpty(String comment, int rating) {
		boolean result = false;
		if (TextUtils.isEmpty(comment.trim())) {
			return true;
		} else if (rating == 0) {
			return true;
		}		
			
		return result;
	}

}
