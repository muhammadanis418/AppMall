package cn.koolcloud.ipos.appstore.fragment;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import cn.koolcloud.ipos.appstore.MyApp;
import cn.koolcloud.ipos.appstore.R;


public class BaseFragment extends Fragment {
	
	private Dialog myDialog = null;
	private LayoutInflater mInflater;
	private ImageView imageView = null;
	protected static MyApp application;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		application = (MyApp) getActivity().getApplication();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

	private void showProgressDialog() {
		dissmissProgressDialog();
		
		myDialog = new Dialog(getActivity(), R.style.dialog);
		myDialog.show();
		View view = mInflater.inflate(R.layout.loading_image, null);
		imageView = (ImageView) view.findViewById(R.id.animationImage);
		myDialog.setContentView(view);
		AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getDrawable();
		animationDrawable.start();
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
	
	/**
	* @Title: unbindDrawables
	* @Description: TODO release bitmap resources
	* @param @param view
	* @return void 
	* @throws
	*/
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
	
	public boolean switchStatus(boolean isChange) {
		return !isChange;
	}

	@Override
	public void onDestroy() {
		dissmissProgressDialog();
		super.onDestroy();
	}
}
