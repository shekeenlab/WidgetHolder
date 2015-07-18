package jp.co.shekeen.WidgetHolder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

public class SettingLoader {

	public static final int PRIORITY_MIN = -2;
	public static final int PRIORITY_HIGH = 1;
	public static final int STATUS_ICON_DEFAULT = 0;
	public static final int STATUS_ICON_TRANSPARENT = 1;
	public static final int STATUS_ICON_BATTERY_LEVEL = 2;
	public static final int STATUS_ICON_BATTERY_TEMPERATURE = 3;
	public static final int STATUS_ICON_BATTERY_LEVEL_BAR = 4;
	public static final int STATUS_ICON_BATTERY_TEMPERATURE_FAR = 5;
	public static final int ASSIST_EXPAND_NOTIFICATION = 0;
	public static final int ASSIST_EXPAND_QUICK_SETTINGS = 1;
	public static final int DIRECT_DIAL_NORMAL = 0;
	public static final int DIRECT_DIAL_MANUAL_START = 1;
	public static final int DIRECT_DIAL_REDIRECT_INTENT = 2;
	
	public static final String REFERENCES_SETTING = "setting";
	public static final String ACTION_SETTING_CHANGED ="jp.co.shekeen.WidgetHolder.ACTION_SETTING_CHANGED";
	public static final String EXTRA_CHANGED_KEY ="jp.co.shekeen.WidgetHolder.EXTRA_CHANGED_KEY";
	public static final String EXTRA_NEW_VALUE ="jp.co.shekeen.WidgetHolder.EXTRA_NEW_VALUE";
	public String key_priority;
	public String key_transparent;
	public String key_show_title;
	public String key_column_count;
	public String key_smaller_height;
	public String key_ics_compat;
	public String key_status_icon1;
	public String key_status_icon2;
	public String key_status_icon3;
	public String key_widget_compat;
	public String key_assist_action;
	public String key_direct_dial;
	
	private Context mContext;
	private SharedPreferences mSettingPref;
	private int mPriority;
	private boolean mTransparent;
	private boolean mShowTitle;
	private int mColumnCount;
	private boolean mSmaller;
	private boolean mIcsCompat;
	private int mStatusIcon1;
	private int mStatusIcon2;
	private int mStatusIcon3;
	private boolean mWidgetCompat;
	private int mAssistAction;
	private int mDirectDial;
	
