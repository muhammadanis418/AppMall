package cn.koolcloud.ipos.appstore.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.cache.ImageDownloader;
import cn.koolcloud.ipos.appstore.entity.Category;


/**
 * <p>Title: CategoryListAdapter.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: All In Pay</p>
 * @author 		Teddy
 * @date 		2013-11-6
 * @version 	
 */
public class CategoryListAdapter extends BaseAdapter {
	private List<Category> dataList = new ArrayList<Category>();
	private Context ctx;
	private LayoutInflater mInflater;

	public CategoryListAdapter(Context context, List<Category> dataSource) {
		this.ctx = context;
		dataList = dataSource;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);	
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
			convertView = mInflater.inflate(R.layout.category_grid_item, null);
			holder = new CategoryItemViewHolder();
			holder.iconImageView = (ImageView) convertView.findViewById(R.id.category_icon);	
			holder.categoryNameTextView = (TextView) convertView.findViewById(R.id.category_title);							
			holder.categoryDescTextView = (TextView) convertView.findViewById(R.id.category_comment);
			convertView.setTag(holder);
		} else {
			holder = (CategoryItemViewHolder) convertView.getTag();
		}

		final Category category = dataList.get(position);
		holder.categoryDescTextView.setText(category.getName());
		holder.categoryNameTextView.setText(category.getName());
		
		//app icon
		Bitmap defaultBitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.moren_icon);
		ImageDownloader.getInstance(ctx).download(category.getIconFileName(), defaultBitmap, holder.iconImageView);

		return convertView;
	}
	
	class CategoryItemViewHolder {
		ImageView iconImageView;
		TextView categoryDescTextView;
		TextView categoryNameTextView;
	}
}
