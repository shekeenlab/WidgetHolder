package jp.co.shekeen.WidgetHolder;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.RemoteViews;

public class CellInfo implements Parcelable {

	public static final long INVALID_DB_ID = -1;
	
	protected static final int HOLDER_ID_LIST[] = new int[]{
		R.layout.holder1_1, R.layout.holder1_2, R.layout.holder1_3, R.layout.holder1_4, R.layout.holder1_5,
		R.layout.holder1_6, R.layout.holder1_7, R.layout.holder1_8, R.layout.holder1_9, R.layout.holder1_10,
		R.layout.holder1_11, R.layout.holder1_12
	};
	protected static final int SHRINK_ID_LIST[] = new int[]{
		R.layout.shrink5, R.layout.shrink6, R.layout.shrink7, R.layout.shrink8, R.layout.shrink9,
		R.layout.shrink10, R.layout.shrink11, R.layout.shrink12, R.layout.shrink13, R.layout.shrink14,
		R.layout.shrink15, R.layout.shrink16, R.layout.shrink17, R.layout.shrink18, R.layout.shrink19,
		R.layout.shrink20, R.layout.shrink21, R.layout.shrink22, R.layout.shrink23, R.layout.shrink24,
		R.layout.shrink25, R.layout.shrink26, R.layout.shrink27, R.layout.shrink28, R.layout.shrink29,
		R.layout.shrink30, R.layout.shrink31, R.layout.shrink32, R.layout.shrink33, R.layout.shrink34,
		R.layout.shrink35, R.layout.shrink36, R.layout.shrink37, R.layout.shrink38, R.layout.shrink39,
		R.layout.shrink40, R.layout.shrink41, R.layout.shrink42, R.layout.shrink43, R.layout.shrink44,
		R.layout.shrink45, R.layout.shrink46, R.layout.shrink47, R.layout.shrink48, R.layout.shrink49,
		R.layout.shrink50, R.layout.shrink51, R.layout.shrink52, R.layout.shrink53, R.layout.shrink54,
		R.layout.shrink55, R.layout.shrink56, R.layout.shrink57, R.layout.shrink58, R.layout.shrink59,
		R.layout.shrink60, R.layout.shrink61, R.layout.shrink62, R.layout.shrink63, R.layout.shrink64,
		R.layout.shrink65, R.layout.shrink66, R.layout.shrink67, R.layout.shrink68, R.layout.shrink69,
		R.layout.shrink70, R.layout.shrink71, R.layout.shrink72, R.layout.shrink73, R.layout.shrink74,
		R.layout.shrink75, R.layout.shrink76, R.layout.shrink77, R.layout.shrink78, R.layout.shrink79,
		R.layout.shrink80, R.layout.shrink81, R.layout.shrink82, R.layout.shrink83, R.layout.shrink84,
		R.layout.shrink85, R.layout.shrink86, R.layout.shrink87, R.layout.shrink88, R.layout.shrink89,
		R.layout.shrink90, R.layout.shrink91, R.layout.shrink92, R.layout.shrink93, R.layout.shrink94,
		R.layout.shrink95, R.layout.shrink96, R.layout.shrink97, R.layout.shrink98, R.layout.shrink99,
		R.layout.shrink100, R.layout.shrink101, R.layout.shrink102, R.layout.shrink103, R.layout.shrink104,
		R.layout.shrink105, R.layout.shrink106, R.layout.shrink107, R.layout.shrink108, R.layout.shrink109,
		R.layout.shrink110, R.layout.shrink111, R.layout.shrink112, R.layout.shrink113, R.layout.shrink114,
		R.layout.shrink115, R.layout.shrink116, R.layout.shrink117, R.layout.shrink118, R.layout.shrink119,
		R.layout.shrink120
	};
	protected static final int SHRINK_SIZE_LIST[] = new int[]{
		5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
		15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
		25, 26, 27, 28, 29, 30, 31, 32, 33, 34,
		35, 36, 37, 38, 39, 40, 41, 42, 43, 44,
		45, 46, 47, 48, 49, 50, 51, 52, 53, 54,
		55, 56, 57, 58, 59, 60, 61, 62, 63, 64,
		65, 66, 67, 68, 69, 70, 71, 72, 73, 74,
		75, 76, 77, 78, 79, 80, 81, 82, 83, 84,
		85, 86, 87, 88, 89, 90, 91, 92, 93, 94,
		95, 96, 97, 98, 99, 100, 101, 102, 103, 104,
		105, 106, 107, 108, 109, 110, 111, 112, 113, 114,
		115, 116, 117, 118, 119, 120
	};
	
