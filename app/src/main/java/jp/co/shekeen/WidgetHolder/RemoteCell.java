package jp.co.shekeen.WidgetHolder;

import android.content.Context;
import android.widget.RemoteViews;

public class RemoteCell {

	private Context mContext;
	private CellInfo mCellInfo;
	private RemoteViews mRemoteViews;
	
	public RemoteCell(Context context, CellInfo cellInfo){
		mContext = context;
		mCellInfo = cellInfo;
	}
	
	public RemoteCell(Context context, CellInfo cellInfo, RemoteViews remoteViews){
		this(context, cellInfo);
		mRemoteViews = remoteViews;
	}
	
	public int getAppWidgetId(){
		return mCellInfo.getAppWidgetId();
	}
	
	public CellInfo getCellInfo(){
		return mCellInfo;
	}
	
	public boolean isValid(){
		return !mCellInfo.isSpacer();
	}
	
	public void updateView(Context context, RemoteViews remoteViews, boolean icsCompat){/* mCellInfoがWidgetInfoの場合のみ呼ばれる */
		mRemoteViews = remoteViews;
	}
	
	public RemoteViews createFormatedView(){
		return mCellInfo.createRemoteViews(mContext, mRemoteViews);
	}
	
}
