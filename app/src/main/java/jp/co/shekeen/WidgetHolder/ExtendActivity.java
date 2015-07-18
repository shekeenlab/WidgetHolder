package jp.co.shekeen.WidgetHolder;

import android.app.Activity;
import android.os.Bundle;

public class ExtendActivity extends Activity {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SettingLoader settingLoader = new SettingLoader(this);
        int assistAction = settingLoader.getAssistAction();
        MyStatusBarManager statusbar = MyStatusBarManager.getInstance(this);
        if(assistAction == SettingLoader.ASSIST_EXPAND_NOTIFICATION){
        	statusbar.expand();
        }
        else if(assistAction == SettingLoader.ASSIST_EXPAND_QUICK_SETTINGS){
        	statusbar.expandSettingsPanel();
        }
		finish();
	}
}
