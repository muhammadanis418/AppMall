package cn.koolcloud.ipos.appstore.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.HorizontalScrollView;

/**
 * <p>Title: ElasticHScrollView.java </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2014</p>
 * <p>Company: All In Pay</p>
 * @author 		Teddy
 * @date 		2014-1-30
 * @version 	
 */
public class ElasticHScrollView extends HorizontalScrollView {

    private View inner;
    private float x;
    private Rect normal = new Rect();
    
    public ElasticHScrollView(Context context) {
        super(context);
    }

    public ElasticHScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    protected void onFinishInflate() {
        if (getChildCount() > 0) {
            inner = getChildAt(0);
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (inner == null) {
            return super.onTouchEvent(ev);
        } else {
            commOnTouchEvent(ev);
        }
        return super.onTouchEvent(ev);
    }
    
    public void commOnTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            x = ev.getX();
            break;
        case MotionEvent.ACTION_UP:
            if (isNeedAnimation()) {
                animation();
            }
            break;
        case MotionEvent.ACTION_MOVE:
            final float preX = x;
            float nowX = ev.getX();
            int deltaX = (int) (preX - nowX);
            // Scroll
            scrollBy(deltaX, 0);
            x = nowX;
            // not scroll on the top or bottom position
            if (isNeedMove()) {
                if (normal.isEmpty()) {
                    // save the normal layout position
                    normal.set(inner.getLeft(), inner.getTop(), inner
                            .getRight(), inner.getBottom());

                }
                // Move layout
                inner.layout(inner.getLeft() - deltaX, inner.getTop(), inner
                        .getRight() - deltaX, inner.getBottom());
            }
            break;
        default:
            break;
        }
    }

    // start moving animation
    public void animation() {
        // start moving animation        
        TranslateAnimation ta=new TranslateAnimation(inner.getLeft(), normal.left, 0, 0);
        ta.setDuration(50);
        inner.startAnimation(ta);
        // set back to normal position
        inner.layout(normal.left, normal.top, normal.right, normal.bottom);
        normal.setEmpty();
    }

    // need start animation or not
    public boolean isNeedAnimation() {
        return !normal.isEmpty();
    }

    // need move layout or not
    public boolean isNeedMove() {
        int offset = inner.getMeasuredWidth() - getWidth();
        int scrollX = getScrollX();
        if (scrollX == 0 || scrollX == offset) {
            return true;
        }
        return false;
    }
}