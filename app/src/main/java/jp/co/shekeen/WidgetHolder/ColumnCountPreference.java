package jp.co.shekeen.WidgetHolder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

public class ColumnCountPreference extends DialogPreference implements OnClickListener {

	public static final int DEFAULT_VALUE = 4;
	private static final int MIN_VALUE = 1;
	private static final int MAX_VALUE = 12;
	private Context mContext;
	private NumberPicker mPickerColumn;
	private int mOriginalValue;
	private int mCurrentValue;
	private AlertDialog mAlertDialog;
	private SettingLoader mSettingLoader;/* 設定送信用 */
	
	public ColumnCountPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		mSettingLoader = new SettingLoader(context);
	}

	public ColumnCountPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mSettingLoader = new SettingLoader(context);
	}

	@Override
	protected View onCreateDialogView() {
		View view = View.inflate(mContext, R.layout.column_count, null);
		mPickerColumn = (NumberPicker) view.findViewById(R.id.pickerColumn);
		mPickerColumn.setMinValue(MIN_VALUE);
		mPickerColumn.setMaxValue(MAX_VALUE);
		mPickerColumn.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		SharedPreferences pref = getSharedPreferences();
		mOriginalValue = pref.getInt(getKey(), DEFAULT_VALUE);
		mPickerColumn.setValue(mOriginalValue);
		return view;
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if(positiveResult){
			mCurrentValue = mPickerColumn.getValue();
			if(mCurrentValue < mOriginalValue){
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle(mContext.getString(R.string.title_decrease_column));
				builder.setMessage(mContext.getString(R.string.message_decrease_column));
				builder.setPositiveButton(mContext.getString(R.string.button_yes), this);
				builder.setNegativeButton(mContext.getString(R.string.button_no), this);
				mAlertDialog = builder.create();
				mAlertDialog.show();
			}
			else{
				persistInt(mCurrentValue);
				/* 設定の変化を別プロセスのサービスに通知 */
				mSettingLoader.sendNewValue(mSettingLoader.key_column_count, mCurrentValue);
			}
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		super.onClick(dialog, which);
		
		if(mAlertDialog == dialog && which == DialogInterface.BUTTON_POSITIVE){
			persistInt(mCurrentValue);
			/* 設定の変化を別プロセスのサービスに通知 */
			mSettingLoader.sendNewValue(mSettingLoader.key_column_count, mCurrentValue);
		}
	}
}
