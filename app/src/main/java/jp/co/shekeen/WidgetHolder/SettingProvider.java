package jp.co.shekeen.WidgetHolder;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;

public class SettingProvider extends ContentProvider {

	public static final String TABLE_FAVORITES = "favorites";
	public static final String TABLE_BITMAPS = "bitmaps";
	private static final String TABLE_TEMP_FAVORITES = "temp_favorites";
	private static final String TABLE_TEMP_BITMAPS = "temp_bitmaps";
	private static final String DATABASE_NAME = "widgetholder.db";
	private static final int DATABASE_VERSION = 4;
	
	private SQLiteOpenHelper mOpenHelper;
	
	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
        return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		String table = uri.getPathSegments().get(0);
		if(uri.getPathSegments().size() == 2){
			selection = "_id=" + ContentUris.parseId(uri);
			selectionArgs = null;
		}
		
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(table);

        
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Cursor result = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        if(result == null){
        	return null;
        }
        
        result.setNotificationUri(getContext().getContentResolver(), uri);

        return result;
	}

	@Override
	public String getType(Uri uri) {
		return "*/*";
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		String table = uri.getPathSegments().get(0);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final long rowId = db.insert(table, null, values);
        if (rowId <= 0){
        	return null;
        }
        uri = ContentUris.withAppendedId(uri, rowId);

        return uri;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		String table = uri.getPathSegments().get(0);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        if(uri.getPathSegments().size() == 2){
			selection = "_id=" + ContentUris.parseId(uri);
			selectionArgs = null;
		}
		return db.delete(table, selection, selectionArgs);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		String table = uri.getPathSegments().get(0);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        if(uri.getPathSegments().size() == 2){
			selection = "_id=" + ContentUris.parseId(uri);
			selectionArgs = null;
		}
        return db.update(table, values, selection, selectionArgs);
	}
	
	@Override
	public AssetFileDescriptor openTypedAssetFile(Uri uri, String mimeTypeFilter, Bundle opts) throws FileNotFoundException {
		/* http://developer.android.com/intl/ja/reference/android/content/ClipData.htmlをもとに実装 */
		Cursor cursor = query(uri, null, null, null, null);
		if (cursor == null || !cursor.moveToFirst()) {
			throw new FileNotFoundException("Unable to query " + uri);
		}
		ParcelFileDescriptor fd = openPipeHelper(uri, "*/*", opts, cursor, new BitmapWriter());
		return new AssetFileDescriptor(fd, 0, AssetFileDescriptor.UNKNOWN_LENGTH);
	}

	private class BitmapWriter implements PipeDataWriter<Object>{

		@Override
		public void writeDataToPipe(ParcelFileDescriptor output, Uri uri, String mimeType, Bundle opts, Object args) {
			Cursor cursor = query(uri, null, null, null, null);
			if(cursor == null || !cursor.moveToFirst()){
				return;
			}
			String table = uri.getPathSegments().get(0);
			int index = -1;
			if(TABLE_FAVORITES.equals(table)){
				index = cursor.getColumnIndex(SettingColumns.KEY_ICON_BITMAP);
				
			}
			else if(TABLE_BITMAPS.equals(table)){
				index = cursor.getColumnIndex(BitmapColumns.KEY_BITMAP);
			}
			
			if(index < 0){
				return;
			}
			byte[] data = cursor.getBlob(index);
			FileOutputStream outStream = new FileOutputStream(output.getFileDescriptor());
			try {
				outStream.write(data);
				outStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private class DatabaseHelper extends SQLiteOpenHelper {
		
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TABLE_FAVORITES + " (" + 
	                SettingColumns._ID + " INTEGER PRIMARY KEY," +
					SettingColumns.KEY_CELL_TYPE + " INTEGER," +
	                SettingColumns.KEY_COLUMN + " INTEGER," +
					SettingColumns.KEY_ROW + " INTEGER," +
	                SettingColumns.KEY_COLUMN_COUNT + " INTEGER," +
	                SettingColumns.KEY_ROW_COUNT + " INTEGER," +
	                SettingColumns.KEY_APPWIDGET_ID + " INTEGER NOT NULL DEFAULT -1," +
	                SettingColumns.KEY_HOOK_INTENT + " INTEGER," +
	                SettingColumns.KEY_SHOW_SHRINK + " INTEGER," +
	                SettingColumns.KEY_PACKAGE_NAME + " TEXT," +
	                SettingColumns.KEY_ACTIVITY_NAME + " TEXT," +
	                SettingColumns.KEY_INTENT + " TEXT," +
	                SettingColumns.KEY_TITLE + " TEXT," +
	                SettingColumns.KEY_ICON_TYPE + " INTEGER," +
	                SettingColumns.KEY_ICON_PACKAGE + " TEXT," +
	                SettingColumns.KEY_ICON_RESOURCE + " TEXT," +
	                SettingColumns.KEY_ICON_BITMAP + " BLOB" +
	                ");");
			
			db.execSQL("CREATE TABLE " + TABLE_BITMAPS + " (" + 
	                BitmapColumns._ID + " INTEGER PRIMARY KEY," +
					BitmapColumns.KEY_CELL_ID + " INTEGER," +
	                BitmapColumns.KEY_VIEW_ID + " INTEGER," +
	                BitmapColumns.KEY_BITMAP + " BLOB" +
	                ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.beginTransaction();
			/* 現状のDBをTEMPに退避 */
			db.execSQL("ALTER TABLE " + TABLE_FAVORITES + " RENAME TO " + TABLE_TEMP_FAVORITES);
			
			String[] tables;
			String[] temps;
			if(oldVersion >= 4){/* ver3以下ではBITMAP TABLEが存在しない */
				db.execSQL("ALTER TABLE " + TABLE_BITMAPS + " RENAME TO " + TABLE_TEMP_BITMAPS);
				tables = new String[]{ TABLE_FAVORITES, TABLE_BITMAPS };
				temps = new String[]{ TABLE_TEMP_FAVORITES, TABLE_TEMP_BITMAPS };
			}
			else{
				tables = new String[]{ TABLE_FAVORITES };
				temps = new String[]{ TABLE_TEMP_FAVORITES };
			}
			/* 新しいDBを作成 */
			onCreate(db);
			
			for(int i = 0; i < tables.length; i++){
				/* 旧DBからコラムを取得 */
				List<String> columns = getColumns(db, temps[i]);
				/* 新DBからコラムを取得 */
				List<String> newColumns = getColumns(db, tables[i]);

				/* 旧DBのコラムから新DBのコラムにないものを削除 */
				columns.retainAll(newColumns);

				/* 共通データを移す。(OLDにしか存在しないものは捨てられ, NEWにしか存在しないものはNULLになる)*/
				String cols = join(columns, ",");
				db.execSQL("INSERT INTO " + tables[i] + " (" + cols + ") SELECT " + cols + " FROM " + temps[i]);
				/* TEMPを削除 */
				db.execSQL("DROP TABLE " + temps[i]);
			}
			db.setTransactionSuccessful();
			db.endTransaction();
		}

		private List<String> getColumns(SQLiteDatabase db, String tableName) {
			List<String> list = null;
			Cursor c = null;
			c = db.rawQuery("SELECT * FROM " + tableName + " LIMIT 1", null);
			if (c != null) {
				list = new ArrayList<String>(Arrays.asList(c.getColumnNames()));
				c.close();
			}
			return list;
		}
		
		private String join(List<String> list, String delim) {
			final StringBuilder buf = new StringBuilder();
			final int num = list.size();
			for (int i = 0; i < num; i++) {
				if (i > 0){
					buf.append(delim);
				}
				buf.append(list.get(i));
			}
			return buf.toString();
		}
	}

}
