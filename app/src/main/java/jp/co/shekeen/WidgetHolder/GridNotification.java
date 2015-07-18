package jp.co.shekeen.WidgetHolder;

import java.util.ArrayList;
import java.util.List;

import jp.co.shekeen.WidgetHolder.CellInfo.CellRect;

import android.content.Context;
import android.os.Build;
import android.widget.RemoteViews;

public class GridNotification {

	private static final int BASE_ID_LIST_SINGLE[] = new int[]{/* LayoutID競合エラー回避のため後ろから使っていく。 */
		R.id.layoutBase12, R.id.layoutBase11, R.id.layoutBase10, R.id.layoutBase9, R.id.layoutBase8, 
		R.id.layoutBase7, R.id.layoutBase6, R.id.layoutBase5, R.id.layoutBase4, R.id.layoutBase3, 
		R.id.layoutBase2, R.id.layoutBase1
	};
	
	private static final int BASE_ID_LIST_DUAL[] = new int[]{/* LayoutID競合エラー回避のため後ろから使っていく。 */
		R.id.layoutBase24, R.id.layoutBase23, R.id.layoutBase22, R.id.layoutBase21, R.id.layoutBase20, 
		R.id.layoutBase19, R.id.layoutBase18, R.id.layoutBase17, R.id.layoutBase16, R.id.layoutBase15, 
		R.id.layoutBase14, R.id.layoutBase13, R.id.layoutBase12, R.id.layoutBase11, R.id.layoutBase10,
		R.id.layoutBase9, R.id.layoutBase8, R.id.layoutBase7, R.id.layoutBase6, R.id.layoutBase5,
		R.id.layoutBase4, R.id.layoutBase3, R.id.layoutBase2, R.id.layoutBase1
	};
	
	private static final int BASE_ID_LIST_TRIPLE[] = new int[]{/* LayoutID競合エラー回避のため後ろから使っていく。 */
		R.id.layoutBase36, R.id.layoutBase35, R.id.layoutBase34, R.id.layoutBase33, R.id.layoutBase32,
		R.id.layoutBase31, R.id.layoutBase30, R.id.layoutBase29, R.id.layoutBase28, R.id.layoutBase27,
		R.id.layoutBase26, R.id.layoutBase25, R.id.layoutBase24, R.id.layoutBase23, R.id.layoutBase22,
		R.id.layoutBase21, R.id.layoutBase20, R.id.layoutBase19, R.id.layoutBase18, R.id.layoutBase17,
		R.id.layoutBase16, R.id.layoutBase15, R.id.layoutBase14, R.id.layoutBase13, R.id.layoutBase12,
		R.id.layoutBase11, R.id.layoutBase10, R.id.layoutBase9, R.id.layoutBase8, R.id.layoutBase7,
		R.id.layoutBase6, R.id.layoutBase5, R.id.layoutBase4, R.id.layoutBase3, R.id.layoutBase2, R.id.layoutBase1
	};
	
	private static final int[] CHILD_ID_LIST_DUAL = new int[]{
		R.layout.child1, R.layout.child2, R.layout.child3, R.layout.child4, R.layout.child5,
		R.layout.child6, R.layout.child7, R.layout.child8, R.layout.child9, R.layout.child10,
		R.layout.child11, R.layout.child12
		};
	private static final int[] CHILD_ID_LIST_DUAL_SMALL = new int[]{
		R.layout.child1_s, R.layout.child2_s, R.layout.child3_s, R.layout.child4_s, R.layout.child5_s,
		R.layout.child6_s, R.layout.child7_s, R.layout.child8_s, R.layout.child9_s, R.layout.child10_s,
		R.layout.child11_s, R.layout.child12_s
		};
	
	private static final int[] CHILD_ID_LIST_TRIPLE = new int[]{
		R.layout.child_3_1, R.layout.child_3_2, R.layout.child_3_3, R.layout.child_3_4, R.layout.child_3_5,
		R.layout.child_3_6, R.layout.child_3_7, R.layout.child_3_8, R.layout.child_3_9, R.layout.child_3_10,
		R.layout.child_3_11, R.layout.child_3_12
	};
	
