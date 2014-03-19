package cn.koolcloud.ipos.appstore.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

public class ShadeImageView extends ImageView {
	
	private Animation animation;
	private boolean isAnimation = true;

	public ShadeImageView(Context context) {
		super(context);
	}

	public ShadeImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initAnimition();
	}

	public ShadeImageView(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
		
	}
	
	private void initAnimition() {
	    this.animation = new AlphaAnimation(0.0F, 1.0F);
	    this.animation.setDuration(500L);
	}
	
	public void setupAnimation(boolean paramBoolean) {
	    this.isAnimation = paramBoolean;
	}

	protected void onAttachedToWindow() {
	    super.onAttachedToWindow();
	    if (this.isAnimation) {
	    	if (animation == null) {
	    		initAnimition();
	    	}
	    	startAnimation(animation);
	    }
	}

}
