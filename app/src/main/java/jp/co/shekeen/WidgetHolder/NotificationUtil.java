package jp.co.shekeen.WidgetHolder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Notification;
import android.app.Notification.Builder;
import android.os.Build;
import android.widget.RemoteViews;

public class NotificationUtil {

	public static void setPriority(Builder builder, int priority){
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN){/* IF NOT JELLY BEAN */
			return;
		}
		try {
			Method setPriority = Builder.class.getDeclaredMethod("setPriority", int.class);
			setPriority.invoke(builder, priority);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public static void setBigContentView(Notification notification, RemoteViews remoteViews){
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN){/* IF NOT JELLY BEAN */
			return;
		}
		try {
			Field bigContentView = Notification.class.getDeclaredField("bigContentView");
			bigContentView.setAccessible(true);
			bigContentView.set(notification, remoteViews);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public static Notification build(Builder builder){
		try {
			if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){/* IF JELLY BEAN */
				Method build = Builder.class.getDeclaredMethod("build");
				return (Notification) build.invoke(builder);
			}
			else{
				Method getNotification = Builder.class.getDeclaredMethod("getNotification");
				return (Notification) getNotification.invoke(builder);
			}
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
}
