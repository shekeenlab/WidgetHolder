package jp.co.shekeen.WidgetHolder;

import java.lang.reflect.Field;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.widget.RemoteViews;

public class MyAppWidgetHost extends AppWidgetHost {

	public static final int APPWIDGET_HOST_ID = 0x56e12ee3;
	
	private static class Interceptor extends Handler{
		
		static final int HANDLE_UPDATE = 1;
		static final int HANDLE_PROVIDER_CHANGED = 2;
		static final int HANDLE_VIEW_DATA_CHANGED = 3;
		
		private IAppWidgetCallback mCallbacks;
		private AppWidgetHost mWidgetHost;
		
		private Interceptor(Looper looper, IAppWidgetCallback callbacks, MyAppWidgetHost appwidgetHost){
			super(looper);
			mCallbacks = callbacks;
			mWidgetHost = appwidgetHost;
			start();
		}

		private void start(){
			try {
				Field mHandler = AppWidgetHost.class.getDeclaredField("mHandler");
				mHandler.setAccessible(true);
				mHandler.set(mWidgetHost, this);
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void handleMessage(Message msg) {
			try {
				switch (msg.what) {
				case HANDLE_UPDATE:
					mCallbacks.onUpdateAppWidgetView(msg.arg1, (RemoteViews)msg.obj);
					break;
				case HANDLE_PROVIDER_CHANGED:
					mCallbacks.onProviderChanged(msg.arg1, (AppWidgetProviderInfo)msg.obj);
					break;
				case HANDLE_VIEW_DATA_CHANGED:
					mCallbacks.onViewDataChanged(msg.arg1, msg.arg2);
					break;
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	public MyAppWidgetHost(Context context, IAppWidgetCallback callbacks) {
		super(context, APPWIDGET_HOST_ID);
		new Interceptor(context.getMainLooper(), callbacks, this);
	}

}
