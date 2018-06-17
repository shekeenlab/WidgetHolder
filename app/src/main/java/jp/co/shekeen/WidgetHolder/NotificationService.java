package jp.co.shekeen.WidgetHolder;

import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.widget.FrameLayout;
import android.widget.RemoteViews;
import android.widget.Toast;

public class NotificationService {
	
	private GridNotification mGridNotif;
	private List<Integer> mNotifIdList;
	private RemoteViews[] mRemoteNotifs;
	private SettingLoader mSettingLoader;
	private Context mContext;

	public NotificationService(Context context) {

		DebugHelper.print("SERVICE CREATE");
		mContext = context;

		/* MainActivityとはプロセスが異なるので静的クラスの初期化は注意 */
		Config.initialize(mContext);
        SettingColumns.initialize(mContext);
		
        mSettingLoader = new SettingLoader(mContext);
		mGridNotif = new GridNotification(mContext, mSettingLoader);
		mNotifIdList = new ArrayList<Integer>();
		updateNotification(true);
	}
	
	public CellInfo[] getCellInfos(){
		return mGridNotif.getCellInfos();
	}
	
	public void addCellInfo(CellInfo cellInfo){
		mGridNotif.addCellInfo(cellInfo);
		updateNotification(true);
	}
	
	public void updateCellInfos(CellInfo[] cellInfos){
		mGridNotif.updateCellInfos(cellInfos);
		mGridNotif.updateAllView(mSettingLoader);
		updateNotification(true);
	}
	
	public void deleteCellInfo(CellInfo cellInfo){
		mGridNotif.deleteCellInfo(cellInfo);
		updateNotification(true);
	}

	public void onSettingChanged(Intent intent) {
		mSettingLoader.updateValue(intent);
		String key = intent.getStringExtra(SettingLoader.EXTRA_CHANGED_KEY);
		DebugHelper.print("CALLBACK", key);
		
		if(mSettingLoader.key_column_count.equals(key)){
			/* GridNotificationを再生成することではみ出たアイテムは削除される */
			mGridNotif = new GridNotification(mContext, mSettingLoader);
		}
		else if(mSettingLoader.key_smaller_height.equals(key)){
			mGridNotif = new GridNotification(mContext, mSettingLoader);
		}
		else if(mSettingLoader.key_ics_compat.equals(key)){
			/* IcsCompatが変更されたら必ずGridNotificationを再生成する必要があるわけではないが（SmallerHeightがfalseのときのみ）、 */
			/* あれこれ考えてもエンバグしそうなので、とりあえず再生成する */
			mGridNotif = new GridNotification(mContext, mSettingLoader);
		}
		else if(mSettingLoader.key_show_title.equals(key)){
			mGridNotif.updateAllView(mSettingLoader);
		}
		/* 通知優先度が変更されたときのみ、通知を再表示する */
		applySetting(mSettingLoader.key_priority.equals(key));
	}
	
	private void applySetting(boolean hide){
		mGridNotif.updateAllView(mSettingLoader);
		if(hide){
			for(Integer id : mNotifIdList){
				hideNotification(id);
			}
		}
		updateNotification(true);
	}

	private void updateNotification(){
		updateNotification(true);
	}
	
	private void updateNotification(boolean regenerate){
		if(regenerate || mRemoteNotifs == null){
			mRemoteNotifs = mGridNotif.createFormatedView();
		}
		mNotifIdList.clear();
		int validCount = 0;
		for(int i = 0; i < mRemoteNotifs.length; i++){
			if(mRemoteNotifs[i] != null){
				validCount++;
			}
		}
		for(int i = mRemoteNotifs.length - 1; i >= 0; i--){
			if(mRemoteNotifs[i] != null){
				try{
					FrameLayout testLayout = new FrameLayout(mContext);
					mRemoteNotifs[i].apply(mContext, testLayout);
					showNotification(mRemoteNotifs[i], i, validCount);
					mNotifIdList.add(i);
				}
				catch(RuntimeException e){
					e.printStackTrace();
					Toast.makeText(mContext, mContext.getString(R.string.error_id_conflist), Toast.LENGTH_LONG).show();
				}
				
			}
			else{
				hideNotification(i);
			}
		}
	}
	
	private void showNotification(RemoteViews remoteViews, int id, int total){
		Notification.Builder builder = new Notification.Builder(mContext);
		
		builder.setOngoing(true);
		builder.setSmallIcon(getStatusIcon(id));
		builder.setTicker(mContext.getString(R.string.app_name));
		builder.setContent(remoteViews);
		builder.setWhen(Long.MAX_VALUE - 10 * total - id);
		
		int priority = mSettingLoader.getPriority();
		NotificationUtil.setPriority(builder, priority);
		Notification notification = NotificationUtil.build(builder);
		NotificationUtil.setBigContentView(notification, notification.contentView);

		android.app.NotificationManager notificationManager = (android.app.NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(id, notification);
	}
	
	private void hideNotification(int id){
		android.app.NotificationManager notificationManager = (android.app.NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(id);
	}
	
	private int getStatusIcon(int id){
		int iconType = mSettingLoader.getStatusIconType(id);
		switch(iconType) {
			case SettingLoader.STATUS_ICON_DEFAULT:
				return R.drawable.statusbar;

			case SettingLoader.STATUS_ICON_TRANSPARENT:
				return R.drawable.transparent;
		}
		return R.drawable.statusbar;
	}
}