	private static final int[] CHILD_ID_LIST_TRIPLE_UP = new int[]{
		R.layout.child_3_1_up, R.layout.child_3_2_up, R.layout.child_3_3_up, R.layout.child_3_4_up, R.layout.child_3_5_up,
		R.layout.child_3_6_up, R.layout.child_3_7_up, R.layout.child_3_8_up, R.layout.child_3_9_up, R.layout.child_3_10_up,
		R.layout.child_3_11_up, R.layout.child_3_12_up
	};
	
	private static final int[] CHILD_ID_LIST_TRIPLE_DOWN = new int[]{
		R.layout.child_3_1_down, R.layout.child_3_2_down, R.layout.child_3_3_down, R.layout.child_3_4_down, R.layout.child_3_5_down,
		R.layout.child_3_6_down, R.layout.child_3_7_down, R.layout.child_3_8_down, R.layout.child_3_9_down, R.layout.child_3_10_down,
		R.layout.child_3_11_down, R.layout.child_3_12_down
	};
	
	private static final int[] LINE_ID_LIST = new int[]{ R.id.layoutLine1, R.id.layoutLine2, R.id.layoutLine3 };
	
	private List<RemoteCell> mCellList;
	private Context mContext;
	private int mRowCount;
	private int mColumnCount;
	private boolean mIcsCompat;
	private boolean mShowSmaller;
	
	/* そのときの設定値でGridNotificationを生成する。設定は自動的に反映されることはない */
	public GridNotification(Context context, SettingLoader settingLoader){
		this(context, settingLoader.getRowCount(), settingLoader.getColumnCount(), 
				settingLoader.getIcsCompat(), settingLoader.getSmaller());
		
		updateAllView(settingLoader);
	}
	
	private GridNotification(Context context, int rowCount, int columnCount, boolean icsCompat, boolean showSmaller){
		mContext = context;
		mCellList = new ArrayList<RemoteCell>();
		mRowCount = rowCount;
		mColumnCount = columnCount;
		mIcsCompat = icsCompat;
		mShowSmaller = showSmaller;
		CellInfo[] infos = SettingResolver.loadFromDatabase(context);
		
		for(CellInfo info : infos){
			CellRect rect = info.getRect();
			if(rect.column >= mColumnCount || rect.row >= mRowCount){/* アイテムの左上座標が範囲外ならDBから削除する */
				SettingResolver.deleteItemFromDatabase(context, info);
			}
			else{
				boolean update = false;
				if(rect.getRight() > mColumnCount && rect.columnCount > 1){
					info.resize(mColumnCount - rect.column, rect.rowCount);
					update = true;
				}
				if(rect.getBottom() > mRowCount && rect.rowCount > 1){
					info.resize(rect.columnCount, mRowCount - rect.row);
					update = true;
				}
				if(update){
					SettingResolver.updateItemInDatabase(mContext, info);
				}
				mCellList.add(new RemoteCell(context, info));
			}
		}
	}
	
	public void updateAllView(SettingLoader settingLoader){
		AppWidgetServiceIF service = new AppWidgetServiceIF(mContext);
		for(RemoteCell cell : mCellList){
			RemoteViews remoteViews = service.getAppWidgetViews(cell.getAppWidgetId());
			if(settingLoader != null){
				cell.getCellInfo().applySetting(settingLoader);/* RemoteViewsを生成する前に設定を更新する */
			}
			cell.updateView(mContext, remoteViews, mIcsCompat);
		}
	}
	
	private void updateAllView(){
		updateAllView(null);
	}
	
	public void updateView(int appWidgetId, RemoteViews remoteViews){
		RemoteCell cellInfo = find(appWidgetId);
		if(cellInfo == null){
			return;
		}
		cellInfo.updateView(mContext, remoteViews, mIcsCompat);
	}
	
	public boolean contains(int appWidgetId){
		for(RemoteCell cell : mCellList){
			if(cell.getAppWidgetId() == appWidgetId){
				return true;
			}
		}
		return false;
	}
	
