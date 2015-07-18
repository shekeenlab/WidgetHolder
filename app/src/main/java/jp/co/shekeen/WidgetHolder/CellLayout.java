package jp.co.shekeen.WidgetHolder;

import java.util.ArrayList;
import java.util.List;

import jp.co.shekeen.WidgetHolder.CellInfo.CellPosition;
import jp.co.shekeen.WidgetHolder.CellInfo.CellRect;
import jp.co.shekeen.WidgetHolder.CellInfo.CellSize;
import android.appwidget.AppWidgetHost;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;

public class CellLayout extends GridLayout{

	private int mColumnCount;
	private int mRowCount;
	private ViewGroup mParent;
	private Context mContext;
	private List<CellView> mViewList;
	private OnClickListener mClickListener;
	private OnCellLayoutChangedListener mLayoutListener;
	private AppWidgetHost mWidgetHost;
	private boolean[][] mOccupied;
	private CellSize mCellSize;
	private CellView mDragTarget;
	private ResizeLayer mResizeLayer;
	
	public CellLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context);
	}

	public CellLayout(Context context, AttributeSet attrs) {
		super(context);
	}

	public CellLayout(Context context) {
		super(context);
	}
	
	public CellLayout(ViewGroup parent, int columnCount, int rowCount, boolean smallHeight){
		super(parent.getContext());
		mParent = parent;
		mContext = parent.getContext();
		mViewList = new ArrayList<CellView>();
		mColumnCount = columnCount;
		mRowCount = rowCount;
		mOccupied = new boolean[mColumnCount][mRowCount];
		
		int height;
		if(smallHeight){
			height = mContext.getResources().getDimensionPixelSize(R.dimen.cell_height_s) * mRowCount;
		}
		else{
			height = mContext.getResources().getDimensionPixelSize(R.dimen.cell_height) * mRowCount;
		}
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, height);
		setLayoutParams(params);
		setColumnCount(mColumnCount);
		setRowCount(mRowCount);
		mParent.addView(this);
		setBackgroundColor(Color.rgb(31, 34, 37));
		mWidgetHost = new AppWidgetHost(mContext, MyAppWidgetHost.APPWIDGET_HOST_ID);
		
		Point displaySize = new GuiUtil(mContext).getDisplaySize();
		mCellSize = new CellSize();
		mCellSize.width = Math.round((float)displaySize.x / mColumnCount);
		if(smallHeight){
			mCellSize.height = mContext.getResources().getDimensionPixelSize(R.dimen.cell_height_s);
		}
		else{
			mCellSize.height = mContext.getResources().getDimensionPixelSize(R.dimen.cell_height);
		}
		
		for(int i = 0; i < mColumnCount; i++){
			for(int j = 0; j < mRowCount; j++){
				CellView cellView = new CellView(this);
				cellView.setLayoutPosition(i, j);
			}
		}
	}
	
	public void setup(ResizeLayer resizeLayer){
		mResizeLayer = resizeLayer;
	}
	
	public void setOnClickListener(OnClickListener listener){
		mClickListener = listener;
	}
	
	public void setOnUserLayoutListener(OnCellLayoutChangedListener listener){
		mLayoutListener = listener;
	}
	
	public boolean append(CellInfo cellInfo){
		/* 空きが見つからなかった場合には、コラム方向に縮めてトライ */
		CellPosition pos = null;
		while(cellInfo.mColumnCount > 0){
			pos = findEmptySpace(cellInfo);
			if(pos != null){
				break;
			}
			cellInfo.mColumnCount--;
		}
		if(pos == null){
			return false;
		}
		CellView cellView = new CellView(this, cellInfo);
		cellView.setOnClickListener(mClickListener);
		replaceSpacer(pos, cellView);
		return true;
	}
	
	public boolean putAt(CellPosition position, CellInfo cellInfo){
		CellView cellView = new CellView(this, cellInfo);
		cellView.setOnClickListener(mClickListener);
		return putAt(position, cellView);
	}
	
	private boolean putAt(CellPosition position, CellView cellView){
		replaceSpacer(position, cellView);
		return true;
	}
	
	public void remove(CellView cellView){
		mViewList.remove(cellView);
		removeView(cellView);
		CellRect rect = cellView.getCellInfo().getRect();
		for(int i = rect.column; i < rect.column + rect.columnCount; i++){
			for(int j = rect.row; j < rect.row + rect.rowCount; j++){
				mOccupied[i][j] = false;
				CellView spacer = new CellView(this);
				spacer.setLayoutPosition(i, j);
			}
		}
	}
	
	
	public void remove(int appWidgetId){
		for(CellView cellView : mViewList){
			CellInfo info = cellView.getCellInfo();
			if(info.getAppWidgetId() == appWidgetId){
				remove(cellView);
				return;
			}
		}
	}
	
	public boolean contains(View view){
		return mViewList.contains(view);
	}
	
	
	public boolean contains(int appWidgetId){
		int size = mViewList.size();
		for(int i = 0; i < size; i++){
			CellInfo cellInfo = mViewList.get(i).getCellInfo();
			if(cellInfo.getAppWidgetId() == appWidgetId){
				return true;
			}
		}
		return false;
	}
	
	private CellPosition findEmptySpace(CellInfo info){
		int column = info.getColumnCount();
		int row = info.getRowCount();
		
		for(int r = 0; r < mRowCount - row + 1; r++){
			for(int c = 0; c < mColumnCount - column + 1; c++){
				boolean found = true;
				for(int x = c; x < c + column; x++){
					for(int y = r; y < r + row; y++){
						if(mOccupied[x][y]){
							found = false;
							break;
						}
					}
				}
				if(found){
					return new CellPosition(c, r);
				}
			}
			
		}
		
		return null;
	}
	
	public boolean isReplaceable(CellRect newRect){
		if(!newRect.isValid()){
			return false;
		}
		if(newRect.getRight() > mColumnCount || newRect.getBottom() > mRowCount){
			return false;
		}
		for(CellView view : mViewList){
			CellRect rect = view.getCellInfo().getRect();
			if(rect.equals(newRect)){
				continue;
			}
			else if(rect.contains(newRect)){
				return false;
			}
			else if(newRect.intersects(rect)){
				return false;
			}
		}
		return true;
	}
	
	public boolean dropAt(CellInfo info, CellRect newRect){
		CellRect oldRect = info.getRect();
		if(oldRect.equals(newRect)){
			return false;
		}
		if(!isReplaceable(newRect)){
			return false;
		}
		List<CellInfo> updateList = new ArrayList<CellInfo>();
		CellView[] viewArray = mViewList.toArray(new CellView[0]);
		for(CellView view: viewArray){
			CellRect rect = view.getCellInfo().getRect();
			if(newRect.contains(rect)){
				remove(view);
				CellPosition pos = getReplacedPosition(rect, newRect, oldRect);
				putAt(pos, view);
				updateList.add(view.getCellInfo());
			}
		}
		putAt(new CellPosition(newRect.column, newRect.row), info);
		updateList.add(info);
		updateNotification(updateList.toArray(new CellInfo[0]));
		dragDrop();
		return true;
	}
	
	private CellPosition getReplacedPosition(CellRect rect, CellRect newRect, CellRect oldRect){
		if(newRect.shares(oldRect)){
			DebugHelper.print("INTERSECTS");
			newRect = new CellRect(newRect);
			oldRect = new CellRect(oldRect);
			/* 2列しかないため、交差するのはカラム方向のみ */
			if(newRect.column > oldRect.column){
				excludeAndArea(newRect, oldRect);
			}
			else if(newRect.column < oldRect.column){
				excludeAndArea(oldRect, newRect);
			}
		}
		int columnDelta = rect.column - newRect.column;
		int rowDelta = rect.row - newRect.row;
		CellPosition position = new CellPosition();
		position.column = oldRect.column + columnDelta;
		position.row = oldRect.row + rowDelta;
		return position;
	}
	
	private void excludeAndArea(CellRect largerRect, CellRect smallerRect){
		int columnCount = smallerRect.columnCount;
		smallerRect.columnCount = largerRect.column - smallerRect.column;
		int column = largerRect.column;
		largerRect.column = smallerRect.column + columnCount;
		largerRect.columnCount -= largerRect.column - column;
	}
	
	public boolean isAcceptable(CellRect rect, CellInfo ignore){
		CellRect ignoreRect = ignore.getRect();
		boolean[][] occupied = mOccupied.clone();
		for(int i = ignoreRect.column; i < ignoreRect.getRight(); i++){
			for(int j = ignoreRect.row; j < ignoreRect.getBottom(); j++){
				occupied[i][j] = false;
			}
		}
		if(!rect.isValid()){
			return false;
		}
		for(int i = rect.column; i < rect.getRight(); i++){
			for(int j = rect.row; j < rect.getBottom(); j++){
				if(i >= mColumnCount || j >= mRowCount){
					return false;
				}
				if(occupied[i][j]){
					return false;
				}
			}
		}
		return true;
	}
	
	private void replaceSpacer(CellPosition position, CellView cellView){
		CellInfo info = cellView.getCellInfo();
		info.setPosition(position);
		CellRect rect = info.getRect();
		if(!rect.isValid()){
			return;
		}
		for(int i = rect.column; i < rect.column + rect.columnCount; i++){
			for(int j = rect.row; j < rect.row + rect.rowCount; j++){
				mOccupied[i][j] = true;
				for(CellView view : mViewList){
					CellPosition pos = view.getCellInfo().getPosition();
					if(pos.column == i && pos.row == j){
						removeView(view);
						mViewList.remove(view);
						break;
					}
				}
			}
			
		}
		cellView.setLayoutPosition(position);
		mViewList.add(cellView);
	}
	
	public AppWidgetHost getAppWidgetHost(){
		return mWidgetHost;
	}
	
	public CellSize getCellSize(){
		return mCellSize;
	}
	
	public CellInfo[] getCellInfos(){
		CellInfo[] infos = new CellInfo[mViewList.size()];
		for(int i = 0; i < mViewList.size(); i++){
			infos[i] = mViewList.get(i).getCellInfo();
		}
		return infos;
	}
	
	public void dragStart(CellView target){
		mDragTarget = target;
		remove(target);
		if(mLayoutListener != null){
			mLayoutListener.onWidgetSelected(null);
		}
	}
	
	public void dragDrop(){
		mDragTarget = null;
	}
	
	public void dragEnd(){
		if(mDragTarget == null){
			return;
		}
		CellPosition pos = mDragTarget.getCellInfo().getPosition();
		putAt(pos, mDragTarget);
		mDragTarget = null;
	}
	
	public void resizeStart(CellView target){
		if(target != null){
			new WidgetResizeFrame(this, mResizeLayer, target);
		}
		if(mLayoutListener != null){
			CellInfo cellInfo = target != null ? target.getCellInfo() : null;
			mLayoutListener.onWidgetSelected(cellInfo);
		}
	}
	
	public void updateWidgetView(int appWidgetId){
		CellView target = null;
		for(CellView cell : mViewList){
			CellInfo info = cell.getCellInfo();
			if(info.getAppWidgetId() == appWidgetId){
				target = cell;
				break;
			}
		}
		
		if(target != null){
			mViewList.remove(target);
			removeView(target);
			CellView cellView = new CellView(this, target.getCellInfo());
			cellView.setOnClickListener(mClickListener);
			CellPosition position = target.getCellInfo().getPosition();
			cellView.setLayoutPosition(position);
			mViewList.add(cellView);
		}
		
	}
	
	/* THIS FUNCTION IS CALLED BY CELLVIEW ONLY */
	public void updateNotification(CellInfo cellInfo){
		if(mLayoutListener != null){
			mLayoutListener.onCellLayoutChanged(new CellInfo[]{ cellInfo });
		}
	}
	
	private void updateNotification(CellInfo[] cellInfos){
		if(mLayoutListener != null){
			mLayoutListener.onCellLayoutChanged(cellInfos);
		}
	}
	
	public static interface OnCellLayoutChangedListener{
		public void onCellLayoutChanged(CellInfo[] cellInfos);
		public void onWidgetSelected(CellInfo cellInfo);
	}
}
