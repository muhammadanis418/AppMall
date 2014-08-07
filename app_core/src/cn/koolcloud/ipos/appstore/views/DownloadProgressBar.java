package cn.koolcloud.ipos.appstore.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import cn.koolcloud.ipos.appstore.R;

/**
 * <p>Title: DownloadProgressBar.java</p>
 * <p>Description: apk download progress bar </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: All In Pay</p>
 * @author 		Teddy
 * @date 		2013-11-20
 * @version 	
 */
public class DownloadProgressBar extends View {
	
	Context cxt;
	private int width, height;
	private float rate = 0;
	private float rate_t;
	private Bitmap bitmap;
	
	public DownloadProgressBar(Context context) {
		this(context, null);
	}

	public DownloadProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		cxt = context;
		this.setBackgroundResource(R.drawable.gray_bar);
		
		if (bitmap != null) {
			bitmap.recycle();
		}
		
		//set the pure green picture same with backgroud's size
		bitmap = BitmapFactory.decodeResource(cxt.getResources(), R.drawable.green_bar);
		new Canvas();
		new Paint();
	}

	/**
	* @Title: setWH
	* @Description: set progress bar width and height
	* @param @param w width
	* @param @param h height
	* @return void 
	* @throws
	*/
	public void setWH(int w, int h) {
		this.width = w;
		this.height = h;
	}

	/**
	* @Title: setProgress
	* @Description: set progress percentage
	* @param @param rate current percentage
	* @return void 
	* @throws
	*/
	public void setProgress(float rate) {
		if (rate > 99) {
			this.setBackgroundResource(R.drawable.green_bar);
			return;
		}
		this.rate_t = width / 100 * rate;
		
		//Log.i(TAG,"setProgress:rate="+rate+",rate_t:"+rate_t);
		//invalidate((int)rate,0,w,h);
	     invalidate();
	}

	public void clearProgress() {
		rate = 0;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		while (rate <= rate_t) {
			for (int i = 0; i < width / 100; i++) {
				//Log.i(TAG,"rate:"+rate+",rate_t:"+rate_t);
				canvas.drawBitmap(bitmap, rate++, 0, null);
			}
		}
		clipDraw(canvas);
	}

	/**
	* @Title: clipDraw
	* @Description: Display progress with cut area, reduce the times of painting one pixel
	* @param @param canvas
	* @return void 
	* @throws
	*/
	private void clipDraw(Canvas canvas) {
		canvas.save();
		canvas.clipRect(0, 0, rate_t, height);
		canvas.drawBitmap(bitmap, 0, 0, null);
		canvas.restore();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(width, height);
	}
	
}
