package jp.co.shekeen.WidgetHolder;

import android.os.IBinder;
import android.os.RemoteException;

public class NotificationServiceWrapper {

	private INotificationService mServiceStub;
	
	public NotificationServiceWrapper(IBinder service){
		mServiceStub = INotificationService.Stub.asInterface(service);
	}

	public CellInfo[] getCellInfos() {
		try {
			return mServiceStub.getCellInfos();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void registerCallbacks(IAppWidgetCallback callback){
		try {
			mServiceStub.registerCallbacks(callback);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void updateCellInfos(CellInfo[] cellInfos) {
		try {
			mServiceStub.updateCellInfos(cellInfos);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public int allocateAppWidgetId() {
		try {
			return mServiceStub.allocateAppWidgetId();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public void deleteAppWidgetId(int appWidgetId) {
		try {
			mServiceStub.deleteAppWidgetId(appWidgetId);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void addCellInfo(CellInfo cellInfo) {
		try {
			mServiceStub.addCellInfo(cellInfo);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void deleteCellInfo(CellInfo cellInfo) {
		try {
			mServiceStub.deleteCellInfo(cellInfo);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