	public static final Creator<CellInfo> CREATOR = new CellInfoCreator();
	protected long mDbId = INVALID_DB_ID;
	protected Uri mUri;/* DBのURI */
	protected int mAppWidgetId = -1;
	protected int mColumn = -1;
	protected int mRow = -1;
	protected int mColumnCount = 1;
	protected int mRowCount = 1;
	/* アプリ全体の設定項目。以下のメンバにコピーする。 */
	protected boolean mShowTitle;
	protected boolean mSmaller;
	protected boolean mIcsCompat;
	protected int mMaxColumnCount;
	protected int mDirectDial;

	public static class CellPosition{
		public int column;
		public int row;
		
		public CellPosition(){}
		
		public CellPosition(int c, int r){
			column = c;
			row = r;
		}
	}
	
	public static class CellSize{
		public int width;
		public int height;
		
		public CellSize(){}
		
		public CellSize(int w, int h){
			width = w;
			height = h;
		}
	}
	
	public static class CellRect{
		public int column;
		public int row;
		public int columnCount;
		public int rowCount;
		
		public CellRect(){}
		
		public CellRect(int c, int r, int w, int h){
			column = c;
			row = r;
			columnCount = w;
			rowCount = h;
		}
		
		public CellRect(CellRect orig){
			this(orig.column, orig.row, orig.columnCount, orig.rowCount);
		}
		
		public boolean isValid(){
			return (row >= 0 && column >= 0 && columnCount >= 1 && rowCount >= 1);
		}
		
		public int getRight(){
			return column + columnCount;
		}
		
		public int getBottom(){
			return row + rowCount;
		}

		public boolean equals(CellRect rect) {
			return column == rect.column && row == rect.row && columnCount == rect.columnCount && rowCount == rect.rowCount;
		}
		
		public boolean intersects(CellRect rect) {
			if(equals(rect)){
				return false;
			}
			boolean outer = false;
			boolean inner = false;
			for(int i = rect.column; i < rect.getRight(); i++){
				for(int j = rect.row; j < rect.getBottom(); j++){
					if(i >= column && i < column + columnCount && j >= row && j < row + rowCount){
						inner = true;
					}
					else{
						outer = true;
					}
				}
			}
			return inner & outer;
	    }
		
		public boolean shares(CellRect rect) {
			if(equals(rect)){
				return false;
			}
			return (column <= rect.getRight() - 1 && rect.column <= getRight() - 1) && (row <= rect.getBottom() - 1 && rect.row <= getBottom() - 1);
	    }
		
		public boolean contains(CellRect rect){
			return rect.column >= column && rect.row >= row && rect.getRight() <= getRight() && rect.getBottom() <= getBottom();
		}
	}
	
	public CellInfo(){
	}
	
	public void applySetting(SettingLoader settingLoader){
		/* 設定値をコピーする */
		mShowTitle = settingLoader.getShowTitle();
		mSmaller = settingLoader.getSmaller();
		mIcsCompat = settingLoader.getIcsCompat();
		mMaxColumnCount = settingLoader.getColumnCount();
		mDirectDial = settingLoader.getDirectDial();
	}
	
	public long getDbId(){
		return mDbId;
	}
	
	public void setDbId(long id){
		mDbId = id;
		mUri = SettingColumns.getContentUri(mDbId);/* AppInfoのURIを取得 */
	}
	
	/* 子クラスでオーバーライドする */
	public boolean isSpacer(){
		return true;
	}
	
	protected int getCellType(){
		return SettingColumns.CELL_TYPE_SPACER;
	}
	
	public int getAppWidgetId(){
		return mAppWidgetId;
	}
	
	public int getColumnCount(){
		return mColumnCount;
	}
	
	public int getRowCount(){
		return mRowCount;
	}
	
	public CellPosition getPosition(){
		return new CellPosition(mColumn, mRow);
	}
	
	public CellRect getRect(){
		return new CellRect(mColumn, mRow, mColumnCount, mRowCount);
	}
	
	public void setPosition(int column, int row){
		mColumn = column;
		mRow = row;
	}
	
	public void setPosition(CellPosition position){
		mColumn = position.column;
		mRow = position.row;
	}
	
	public void resize(int columnCount, int rowCount){
		mColumnCount = columnCount;
		mRowCount = rowCount;
	}
	
	public static CellInfo createFromCursor(Context context, Cursor cursor){
		int cellType = cursor.getInt(SettingColumns.INDEX_CELL_TYPE);
		CellInfo info = null;
		
		switch(cellType){
		case SettingColumns.CELL_TYPE_APPLICATION:
			info = AppInfo.createFromCursor(context, cursor);
			break;
		case SettingColumns.CELL_TYPE_SHORTCUT:
			info = ShortcutInfo.createFromCursor(context, cursor);
			break;
		default:
			return null;
		}
		if(info == null){
			return null;
		}
		/* mUriを更新したいので、mDbIdはメソッドで設定する。 */
		info.setDbId(cursor.getLong(SettingColumns.INDEX_DB_ID));
		info.mColumn = cursor.getInt(SettingColumns.INDEX_COLUMN);
		info.mRow = cursor.getInt(SettingColumns.INDEX_ROW);
		info.mColumnCount = cursor.getInt(SettingColumns.INDEX_COLUMN_COUNT);
		info.mRowCount = cursor.getInt(SettingColumns.INDEX_ROW_COUNT);
		
		return info;
	}
	
