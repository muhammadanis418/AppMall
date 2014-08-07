package cn.koolcloud.ipos.appstore.adapter;

import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.constant.Constants;
import cn.koolcloud.ipos.appstore.fragment.MainLeftFragment;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MainLeftNaviAdapter extends BaseAdapter {
	private Context context;
	private String []mainArray;
	
	public MainLeftNaviAdapter(Context context) {
		this.context = context;
		mainArray = context.getResources().getStringArray(R.array.main_array);
	}

	@Override
	public int getCount() {
		return mainArray.length;
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		ViewHolder holder = null;
		if(arg1 == null) {
			holder = new ViewHolder();
			arg1 = LayoutInflater.from(context).inflate(R.layout.main_left_nav_row, null);
			arg1.setTag(holder);
		} else {
			holder = (ViewHolder) arg1.getTag();
		}
		
		holder.mainLeftNaviImage = (ImageView) arg1.findViewById(R.id.mainLeftNaviImage);
		holder.mainLeftNaviText = (TextView) arg1.findViewById(R.id.mainLeftNaviText);
		holder.indicatorImage = (ImageView) arg1.findViewById(R.id.indicatorImage);
		holder.mainSelectHintText = (TextView) arg1.findViewById(R.id.mainSelectHintText);

		holder.mainLeftNaviText.setText(mainArray[arg0]);
		arg1.setBackgroundColor(Color.TRANSPARENT);
		holder.indicatorImage.setVisibility(View.GONE);
		holder.mainLeftNaviText.setTextColor(context.getResources().getColor(R.color.left_nav_text));
		holder.mainSelectHintText.setVisibility(View.GONE);
		
		if(arg0 == 0) {
			if(MainLeftFragment.currentNavItem == Constants.NAV_ITEM_CATEGORY) {
				holder.mainLeftNaviImage.setBackgroundResource(R.drawable.fenlei_on);
				holder.mainLeftNaviText.setTextColor(Color.WHITE);
				holder.indicatorImage.setVisibility(View.VISIBLE);
				arg1.setBackgroundColor(context.getResources().getColor(R.color.category_select_bg));
				holder.mainSelectHintText.setVisibility(View.VISIBLE);
			} else {
				holder.mainLeftNaviImage.setBackgroundResource(R.drawable.fenlei_off);
			}
		} else if(arg0 == 1) {
			if(MainLeftFragment.currentNavItem == Constants.NAV_ITEM_MANAGEMENT) {
				holder.mainLeftNaviImage.setBackgroundResource(R.drawable.guanli_on);
				holder.mainLeftNaviText.setTextColor(Color.WHITE);
				holder.indicatorImage.setVisibility(View.VISIBLE);
				arg1.setBackgroundColor(context.getResources().getColor(R.color.category_select_bg));
				holder.mainSelectHintText.setVisibility(View.VISIBLE);
			} else {
				holder.mainLeftNaviImage.setBackgroundResource(R.drawable.guanli_off);
			}
		} else if(arg0 == 2) { // settings
			holder.mainLeftNaviImage.setBackgroundResource(R.drawable.setting_normal);
		} else if(arg0 == 3) { // about
			holder.mainLeftNaviImage.setBackgroundResource(R.drawable.about_normal);
		}
		
		return arg1;
	}

	class ViewHolder {
		TextView mainLeftNaviText;
		ImageView mainLeftNaviImage;
		ImageView indicatorImage;
		TextView mainSelectHintText;
	}
}
