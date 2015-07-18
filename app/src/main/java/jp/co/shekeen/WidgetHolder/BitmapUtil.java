package jp.co.shekeen.WidgetHolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapUtil {

	public static byte[] flatten(Bitmap bitmap) {
        // Try go guesstimate how much space the icon will take when serialized
        // to avoid unnecessary allocations/copies during the write.
        int size = bitmap.getWidth() * bitmap.getHeight() * 4;
        ByteArrayOutputStream out = new ByteArrayOutputStream(size);
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
            DebugHelper.print("Could not write icon");
            return null;
        }
    }

	public static Bitmap decode(byte[] data){
		try {
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        } catch (Exception e) {
            return null;
        }
	}
	
	public static void writeBitmap(ContentValues values, String key, Bitmap bitmap) {
        if (bitmap != null) {
            byte[] data = flatten(bitmap);
            values.put(key, data);
        }
    }
	
	public static Bitmap getIconFromCursor(Cursor cursor, int iconIndex) {
        byte[] data = cursor.getBlob(iconIndex);
        return decode(data);
    }
}
