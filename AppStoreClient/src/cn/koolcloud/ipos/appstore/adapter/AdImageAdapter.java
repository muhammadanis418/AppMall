package cn.koolcloud.ipos.appstore.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.cache.ImageDownloader;


public class AdImageAdapter extends BaseAdapter {

	private List<String> imageList;
	private Context context;

	public AdImageAdapter(List<String> images, Context context) {
		this.imageList = images;
		this.context = context;
	}

	@Override
	public int getCount() {
		return Integer.MAX_VALUE;
	}

	@Override
	public Object getItem(int position) {
		return imageList.get(position % imageList.size());
	}

	@Override
	public long getItemId(int position) {
		return position % imageList.size();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.ad_item, null); // init convertView
			Gallery.LayoutParams params = new Gallery.LayoutParams(
					Gallery.LayoutParams.WRAP_CONTENT,
					Gallery.LayoutParams.WRAP_CONTENT);
			convertView.setLayoutParams(params);
			holder = new ViewHolder();
			holder.imageView = (ImageView) convertView.findViewById(R.id.gallery_image);
//			holder.textView = (TextView) convertView.findViewById(R.id.gallery_text);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		Bitmap defaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pic_ad_default);
		ImageDownloader.getInstance(context).download(imageList.get(position % imageList.size()), defaultBitmap, holder.imageView);
		return convertView;
	}

	static class ViewHolder {
		ImageView imageView;
//		TextView textView;
	}
}
