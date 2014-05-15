package cn.koolcloud.ipos.appstore.adapter;

import java.util.ArrayList;
import java.util.List;

import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.adapter.LocalSoftListAdapter.SoftItemViewHolder;
import cn.koolcloud.ipos.appstore.cache.ImageDownloader;
import cn.koolcloud.ipos.appstore.entity.AppInfo;
import cn.koolcloud.ipos.appstore.entity.NotificationPromotionInfo;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * <p>Title: NotificationPromotionAdapter.java </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2014</p>
 * <p>Company: All In Pay</p>
 * @author 		Teddy
 * @date 		2014-5-14
 * @version 	
 */
public class NotificationPromotionAdapter extends BaseAdapter {
	
	private List<NotificationPromotionInfo> dataList = new ArrayList<NotificationPromotionInfo>();
	private Context ctx;
	private LayoutInflater mInflater;

	public NotificationPromotionAdapter(
			List<NotificationPromotionInfo> dataList, Context ctx) {
		this.dataList = dataList;
		this.ctx = ctx;
		this.mInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return dataList.size();
	}

	@Override
	public Object getItem(int position) {
		return dataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		NotifyPromotionViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.notification_promotion_item, null);
			holder = new NotifyPromotionViewHolder();
			holder.promotionImage = (ImageView) convertView.findViewById(R.id.notificationImageView);	
			holder.promotionTitle = (TextView) convertView.findViewById(R.id.titleTextView);							
			holder.promotionDate = (TextView) convertView.findViewById(R.id.dateTextView);
			holder.promotionDesc = (TextView) convertView.findViewById(R.id.dataContentTextView);
			convertView.setTag(holder);
		} else {
			holder = (NotifyPromotionViewHolder) convertView.getTag();
		}
		NotificationPromotionInfo promotionInfo = dataList.get(position);
		
		Bitmap defaultBitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.pic_ad_default);
		ImageDownloader.getInstance(ctx).download(promotionInfo.getImageFileName(), defaultBitmap, holder.promotionImage);
		
		holder.promotionTitle.setText(promotionInfo.getTitle());
		holder.promotionDate.setText(promotionInfo.getDate());
		holder.promotionDesc.setText(promotionInfo.getDescription());
		
		return convertView;
	}
	
	class NotifyPromotionViewHolder {
		ImageView promotionImage;
		TextView promotionDate;
		TextView promotionTitle;
		TextView promotionDesc;
	}

}
