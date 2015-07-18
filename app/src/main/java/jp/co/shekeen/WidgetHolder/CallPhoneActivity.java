package jp.co.shekeen.WidgetHolder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class CallPhoneActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		if(intent != null){
			String action = intent.getAction();
			if(NotificationService.ACTION_LAUNCH_SHORTCUT.equals(action)){
				Intent launcher = intent.getParcelableExtra(ShortcutInfo.EXTRA_INTENT);
				launcher.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				try{
					startActivity(launcher);
				}
				catch(RuntimeException e){
					DebugHelper.printStackTrace(e);
				}
			}
		}
		
		finish();
	}

}
