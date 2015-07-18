package jp.co.shekeen.WidgetHolder;

import android.content.Context;
import android.net.Uri;
import android.provider.BaseColumns;

public class SettingColumns implements BaseColumns {

	private static String AUTHORITY;
	/* CellInfo */
	public static final String KEY_CELL_TYPE = "cellType";
	public static final String KEY_COLUMN = "column";
	public static final String KEY_ROW = "row";
	public static final String KEY_COLUMN_COUNT = "columnCount";
	public static final String KEY_ROW_COUNT = "rowCount";
	/* WidgetInfo */
	public static final String KEY_APPWIDGET_ID = "widgetId";
	public static final String KEY_HOOK_INTENT = "hookIntent";
	public static final String KEY_SHOW_SHRINK = "showShrink";
	/* AppInfo */
	public static final String KEY_PACKAGE_NAME = "packageName";
	public static final String KEY_ACTIVITY_NAME = "activityName";
	/* ShortcutInfo */
	public static final String KEY_INTENT = "intent";
	public static final String KEY_TITLE = "title";
	public static final String KEY_ICON_TYPE = "iconType";
	public static final String KEY_ICON_PACKAGE = "iconPackage";
	public static final String KEY_ICON_RESOURCE = "iconResource";
	public static final String KEY_ICON_BITMAP = "iconBitmap";
	
	public static final int CELL_TYPE_SPACER = 0;
	public static final int CELL_TYPE_WIDGET = 1;
	public static final int CELL_TYPE_APPLICATION = 2;
	public static final int CELL_TYPE_SHORTCUT = 3;
	
	public static final int ICON_TYPE_RESOURCE = 0;
	public static final int ICON_TYPE_BITMAP = 1;
	
	public static int INDEX_DB_ID = -1;
	public static int INDEX_CELL_TYPE = -1;
	public static int INDEX_COLUMN = -1;
	public static int INDEX_ROW = -1;
	public static int INDEX_COLUMN_COUNT = -1;
	public static int INDEX_ROW_COUNT = -1;
	public static int INDEX_APPWIDGET_ID = -1;
	public static int INDEX_HOOK_INTENT = -1;
	public static int INDEX_SHOW_SHRINK = -1;
	public static int INDEX_PACKAGE_NAME = -1;
	public static int INDEX_ACTIVITY_NAME = -1;
	public static int INDEX_INTENT = -1;
	public static int INDEX_TITLE = -1;
	public static int INDEX_ICON_TYPE = -1;
	public static int INDEX_ICON_PACKAGE = -1;
	public static int INDEX_ICON_RESOURCE = -1;
	public static int INDEX_ICON_BITMAP = -1;
	
	public static void initialize(Context context){
		if(AUTHORITY != null){
			return;
		}
		AUTHORITY = context.getString(R.string.authorities);
	}
	
	public static Uri getContentUri() {
        return Uri.parse("content://" + AUTHORITY + "/" + SettingProvider.TABLE_FAVORITES);
    }
	
	public static Uri getContentUri(long id) {
        return Uri.parse("content://" + AUTHORITY + "/" + SettingProvider.TABLE_FAVORITES + "/" + id);
    }
}
