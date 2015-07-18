package jp.co.shekeen.WidgetHolder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;

public class AppWidgetServiceIF {
	static final String APPWIDGET_SERVICE = "appwidget";
	private Class<?> mServiceClass;
	private Object mService;
	private Context mContext;
	
	public AppWidgetServiceIF(Context context){
		try {
			mServiceClass = Object.class.getClassLoader().loadClass("com.android.internal.appwidget.IAppWidgetService");
			mService = obtainService();
			mContext = context;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private Object obtainService(){
		try {
			Class<?> classServiceManager = Class.forName("android.os.ServiceManager");
			Method getService = classServiceManager.getDeclaredMethod("getService", String.class);
			IBinder b = (IBinder) getService.invoke(null, APPWIDGET_SERVICE);
			if(b == null){
				DebugHelper.print("IBinder IS NULL");
				return null;
			}
			Class<?> classStub = Class.forName("com.android.internal.appwidget.IAppWidgetService$Stub");
			Method asInterface = classStub.getDeclaredMethod("asInterface", IBinder.class);
			return asInterface.invoke(null, b);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
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
	
	public RemoteViews getAppWidgetViews(int appWidgetId){
		if(appWidgetId <= 0){
			return null;
		}
		try {
			RemoteViews remoteViews;
			if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1){/* IF Android 4.2 and lower */
				Method getAppWidgetViews = mServiceClass.getDeclaredMethod("getAppWidgetViews", int.class);
				remoteViews = (RemoteViews) getAppWidgetViews.invoke(mService, appWidgetId);
			}
			else{/* IF Android 4.3 */
				Method getAppWidgetViews = mServiceClass.getDeclaredMethod("getAppWidgetViews", int.class, int.class);
				remoteViews = (RemoteViews) getAppWidgetViews.invoke(mService, appWidgetId, getUserId());
			}
			return remoteViews;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.getCause().printStackTrace();
			e.printStackTrace();
		}
		
		return null;
	}

	private int getUserId() throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		Method method = Context.class.getDeclaredMethod("getUserId");
		return (Integer)method.invoke(mContext);
	}
}

