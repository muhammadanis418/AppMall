package cn.koolcloud.ipos.appstore.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.HorizontalScrollView;

public class ReboundHorizotalScrollView extends HorizontalScrollView {

	private static final int MAX_X_OVERSCROLL_DISTANCE = 200;
	private Context mContext;
	private int mMaxXOverscrollDistance;

	public ReboundHorizotalScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
		initBounceDistance();
	}

	public ReboundHorizotalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
		initBounceDistance();
	}

	public ReboundHorizotalScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		mContext = context;
		initBounceDistance();
	}

	private void initBounceDistance() {
		final DisplayMetrics metrics = mContext.getResources()
				.getDisplayMetrics();
		mMaxXOverscrollDistance = (int) (metrics.density * MAX_X_OVERSCROLL_DISTANCE);
	}

	@Override
	protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,
			int scrollY, int scrollRangeX, int scrollRangeY,
			int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
		// this important
		return super.overScrollBy(deltaX, deltaY, scrollX, scrollY,
				scrollRangeX, scrollRangeY, mMaxXOverscrollDistance,
				maxOverScrollY, isTouchEvent);
	}

}