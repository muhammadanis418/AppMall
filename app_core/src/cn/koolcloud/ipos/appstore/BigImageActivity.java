package cn.koolcloud.ipos.appstore;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouch.OnImageViewTouchSingleTapListener;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.ViewGroup;
import cn.koolcloud.ipos.appstore.cache.ImageFileCache;
import cn.koolcloud.ipos.appstore.cache.base.FileNameGenerator;
import cn.koolcloud.ipos.appstore.utils.Env;

public class BigImageActivity extends BaseActivity {
	private ImageViewTouch mImage;
	private String url;

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
//		requestWindowFeature( Window.FEATURE_NO_TITLE );
		setContentView( R.layout.big_image );
		getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		
        url = getIntent().getExtras().getString("url");
		mImage = (ImageViewTouch) findViewById( R.id.image );
		
		// set the default image display type
//		mImage.setDisplayType( DisplayType.FIT_IF_BIGGER );
		mImage.setDisplayType(DisplayType.FIT_IF_BIGGER);
		String fileName = FileNameGenerator.generator(url);
		Bitmap bm = BitmapFactory.decodeFile(
				Env.SD_CARD_IMAGE_CACHE_DIR + fileName + ImageFileCache.IMG_SUFFIX);
		if(bm != null) {
			mImage.setImageBitmap(bm);
		} else {
			mImage.setImageBitmap(BitmapFactory.decodeResource(getResources(),
					R.drawable.pic_ad_default));
		}
		
		mImage.setSingleTapListener(new OnImageViewTouchSingleTapListener() {
			@Override
			public void onSingleTapConfirmed() {
				BigImageActivity.this.finish();
			}
		});
	}
}
