package jp.co.shekeen.WidgetHolder;

import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.widget.FrameLayout;
import android.widget.RemoteViews;
import android.widget.Toast;

public class NotificationService {
	
	public static final String ACTION_LAUNCH_APP = "jp.co.shekeen.WidgetHolder.ACTION_LAUNCH_APP";
	public static final String ACTION_LAUNCH_SHORTCUT = "jp.co.shekeen.WidgetHolder.ACTION_LAUNCH_SHORTCUT";
	public static final String ACTION_LAUNCH_WIDGET = "jp.co.shekeen.WidgetHolder.ACTION_LAUNCH_WIDGET";
	public static final String ACTION_COLUMN_COUNT_CHANGED = "jp.co.shekeen.WidgetHolder.ACTION_COLUMN_COUNT_CHANGED";
	public static final String ACTION_SMALLER_CHANGED = "jp.co.shekeen.WidgetHolder.ACTION_SMALLER_CHANGED";
	public static final String ACTION_ICS_COMPAT_CHANGED = "jp.co.shekeen.WidgetHolder.ACTION_ICS_COMPAT_CHANGED";
	public static final String ACTION_SHOW_TITLE_CHANGED = "jp.co.shekeen.WidgetHolder.ACTION_SHOW_TITLE_CHANGED";
	private static final int FORGROUND_NOTIFICATION_ID = 100;
	private GridNotification mGridNotif;
	private List<Integer> mNotifIdList;
	private SystemEventReceiver mReceiver;
	private int mBatteryLevel = 0;
	private int mBatteryTemp = 0;
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
		
		startReceiver();
	}

	// TODO: ACTION_DIALを通知から直接実行できない場合があるらしい。その場合は、一旦サービスで受け、アクティビティを起動してShourtCutInto.launchShortcutを実行する。
	// ただ、通知から直接CallPhoneActivityを起動するでも良い気がする
	public int onStartCommand(Intent intent, int flags, int startId) {
		DebugHelper.print("SERVICE START");
		if(intent != null){
			String action = intent.getAction();
			try{
				if(ACTION_LAUNCH_APP.equals(action)){
					AppInfo.launchApp(mContext, intent);
				}
				else if(ACTION_LAUNCH_SHORTCUT.equals(action)){
					ShortcutInfo.launchShortcut(mContext, intent);
				}
				else if(ACTION_LAUNCH_WIDGET.equals(action)){
					WidgetInfo.launchWidget(mContext, intent);
				}
				else if(SettingLoader.ACTION_SETTING_CHANGED.equals(action)){
					onSettingChanged(intent);

				}
			}
			catch(RuntimeException e){
				Toast.makeText(mContext, mContext.getString(R.string.error_launch_failed), Toast.LENGTH_LONG).show();
				DebugHelper.printStackTrace(e);
			}
		}
		return 0;
	}

	public void onDestroy() {
		DebugHelper.print("SERVICE DESTROY");
		stopReceiver();
		mSettingLoader = null;
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
		
		startReceiver();
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
		if(Config.EX_VER){
			builder.setWhen(Long.MAX_VALUE - 10 * total - id);
		}
		else{
			builder.setWhen(Long.MAX_VALUE - 10 * total - id - 100);
		}
		
		int priority = mSettingLoader.getPriority();
		NotificationUtil.setPriority(builder, priority);
		Notification notification = NotificationUtil.build(builder);
		NotificationUtil.setBigContentView(notification, notification.contentView);

		NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(id, notification);
	}
	
	private void hideNotification(int id){
		NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(id);
	}
	
	private int getStatusIcon(int id){
		int iconType = mSettingLoader.getStatusIconType(id);
		switch(iconType){
		case SettingLoader.STATUS_ICON_DEFAULT:
			return R.drawable.statusbar;
			
		case SettingLoader.STATUS_ICON_TRANSPARENT:
			return R.drawable.transparent;
			
		case SettingLoader.STATUS_ICON_BATTERY_LEVEL:
			if(mBatteryLevel >= 0 && mBatteryLevel < StatusIcons.BATTERY_LEVEL_ICONS.length){
				return StatusIcons.BATTERY_LEVEL_ICONS[mBatteryLevel];
			}
			
		case SettingLoader.STATUS_ICON_BATTERY_TEMPERATURE:
			if(mBatteryTemp >= 0 && mBatteryTemp < StatusIcons.BATTERY_TEMP_ICONS.length){
				return StatusIcons.BATTERY_TEMP_ICONS[mBatteryTemp];
			}
			
		case SettingLoader.STATUS_ICON_BATTERY_LEVEL_BAR:
			if(mBatteryLevel >= 0 && mBatteryLevel < StatusIcons.BATTERY_LEVEL_BAR_ICONS.length){
				return StatusIcons.BATTERY_LEVEL_BAR_ICONS[mBatteryLevel];
			}
			
		case SettingLoader.STATUS_ICON_BATTERY_TEMPERATURE_FAR:
			int far = StatusIcons.getFarTemp(mBatteryTemp);
			if(far >= 0 && far < StatusIcons.BATTERY_TEMP_ICONS_FAH.length){
				return StatusIcons.BATTERY_TEMP_ICONS_FAH[far];
			}
		}
		return R.drawable.statusbar;
	}
	
	private void startReceiver(){
		stopReceiver();
		if(!mSettingLoader.needsBatteryInfo()){
			DebugHelper.print("Receiver stopped");
			return;
		}
		mReceiver = new SystemEventReceiver(this);
		IntentFilter filter = new IntentFilter();
		if(mSettingLoader.needsBatteryInfo()){
			filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		}
		mContext.registerReceiver(mReceiver, filter);
    	DebugHelper.print("Receiver started");
	}
	
	private void stopReceiver(){
		if(mReceiver != null){
			mContext.unregisterReceiver(mReceiver);
			mReceiver = null;
		}
	}
	
	private class SystemEventReceiver extends BroadcastReceiver {

		private NotificationService mService;
		
		private SystemEventReceiver(NotificationService service){
			mService = service;
		}
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if(Intent.ACTION_BATTERY_CHANGED.equals(action)){
				boolean update = false;
				if(mSettingLoader.hasStatusIconTypeOf(SettingLoader.STATUS_ICON_BATTERY_LEVEL) ||
						mSettingLoader.hasStatusIconTypeOf(SettingLoader.STATUS_ICON_BATTERY_LEVEL_BAR)){
					int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
					if(level > -1 && level != mService.mBatteryLevel){
						mService.mBatteryLevel = level;
						update = true;
					}
				}
				if(mSettingLoader.hasStatusIconTypeOf(SettingLoader.STATUS_ICON_BATTERY_TEMPERATURE) ||
						mSettingLoader.hasStatusIconTypeOf(SettingLoader.STATUS_ICON_BATTERY_TEMPERATURE_FAR)){
					int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
					temperature = (temperature + 5) / 10;
					if(temperature > -1 && temperature != mService.mBatteryTemp){
						mService.mBatteryTemp = temperature;
						update = true;
					}
				}
				if(update){
					mService.updateNotification(false);
				}
			}
			
		}
		
	}
}
