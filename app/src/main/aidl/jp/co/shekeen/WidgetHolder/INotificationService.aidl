package jp.co.shekeen.WidgetHolder;

import jp.co.shekeen.WidgetHolder.CellInfo;
import jp.co.shekeen.WidgetHolder.IAppWidgetCallback;

interface INotificationService
{
	CellInfo[] getCellInfos();
	void registerCallbacks(in IAppWidgetCallback callback);
	void updateCellInfos(in CellInfo[] cellInfos);
	int allocateAppWidgetId();
	void deleteAppWidgetId(int appWidgetId);
	void addCellInfo(in CellInfo cellInfo);
	void deleteCellInfo(in CellInfo cellInfo);
}
