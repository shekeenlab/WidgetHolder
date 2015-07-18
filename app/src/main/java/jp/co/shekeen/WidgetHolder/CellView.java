package jp.co.shekeen.WidgetHolder;

import jp.co.shekeen.WidgetHolder.CellInfo.CellPosition;
import jp.co.shekeen.WidgetHolder.CellInfo.CellRect;
import jp.co.shekeen.WidgetHolder.CellInfo.CellSize;
import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;

public class CellView extends FrameLayout implements OnDragListener, OnLongClickListener, OnClickListener {

	private CellLayout mParent;
	private CellInfo mCellInfo;
	
	public CellView(Context context, AttributeSet attrs, int defStyle) {
		super(context);
	}

	public CellView(Context context, AttributeSet attrs) {
		super(context);
	}
	
	public CellView(CellLayout parent){
		this(parent, new CellInfo());
	}
	
	public CellView(CellLayout parent, CellInfo cellInfo){
		super(parent.getContext());
		mParent = parent;
		mCellInfo = cellInfo;
		Context context = parent.getContext();
		
		setBackgroundResource(R.drawable.cell_bg);
		setOnDragListener(this);
		
		View innerView = cellInfo.createView(mParent);
		if(innerView != null){
			addView(innerView);
		}
		
		ImageView blockerView = new ImageView(context);
		addView(blockerView);
		LayoutParams params = (LayoutParams)blockerView.getLayoutParams();
		params.height = LayoutParams.MATCH_PARENT;
		params.width = LayoutParams.MATCH_PARENT;
		blockerView.setLayoutParams(params);
		blockerView.setOnClickListener(this);
		blockerView.setOnLongClickListener(this);
	}
	
	public boolean isSpacer(){
		return mCellInfo.isSpacer();
	}
	
	public CellInfo getCellInfo(){
		return mCellInfo;
	}
	
	public void setLayoutPosition(int column, int row){
		mParent.addView(this);
		mCellInfo.setPosition(column, row);
		updateLayout();
	}
	
	public void setLayoutPosition(CellPosition pos){
		setLayoutPosition(pos.column, pos.row);
	}
	
	public void updateLayout(){
		if(!mCellInfo.getRect().isValid()){
			return;
		}
		GridLayout.LayoutParams params = (GridLayout.LayoutParams)getLayoutParams();
		params.setGravity(Gravity.FILL);
		
		CellSize cellSize = mParent.getCellSize();
		CellRect rect = mCellInfo.getRect();
		params.columnSpec = GridLayout.spec(rect.column, rect.columnCount);
		params.rowSpec = GridLayout.spec(rect.row, rect.rowCount);
		params.width = cellSize.width * rect.columnCount;
		params.height = cellSize.height * rect.rowCount;
		setLayoutParams(params);
	}
	
	@Override
	public boolean onDrag(View v, DragEvent event) {
		int action = event.getAction();
		
		switch(action){
		case DragEvent.ACTION_DRAG_STARTED:
			break;

		case DragEvent.ACTION_DRAG_ENTERED:
			break;
		
		case DragEvent.ACTION_DRAG_LOCATION:
			break;
			
		case DragEvent.ACTION_DRAG_EXITED:
			break;
			
		case DragEvent.ACTION_DROP:
			onDrop((CellView)event.getLocalState());
			break;
			
		case DragEvent.ACTION_DRAG_ENDED:
			mParent.dragEnd();
			break;
		}
		return true;
	}
	
	private void onDrop(CellView from){
		CellInfo fromInfo = from.getCellInfo();
		CellRect fromRect = fromInfo.getRect();
		CellRect newRect = mCellInfo.getRect();
		
		int columnDelta = newRect.getRight() - fromRect.getRight();
		int rowDelta = newRect.getBottom() - fromRect.getBottom();
		newRect.column = fromRect.column + columnDelta;
		newRect.row = fromRect.row + rowDelta;
		
		newRect.columnCount = fromRect.columnCount;
		newRect.rowCount = fromRect.rowCount;
		
		DebugHelper.print("Replaceable", mParent.isReplaceable(newRect));
		mParent.dropAt(fromInfo, newRect);
	}
	
	@Override
	public boolean onLongClick(View v) {
		if(mCellInfo.isSpacer()){
			return true;
		}
		DragShadowBuilder builder = new MyDragShadowBuilder(this);
		startDrag(null, builder, this, 0);
		mParent.dragStart(this);
		return true;
	}

	private static class MyDragShadowBuilder extends DragShadowBuilder{

		private View mView;
		
		private MyDragShadowBuilder(CellView view){
			super(view);
			mView = view;
		}
		
		@Override
		public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint) {
			int offset = mView.getContext().getResources().getDimensionPixelSize(R.dimen.shadow_offset);
			shadowSize.set(mView.getWidth(), mView.getHeight());
			shadowTouchPoint.set(mView.getWidth() - offset, mView.getHeight() - offset);
		}
		
	}

	@Override
	public void onClick(View v) {
		if(mCellInfo instanceof WidgetInfo){
			mParent.resizeStart(this);
		}
		else{
			mParent.resizeStart(null);
		}
	}
}
