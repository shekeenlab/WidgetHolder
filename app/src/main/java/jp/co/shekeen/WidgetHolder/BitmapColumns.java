package jp.co.shekeen.WidgetHolder;

import android.content.Context;
import android.net.Uri;
import android.provider.BaseColumns;

public class BitmapColumns implements BaseColumns {

	private static String AUTHORITY;
	
	public static final String KEY_CELL_ID = "cellId";
	public static final String KEY_VIEW_ID = "viewId";
	public static final String KEY_BITMAP = "bitmap";
	
	public static int INDEX_DB_ID = -1;
	public static int INDEX_CELL_ID = -1;
	public static int INDEX_VIEW_ID = -1;
	public static int INDEX_BITMAP = -1;
	
	public static void initialize(Context context){
		if(AUTHORITY != null){
			return;
		}
		AUTHORITY = context.getString(R.string.authorities);
	}
	
	public static Uri getContentUri() {
        return Uri.parse("content://" + AUTHORITY + "/" + SettingProvider.TABLE_BITMAPS);
    }
	
	public static Uri getContentUri(long id) {
        return Uri.parse("content://" + AUTHORITY + "/" + SettingProvider.TABLE_BITMAPS + "/" + id);
    }
}