	public void onAddToDatabase(ContentValues values) {
		values.put(SettingColumns.KEY_CELL_TYPE, getCellType());
		values.put(SettingColumns.KEY_COLUMN, mColumn);
		values.put(SettingColumns.KEY_ROW, mRow);
		values.put(SettingColumns.KEY_COLUMN_COUNT, mColumnCount);
		values.put(SettingColumns.KEY_ROW_COUNT, mRowCount);
	}
	
	public View createView(CellLayout cellLayout){
		/* 子クラスでオーバーライドする */
		return null;
	}
	
	public RemoteViews createRemoteViews(Context context, RemoteViews widgetView){
		/* スペーサを作成する */
		RemoteViews holder = new RemoteViews(context.getPackageName(), R.layout.spacer);
		Intent intent = new Intent();
		PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
		holder.setOnClickPendingIntent(R.id.imageSpacer, pendingIntent);
		return holder;
	}
	
	public static Bitmap getDefaultIcon(PackageManager pm){
		Drawable drawable = pm.getDefaultActivityIcon();
		if(drawable instanceof BitmapDrawable){
			return ((BitmapDrawable)drawable).getBitmap();
		}
		return null;
	}
	
	public static String getDefaultTitle(Context context){
		return context.getString(R.string.default_title);
	}
	
	protected int getIntentCode(){
		return mColumn * 1000 + mRow * 100000;
	}
	/* 削除予定 */
	protected static Bitmap getIconFromCursor(Cursor cursor, int iconIndex) {
        byte[] data = cursor.getBlob(iconIndex);
        try {
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        } catch (Exception e) {
            return null;
        }
    }

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(getCellType());
		dest.writeLong(mDbId);
		dest.writeParcelable(mUri, flags);
		dest.writeInt(mAppWidgetId);
		dest.writeInt(mColumn);
		dest.writeInt(mRow);
		dest.writeInt(mColumnCount);
		dest.writeInt(mRowCount);
		
		/* ParcelでMainActivityからさービスに設定も転送したほうがいいのか、
		 * サービスにてaddCellInfoする前にapplySettingしたほうがいいのか、よく検討 */
		dest.writeInt(mShowTitle ? 1 : 0);
		dest.writeInt(mSmaller ? 1 : 0);
		dest.writeInt(mIcsCompat ? 1 : 0);
		dest.writeInt(mMaxColumnCount);
		dest.writeInt(mDirectDial);
	}
	
	private static class CellInfoCreator implements Creator<CellInfo> {

		@Override
		public CellInfo createFromParcel(Parcel source) {
			int cellType = source.readInt();
			/* 読み出し順があるのでまず共通部分を読み出す */
			CellInfo tmp = new CellInfo();
			tmp.mDbId = source.readLong();
			tmp.mUri = source.readParcelable(Uri.class.getClassLoader());
			tmp.mAppWidgetId = source.readInt();
			tmp.mColumn = source.readInt();
			tmp.mRow = source.readInt();
			tmp.mColumnCount = source.readInt();
			tmp.mRowCount = source.readInt();
			
			tmp.mShowTitle = source.readInt() != 0;
			tmp.mSmaller = source.readInt()	!= 0;
			tmp.mIcsCompat = source.readInt() != 0;
			tmp.mMaxColumnCount = source.readInt();
			tmp.mDirectDial = source.readInt();
			
			CellInfo info = null;
			
			switch(cellType){
			case SettingColumns.CELL_TYPE_APPLICATION:
				info = AppInfo.createFromParcel(source);
				break;
			case SettingColumns.CELL_TYPE_SHORTCUT:
				info = ShortcutInfo.createFromParcel(source);
				break;
			default:
				return null;
			}
			
			info.mDbId = tmp.mDbId;
			info.mUri = tmp.mUri;
			info.mAppWidgetId = tmp.mAppWidgetId;
			info.mColumn = tmp.mColumn;
			info.mRow = tmp.mRow;
			info.mColumnCount = tmp.mColumnCount;
			info.mRowCount = tmp.mRowCount;
			
			info.mShowTitle = tmp.mShowTitle;
			info.mSmaller = tmp.mSmaller;
			info.mIcsCompat = tmp.mIcsCompat;
			info.mMaxColumnCount = tmp.mMaxColumnCount;
			info.mDirectDial = tmp.mDirectDial;
			
			return info;
		}

		@Override
		public CellInfo[] newArray(int size) {
			return new CellInfo[size];
		}
	}
}
