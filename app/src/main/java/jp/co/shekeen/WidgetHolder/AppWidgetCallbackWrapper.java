package jp.co.shekeen.WidgetHolder;

import android.appwidget.AppWidgetProviderInfo;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.widget.RemoteViews;

public class AppWidgetCallbackWrapper extends IAppWidgetCallback.Stub {

	private static final int EVENT_ON_UPDATE_APP_WIDGET_VIEW = 1;
	private static final int EVENT_ON_PRIVATE_CHANGED = 2;
	private static final int EVENT_ON_VIEW_DATA_CHANGED = 3;
	
	private CallbackHandler mHandler;
	
	private static class ArgOnUpdateAppWidgetView {
		private int appWidgetId;
		private RemoteViews remoteViews;
	}
	
	private static class ArgOnProviderChanged {
		private int appWidgetId;
		private AppWidgetProviderInfo appWidget;
	}
	
	private static class ArgOnViewDataChanged {
		int appWidgetId;
		int viewId;
	}
	
	private static class CallbackHandler extends Handler {

		private MainActivity mActivity;
		
		private CallbackHandler(MainActivity activity){
			mActivity = activity;
		}
		
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case EVENT_ON_UPDATE_APP_WIDGET_VIEW:
			{
				ArgOnUpdateAppWidgetView arg = (ArgOnUpdateAppWidgetView) msg.obj;
				mActivity.onUpdateAppWidgetView(arg.appWidgetId, arg.remoteViews);
				break;
			}
			case EVENT_ON_PRIVATE_CHANGED:
			{
				ArgOnProviderChanged arg = (ArgOnProviderChanged) msg.obj;
				mActivity.onProviderChanged(arg.appWidgetId, arg.appWidget);
			}
			case EVENT_ON_VIEW_DATA_CHANGED:
				ArgOnViewDataChanged arg = (ArgOnViewDataChanged) msg.obj;
				mActivity.onViewDataChanged(arg.appWidgetId, arg.viewId);
			}
		}
		
	}
	
	public AppWidgetCallbackWrapper(MainActivity activity){
		mHandler = new CallbackHandler(activity);
	}

	@Override
	public void onUpdateAppWidgetView(int appWidgetId, RemoteViews views) throws RemoteException {
		ArgOnUpdateAppWidgetView arg = new ArgOnUpdateAppWidgetView();
		arg.appWidgetId = appWidgetId;
		arg.remoteViews = views;
		Message msg = new Message();
		msg.what = EVENT_ON_UPDATE_APP_WIDGET_VIEW;
		msg.obj = arg;
		mHandler.sendMessage(msg);
	}

	@Override
	public void onProviderChanged(int appWidgetId, AppWidgetProviderInfo appWidget) throws RemoteException {
		ArgOnProviderChanged arg = new ArgOnProviderChanged();
		arg.appWidgetId = appWidgetId;
		arg.appWidget = appWidget;
		Message msg = new Message();
		msg.what = EVENT_ON_PRIVATE_CHANGED;
		msg.obj = arg;
		mHandler.sendMessage(msg);
	}

	@Override
	public void onViewDataChanged(int appWidgetId, int viewId) throws RemoteException {
		ArgOnViewDataChanged arg = new ArgOnViewDataChanged();
		arg.appWidgetId = appWidgetId;
		arg.viewId = viewId;
		Message msg = new Message();
		msg.what = EVENT_ON_VIEW_DATA_CHANGED;
		msg.obj = arg;
		mHandler.sendMessage(msg);
	}
	
}
