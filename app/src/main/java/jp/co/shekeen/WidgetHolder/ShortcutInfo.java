package jp.co.shekeen.WidgetHolder;

import java.net.URISyntaxException;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView.ScaleType;

public class ShortcutInfo extends CellInfo {

	public static final String EXTRA_INTENT = "jp.co.shekeen.WidgetHolder.ShortcutInfo.extra.INTENT";
	private String mTitle;
	private Intent mIntent;
	private Bitmap mIconToSave;/* 初めてDBに保存するときのみ使う。基本的にNULL */
	private int mIconType;
	private String mPackageName;
	private String mIconName;
	private Uri mIconUri;/* 削除してよいかも */
	
	private ShortcutInfo(){
		super();
	}
	
	public ShortcutInfo(Context context, Intent intent){
		super();
		PackageManager pm = context.getPackageManager();
		mTitle = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
		mIntent = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
		Parcelable bitmap = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);
		Parcelable extra = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
		
		if(bitmap != null && bitmap instanceof Bitmap){
			mIconToSave = (Bitmap)bitmap;
			mIconType = SettingColumns.ICON_TYPE_BITMAP;
			/* コンストラクタの時点ではまだmUriはnullなので注意 */
		}
		else if(extra != null && extra instanceof ShortcutIconResource){
			ShortcutIconResource iconRes = (ShortcutIconResource)extra;
			try {
				mPackageName = iconRes.packageName;
				mIconName = iconRes.resourceName;
				Resources resources = pm.getResourcesForApplication(mPackageName);
				int iconResId = resources.getIdentifier(mIconName, null, null);
//				mIconUri = Uri.parse("android.resource://" + mPackageName + "/" + iconResId);
//				mIconType = SettingColumns.ICON_TYPE_RESOURCE;
				/* アプリのアップデートでリソースIDが変わることがあるので、SettingColumns.ICON_TYPE_RESOURCEは使わない。 */
				mIconToSave = BitmapFactory.decodeResource(resources, iconResId);
				mIconType = SettingColumns.ICON_TYPE_BITMAP;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		if(mIconToSave == null && mIconUri == null){
			mIconToSave = getDefaultIcon(pm);
			mIconType = SettingColumns.ICON_TYPE_BITMAP;
			mIconUri = mUri;
		}
		if(mTitle == null || mTitle.length() == 0){
			mTitle = getDefaultTitle(context);
		}
	}
	
	public static ShortcutInfo createFromCursor(Context context, Cursor cursor){
		ShortcutInfo info = new ShortcutInfo();
		String uri = cursor.getString(SettingColumns.INDEX_INTENT);
		try {
			info.mIntent = Intent.parseUri(uri, 0);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		};
		info.mTitle = cursor.getString(SettingColumns.INDEX_TITLE);
		info.mIconType = cursor.getInt(SettingColumns.INDEX_ICON_TYPE);
		if(info.mIconType == SettingColumns.ICON_TYPE_BITMAP){
			/* createFromCursorの時点ではまだmUriはnullなので注意 */
		}
		else{
			info.mPackageName = cursor.getString(SettingColumns.INDEX_ICON_PACKAGE);
			info.mIconName = cursor.getString(SettingColumns.INDEX_ICON_RESOURCE);
			PackageManager pm = context.getPackageManager();
			try {
				Resources resources = pm.getResourcesForApplication(info.mPackageName);
				int iconResId = resources.getIdentifier(info.mIconName, null, null);
				info.mIconUri = Uri.parse("android.resource://" + info.mPackageName + "/" + iconResId);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
				/* SDカード読み取り中でアプリが存在しないわけではない可能性あり。 
				 * デフォルトアイコンになってしまう。SDカード準備できたタイミングで再読み込みしたほうがよいかも。*/
			}
		}
		if(info.mTitle == null || info.mTitle.length() == 0){
			info.mTitle = getDefaultTitle(context);
		}
		return info;
	}

	@Override
	public boolean isSpacer() {
		return false;
	}

	@Override
	protected int getCellType() {
		return SettingColumns.CELL_TYPE_SHORTCUT;
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
		if(mIconType == SettingColumns.ICON_TYPE_BITMAP){
			/* 本当はmIconUriに生成時にmUriを代入しておきたいが、タイミングがないので仕方なく分岐 */
			imageAppIcon.setImageURI(mUri);
		}
		else{
			imageAppIcon.setImageURI(mIconUri);
		}
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
			textAppName.setText(mTitle);
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
		return createRemoteForJB(context);
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
			launcher.setTextViewText(R.id.textAppName, mTitle);
		}
		Intent argIntent = new Intent(mIntent);/* mIntentはそのまま残しておきたいので、コピーを作る。 */
		argIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);/* FLAG_ACTIVITY_RESET_TASK_IF_NEEDEDをセットするとEverNoteのショートカットが起動できなくなる */
		
		/* パーミッション不要のダイアル動作に置き換える。 */
		if(mDirectDial == SettingLoader.DIRECT_DIAL_MANUAL_START){
			if(Intent.ACTION_CALL.equals(argIntent.getAction())){
				argIntent.setAction(Intent.ACTION_DIAL);
			}
		}
		
		PendingIntent pendingIntent = PendingIntent.getActivity(context, getIntentCode(), argIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		launcher.setOnClickPendingIntent(R.id.imageBlocker, pendingIntent);
		if(mIconType == SettingColumns.ICON_TYPE_BITMAP){
			/* 本当はmIconUriに生成時にmUriを代入しておきたいが、タイミングがないので仕方なく分岐 */
			launcher.setImageViewUri(R.id.imageAppIcon, mUri);
		}
		else{
			launcher.setImageViewUri(R.id.imageAppIcon, mIconUri);
		}
		return launcher;
	}
	
	@Override
	public void onAddToDatabase(ContentValues values) {
		super.onAddToDatabase(values);
		values.put(SettingColumns.KEY_TITLE, mTitle);
		String uri = mIntent.toUri(0);
		values.put(SettingColumns.KEY_INTENT, uri);
		values.put(SettingColumns.KEY_ICON_TYPE, mIconType);
		
		if(mIconType == SettingColumns.ICON_TYPE_BITMAP){
			if(mIconToSave != null){
				BitmapUtil.writeBitmap(values, SettingColumns.KEY_ICON_BITMAP, mIconToSave);
				/* DBに保存したのでビットマップを破棄。後はURIでのみアクセスする。 */
				mIconToSave.recycle();
				mIconToSave = null;
			}
		}
		else{
			values.put(SettingColumns.KEY_ICON_PACKAGE, mPackageName);
			values.put(SettingColumns.KEY_ICON_RESOURCE, mIconName);
		}
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(mTitle);
		dest.writeParcelable(mIntent, flags);
		dest.writeInt(mIconType);
		if(mIconType == SettingColumns.ICON_TYPE_RESOURCE){
			dest.writeString(mPackageName);
			dest.writeString(mIconName);
			dest.writeParcelable(mIconUri, flags);
		}
	}
	
	public static ShortcutInfo createFromParcel(Parcel source){
		ShortcutInfo info = new ShortcutInfo();
		info.mTitle = source.readString();
		info.mIntent = source.readParcelable(Intent.class.getClassLoader());
		info.mIconType = source.readInt();
		if(info.mIconType == SettingColumns.ICON_TYPE_RESOURCE){
			info.mPackageName = source.readString();
			info.mIconName = source.readString();
			info.mIconUri = source.readParcelable(Uri.class.getClassLoader());
		}
		
		return info;
	}
}
