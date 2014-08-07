package cn.koolcloud.ipos.appstore.adapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.koolcloud.ipos.appstore.AppDetailActivity;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.api.ApiService;
import cn.koolcloud.ipos.appstore.constant.Constants;
import cn.koolcloud.ipos.appstore.entity.App;
import cn.koolcloud.ipos.appstore.entity.AppInfo;
import cn.koolcloud.ipos.appstore.entity.ResultSet;
import cn.koolcloud.ipos.appstore.interfaces.CallBack;
import cn.koolcloud.ipos.appstore.utils.ConvertUtils;
import cn.koolcloud.ipos.appstore.utils.Env;
import cn.koolcloud.ipos.appstore.utils.JsonUtils;
import cn.koolcloud.ipos.appstore.utils.MyLog;
import cn.koolcloud.ipos.appstore.utils.MySPEdit;
import cn.koolcloud.ipos.appstore.utils.ToastUtil;

/**
 * <p>Title: LocalSoftListAdapter.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: All In Pay</p>
 * @author 		Teddy
 * @date 		2013-11-5
 * @version 	
 */
public class LocalSoftListAdapter extends BaseAdapter {
	private List<AppInfo> dataList = new ArrayList<AppInfo>();
	private Context ctx;
	private Dialog myDialog = null;
	private ImageView imageView = null;

	public LocalSoftListAdapter(Context context, List<AppInfo> dataSource) {
		this.ctx = context;
		dataList = dataSource;
	}

	@Override
	public int getCount() {
		return dataList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return dataList.get(arg0);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		SoftItemViewHolder holder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(ctx).inflate(R.layout.local_software_list_item, null);
			holder = new SoftItemViewHolder();
			holder.IconImageView = (ImageView) convertView.findViewById(R.id.software_icon);	
			holder.softwareVersionTextView = (TextView) convertView.findViewById(R.id.software_version);							
			holder.softwareNameTextView = (TextView) convertView.findViewById(R.id.software_item_name);
			holder.softwareSizeTextView = (TextView) convertView.findViewById(R.id.software_size);
			holder.uninstallButton = (Button) convertView.findViewById(R.id.bt_uninstall);
			convertView.setTag(holder);
		} else {
			holder = (SoftItemViewHolder) convertView.getTag();
		}

		final AppInfo appInfo = dataList.get(position);
		holder.IconImageView.setImageDrawable(appInfo.getIcon());
		holder.softwareNameTextView.setText(appInfo.getName());
		holder.softwareSizeTextView.setText(ConvertUtils.bytes2kb(appInfo.getSoftSize()));
		holder.softwareVersionTextView.setText(appInfo.getVersionName());
		
		holder.uninstallButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Env.uninstallApp(ctx, appInfo.getPackageName());
			}
		});

		convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getAppDetailFromNetwork(dataList.get(position).getPackageName());
			}
		});

		return convertView;
	}
	
	private void getAppDetailFromNetwork(String packageName) {
		JSONArray jsonArray = new JSONArray();
		jsonArray.put(packageName);
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("packageNames", jsonArray);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ApiService.getAppDetail(ctx, MySPEdit.getTerminalID(ctx), 
				jsonObject.toString(), getAppDetailCallBack);
	}
	
	//get categories call back
	private CallBack getAppDetailCallBack = new CallBack() {
		@Override
		public void onCancelled() {
			dismissLoading();
		}

		@Override
		public void onStart() {
			showLoading();
			MyLog.e("onStart");
		}

		@Override
		public void onSuccess(JSONObject jsonObj) {
			try {
				MyLog.e("-------getCategoryInfo=" + jsonObj.toString());
				List<App> appListDataSource = new ArrayList<App>();
				appListDataSource = JsonUtils.parseJSONAppDetails(jsonObj);
				if(appListDataSource != null && appListDataSource.size() > 0) {
					Intent mIntent = new Intent(ctx, AppDetailActivity.class);
					Bundle mBundle = new Bundle();
					mBundle.putSerializable(Constants.SER_KEY, (Serializable) appListDataSource);
					mBundle.putInt(Constants.APP_LIST_POSITION, 0);
					mIntent.putExtras(mBundle);
					ctx.startActivity(mIntent);
				} else {
					Toast.makeText(ctx, R.string.not_get_app_detail, Toast.LENGTH_LONG).show();
				}
				dismissLoading();
			} catch (Exception e) {
				e.printStackTrace();
				onFailure("category response error!");
			}
		}

		@Override
		public void onFailure(String msg) {
			dismissLoading();
			MyLog.e("describe=" + msg);
			if (msg.contains(ResultSet.NET_ERROR.describe)) {
				ToastUtil.showToast(ctx, R.string.nonetwork_prompt_server_error, Toast.LENGTH_LONG);
			}
		}
	};
	
	class SoftItemViewHolder {
		ImageView IconImageView;
		TextView softwareNameTextView;
		TextView softwareVersionTextView;
		TextView softwareSizeTextView;
		Button uninstallButton;
	}
	
	private void showProgressDialog() {
		dissmissProgressDialog();
		
		myDialog = new Dialog(ctx, R.style.dialog);
		myDialog.setCancelable(false);
		myDialog.show();
		View view = LayoutInflater.from(ctx).inflate(R.layout.loading_image, null);
		imageView = (ImageView) view.findViewById(R.id.animationImage);
		myDialog.setContentView(view);
		AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getDrawable();
		animationDrawable.start();
	}
	public void showLoading() {
		/*Message msg = new Message();
		msg.what = SHOW_LOADING;
		myHandler.sendMessage(msg);*/
		showProgressDialog();
	}
	public void dismissLoading() {
		/*Message msg = new Message();
		msg.what = DISMISS_LOADING;
		myHandler.sendMessage(msg);*/
		dissmissProgressDialog();
	}
	
	private void dissmissProgressDialog() {
		if(myDialog != null) {
			if(myDialog.isShowing()) {
				if(imageView != null) {
					unbindDrawables(imageView);
				}
				myDialog.dismiss();
			}
			myDialog = null;
		}
	}
	
	private void unbindDrawables(View view) {
		Drawable back = view.getBackground();
	    if (back != null) {
	    	back.setCallback(null);
	    }
	    if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
	        for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
	            unbindDrawables(((ViewGroup) view).getChildAt(i));
	        }
	        ((ViewGroup) view).removeAllViews();
	    }
	}
}
