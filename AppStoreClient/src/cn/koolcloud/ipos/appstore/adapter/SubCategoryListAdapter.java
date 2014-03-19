package cn.koolcloud.ipos.appstore.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.entity.Category;

/**
 * <p>Title: SubCategoryListAdapter.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: All In Pay</p>
 * @author 		Teddy
 * @date 		2013-11-8
 * @version 	
 */
public class SubCategoryListAdapter extends BaseAdapter {
	private List<Category> dataList = new ArrayList<Category>();
	private Context ctx;
	private LayoutInflater mInflater;
	
	private int selectedPosition = 0;

	public SubCategoryListAdapter(Context context, List<Category> dataSource, int currentPosition) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);	
		this.ctx = context;
		dataList = dataSource;
		selectedPosition = currentPosition;
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
		CategoryItemViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.left_nav_category_item, null);
			holder = new CategoryItemViewHolder();
			holder.rootLinearLayout = (LinearLayout) convertView.findViewById(R.id.left_nav_item_root);
			holder.indicatorImageView = (ImageView) convertView.findViewById(R.id.indicator);
			holder.leftMenuImageView = (ImageView) convertView.findViewById(R.id.left_menu_icon);	
			holder.menuTextView = (TextView) convertView.findViewById(R.id.left_menu_text);							
			convertView.setTag(holder);
		} else {
			holder = (CategoryItemViewHolder) convertView.getTag();
		}

		final Category categoryInfo = dataList.get(position);
		holder.menuTextView.setText(categoryInfo.getName());
		if (position == selectedPosition) {
			holder.rootLinearLayout.setSelected(true);
			holder.indicatorImageView.setVisibility(View.VISIBLE);
		} else {
			holder.rootLinearLayout.setSelected(false);
			holder.indicatorImageView.setVisibility(View.GONE);
		}

		return convertView;
	}
	
	class CategoryItemViewHolder {
		LinearLayout rootLinearLayout;
		ImageView indicatorImageView;
		ImageView leftMenuImageView;
		TextView menuTextView;
	}
}
