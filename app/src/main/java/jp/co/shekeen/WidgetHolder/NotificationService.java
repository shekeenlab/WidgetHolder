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

public class NotificationService extends Service implements IAppWidgetCallback {
	
	public static final String ACTION_LAUNCH_APP = "jp.co.shekeen.WidgetHolder.ACTION_LAUNCH_APP";
	public static final String ACTION_LAUNCH_SHORTCUT = "jp.co.shekeen.WidgetHolder.ACTION_LAUNCH_SHORTCUT";
	public static final String ACTION_LAUNCH_WIDGET = "jp.co.shekeen.WidgetHolder.ACTION_LAUNCH_WIDGET";
	public static final String ACTION_COLUMN_COUNT_CHANGED = "jp.co.shekeen.WidgetHolder.ACTION_COLUMN_COUNT_CHANGED";
	public static final String ACTION_SMALLER_CHANGED = "jp.co.shekeen.WidgetHolder.ACTION_SMALLER_CHANGED";
	public static final String ACTION_ICS_COMPAT_CHANGED = "jp.co.shekeen.WidgetHolder.ACTION_ICS_COMPAT_CHANGED";
	public static final String ACTION_SHOW_TITLE_CHANGED = "jp.co.shekeen.WidgetHolder.ACTION_SHOW_TITLE_CHANGED";
	private static final int FORGROUND_NOTIFICATION_ID = 100;
	private ServiceBinder mBinder;
	private MyAppWidgetHost mWidgetHost;
	private IAppWidgetCallback mCallbacks;
	private GridNotification mGridNotif;
	private Handler mDelayHandler;
	private List<Integer> mNotifIdList;
	private SystemEventReceiver mReceiver;
	private int mBatteryLevel = 0;
	private int mBatteryTemp = 0;
	private RemoteViews[] mRemoteNotifs;
	private SettingLoader mSettingLoader;
	
	private static class ServiceBinder extends INotificationService.Stub {

		private NotificationService mService;
		
		private ServiceBinder(NotificationService service){
			mService = service;
		}
		
		@Override
		public CellInfo[] getCellInfos() throws RemoteException {
			return mService.getCellInfos();
		}

		@Override
		public void registerCallbacks(IAppWidgetCallback callback) throws RemoteException {
			mService.registerCallbacks(callback);
		}

		@Override
		public void updateCellInfos(CellInfo[] cellInfos) throws RemoteException {
			mService.updateCellInfos(cellInfos);
		}

		@Override
		public int allocateAppWidgetId() throws RemoteException {
			return mService.allocateAppWidgetId();
		}

		@Override
		public void deleteAppWidgetId(int appWidgetId) throws RemoteException {
			mService.deleteAppWidgetId(appWidgetId);
		}

		@Override
		public void addCellInfo(CellInfo cellInfo) throws RemoteException {
			mService.addCellInfo(cellInfo);
		}

		@Override
		public void deleteCellInfo(CellInfo cellInfo) throws RemoteException {
			mService.deleteCellInfo(cellInfo);
		}
		
	}
	
