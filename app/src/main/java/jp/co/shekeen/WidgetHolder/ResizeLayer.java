package jp.co.shekeen.WidgetHolder;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class ResizeLayer extends FrameLayout {

	private WidgetResizeFrame mTargetFrame;
	private boolean mResizing;
	private int mXDown;
	private int mYDown;
	
	public ResizeLayer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ResizeLayer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public ResizeLayer(ViewGroup parent, int rowCount, boolean smallHeight){
		super(parent.getContext());
		
		Context context = parent.getContext();
		int height;
		if(smallHeight){
			height = context.getResources().getDimensionPixelSize(R.dimen.cell_height_s) * rowCount;
		}
		else{
			height = context.getResources().getDimensionPixelSize(R.dimen.cell_height) * rowCount;
		}
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, height);
		setLayoutParams(params);
		parent.addView(this);
	}
	
	public void addFrame(WidgetResizeFrame frame){
		removeAllViews();
		addView(frame);
		mTargetFrame = frame;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean handled = false;
        int action = event.getAction();

        int x = (int) event.getX();
        int y = (int) event.getY();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
        	if (handleTouchDown(event, false)) {
        		return true;
        	}
        	else{
        		removeAllViews();
        		mTargetFrame = null;
        		mResizing = false;
        		return false;
        	}
        }

        if (mTargetFrame != null && mResizing) {
            handled = true;
            switch (action) {
                case MotionEvent.ACTION_MOVE:
                    mTargetFrame.visualizeResizeForDelta(x - mXDown, y - mYDown);
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    mTargetFrame.visualizeResizeForDelta(x - mXDown, y - mYDown);
                    mTargetFrame.onTouchUp();
                    mResizing = false;
                    break;
            }
        }
        return handled;
	}

	private boolean handleTouchDown(MotionEvent ev, boolean intercept) {
        Rect hitRect = new Rect();
        int x = (int) ev.getX();
        int y = (int) ev.getY();

        if(mTargetFrame == null){
        	return false;
        }
        
        mTargetFrame.getHitRect(hitRect);
        if (hitRect.contains(x, y)) {
        	if (mTargetFrame.beginResizeIfPointInRegion(x - mTargetFrame.getLeft(), y - mTargetFrame.getTop())) {
        		mXDown = x;
        		mYDown = y;
        		mResizing = true;
        		requestDisallowInterceptTouchEvent(true);
        		return true;
        	}
        }
        return false;
	}
	
	public static class LayoutParams extends FrameLayout.LayoutParams {
        public int x, y;

        /**
         * {@inheritDoc}
         */
        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getWidth() {
            return width;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getHeight() {
            return height;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getX() {
            return x;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getY() {
            return y;
        }
    }

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            final FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) child.getLayoutParams();
            if (flp instanceof LayoutParams) {
                final LayoutParams lp = (LayoutParams) flp;
                child.layout(lp.x, lp.y, lp.x + lp.width, lp.y + lp.height);
            }
        }
	}

    
}
