package jp.co.shekeen.WidgetHolder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ServiceStarter extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if(Intent.ACTION_BOOT_COMPLETED.equals(action)){
			// コンストラクタで通知を表示してくれる
			new NotificationBuilder(context);
		}
	}
}
