package cn.koolcloud.ipos.appstore.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.koolcloud.ipos.appstore.R;

public class ViewUtils {

	/**
	* @Title: inflateLayoutView
	* @Description: TODO inflate layout to view
	* @param @param ctx
	* @param @param inflater
	* @param @param layoutId
	* @param @param imageViewDrawable
	* @param @param textViewString
	* @param @return
	* @return View 
	* @throws
	*/
	public static View inflateNavigationBarItem(Context ctx, LayoutInflater inflater,
			int imageViewDrawable, int textViewString, int tag, LinearLayout container) {
		View navPageView = inflater.inflate(R.layout.left_nav_item, null);
		navPageView.setBackgroundResource(R.drawable.left_nav_item_selected);
		ImageView imageView = (ImageView) navPageView.findViewById(R.id.left_menu_icon);
		imageView.setImageResource(imageViewDrawable);
		TextView textView = (TextView) navPageView.findViewById(R.id.left_menu_text);
		textView.setText(ctx.getResources().getString(textViewString));
		navPageView.setTag(tag);
		container.addView(navPageView);
		
		return navPageView;
	}
	
	public static int getViewLocationScreenX(View view) {
		int[] location = new int[2];
		view.getLocationOnScreen(location);
	    return location[0];
	}
	
	public static int getViewLocationScreenY(View view) {
		int[] location = new int[2];
		view.getLocationOnScreen(location);
	    return location[1];
	}
}
