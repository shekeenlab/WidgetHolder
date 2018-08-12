package jp.co.shekeen.WidgetHolder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.os.Build;
import android.widget.FrameLayout;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class NotificationBuilder {

	private static final String CHANNEL_ID = "WidgetHolder";
	private GridNotification mGridNotif;
	private List<Integer> mNotifIdList;
	private RemoteViews[] mRemoteNotifs;
	private SettingLoader mSettingLoader;
	private Context mContext;

	public NotificationBuilder(Context context) {

		DebugHelper.print("SERVICE CREATE");
		mContext = context;

		/* MainActivityとはプロセスが異なるので静的クラスの初期化は注意 */
		Config.initialize(mContext);
        SettingColumns.initialize(mContext);
        InitChannel();
		
        mSettingLoader = new SettingLoader(mContext);
		mGridNotif = new GridNotification(mContext, mSettingLoader);
		mNotifIdList = new ArrayList<Integer>();
		updateNotification(true);
	}

	private void InitChannel(){
		/* Android8向けに通知チャネルを設定する */
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
			NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Show launcher", NotificationManager.IMPORTANCE_HIGH);
			channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
			channel.enableLights(false);
			channel.enableVibration(false);
			channel.setShowBadge(false);
			channel.setBypassDnd(false);
			// 音を鳴らさないための設定
			AudioAttributes.Builder builder = new AudioAttributes.Builder();
			builder.setUsage(AudioAttributes.USAGE_NOTIFICATION);
			builder.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION);
			AudioAttributes audioattr = builder.build();
			channel.setSound(null, audioattr);

			manager.createNotificationChannel(channel);
		}
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
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
			builder.setChannelId(CHANNEL_ID);
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
		switch(iconType) {
			case SettingLoader.STATUS_ICON_DEFAULT:
				return R.drawable.statusbar;

			case SettingLoader.STATUS_ICON_TRANSPARENT:
				return R.drawable.transparent;
		}
		return R.drawable.statusbar;
	}
}
