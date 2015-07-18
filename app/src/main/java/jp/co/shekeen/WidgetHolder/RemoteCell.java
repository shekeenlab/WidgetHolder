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
		/* RemoteCellの責務はRemoteViewsをServiceに閉じて管理すること */
		if(icsCompat){
			DebugHelper.print("EXPERIMENTAL WIDGET");
			if(mCellInfo instanceof WidgetInfo && remoteViews != null){
				WidgetInfo widgetInfo = (WidgetInfo) mCellInfo;
				if(widgetInfo.optimizeRemoteViews(context, mRemoteViews, remoteViews) == false){
					mRemoteViews = remoteViews;
				}
			}
		}
		else{
			DebugHelper.print("NORMAL WIDGET");
			/* 動作確認を十分行った後に以下のコードを有効にする */
//			if(mCellInfo instanceof WidgetInfo && remoteViews != null){
//				WidgetInfo widgetInfo = (WidgetInfo) mCellInfo;
//				widgetInfo.replaceBitmap(context, mRemoteViews);
//			}
			mRemoteViews = remoteViews;
		}
	}
	
	public RemoteViews createFormatedView(){
		return mCellInfo.createRemoteViews(mContext, mRemoteViews);
	}
	
}
