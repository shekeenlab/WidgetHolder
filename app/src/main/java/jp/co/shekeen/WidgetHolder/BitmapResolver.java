package jp.co.shekeen.WidgetHolder;

import java.util.ArrayList;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;

public class BitmapResolver {
	
	public static class BitmapContainer{
		public long dbId;
		public long cellId;
		public int viewId;
		public Bitmap bitmap;
		public Uri uri;
		
		public static BitmapContainer createFromCursor(Context context, Cursor cursor){
			BitmapContainer container = new BitmapContainer();
			container.dbId = cursor.getLong(BitmapColumns.INDEX_DB_ID);
			container.cellId = cursor.getLong(BitmapColumns.INDEX_CELL_ID);
			container.viewId = cursor.getInt(BitmapColumns.INDEX_VIEW_ID);
			container.bitmap = null;/* Bitmapは読み込まない。Write-Only */
			long id = cursor.getLong(BitmapColumns.INDEX_DB_ID);
			container.uri = BitmapColumns.getContentUri(id);
			return container;
		}
		
		public void onAddToDatabase(ContentValues values) {
			values.put(BitmapColumns.KEY_CELL_ID, cellId);
			values.put(BitmapColumns.KEY_VIEW_ID, viewId);
			BitmapUtil.writeBitmap(values, BitmapColumns.KEY_BITMAP, bitmap);
		}
	}
	
	public static BitmapContainer[] loadFromDatabase(Context context, long cellId){
		BitmapColumns.initialize(context);
		ContentResolver resolver = context.getContentResolver();
		String select = BitmapColumns.KEY_CELL_ID + "=?";
        String[] arg = new String[]{ String.valueOf(cellId) };
        Cursor cursor = resolver.query(BitmapColumns.getContentUri(), null, select, arg, null);
		
        if(cursor == null){
        	return new BitmapContainer[0];
        }
        
		BitmapColumns.INDEX_DB_ID = cursor.getColumnIndex(BitmapColumns._ID);
		BitmapColumns.INDEX_CELL_ID = cursor.getColumnIndex(BitmapColumns.KEY_CELL_ID);
		BitmapColumns.INDEX_VIEW_ID = cursor.getColumnIndex(BitmapColumns.KEY_VIEW_ID);
		BitmapColumns.INDEX_BITMAP = cursor.getColumnIndex(BitmapColumns.KEY_BITMAP);

		ArrayList<BitmapContainer> bitmapList = new ArrayList<BitmapContainer>();
		ArrayList<Long> idToDelete = new ArrayList<Long>();
		while(cursor.moveToNext()){
			BitmapContainer container = BitmapContainer.createFromCursor(context, cursor);
			if(container == null){
				idToDelete.add(cursor.getLong(BitmapColumns.INDEX_DB_ID));
				continue;
			}
			bitmapList.add(container);
		}
		for(long id : idToDelete){
			Uri uri = BitmapColumns.getContentUri(id);
			resolver.delete(uri, null, null);
		}
		cursor.close();
		return bitmapList.toArray(new BitmapContainer[0]);
	}
	
	public static void updateItemInDatabase(Context context, BitmapContainer container) {
		ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        container.onAddToDatabase(values);
        
        if(container.dbId == 0){
        	/* DBに追加する */
        	Uri result = resolver.insert(BitmapColumns.getContentUri(), values);
        	if (result != null) {
        		long id = Integer.parseInt(result.getPathSegments().get(1));
        		container.dbId = id;
        		container.uri = result;
        	}
        }
        else if(container.bitmap != null){
        	/* DBを更新する */
        	resolver.update(container.uri, values, null, null);
        }
    }
	
	public static void deleteItemFromDatabase(Context context, long cellId) {
        ContentResolver resolver = context.getContentResolver();
        String select = BitmapColumns.KEY_CELL_ID + "=?";
        String[] arg = new String[]{ String.valueOf(cellId) };
        Cursor cursor = resolver.query(BitmapColumns.getContentUri(), null, select, arg, null);
        
        if(cursor == null){
        	return;
        }
        
        ArrayList<Long> idToDelete = new ArrayList<Long>();
        while(cursor.moveToNext()){
        	idToDelete.add(cursor.getLong(BitmapColumns.INDEX_DB_ID));
        }
        for(long id : idToDelete){
			Uri uri = BitmapColumns.getContentUri(id);
			resolver.delete(uri, null, null);
			DebugHelper.print("BITMAP DELTED", uri.toString());
		}
        cursor.close();
    }
}