	private RemoteCell find(int appWidgetId){
		for(RemoteCell cell : mCellList){
			if(cell.getAppWidgetId() == appWidgetId){
				return cell;
			}
		}
		return null;
	}
	
	private boolean hasContent(){
		for(RemoteCell cell : mCellList){
			if(cell.isValid()){
				return true;
			}
		}
		return false;
	}
	
	public RemoteViews[] createFormatedView(){
		if(!hasContent()){
			return createNullView();
		}
		RemoteCell[][] occupied = new RemoteCell[mRowCount][mColumnCount];/* ROWとCOLUMNが逆であることに注意 */
		for(RemoteCell cell : mCellList){
			CellRect rect = cell.getCellInfo().getRect();
			for(int i = rect.row; i < rect.getBottom(); i++){
				for(int j = rect.column; j < rect.getRight(); j++){
					occupied[i][j] = cell;
				}
			}
		}
		if(mIcsCompat){
			return createViewForICS(occupied);
		}
		else{
			return createViewForJB(occupied);
		}
	}
	
	private RemoteViews[] createViewForICS(RemoteCell[][] occupied){
		RemoteViews[] notifList = new RemoteViews[mRowCount];
		for(int i = 0; i < mRowCount; i++){
			notifList[i] = createSingleLineNotificationForICS(occupied[i]);
		}
		GuiUtil guiUtil = new GuiUtil(mContext);
		/* ICSタブレットの場合、行を入れ替える */
		if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 && guiUtil.isTablet()){
			RemoteViews[] tmpList = new RemoteViews[mRowCount];
			for(int i = 0; i < mRowCount; i++){
				tmpList[i] = notifList[mRowCount - i - 1];
			}
			notifList = tmpList;
		}
		return notifList;
	}
	
	private RemoteViews[] createViewForJB(RemoteCell[][] occupied){
		RemoteViews[] notifList = new RemoteViews[mRowCount];
		notifList[0] = createJbNotification(occupied);/* とりあえず２行以上にならない前提 */
		return notifList;
	}
	
	private RemoteViews createJbNotificationDirect(RemoteCell[][] remoteCells){
		if(remoteCells.length == 1){
			return createSingleLineNotification(remoteCells[0]);
		}
		else if(remoteCells.length == 2){
			return createDualLineNotification(remoteCells);
		}
		else if(remoteCells.length == 3){
			return createTripleNotification(remoteCells);
		}
		return null;
	}
	
	private RemoteViews createJbNotification(RemoteCell[][] remoteCells){
		ArrayList<RemoteCell[]> usedList = new ArrayList<RemoteCell[]>();
		for(int i = 0; i < remoteCells.length; i++){
			for(RemoteCell cell : remoteCells[i]){
				if(cell != null){
					usedList.add(remoteCells[i]);
					break;
				}
			}
		}
		RemoteCell[][] usedRows = usedList.toArray(new RemoteCell[0][]);
		return createJbNotificationDirect(usedRows);
	}
	
	private RemoteViews createSingleLineNotificationForICS(RemoteCell[] remoteCells){
		boolean content = false;
		for(RemoteCell cell : remoteCells){
			if(cell != null){
				content = true;
				break;
			}
		}
		if(!content){
			return null;
		}
		return createSingleLineNotification(remoteCells);
	}
	
	private RemoteViews createSingleLineNotification(RemoteCell[] remoteCells){
		RemoteViews notificationViews;
		if(mShowSmaller){
			notificationViews = new RemoteViews(mContext.getPackageName(), R.layout.notification1_s);
		}
		else{
			notificationViews = new RemoteViews(mContext.getPackageName(), R.layout.notification1);
		}
		
		/* 各BASEに格納されているアイテムを削除する */
		for(int baseId : BASE_ID_LIST_SINGLE){
			notificationViews.removeAllViews(baseId);
		}
		
		ArrayList<RemoteCell> usedList = new ArrayList<RemoteCell>();/* 将来的に不要 */
		int baseId = 0;
		for(int col = 0; col < remoteCells.length; col++){
			RemoteCell cell = remoteCells[col];
			if(cell != null && !usedList.contains(cell)){
				addSingleWrappedViews(notificationViews, BASE_ID_LIST_SINGLE[baseId], cell, remoteCells.length);
				baseId++;
				usedList.add(cell);
			}
		}
		
		return notificationViews;
	}
	
	private void addSingleWrappedViews(RemoteViews baseViews, int targetBase, RemoteCell remoteCell, int widthCount){
		CellRect rect = remoteCell.getCellInfo().getRect();
		addSingleWrappedViews(baseViews, targetBase, remoteCell.createFormatedView(), rect, widthCount);
	}
	
	private void addSingleWrappedViews(RemoteViews baseViews, int targetBase, RemoteViews addViews, CellRect rect, int widthCount){
		for(int i = 0; i < widthCount; i++){
			if(i < rect.column){
				baseViews.addView(targetBase, new RemoteViews(mContext.getPackageName(), R.layout.blank));
			}
			else if(i == rect.column){
				baseViews.addView(targetBase, addViews);
			}
			else if(i >= rect.column + rect.columnCount){
				baseViews.addView(targetBase, new RemoteViews(mContext.getPackageName(), R.layout.blank));
			}
		}
	}
	
	private RemoteViews createDualLineNotification(RemoteCell[][] remoteCells){
		RemoteViews notificationViews;
		if(mShowSmaller){
			notificationViews = new RemoteViews(mContext.getPackageName(), R.layout.notification2_s);
		}
		else{
			notificationViews = new RemoteViews(mContext.getPackageName(), R.layout.notification2);
		}
		
		/* 各BASEに格納されているアイテムを削除する */
		for(int baseId : BASE_ID_LIST_DUAL){
			notificationViews.removeAllViews(baseId);
		}
		
		ArrayList<RemoteCell> usedList = new ArrayList<RemoteCell>();
		int baseId = 0;
		for(int i = 0; i < remoteCells[0].length; i++){
			for(int j = 0; j < remoteCells.length; j++){
				RemoteCell cell = remoteCells[j][i];
				if(cell != null && !usedList.contains(cell)){
					addDualWrappedViews(notificationViews, BASE_ID_LIST_DUAL[baseId], cell, remoteCells[0].length);
					baseId++;
					usedList.add(cell);
				}
			}
		}
		
		return notificationViews;
	}
	
	private void addDualWrappedViews(RemoteViews baseViews, int targetBase, RemoteCell remoteCell, int widthCount){
		CellRect rect = remoteCell.getCellInfo().getRect();
		if(rect.rowCount == 2){
			/* 2行を大きな1行としてレイアウトすればよい。 */
			addSingleWrappedViews(baseViews, targetBase, remoteCell, widthCount);
		}
		else if(rect.rowCount == 1){
			RemoteViews remoteViews;
			if(mShowSmaller){
				remoteViews = new RemoteViews(mContext.getPackageName(), CHILD_ID_LIST_DUAL_SMALL[rect.columnCount - 1]);
			}
			else{
				remoteViews = new RemoteViews(mContext.getPackageName(), CHILD_ID_LIST_DUAL[rect.columnCount - 1]);
			}
			remoteViews.addView(LINE_ID_LIST[rect.row], remoteCell.createFormatedView());
			addSingleWrappedViews(baseViews, targetBase, remoteViews, rect, widthCount);
		}
	}
	
	private RemoteViews createTripleNotification(RemoteCell[][] remoteCells){
		RemoteViews notificationViews = new RemoteViews(mContext.getPackageName(), R.layout.notification3_s);
		/* 各BASEに格納されているアイテムを削除する */
		for(int baseId : BASE_ID_LIST_TRIPLE){
			notificationViews.removeAllViews(baseId);
		}
		
		ArrayList<RemoteCell> usedList = new ArrayList<RemoteCell>();
		int baseId = 0;
		for(int i = 0; i < remoteCells[0].length; i++){
			for(int j = 0; j < remoteCells.length; j++){
				RemoteCell cell = remoteCells[j][i];
				if(cell != null && !usedList.contains(cell)){
					addTripleWrappedViews(notificationViews, BASE_ID_LIST_TRIPLE[baseId], cell, remoteCells[0].length);
					baseId++;
					usedList.add(cell);
				}
			}
		}
		
		return notificationViews;
	}
	
	private void addTripleWrappedViews(RemoteViews baseViews, int targetBase, RemoteCell remoteCell, int widthCount){
		CellRect rect = remoteCell.getCellInfo().getRect();
		if(rect.rowCount == 3){
			/* 3行を大きな1行としてレイアウトすればよい。 */
			addSingleWrappedViews(baseViews, targetBase, remoteCell, widthCount);
		}
		else if(rect.rowCount == 2){
			RemoteViews remoteViews;
			if(rect.row == 0){
				remoteViews = new RemoteViews(mContext.getPackageName(), CHILD_ID_LIST_TRIPLE_UP[rect.columnCount - 1]);
			}
			else{/* rect.row == 1 */
				remoteViews = new RemoteViews(mContext.getPackageName(), CHILD_ID_LIST_TRIPLE_DOWN[rect.columnCount - 1]);
			}
			remoteViews.addView(LINE_ID_LIST[rect.row], remoteCell.createFormatedView());
			addSingleWrappedViews(baseViews, targetBase, remoteViews, rect, widthCount);
		}
		else if(rect.rowCount == 1){
			RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), CHILD_ID_LIST_TRIPLE[rect.columnCount - 1]);
			remoteViews.addView(LINE_ID_LIST[rect.row], remoteCell.createFormatedView());
			addSingleWrappedViews(baseViews, targetBase, remoteViews, rect, widthCount);
		}
	}
	
	public void remove(int appWidgetId){
		for(RemoteCell cell : mCellList){
			if(cell.getAppWidgetId() == appWidgetId){
				mCellList.remove(cell);
				return;
			}
		}
	}
	
	public CellInfo[] getCellInfos(){
		int size = mCellList.size();
		CellInfo[] cellInfos = new CellInfo[size];
		for(int i = 0; i < size; i++){
			cellInfos[i] = mCellList.get(i).getCellInfo();
		}
		return cellInfos;
	}

	public void addCellInfo(CellInfo cellInfo){
		mCellList.add(new RemoteCell(mContext, cellInfo));
		/* DBIDを事前に取得しているケースに対応。今後addの方は廃止されるはず。 */
		if(cellInfo.getDbId() == CellInfo.INVALID_DB_ID){
			SettingResolver.addItemToDatabase(mContext, cellInfo);
		}
		else{
			SettingResolver.updateItemInDatabase(mContext, cellInfo);
		}
		updateAllView();
	}
	
	public void updateCellInfos(CellInfo[] cellInfos){
		for(CellInfo cellInfo : cellInfos){
			RemoteCell cell = findByDbId(cellInfo.getDbId());
			if(cell == null){
				continue;
			}
			mCellList.remove(cell);
			mCellList.add(new RemoteCell(mContext, cellInfo));
			SettingResolver.updateItemInDatabase(mContext, cellInfo);
		}
	}
	
	public void deleteCellInfo(CellInfo cellInfo){
		RemoteCell cell = findByDbId(cellInfo.getDbId());
		if(cell == null){
			DebugHelper.print("findByDbId FAILED");
			return;
		}
		mCellList.remove(cell);
		SettingResolver.deleteItemFromDatabase(mContext, cellInfo);
		if(cellInfo.getAppWidgetId() > 0){
			BitmapResolver.deleteItemFromDatabase(mContext, cellInfo.getDbId());
		}
		updateAllView();
	}
	
	private RemoteCell findByDbId(long id){
		for(RemoteCell cell : mCellList){
			if(cell.getCellInfo().getDbId() == id){
				return cell;
			}
		}
		return null;
	}
	
	private RemoteViews[] createNullView(){
		if(mIcsCompat){
			return createNullViewForICS();
		}
		else{
			return createNullViewForJB();
		}
	}
	
	private RemoteViews[] createNullViewForICS(){
		return new RemoteViews[mRowCount];
	}
	
	private RemoteViews[] createNullViewForJB(){
		return new RemoteViews[mRowCount];
	}
}
