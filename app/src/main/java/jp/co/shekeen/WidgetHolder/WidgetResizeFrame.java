package jp.co.shekeen.WidgetHolder;

import jp.co.shekeen.WidgetHolder.CellInfo.CellPosition;
import jp.co.shekeen.WidgetHolder.CellInfo.CellRect;
import jp.co.shekeen.WidgetHolder.CellInfo.CellSize;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class WidgetResizeFrame extends FrameLayout {

	final float DIMMED_HANDLE_ALPHA = 0f;
	
	private ImageView mLeftHandle;
    private ImageView mRightHandle;
    private ImageView mTopHandle;
    private ImageView mBottomHandle;
    
    private boolean mLeftBorderActive;
    private boolean mRightBorderActive;
    private boolean mTopBorderActive;
    private boolean mBottomBorderActive;
    
    private boolean mHorizontalActive;
    private boolean mVerticalActive;
    
    private int mBaselineWidth;
    private int mBaselineHeight;
    private int mBaselineX;
    private int mBaselineY;
    
    private int mTouchTargetWidth;
    private int mTopTouchRegionAdjustment = 0;
    private int mBottomTouchRegionAdjustment = 0;
    
    private int mDeltaX;
    private int mDeltaY;
    
    private CellLayout mCellLayout;
    private ResizeLayer mResizeLayer;
    private CellSize mCellSize;
    private int mPadding;
    private CellView mCellView;
    
	public WidgetResizeFrame(Context context, AttributeSet attrs, int defStyle) {
		super(context);
	}

	public WidgetResizeFrame(Context context, AttributeSet attrs) {
		super(context);
	}
	
	public WidgetResizeFrame(CellLayout cellLayout, ResizeLayer layer, CellView cellView) {
		super(layer.getContext());
		
		Context context = layer.getContext();
		mCellLayout = cellLayout;
		mResizeLayer = layer;
		mCellView = cellView;
		setBackgroundResource(R.drawable.widget_resize_frame_holo);
        setPadding(0, 0, 0, 0);

        LayoutParams lp;
        mLeftHandle = new ImageView(context);
        mLeftHandle.setImageResource(R.drawable.widget_resize_handle_left);
        lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 
                Gravity.LEFT | Gravity.CENTER_VERTICAL);
        addView(mLeftHandle, lp);

        mRightHandle = new ImageView(context);
        mRightHandle.setImageResource(R.drawable.widget_resize_handle_right);
        lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 
                Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        addView(mRightHandle, lp);

        mTopHandle = new ImageView(context);
        mTopHandle.setImageResource(R.drawable.widget_resize_handle_top);
        lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 
                Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        addView(mTopHandle, lp);

        mBottomHandle = new ImageView(context);
        mBottomHandle.setImageResource(R.drawable.widget_resize_handle_bottom);
        lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 
                Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        addView(mBottomHandle, lp);
        
        CellRect rect = cellView.getCellInfo().getRect();
        mCellSize = new CellSize();
        mPadding = context.getResources().getDimensionPixelSize(R.dimen.resize_frame_padding);
        int width = cellView.getWidth() + mPadding * 2;
        int height = cellView.getHeight() + mPadding * 2;
        mCellSize.width = cellView.getWidth() / rect.columnCount;
        mCellSize.height = cellView.getHeight() / rect.rowCount;
        int x = mCellSize.width * rect.column - mPadding;
        int y = mCellSize.height * rect.row - mPadding;
        ResizeLayer.LayoutParams resize = new ResizeLayer.LayoutParams(width, height);
        resize.x = x;
        resize.y = y;
        setLayoutParams(resize);
        layer.addFrame(this);
        
        mTouchTargetWidth = mPadding * 4;
        
        mHorizontalActive = true;
        mVerticalActive = Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;
        
        mLeftHandle.setAlpha(mHorizontalActive ? 1.0f : DIMMED_HANDLE_ALPHA);
        mRightHandle.setAlpha(mHorizontalActive ? 1.0f :DIMMED_HANDLE_ALPHA);
        mTopHandle.setAlpha(mVerticalActive ? 1.0f : DIMMED_HANDLE_ALPHA);
        mBottomHandle.setAlpha(mVerticalActive ? 1.0f : DIMMED_HANDLE_ALPHA);
	}

	public boolean beginResizeIfPointInRegion(int x, int y) {
        mLeftBorderActive = (x < mTouchTargetWidth) && mHorizontalActive;
        mRightBorderActive = (x > getWidth() - mTouchTargetWidth) && mHorizontalActive;
        mTopBorderActive = (y < mTouchTargetWidth + mTopTouchRegionAdjustment) && mVerticalActive;
        mBottomBorderActive = (y > getHeight() - mTouchTargetWidth + mBottomTouchRegionAdjustment)
                && mVerticalActive;

        /* バカよけ。縦と横が同時にリサイズ可能になるのを防ぐ。もしくは、mBottomTouchRegionAdjustmentを調整すべきか。 */
        if(mRightBorderActive || mLeftBorderActive){
        	mTopBorderActive = false;
        	mBottomBorderActive = false;
        }
        
        boolean anyBordersActive = mLeftBorderActive || mRightBorderActive
                || mTopBorderActive || mBottomBorderActive;

        mBaselineWidth = getMeasuredWidth();
        mBaselineHeight = getMeasuredHeight();
        mBaselineX = getLeft();
        mBaselineY = getTop();

        if (anyBordersActive) {
            mLeftHandle.setAlpha(mLeftBorderActive ? 1.0f : DIMMED_HANDLE_ALPHA);
            mRightHandle.setAlpha(mRightBorderActive ? 1.0f :DIMMED_HANDLE_ALPHA);
            mTopHandle.setAlpha(mTopBorderActive ? 1.0f : DIMMED_HANDLE_ALPHA);
            mBottomHandle.setAlpha(mBottomBorderActive ? 1.0f : DIMMED_HANDLE_ALPHA);
        }
        return anyBordersActive;
    }
	
	public void updateDeltas(int deltaX, int deltaY) {
        if (mLeftBorderActive) {
            mDeltaX = Math.max(-mBaselineX - mPadding, deltaX);
            mDeltaX = Math.min(mBaselineWidth - 2 * mTouchTargetWidth, mDeltaX);
        } else if (mRightBorderActive) {
            mDeltaX = Math.min(mResizeLayer.getWidth() - (mBaselineX + mBaselineWidth) + mPadding, deltaX);
            mDeltaX = Math.max(-mBaselineWidth + 2 * mTouchTargetWidth, mDeltaX);
        }

        if (mTopBorderActive) {
            mDeltaY = Math.max(-mBaselineY - mPadding, deltaY);
            mDeltaY = Math.min(mBaselineHeight - 2 * mTouchTargetWidth, mDeltaY);
        } else if (mBottomBorderActive) {
            mDeltaY = Math.min(mResizeLayer.getHeight() - (mBaselineY + mBaselineHeight) + mPadding, deltaY);
            mDeltaY = Math.max(-mBaselineHeight + 2 * mTouchTargetWidth, mDeltaY);
        }
    }
	
	public void visualizeResizeForDelta(int deltaX, int deltaY) {
        updateDeltas(deltaX, deltaY);
        ResizeLayer.LayoutParams lp = (ResizeLayer.LayoutParams) getLayoutParams();

        if (mLeftBorderActive) {
            lp.x = mBaselineX + mDeltaX;
            lp.width = mBaselineWidth - mDeltaX;
        } else if (mRightBorderActive) {
            lp.width = mBaselineWidth + mDeltaX;
        }

        if (mTopBorderActive) {
            lp.y = mBaselineY + mDeltaY;
            lp.height = mBaselineHeight - mDeltaY;
        } else if (mBottomBorderActive) {
            lp.height = mBaselineHeight + mDeltaY;
        }
        
//        resizeWidgetIfNeeded(onDismiss);
        requestLayout();
    }
	
	public void onTouchUp() {
		mLeftHandle.setAlpha(mHorizontalActive ? 1.0f : DIMMED_HANDLE_ALPHA);
        mRightHandle.setAlpha(mHorizontalActive ? 1.0f :DIMMED_HANDLE_ALPHA);
        mTopHandle.setAlpha(mVerticalActive ? 1.0f : DIMMED_HANDLE_ALPHA);
        mBottomHandle.setAlpha(mVerticalActive ? 1.0f : DIMMED_HANDLE_ALPHA);
        
        CellRect newRect = new CellRect();
        newRect.columnCount = (int)((float)(getWidth() - mPadding * 2) / mCellSize.width + 0.5f);
        newRect.rowCount = (int)((float)(getHeight() - mPadding * 2) / mCellSize.height + 0.5f);
        newRect.column = (int)((float)(getLeft() + mPadding) / mCellSize.width + 0.5f);
        newRect.row = (int)((float)(getTop() + mPadding) / mCellSize.height + 0.5f);
        CellInfo cellInfo = mCellView.getCellInfo();
        if(mCellLayout.isAcceptable(newRect, cellInfo)){
        	mCellLayout.remove(mCellView);
        	cellInfo.resize(newRect.columnCount, newRect.rowCount);
        	CellPosition pos = new CellPosition();
			pos.column = newRect.column;
			pos.row = newRect.row;
			mCellLayout.putAt(pos, cellInfo);
			mCellLayout.updateNotification(cellInfo);
			
			int width = newRect.columnCount * mCellSize.width + mPadding * 2;
	        int height = newRect.rowCount * mCellSize.height + mPadding * 2;
	        ResizeLayer.LayoutParams resize = new ResizeLayer.LayoutParams(width, height);
	        resize.x = newRect.column * mCellSize.width - mPadding;
	        resize.y = newRect.row * mCellSize.height - mPadding;
	        setLayoutParams(resize);
	        requestLayout();
        }
        else{
        	CellRect rect = cellInfo.getRect();
        	int width = rect.columnCount * mCellSize.width + mPadding * 2;
	        int height = rect.rowCount * mCellSize.height + mPadding * 2;
	        ResizeLayer.LayoutParams resize = new ResizeLayer.LayoutParams(width, height);
	        resize.x = rect.column * mCellSize.width - mPadding;
	        resize.y = rect.row * mCellSize.height - mPadding;
	        setLayoutParams(resize);
	        requestLayout();
        }
        
	}
}
