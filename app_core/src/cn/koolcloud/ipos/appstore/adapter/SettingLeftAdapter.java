package cn.koolcloud.ipos.appstore.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.koolcloud.ipos.appstore.R;

public class SettingLeftAdapter extends BaseAdapter {
	private Context context;
	private String []settingArray;
	private int selectedPosition;
	
	public SettingLeftAdapter(Context context, int selectedPosition) {
		this.context = context;
		this.selectedPosition = selectedPosition;
		settingArray = context.getResources().getStringArray(R.array.setting_array);
	}

	@Override
	public int getCount() {
		return settingArray.length;
	}

	@Override
	public Object getItem(int arg0) {
		return settingArray[arg0];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CategoryItemViewHolder holder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.left_nav_category_item, null);
			holder = new CategoryItemViewHolder();
			holder.rootLinearLayout = (LinearLayout) convertView.findViewById(R.id.left_nav_item_root);
			holder.indicatorImageView = (ImageView) convertView.findViewById(R.id.indicator);
			holder.leftMenuImageView = (ImageView) convertView.findViewById(R.id.left_menu_icon);	
			holder.menuTextView = (TextView) convertView.findViewById(R.id.left_menu_text);		
			holder.selectHintText = (TextView) convertView.findViewById(R.id.selectHintText);
			convertView.setTag(holder);
		} else {
			holder = (CategoryItemViewHolder) convertView.getTag();
		}

		holder.menuTextView.setText(settingArray[position]);
		if (position == selectedPosition) {
			holder.indicatorImageView.setVisibility(View.VISIBLE);
			holder.menuTextView.setTextColor(Color.WHITE);
			holder.leftMenuImageView.setBackgroundResource(R.drawable.fenlei_on);
			convertView.setBackgroundColor(context.getResources().getColor(R.color.category_select_bg));
			holder.selectHintText.setVisibility(View.VISIBLE);
		} else {
			holder.indicatorImageView.setVisibility(View.GONE);
			holder.menuTextView.setTextColor(context.getResources().getColor(R.color.left_nav_text));
			holder.leftMenuImageView.setBackgroundResource(R.drawable.fenlei_off);
			convertView.setBackgroundColor(Color.TRANSPARENT);
			holder.selectHintText.setVisibility(View.GONE);
		}

		return convertView;
	}
	
	class CategoryItemViewHolder {
		LinearLayout rootLinearLayout;
		ImageView indicatorImageView;
		ImageView leftMenuImageView;
		TextView menuTextView;
		TextView selectHintText;
	}

	public void updateSelectedPosition(int position) {
		this.selectedPosition = position;
		notifyDataSetChanged();
	}
}
