package jp.co.shekeen.WidgetHolder;

import android.appwidget.AppWidgetProviderInfo;

interface IAppWidgetCallback{
	void onUpdateAppWidgetView(int appWidgetId, in RemoteViews views);
	void onProviderChanged(int appWidgetId, in AppWidgetProviderInfo appWidget);
	void onViewDataChanged(int appWidgetId, int viewId);
}
