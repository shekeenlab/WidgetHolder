package jp.co.shekeen.WidgetHolder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.os.Build;

public class MyStatusBarManager {

	private Object mStatusBarManager;
	private Method mExpand;
	private Method mCollapse;
	private Method mExpandSettings;
	
	public static MyStatusBarManager getInstance(Context context){
		Object service = context.getSystemService("statusbar");
		return new MyStatusBarManager(service);
	}
	
	private MyStatusBarManager(Object statusBarManager){
		mStatusBarManager = statusBarManager;
		try {
			Class<?> clazz = Class.forName("android.app.StatusBarManager");
			if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN){/* IF JELLY BEAN MR1 AND HIGHER */
				mExpand = clazz.getDeclaredMethod("expandNotificationsPanel");
				mCollapse = clazz.getDeclaredMethod("collapsePanels");
				mExpandSettings = clazz.getDeclaredMethod("expandSettingsPanel");
			}
			else{
				mExpand = clazz.getDeclaredMethod("expand");
				mCollapse = clazz.getDeclaredMethod("collapse");
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}
	
	public void expand(){
		if(mStatusBarManager == null || mExpand == null){
			return;
		}
		try {
			mExpand.invoke(mStatusBarManager);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public void collapse(){
		if(mStatusBarManager == null || mCollapse == null){
			return;
		}
		try {
			mCollapse.invoke(mStatusBarManager);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public void expandSettingsPanel(){
		if(mStatusBarManager == null || mExpandSettings == null){
			return;
		}
		try {
			mExpandSettings.invoke(mStatusBarManager);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}
