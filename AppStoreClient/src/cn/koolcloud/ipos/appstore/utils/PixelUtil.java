package cn.koolcloud.ipos.appstore.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

public class PixelUtil {
	/**
	 * dip to px
	 */
	public static int dip2px(Context context, float dpValue) {

		final float scale = context.getResources().getDisplayMetrics().density;

		return (int) (dpValue * scale + 0.5f);

	}

	/**
	 * px to dip
	 */
	public static int px2dip(Context context, float pxValue) {

		final float scale = context.getResources().getDisplayMetrics().density;

		return (int) (pxValue / scale + 0.5f);

	}
	
	/**
	 * px,dip,sp -> px
	 * 
	 * @param unit  TypedValue.COMPLEX_UNIT_*
	 * @param size
	 * @return
	 */
	public static float getRawSize(Context context, int unit, float size) {
	       Resources r;
	       if (context == null)
	           r = Resources.getSystem();
	       else
	           r = context.getResources();
	        
	       return TypedValue.applyDimension(unit, size, r.getDisplayMetrics());
	}
}
