package jp.co.shekeen.WidgetHolder;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.SparseArray;
import android.view.MenuItem;

public class SettingActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SettingFragment fragment = new SettingFragment();
		
		try {
			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
			fragment.setPackageInfo(info);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		getFragmentManager().beginTransaction()
		.replace(android.R.id.content, fragment)
		.commit();
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
		actionBar.setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int id = item.getItemId();
		if(id == android.R.id.home){
			finish();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	public static class SettingFragment extends PreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener, OnClickListener {
		
		private SharedPreferences mPref;
		private CheckBoxPreference mCheckLowPrio;
		private CheckBoxPreference mCheckShowTitle;
		private CheckBoxPreference mCheckSmallerHeight;
		private ListPreference mListStatusIcon1;
		private ListPreference mListAssistAction;
		private Preference mColumnCount;
		private Preference mUpgrade;
		private PackageInfo mPackageInfo;
		private SparseArray<String> mIconTypes;
		private SparseArray<String> mAssistTypes;
		private AlertDialog mAlertDialog;
		private String mAssistSummary;
		private SettingLoader mLoader;
		
		/* 設定画面でしか使用しないキー */
		private String mKeyUseLowPrio;
		private String mKeyUpgrade;
		
		public void setPackageInfo(PackageInfo info){
			mPackageInfo = info;
		}
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			PreferenceManager prefManager = getPreferenceManager();
			prefManager.setSharedPreferencesName(SettingLoader.REFERENCES_SETTING);
			prefManager.setSharedPreferencesMode(Context.MODE_PRIVATE);
			if(Config.FREE){
				addPreferencesFromResource(R.xml.setting_free);
			}
			else{
				addPreferencesFromResource(R.xml.setting);
			}
			
			mLoader = new SettingLoader(getActivity());
			mPref = mLoader.getPreferences();
			
			/* 設定専用キーの初期化 */
			mKeyUseLowPrio = getString(R.string.key_use_low_prio);
			mKeyUpgrade = getString(R.string.key_upgrade);
			
			mListStatusIcon1 = (ListPreference) findPreference(mLoader.key_status_icon1);
			String[] statusIconTypes = getResources().getStringArray(R.array.status_icon_types);
			String[] statusIconVals = getResources().getStringArray(R.array.status_icon_type_vals);
			mIconTypes = new SparseArray<String>();
			for(int i = 0; i < statusIconTypes.length; i++){
				int index = Integer.valueOf(statusIconVals[i]);
				mIconTypes.put(index, statusIconTypes[i]);
			}
			mListStatusIcon1.setSummary(mListStatusIcon1.getEntry());
			mListStatusIcon1.setOnPreferenceChangeListener(this);

			mCheckShowTitle = (CheckBoxPreference) findPreference(mLoader.key_show_title);
			mCheckShowTitle.setOnPreferenceChangeListener(this);
			
			/* アシスト動作の設定値から説明文を逆引きするハッシュを生成 */
			String[] assistActionTypes = getResources().getStringArray(R.array.assist_action_types);
			String[] assistActionVals = getResources().getStringArray(R.array.assist_action_type_vals);
			mAssistTypes = new SparseArray<String>();
			for(int i = 0; i < assistActionTypes.length; i++){
				int index = Integer.valueOf(assistActionVals[i]);
				mAssistTypes.put(index, assistActionTypes[i]);
			}

			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){/* 4.1以上ならばメニューを設定 */
				/* アシストアクションの設定 */
				mListAssistAction = (ListPreference) findPreference(mLoader.key_assist_action);
				mAssistSummary = "\n" + getResources().getString(R.string.summary_assist_action);
				mListAssistAction.setSummary(mListAssistAction.getEntry() + mAssistSummary);
				mListAssistAction.setOnPreferenceChangeListener(this);
			}

			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){/* IF JB */
				mCheckLowPrio = (CheckBoxPreference) findPreference(mKeyUseLowPrio);
				mCheckSmallerHeight = (CheckBoxPreference) findPreference(mLoader.key_smaller_height);

				mCheckLowPrio.setOnPreferenceChangeListener(this);
				mCheckSmallerHeight.setOnPreferenceChangeListener(this);

				mListStatusIcon1.setEnabled(!mCheckLowPrio.isChecked());
			}
			
			if(mPackageInfo != null){
				Preference prefVersion = findPreference(getString(R.string.key_version));
				String version = "Version " + mPackageInfo.versionName + "   (c) shekeen lab.";
				prefVersion.setSummary(version);
			}
			
			if(Config.FREE){
				mColumnCount = findPreference(mLoader.key_column_count);
				mColumnCount.setSummary(getString(R.string.summary_no_column_count));
				mColumnCount.setEnabled(false);
				
				mUpgrade = findPreference(mKeyUpgrade);
				mUpgrade.setOnPreferenceClickListener(this);
			}
		}
		
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String key = preference.getKey();
			if(preference == mCheckLowPrio){
				if((Boolean)newValue){
					mPref.edit().putInt(mLoader.key_priority, SettingLoader.PRIORITY_MIN).commit();
				}
				else{
					mPref.edit().putInt(mLoader.key_priority, SettingLoader.PRIORITY_HIGH).commit();
				}
				mListStatusIcon1.setEnabled(!(Boolean)newValue);
				/* 配信されるキーと値を上書き */
				key = mLoader.key_priority;
				newValue = (Boolean)newValue ? SettingLoader.PRIORITY_MIN : SettingLoader.PRIORITY_HIGH;
			}
			else if(preference == mListStatusIcon1){
				String entry = mIconTypes.get(Integer.valueOf((String)newValue));
				if(entry != null){
					mListStatusIcon1.setSummary(entry);
				}
			}
			else if(preference == mListAssistAction){
				int value = Integer.valueOf((String)newValue);
				String entry = mAssistTypes.get(value);
				if(entry != null){
					mListAssistAction.setSummary(entry + mAssistSummary);
				}
				PackageManager pm = getActivity().getPackageManager();
				ComponentName component = new ComponentName(getActivity(), ExtendActivity.class);
				if(value == -1){/* アシストを無効にする */
					pm.setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				}
				else{/* アシストを有効にする */
					pm.setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
				}
			}
			else if(preference == mCheckSmallerHeight){
				if(!(Boolean)newValue){/* trueからfalseに変わったならば */
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle(getString(R.string.title_decrease_row));
					builder.setMessage(getString(R.string.message_decrease_row));
					builder.setPositiveButton(getString(R.string.button_yes), this);
					builder.setNegativeButton(getString(R.string.button_no), this);
					mAlertDialog = builder.create();
					mAlertDialog.show();
					return false;
				}
			}
			/* 設定の変化を別プロセスのサービスに通知 */
			mLoader.sendNewValue(key, newValue);
			return true;
		}

		@Override
		public boolean onPreferenceClick(Preference preference) {
			if(preference == mUpgrade){
				Uri uri = Uri.parse("market://details?id=jp.co.shekeen.Widget" + "HolderEx");/* merge.shによる置換を苦し紛れの回避 */
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
			return true;
		}
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			/* 縮小表示OFF時の３列目削除の確認ダイアログに対する応答 */
			if(mAlertDialog == dialog && which == DialogInterface.BUTTON_POSITIVE){
				mCheckSmallerHeight.setOnPreferenceChangeListener(null);/* 余計なコールバックを呼ばない */
				mCheckSmallerHeight.setChecked(false);
				mCheckSmallerHeight.setOnPreferenceChangeListener(this);
				/* 設定の変化を別プロセスのサービスに通知 */
				mLoader.sendNewValue(mLoader.key_smaller_height, false);
			}
		}
		
	}

}
