package cn.koolcloud.ipos.appstore.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Gallery;
import android.widget.ImageView;
import cn.koolcloud.ipos.appstore.R;
import cn.koolcloud.ipos.appstore.cache.ImageDownloader;
import cn.koolcloud.ipos.appstore.entity.App;
import cn.koolcloud.ipos.appstore.fancycoverflow.FancyCoverFlowAdapter;
import cn.koolcloud.ipos.appstore.fragment.MainRightFragment;

@SuppressWarnings("deprecation")
public class MainGalleryAdapter extends FancyCoverFlowAdapter {
	private Context context;
	private List<App> adsList = new ArrayList<App>();
	private Bitmap defaultBitmap;
	
	public MainGalleryAdapter(Context context) {
		this.context = context;
		defaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pic_default1);
	}

	@Override
	public int getCount() {
		return Integer.MAX_VALUE;
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

//	@Override
//	public View getView(final int position, View arg1, ViewGroup arg2) {
//		
//	}

	public void refreshGallery(List<App> adsList) {
		this.adsList = adsList;
		notifyDataSetChanged();
	}

	@Override
	public View getCoverFlowItem(int position, View reusableView,
			ViewGroup parent) {
		ImageView imageView = new ImageView(context);
		// 第2点改进，通过取余来循环取得resIds数组中的图像资源ID
		imageView.setScaleType(ImageView.ScaleType.FIT_XY);
		imageView.setLayoutParams(new Gallery.LayoutParams(MainRightFragment.galleryImageWidth,
				MainRightFragment.galleryImageHeight));
		imageView.setImageResource(R.drawable.pic_default1);
//		imageView.setPadding(1, 1, 1, 1);
		
		if(adsList != null && adsList.size() > 0) {
			final App app = adsList.get(position % adsList.size());
			ImageDownloader.getInstance(context).download(app.getAdPromotionImageName(), defaultBitmap, imageView);
		}
		return imageView;
	}
}