	public SettingLoader(Context context){
		key_priority = context.getString(R.string.key_priority);
		key_transparent = context.getString(R.string.key_transparent);
		key_show_title = context.getString(R.string.key_show_title);
		key_column_count = context.getString(R.string.key_column_count);
		key_smaller_height = context.getString(R.string.key_smaller_height);
		key_ics_compat = context.getString(R.string.key_ics_compat);
		key_status_icon1 = context.getString(R.string.key_status_icon1);
		key_status_icon2 = context.getString(R.string.key_status_icon2);
		key_status_icon3 = context.getString(R.string.key_status_icon3);
		key_widget_compat = context.getString(R.string.key_widget_compat);
		key_assist_action = context.getString(R.string.key_assist_action);
		key_direct_dial = context.getString(R.string.key_direct_dial);
		
		mContext = context;
		mSettingPref = context.getSharedPreferences(REFERENCES_SETTING, Context.MODE_MULTI_PROCESS);
		mPriority = mSettingPref.getInt(key_priority, PRIORITY_HIGH);
		mTransparent = mSettingPref.getBoolean(key_transparent, false);
		mShowTitle = mSettingPref.getBoolean(key_show_title, true);
		mColumnCount = mSettingPref.getInt(key_column_count, ColumnCountPreference.DEFAULT_VALUE);
		if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){/* IF JB */
			mSmaller = mSettingPref.getBoolean(key_smaller_height, true);/* 縮小表示をデフォルトにする */
			mIcsCompat = mSettingPref.getBoolean(key_ics_compat, false);
		}
		else{/* IF ICS */
			mSmaller = true;
			mIcsCompat = true;
		}
		initStatusIconInt(key_status_icon1);
		initStatusIconInt(key_status_icon2);
		initStatusIconInt(key_status_icon3);
		mStatusIcon1 = Integer.valueOf(mSettingPref.getString(key_status_icon1, "0"));
		mStatusIcon2 = Integer.valueOf(mSettingPref.getString(key_status_icon2, "0"));
		mStatusIcon3 = Integer.valueOf(mSettingPref.getString(key_status_icon3, "0"));
		mWidgetCompat = mSettingPref.getBoolean(key_widget_compat, false);
		/* ListPreferenceの初期化をここで行う */
		initListPreference();
		mAssistAction = Integer.valueOf(mSettingPref.getString(key_assist_action, "0"));
		mDirectDial = Integer.valueOf(mSettingPref.getString(key_direct_dial, "0"));
	}
	
	/* 初期値を設定する。前のバージョンとの整合性をここで取る。 */
	private void initStatusIconInt(String key){
		int val = Integer.valueOf(mSettingPref.getString(key, "-1"));
		if(val >= 0){
			return;
		}
		if(mTransparent){
			mSettingPref.edit().putString(key, String.valueOf(STATUS_ICON_TRANSPARENT)).commit();
		}
		else{
			mSettingPref.edit().putString(key, String.valueOf(STATUS_ICON_DEFAULT)).commit();
		}
	}
	
	private void initListPreference(){
		/* 一度SharedPreferencesにコミットしないと、getEntryしたときにnullが返ってくる */
		int val = Integer.valueOf(mSettingPref.getString(key_assist_action, "-1"));
		if(val < 0){
			mSettingPref.edit().putString(key_assist_action, "0").commit();
		}
		
		val = Integer.valueOf(mSettingPref.getString(key_direct_dial, "-1"));
		if(val < 0){
			mSettingPref.edit().putString(key_direct_dial, "0").commit();
		}
	}
	
	public int getPriority(){
		return mPriority;
	}
	
	public boolean getTransparent(){
		return mTransparent;
	}
	
	public boolean getShowTitle(){
		return mShowTitle;
	}
	
	public int getColumnCount(){
		return mColumnCount;
	}
	
	public int getRowCount(){
		if(getSmaller()){
			return 3;
		}
		return 2;
	}
	
	public boolean getSmaller(){
		if(mIcsCompat){
			return true;
		}
		return mSmaller;
	}
	
	public boolean getIcsCompat(){
		return mIcsCompat;
	}
	
	public int getStatusIconType(int id){
		if(mPriority == PRIORITY_MIN){
			return STATUS_ICON_DEFAULT;
		}
		switch(id){
		case 0:
			return mStatusIcon1;
		case 1:
			return mStatusIcon2;
		case 2:
			return mStatusIcon3;
		default:
			return STATUS_ICON_DEFAULT;
		}
	}
	
	public boolean getWidgetCompat(){
		return mWidgetCompat;
	}
	
	public int getAssistAction(){
		return mAssistAction;
	}
	
	public int getDirectDial(){
		return mDirectDial;
	}
	
	public boolean hasStatusIconTypeOf(int iconType){
		if(mPriority == PRIORITY_MIN){
			return false;
		}
		if(mIcsCompat){
			return iconType == mStatusIcon1 || iconType == mStatusIcon2;
		}
		return iconType == mStatusIcon1;
	}
	
	public boolean needsBatteryInfo(){
		if(mPriority == PRIORITY_MIN){
			return false;
		}
		if(needsBatteryInfo(mStatusIcon1)){
			return true;
		}
		if(mIcsCompat){
			if(needsBatteryInfo(mStatusIcon2)){
				return true;
			}
			if(needsBatteryInfo(mStatusIcon3)){
				return true;
			}
		}
		return false;
	}
	
	private boolean needsBatteryInfo(int statusIcon){
		return statusIcon != STATUS_ICON_DEFAULT && statusIcon != STATUS_ICON_TRANSPARENT;
	}
	
	public SharedPreferences getPreferences(){
		return mSettingPref;
	}
	
	/* 設定画面用 */
	public void sendNewValue(String key, Object newValue){
		Intent intent = new Intent(mContext, NotificationService.class);
		intent.setAction(ACTION_SETTING_CHANGED);
		intent.putExtra(EXTRA_CHANGED_KEY, key);
		
		if(key_priority.equals(key)){
			intent.putExtra(SettingLoader.EXTRA_NEW_VALUE, (Integer)newValue);
		}
		else if(key_transparent.equals(key)){
			intent.putExtra(SettingLoader.EXTRA_NEW_VALUE, (Boolean)newValue);
		}
		else if(key_show_title.equals(key)){
			intent.putExtra(SettingLoader.EXTRA_NEW_VALUE, (Boolean)newValue);
		}
		else if(key_column_count.equals(key)){
			intent.putExtra(SettingLoader.EXTRA_NEW_VALUE, (Integer)newValue);
		}
		else if(key_smaller_height.equals(key)){
			intent.putExtra(SettingLoader.EXTRA_NEW_VALUE, (Boolean)newValue);
		}
		else if(key_ics_compat.equals(key)){
			intent.putExtra(SettingLoader.EXTRA_NEW_VALUE, (Boolean)newValue);
		}
		else if(key_status_icon1.equals(key)){
			intent.putExtra(SettingLoader.EXTRA_NEW_VALUE, (String)newValue);
		}
		else if(key_status_icon2.equals(key)){
			intent.putExtra(SettingLoader.EXTRA_NEW_VALUE, (String)newValue);
		}
		else if(key_status_icon3.equals(key)){
			intent.putExtra(SettingLoader.EXTRA_NEW_VALUE, (String)newValue);
		}
		else if(key_widget_compat.equals(key)){
			intent.putExtra(SettingLoader.EXTRA_NEW_VALUE, (Boolean)newValue);
		}
		else if(key_assist_action.equals(key)){
			intent.putExtra(SettingLoader.EXTRA_NEW_VALUE, (String)newValue);
		}
		else if(key_direct_dial.equals(key)){
			intent.putExtra(SettingLoader.EXTRA_NEW_VALUE, (String)newValue);
		}
		else{
			DebugHelper.print("UNKNOWN KEY", key);
		}
		DebugHelper.print("SEND NEW VALUE", key, newValue);
		mContext.startService(intent);
	}
	
	/* NotificationService用 */
	public void updateValue(Intent intent){
		String key = intent.getStringExtra(EXTRA_CHANGED_KEY);
		
		if(key_priority.equals(key)){
			mPriority = intent.getIntExtra(EXTRA_NEW_VALUE, PRIORITY_HIGH);
		}
		else if(key_transparent.equals(key)){
			mTransparent = intent.getBooleanExtra(EXTRA_NEW_VALUE, false);
		}
		else if(key_show_title.equals(key)){
			mShowTitle = intent.getBooleanExtra(EXTRA_NEW_VALUE, false);
		}
		else if(key_column_count.equals(key)){
			mColumnCount = intent.getIntExtra(EXTRA_NEW_VALUE, ColumnCountPreference.DEFAULT_VALUE);
		}
		else if(key_smaller_height.equals(key)){
			mSmaller = intent.getBooleanExtra(EXTRA_NEW_VALUE, false);
		}
		else if(key_ics_compat.equals(key)){
			mIcsCompat = intent.getBooleanExtra(EXTRA_NEW_VALUE, false);
		}
		else if(key_status_icon1.equals(key)){
			mStatusIcon1 = Integer.valueOf(intent.getStringExtra(EXTRA_NEW_VALUE));
		}
		else if(key_status_icon2.equals(key)){
			mStatusIcon2 = Integer.valueOf(intent.getStringExtra(EXTRA_NEW_VALUE));
		}
		else if(key_status_icon3.equals(key)){
			mStatusIcon3 = Integer.valueOf(intent.getStringExtra(EXTRA_NEW_VALUE));
		}
		else if(key_widget_compat.equals(key)){
			mWidgetCompat = intent.getBooleanExtra(EXTRA_NEW_VALUE, false);
		}
		else if(key_assist_action.equals(key)){
			mAssistAction = Integer.valueOf(intent.getStringExtra(EXTRA_NEW_VALUE));
		}
		else if(key_direct_dial.equals(key)){
			mDirectDial = Integer.valueOf(intent.getStringExtra(EXTRA_NEW_VALUE));
		}
		else{
			DebugHelper.print("UNKNOWN KEY", key);
		}
	}
}
