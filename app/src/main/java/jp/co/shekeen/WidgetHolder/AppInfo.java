package jp.co.shekeen.WidgetHolder;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Parcel;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView.ScaleType;

public class AppInfo extends CellInfo {

	public static final String EXTRA_INTENT = "jp.co.shekeen.WidgetHolder.AppInfo.extra.INTENT";
	private String mAppName;
	private Intent mIntent;
	private Bitmap mIconToSave;/* 初めてDBに保存するときのみ使う。基本的にNULL */
	
	/* Parcelable専用。通常はSettingLoaderで初期化する */
	public AppInfo(){
	}
	
	public AppInfo(Context context, Intent intent, String name, Bitmap icon){
		super();
		/* AppSelectActivityでデフォルト値も含めて詰め込むので、例外処理は不要のはず。 */
		mIntent = intent;
		mAppName = name;
		mIconToSave = icon;
	}
	
	public static AppInfo createFromCursor(Context context, Cursor cursor){
		AppInfo appInfo = new AppInfo();
		try {
			String uri = cursor.getString(SettingColumns.INDEX_INTENT);
			if(uri != null){
				appInfo.mIntent = Intent.parseUri(uri, 0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		appInfo.mAppName = cursor.getString(SettingColumns.INDEX_TITLE);
		
		/* IntentがDBに保存されていなかったときの次善の策 */
		if(appInfo.mIntent == null){
			String packageName = cursor.getString(SettingColumns.INDEX_PACKAGE_NAME);
			String activityName = cursor.getString(SettingColumns.INDEX_ACTIVITY_NAME);
			appInfo.mIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
			if(appInfo.mIntent == null){/* SDカードに置かれたアプリは読み込みに失敗する。そしたら、次次善の策。 ただし、元からnullを返すアプリが存在する？*/
				appInfo.mIntent = new Intent(Intent.ACTION_MAIN);
				appInfo.mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				appInfo.mIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			}
			if(activityName != null && activityName.length() > 0){
				appInfo.mIntent.setClassName(packageName, activityName);
			}
		}
		
		return appInfo;
	}

	@Override
	public boolean isSpacer() {
		return false;
	}

	@Override
	protected int getCellType() {
		return SettingColumns.CELL_TYPE_APPLICATION;
	}

	@Override
	public View createView(CellLayout cellLayout) {
		Context context = cellLayout.getContext();
		LinearLayout base = new LinearLayout(context);
		base.setOrientation(LinearLayout.VERTICAL);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		base.setLayoutParams(params);
		int padding;
		if(mSmaller){
			padding = context.getResources().getDimensionPixelSize(R.dimen.padding_small);
		}
		else{
			padding = context.getResources().getDimensionPixelSize(R.dimen.padding_medium);
		}
		base.setPadding(0, padding, 0, 0);
		
		ImageView imageAppIcon = new ImageView(context);
		imageAppIcon.setImageURI(mUri);
		base.addView(imageAppIcon);
		LinearLayout.LayoutParams lparams;
		int iconSize;
		if(mSmaller){
			iconSize = context.getResources().getDimensionPixelSize(R.dimen.appicon_size_s);
		}
		else{
			iconSize = context.getResources().getDimensionPixelSize(R.dimen.appicon_size);
		}
		lparams = (LinearLayout.LayoutParams) imageAppIcon.getLayoutParams();
		lparams.width = LayoutParams.MATCH_PARENT;
		lparams.height = iconSize;
		imageAppIcon.setLayoutParams(lparams);
		imageAppIcon.setScaleType(ScaleType.FIT_CENTER);
		imageAppIcon.setAdjustViewBounds(true);
		
		if(mShowTitle){
			TextView textAppName = new TextView(context);
			textAppName.setText(mAppName);
			base.addView(textAppName);
			lparams = (LinearLayout.LayoutParams) textAppName.getLayoutParams();
			lparams.width = LayoutParams.MATCH_PARENT;
			lparams.height = LayoutParams.WRAP_CONTENT;
			textAppName.setLayoutParams(lparams);
			textAppName.setGravity(Gravity.CENTER);
			textAppName.setTextColor(Color.WHITE);
			textAppName.setMaxLines(1);
		}
		
		return base;
	}
	
	@Override
	public RemoteViews createRemoteViews(Context context, RemoteViews widgetView) {
		if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){/* IF JELLY BEAN AND HIGHER */
			return createRemoteForJB(context);
		}
		else{
			return createRemoteForICS(context);
		}
	}

	private RemoteViews createRemoteForICS(Context context){
		RemoteViews launcher;
		if(!mShowTitle){
			if(mSmaller){
				launcher = new RemoteViews(context.getPackageName(), R.layout.launcher_no_title_s);
			}
			else{
				launcher = new RemoteViews(context.getPackageName(), R.layout.launcher_no_title);
			}
		}
		else{
			if(mSmaller){
				launcher = new RemoteViews(context.getPackageName(), R.layout.launcher_s);
			}
			else{
				launcher = new RemoteViews(context.getPackageName(), R.layout.launcher);
			}
			launcher.setTextViewText(R.id.textAppName, mAppName);
		}
		
		if(mIntent != null){
			Intent intent = new Intent(context, NotificationService.class);
			intent.setAction(NotificationService.ACTION_LAUNCH_APP);
			intent.putExtra(EXTRA_INTENT, mIntent);
			PendingIntent pendingIntent = PendingIntent.getService(context, getIntentCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
			launcher.setOnClickPendingIntent(R.id.imageBlocker, pendingIntent);
		}
		launcher.setImageViewUri(R.id.imageAppIcon, mUri);
		return launcher;
	}
	
	private RemoteViews createRemoteForJB(Context context){
		RemoteViews launcher;
		if(!mShowTitle){
			if(mSmaller){
				launcher = new RemoteViews(context.getPackageName(), R.layout.launcher_no_title_s);
			}
			else{
				launcher = new RemoteViews(context.getPackageName(), R.layout.launcher_no_title);
			}
		}
		else{
			if(mSmaller){
				launcher = new RemoteViews(context.getPackageName(), R.layout.launcher_s);
			}
			else{
				launcher = new RemoteViews(context.getPackageName(), R.layout.launcher);
			}
			launcher.setTextViewText(R.id.textAppName, mAppName);
		}
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		launcher.setOnClickPendingIntent(R.id.imageBlocker, pendingIntent);
		launcher.setImageViewUri(R.id.imageAppIcon, mUri);
		return launcher;
	}

	@Override
	public void onAddToDatabase(ContentValues values) {
		super.onAddToDatabase(values);
		/* パッケージ名ではなく、IntentをtoUriで保存する。 */
		String uri = mIntent.toUri(0);
		values.put(SettingColumns.KEY_INTENT, uri);
		values.put(SettingColumns.KEY_TITLE, mAppName);
		if(mIconToSave != null){
			BitmapUtil.writeBitmap(values, SettingColumns.KEY_ICON_BITMAP, mIconToSave);
			/* DBに保存したのでビットマップを破棄。後はURIでのみアクセスする。 */
			mIconToSave.recycle();
			mIconToSave = null;
		}
	}

	public static void launchApp(Context context, Intent intent){
		MyStatusBarManager statusbar = MyStatusBarManager.getInstance(context);
		statusbar.collapse();
		Intent launcher = intent.getParcelableExtra(EXTRA_INTENT);
		context.startActivity(launcher);
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeParcelable(mIntent, flags);
		dest.writeString(mAppName);
	}
	
	public static AppInfo createFromParcel(Parcel source){
		AppInfo info = new AppInfo();
		info.mIntent = source.readParcelable(Intent.class.getClassLoader());
		info.mAppName = source.readString();
		return info;
	}
}
