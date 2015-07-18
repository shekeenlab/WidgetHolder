package jp.co.shekeen.WidgetHolder;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class Config {
	
	public static void initialize(Context context){
		if(context.getPackageName().contains("HolderEx")){
			EX_VER = true;
		}
		if(!DEBUG){
			PackageManager pm = context.getPackageManager();
			try {
				pm.getPackageInfo("jp.co.shekeen.Widget" + "HolderEx", PackageManager.GET_META_DATA);
				FREE = false;
			} catch (NameNotFoundException e) {
				FREE = true;
			}
		}
	}
	
	public static boolean FREE = false;
	public static final String TAG = "shekeen";
	public static final boolean DEBUG = false;
	public static boolean EX_VER = false;
}
