package jp.co.shekeen.WidgetHolder;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class SettingResolver {

	public static void addItemToDatabase(Context context, CellInfo cellInfo){
		ContentResolver resolver = context.getContentResolver();
		ContentValues values = new ContentValues();
		
		cellInfo.onAddToDatabase(values);
		Uri result = resolver.insert(SettingColumns.getContentUri(), values);

		if (result != null) {
			long id = Integer.parseInt(result.getPathSegments().get(1));
			cellInfo.setDbId(id);
		}
	}
	
	public static void deleteItemFromDatabase(Context context, CellInfo cellInfo) {
        ContentResolver resolver = context.getContentResolver();
        Uri uriToDelete = SettingColumns.getContentUri(cellInfo.getDbId());
        /* スレッドを別にしたほうがよい？ */
        resolver.delete(uriToDelete, null, null);
    }
	
	public static void updateItemInDatabase(Context context, CellInfo cellInfo) {
		ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();

        cellInfo.onAddToDatabase(values);
        resolver.update(SettingColumns.getContentUri(cellInfo.getDbId()), values, null, null);
    }
	
	public static CellInfo[] loadFromDatabase(Context context){
		SettingColumns.initialize(context);
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = resolver.query(SettingColumns.getContentUri(), null, null, null, null);
		
		if(cursor == null){
			return new CellInfo[0];
		}
		
		SettingColumns.INDEX_DB_ID = cursor.getColumnIndex(SettingColumns._ID);
		SettingColumns.INDEX_CELL_TYPE = cursor.getColumnIndex(SettingColumns.KEY_CELL_TYPE);
		SettingColumns.INDEX_COLUMN = cursor.getColumnIndex(SettingColumns.KEY_COLUMN);
		SettingColumns.INDEX_ROW = cursor.getColumnIndex(SettingColumns.KEY_ROW);
		SettingColumns.INDEX_COLUMN_COUNT = cursor.getColumnIndex(SettingColumns.KEY_COLUMN_COUNT);
		SettingColumns.INDEX_ROW_COUNT = cursor.getColumnIndex(SettingColumns.KEY_ROW_COUNT);
		SettingColumns.INDEX_APPWIDGET_ID = cursor.getColumnIndex(SettingColumns.KEY_APPWIDGET_ID);
		SettingColumns.INDEX_HOOK_INTENT = cursor.getColumnIndex(SettingColumns.KEY_HOOK_INTENT);
		SettingColumns.INDEX_SHOW_SHRINK = cursor.getColumnIndex(SettingColumns.KEY_SHOW_SHRINK);
		SettingColumns.INDEX_PACKAGE_NAME = cursor.getColumnIndex(SettingColumns.KEY_PACKAGE_NAME);
		SettingColumns.INDEX_ACTIVITY_NAME = cursor.getColumnIndex(SettingColumns.KEY_ACTIVITY_NAME);
		SettingColumns.INDEX_INTENT = cursor.getColumnIndex(SettingColumns.KEY_INTENT);
		SettingColumns.INDEX_TITLE = cursor.getColumnIndex(SettingColumns.KEY_TITLE);
		SettingColumns.INDEX_ICON_TYPE = cursor.getColumnIndex(SettingColumns.KEY_ICON_TYPE);
		SettingColumns.INDEX_ICON_PACKAGE = cursor.getColumnIndex(SettingColumns.KEY_ICON_PACKAGE);
		SettingColumns.INDEX_ICON_RESOURCE = cursor.getColumnIndex(SettingColumns.KEY_ICON_RESOURCE);
		SettingColumns.INDEX_ICON_BITMAP = cursor.getColumnIndex(SettingColumns.KEY_ICON_BITMAP);

		ArrayList<CellInfo> indexList = new ArrayList<CellInfo>();
		ArrayList<Long> idToDelete = new ArrayList<Long>();
		while(cursor.moveToNext()){
			CellInfo cellInfo = CellInfo.createFromCursor(context, cursor);
			if(cellInfo == null){
				idToDelete.add(cursor.getLong(SettingColumns.INDEX_DB_ID));
				continue;
			}
			indexList.add(cellInfo);
		}
		for(long id : idToDelete){
			Uri uri = SettingColumns.getContentUri(id);
			resolver.delete(uri, null, null);
		}
		cursor.close();
		return indexList.toArray(new CellInfo[0]);
	}
}
