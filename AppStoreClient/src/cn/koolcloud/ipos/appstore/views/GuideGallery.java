package cn.koolcloud.ipos.appstore.views;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Gallery;

import cn.koolcloud.ipos.appstore.fragment.SoftwareDetailMainFragment;

public class GuideGallery extends Gallery {

	private long lastTime = 0;
	private static final int TIME_DIFF = 3000;
	private Timer timer = new Timer();
	private TimerTask timerTask;

	public GuideGallery(Context context) {
		super(context);
	}

	public GuideGallery(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public GuideGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		int kEvent;
		// scroll one pic at a time
		if (isScrollingLeft(e1, e2)) { // Check if scrolling left
			kEvent = KeyEvent.KEYCODE_DPAD_LEFT;
		} else { // Otherwise scrolling right
			kEvent = KeyEvent.KEYCODE_DPAD_RIGHT;
		}
		onKeyDown(kEvent, null);
		long now = new Date().getTime();
		if (TIME_DIFF > now - lastTime) {// 5 seconds
			timer.cancel();
		}
		SoftwareDetailMainFragment.timeFlag = false;
		initTask();
		lastTime = now;
		return true;
	}

	private boolean isScrollingLeft(MotionEvent e1, MotionEvent e2) {
		return e2.getX() > e1.getX();
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return super.onScroll(e1, e2, distanceX, distanceY);
	}

	private void initTask() {
		timer = new Timer();
		timerTask = new TimerTask() {
			public void run() {
				SoftwareDetailMainFragment.timeFlag = true;
			}
		};
		timer.schedule(timerTask, TIME_DIFF);
	}
}