	@Override
	public IBinder asBinder() {
		return null;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		mCallbacks = null;
		return super.onUnbind(intent);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		DebugHelper.print("SERVICE CREATE");
		mBinder = new ServiceBinder(this);
		mWidgetHost = new MyAppWidgetHost(this, this);
		mWidgetHost.startListening();
		
		/* MainActivityとはプロセスが異なるので静的クラスの初期化は注意 */
		Config.initialize(this);
        SettingColumns.initialize(this);
		
        mSettingLoader = new SettingLoader(this);
		mGridNotif = new GridNotification(this, mSettingLoader);
		mDelayHandler = new DelayHandler(this);
		updateNotificationDelayed(100);
		mNotifIdList = new ArrayList<Integer>();
		
		startReceiver();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		DebugHelper.print("SERVICE START");
		if(intent != null){
			String action = intent.getAction();
			try{
				if(ACTION_LAUNCH_APP.equals(action)){
					AppInfo.launchApp(this, intent);
				}
				else if(ACTION_LAUNCH_SHORTCUT.equals(action)){
					ShortcutInfo.launchShortcut(this, intent);
				}
				else if(ACTION_LAUNCH_WIDGET.equals(action)){
					WidgetInfo.launchWidget(this, intent);
				}
				else if(SettingLoader.ACTION_SETTING_CHANGED.equals(action)){
					onSettingChanged(intent);
					
				}
			}
			catch(RuntimeException e){
				Toast.makeText(this, getString(R.string.error_launch_failed), Toast.LENGTH_LONG).show();
				DebugHelper.printStackTrace(e);
			}
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		DebugHelper.print("SERVICE DESTROY");
		stopReceiver();
		mSettingLoader = null;
		super.onDestroy();
	}
	
	public CellInfo[] getCellInfos(){
		return mGridNotif.getCellInfos();
	}
	
	public void addCellInfo(CellInfo cellInfo){
		mGridNotif.addCellInfo(cellInfo);
		updateNotificationDelayed(100);
	}
	
	public void updateCellInfos(CellInfo[] cellInfos){
		mGridNotif.updateCellInfos(cellInfos);
		mGridNotif.updateAllView(mSettingLoader);
		updateNotificationDelayed(100);
	}
	
	public void deleteCellInfo(CellInfo cellInfo){
		int appWidgetId = cellInfo.getAppWidgetId();
		if(appWidgetId > 0){
			mWidgetHost.deleteAppWidgetId(appWidgetId);
		}
		mGridNotif.deleteCellInfo(cellInfo);
		updateNotificationDelayed(100);
	}
	
	private void updateWidget(int appWidgetId, RemoteViews remoteViews){
		if(remoteViews == null){
			mWidgetHost.deleteAppWidgetId(appWidgetId);
		}
		mWidgetHost.startListening();
		/* ここではウィジェットの追加は行わない。更新のみ。setAppWidgetIdsで追加する。 */
		mGridNotif.updateView(appWidgetId, remoteViews);
		updateNotificationDelayed(100);
	}
	
	public void registerCallbacks(IAppWidgetCallback callbacks){
		mCallbacks = callbacks;
	}
	
	@Override
	public void onUpdateAppWidgetView(int appWidgetId, RemoteViews views) {
		DebugHelper.print("onUpdateAppWidgetView");
		updateWidget(appWidgetId, views);
		if(mCallbacks != null){
			try {
				mCallbacks.onUpdateAppWidgetView(appWidgetId, views);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onProviderChanged(int appWidgetId, AppWidgetProviderInfo appWidget) {
		if(mCallbacks != null){
			try {
				mCallbacks.onProviderChanged(appWidgetId, appWidget);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onViewDataChanged(int appWidgetId, int viewId) {
		if(mCallbacks != null){
			try {
				mCallbacks.onViewDataChanged(appWidgetId, viewId);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	public int allocateAppWidgetId(){
		int appWidgetId = mWidgetHost.allocateAppWidgetId();
		mWidgetHost.startListening();
		return appWidgetId;
	}
	
	public void deleteAppWidgetId(int appWidgetId) {
		mWidgetHost.deleteAppWidgetId(appWidgetId);
	}
	
	public void onSettingChanged(Intent intent) {
		mSettingLoader.updateValue(intent);
		String key = intent.getStringExtra(SettingLoader.EXTRA_CHANGED_KEY);
		DebugHelper.print("CALLBACK", key);
		
		if(mSettingLoader.key_column_count.equals(key)){
			/* GridNotificationを再生成することではみ出たアイテムは削除される */
			mGridNotif = new GridNotification(this, mSettingLoader);
		}
		else if(mSettingLoader.key_smaller_height.equals(key)){
			mGridNotif = new GridNotification(this, mSettingLoader);
		}
		else if(mSettingLoader.key_ics_compat.equals(key)){
			/* IcsCompatが変更されたら必ずGridNotificationを再生成する必要があるわけではないが（SmallerHeightがfalseのときのみ）、 */
			/* あれこれ考えてもエンバグしそうなので、とりあえず再生成する */
			mGridNotif = new GridNotification(this, mSettingLoader);
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
		updateNotificationDelayed(100);
	}
	
	private void updateNotificationDelayed(int delay){
		mDelayHandler.sendMessageDelayed(new Message(), delay);
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
					FrameLayout testLayout = new FrameLayout(this);
					mRemoteNotifs[i].apply(this, testLayout);
					showNotification(mRemoteNotifs[i], i, validCount);
					mNotifIdList.add(i);
				}
				catch(RuntimeException e){
					e.printStackTrace();
					Toast.makeText(this, getString(R.string.error_id_conflist), Toast.LENGTH_LONG).show();
				}
				
			}
			else{
				hideNotification(i);
			}
		}
	}
	
	private void showNotification(RemoteViews remoteViews, int id, int total){
		Notification.Builder builder = new Notification.Builder(this);
		
		builder.setOngoing(true);
		builder.setSmallIcon(getStatusIcon(id));
		builder.setTicker(getString(R.string.app_name));
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
		
		/* 最初の１つは通知ではなく、フォアグラウンド開始で通知する。こうすることでサービスが停止されにくくなるはず。 */
		if(id == 0){
			/* idに0を渡すと通知が表示されないので、適当に大きい数字を渡す */
			startForeground(FORGROUND_NOTIFICATION_ID, notification);
		}
		else{
			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			notificationManager.notify(id, notification);
		}
	}
	
	private void hideNotification(int id){
		if(id == 0){
			stopForeground(true);
		}
		else{
			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			notificationManager.cancel(id);
		}
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
	
	private static class DelayHandler extends Handler{

		private NotificationService mService;
		
		private DelayHandler(NotificationService service){
			mService = service;
		}
		
		@Override
		public void handleMessage(Message msg) {
			mService.updateNotification();
		}
		
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
    	registerReceiver(mReceiver, filter);
    	DebugHelper.print("Receiver started");
	}
	
	private void stopReceiver(){
		if(mReceiver != null){
			unregisterReceiver(mReceiver);
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
