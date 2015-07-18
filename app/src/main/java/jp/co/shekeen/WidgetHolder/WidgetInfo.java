package jp.co.shekeen.WidgetHolder;

import java.lang.reflect.Field;
import java.util.ArrayList;

import jp.co.shekeen.WidgetHolder.BitmapResolver.BitmapContainer;
import jp.co.shekeen.WidgetHolder.ReflectorICS.RefActionICS;
import jp.co.shekeen.WidgetHolder.ReflectorICS.RefReflectionActionICS;
import jp.co.shekeen.WidgetHolder.ReflectorICS.RefRemoteViewsICS;
import jp.co.shekeen.WidgetHolder.ReflectorICS.RefViewGroupActionICS;
import jp.co.shekeen.WidgetHolder.ReflectorJB.RefActionJB;
import jp.co.shekeen.WidgetHolder.ReflectorJB.RefBitmapReflectionActionJB;
import jp.co.shekeen.WidgetHolder.ReflectorJB.RefRemoteViewsJB;
import jp.co.shekeen.WidgetHolder.ReflectorJB.RefViewGroupActionJB;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.os.Parcel;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RemoteViews;

public class WidgetInfo extends CellInfo {

	public static final String EXTRA_PENDING_INTENT = "jp.co.shekeen.WidgetHolder.AppInfo.extra.PENDING_INTENT";
	
	private boolean mShrink;
	private boolean mHookIntent;
	private RemoteViews mOldViews;
	@SuppressWarnings("rawtypes")
	private ArrayList mOldActionList;
	
	public WidgetInfo(int appWidgetId){
		mAppWidgetId = appWidgetId;
		mShrink = true;/* デフォルトで縮小表示する */
	}
	
	public WidgetInfo(Context context, int appWidgetId){
		this(appWidgetId);
		
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		AppWidgetProviderInfo providerInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
		if(providerInfo == null){
			/* プロバイダ情報が取得できなかったということは、もうappwidgetIdは無効なはず */
			appWidgetId = 0;
			DebugHelper.print("NO PROVIDER INFO FOUND!!!");
		}
		else{
			GuiUtil guiUtil = new GuiUtil(context);
			mRowCount = guiUtil.getCellCount(providerInfo.minHeight);
			mColumnCount = guiUtil.getCellCount(providerInfo.minWidth);
		}
	}
	
	public static WidgetInfo createFromCursor(Context context, Cursor cursor){
		int appWidgetId = cursor.getInt(SettingColumns.INDEX_APPWIDGET_ID);
		WidgetInfo info = new WidgetInfo(context, appWidgetId);
		info.mHookIntent = cursor.getInt(SettingColumns.INDEX_HOOK_INTENT) != 0;
		info.mShrink = cursor.getInt(SettingColumns.INDEX_SHOW_SHRINK) != 0;
		return info;
	}
	
	@Override
	public boolean isSpacer() {
		return false;
	}

	@Override
	protected int getCellType() {
		return SettingColumns.CELL_TYPE_WIDGET;
	}
	
	public boolean getHookIntent(){
		return mHookIntent;
	}
	
	public void setHookIntent(boolean hook){
		mHookIntent = hook;
	}
	
	public boolean getShrink(){
		return mShrink;
	}
	
	public void setShrink(boolean shrink){
		mShrink = shrink;
	}
	
	@Override
	public View createView(CellLayout cellLayout) {
		/* 念のため列数を確認 */
		mColumnCount = Math.min(mColumnCount, mMaxColumnCount);
		
		Context context = cellLayout.getContext();
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		AppWidgetProviderInfo providerInfo = appWidgetManager.getAppWidgetInfo(mAppWidgetId);
		AppWidgetHost widgetHost = cellLayout.getAppWidgetHost();
		AppWidgetHostView hostView = widgetHost.createView(context, mAppWidgetId, providerInfo);
		/* ここで取得したHostViewについてはstartListeningしない。
		 * ルーパ１つあたり複数Listeningすると、1つ目のHostViewがすべて受信してしまう。 */
		
		/* 縮小する場合であっても、まずここでhostViewにLayoutParamを設定する。そうしないと、縮小時の寸法がおかしくなる。 */
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		hostView.setLayoutParams(params);
		hostView.setPadding(0, 0, 0, 0);
		
		if(mSmaller && mShrink){
			GuiUtil guiUtil = new GuiUtil(context);
			LinearLayout layout = new LinearLayout(cellLayout.getContext());
			LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(0, 0);
			hostView.setScaleX(0.75f);
			hostView.setScaleY(0.75f);
			Point dispSize = guiUtil.getDisplaySize();
			int margin = dispSize.x * mColumnCount / (mMaxColumnCount * 6);
			int topMargin = -1 * guiUtil.dp2px(9);
			int bottomMargin = -1 * guiUtil.dp2px(10);/* なぜかshrink.xmlと同じ-4では不足。とりあえず-10dp。 */
			layout.setPadding(-margin, topMargin, -margin, bottomMargin);
			lparams.leftMargin = -margin;
			lparams.rightMargin = -margin;
			lparams.topMargin = topMargin;
			lparams.bottomMargin = bottomMargin;
			lparams.width = LayoutParams.MATCH_PARENT;
			lparams.weight = 1;
			lparams.height = LayoutParams.MATCH_PARENT;
			layout.setLayoutParams(lparams);
			layout.addView(hostView);
			LinearLayout.LayoutParams vparams = (LinearLayout.LayoutParams) hostView.getLayoutParams();
			/* shrink.xmlと同じ設定になるようにwidthとheightを設定する */
			vparams.width = LayoutParams.MATCH_PARENT;
			vparams.weight = 1;
			vparams.gravity = Gravity.CENTER;
			vparams.height = LayoutParams.MATCH_PARENT;
			hostView.setLayoutParams(vparams);
			return layout;
		}
		return hostView;
	}
	
	@Override
	public RemoteViews createRemoteViews(Context context, RemoteViews widgetView) {
		/* 念のため列数を確認 */
		mColumnCount = Math.min(mColumnCount, mMaxColumnCount);
		/* 2行に対応。ただし、ICSでは常に1行用を返す必要があるか要確認 */
		int layoutId = HOLDER_ID_LIST[mColumnCount - 1];
		RemoteViews holder = new RemoteViews(context.getPackageName(), layoutId);
		RemoteViews shrink = null;
		if(mShrink && mSmaller){
			GuiUtil guiUtil = new GuiUtil(context);
			int margin = guiUtil.getNotificationWidthDp() * mColumnCount / (mMaxColumnCount * 6);
			int index;
			for(index = 0; index < SHRINK_ID_LIST.length - 1; index++){
				if(margin <= SHRINK_SIZE_LIST[index]){
					break;
				}
			}
			DebugHelper.print("requested", margin, "selected", SHRINK_SIZE_LIST[index]);
			layoutId = SHRINK_ID_LIST[index];
			shrink = new RemoteViews(context.getPackageName(), layoutId);
			holder.addView(R.id.layoutHolder, shrink);
		}
		if(widgetView != null){
			if(mHookIntent && mOldViews != widgetView){
				replaceIntent(context, widgetView);
				mOldViews = widgetView;
			}
			else if(!mHookIntent && mOldViews == widgetView){
				rollbackIntent(context, widgetView);
				mOldViews = null;
				mOldActionList = null;
			}
			if(shrink != null){
				shrink.addView(R.id.layoutHolder, widgetView);
			}
			else{
				holder.addView(R.id.layoutHolder, widgetView);
			}
		}
		else{/* widgetViewがNULLだったらエラー画面を表示 */
			RemoteViews errorView = new RemoteViews(context.getPackageName(), R.layout.loaderror);
			Intent intent = new Intent(context, NotificationService.class);
			intent.setAction(NotificationService.ACTION_LAUNCH_APP);
			Intent launcher = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
			intent.putExtra(AppInfo.EXTRA_INTENT, launcher);
			PendingIntent pendingIntent = PendingIntent.getService(context, getIntentCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
			errorView.setOnClickPendingIntent(R.id.imageBlocker, pendingIntent);
			if(shrink != null){
				shrink.addView(R.id.layoutHolder, errorView);
			}
			else{
				holder.addView(R.id.layoutHolder, errorView);
			}
		}
		return holder;
	}

	@Override
	public void onAddToDatabase(ContentValues values) {
		super.onAddToDatabase(values);
		values.put(SettingColumns.KEY_APPWIDGET_ID, mAppWidgetId);
		values.put(SettingColumns.KEY_HOOK_INTENT, mHookIntent ? 1 : 0);
		values.put(SettingColumns.KEY_SHOW_SHRINK, mShrink ? 1 : 0);
	}

	private int getIntentCode(int number){
		int code = getIntentCode();
		code += number;
		return code;
	}
	
	@SuppressWarnings({ "rawtypes" })
	private void replaceIntent(Context context, RemoteViews remoteViews){
		try {
			Field fieldActions = RemoteViews.class.getDeclaredField("mActions");
			fieldActions.setAccessible(true);
			ArrayList actionList = (ArrayList) fieldActions.get(remoteViews);
			if(actionList == null){
				return;
			}
			mOldActionList = (ArrayList) actionList.clone();
			Class<?> classSetOnClickPendingIntent = Class.forName("android.widget.RemoteViews$SetOnClickPendingIntent");
			Field fieldPendingIntent = classSetOnClickPendingIntent.getDeclaredField("pendingIntent");
			fieldPendingIntent.setAccessible(true);
			for(int i = 0; i < actionList.size(); i++){
				Object action = actionList.get(i);
				if(action.getClass() == classSetOnClickPendingIntent){
					PendingIntent origIntent = (PendingIntent) fieldPendingIntent.get(action);
					Intent intent = new Intent(context, NotificationService.class);
					intent.setAction(NotificationService.ACTION_LAUNCH_WIDGET);
					intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
					intent.putExtra(EXTRA_PENDING_INTENT, origIntent);
					PendingIntent pendingIntent = PendingIntent.getService(context, getIntentCode(i), intent, PendingIntent.FLAG_UPDATE_CURRENT);
					fieldPendingIntent.set(action, pendingIntent);
				}
			}
			
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void rollbackIntent(Context context, RemoteViews remoteViews){
		try {
			Field fieldActions = RemoteViews.class.getDeclaredField("mActions");
			fieldActions.setAccessible(true);
			fieldActions.set(remoteViews, mOldActionList);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public static void launchWidget(Context context, Intent intent){
		MyStatusBarManager statusbar = MyStatusBarManager.getInstance(context);
		statusbar.collapse();
		PendingIntent launcher = intent.getParcelableExtra(EXTRA_PENDING_INTENT);
		if(launcher != null){
			try {
				launcher.send();
			} catch (CanceledException e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean optimizeRemoteViews(Context context, RemoteViews original, RemoteViews additional){
		if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){/* IF JELLY BEAN AND HIGHER */
			RefRemoteViewsJB refAddtional = new RefRemoteViewsJB(additional);
			
			replaceBitmapForJB(context, refAddtional);
			if(original != null){
				RefRemoteViewsJB refOriginal = new RefRemoteViewsJB(original);
				if(virtualReapplyJB(refOriginal, refAddtional) == false){
					return false;
				}
				resetMemoryUsageJB(refOriginal);
				return true;
			}
			else{
				resetMemoryUsageJB(refAddtional);
				return false;
			}
		}
		else{/* ICS */
			RefRemoteViewsICS refAdditional = new RefRemoteViewsICS(additional);
			
			replaceBitmapForICS(context, refAdditional);
			if(original != null){
				RefRemoteViewsICS refOriginal = new RefRemoteViewsICS(original);
				if(virtualReapplyICS(refOriginal, refAdditional) == false){
					return false;
				}
				resetMemoryUsageICS(refOriginal);
				return true;
			}
			else{
				resetMemoryUsageICS(refAdditional);
				return false;
			}
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void replaceBitmapForJB(Context context, RefRemoteViewsJB refRemoteViews){
		try {
			if(refRemoteViews.mActions == null){
				return;
			}
			
			BitmapContainer[] bitmapList = BitmapResolver.loadFromDatabase(context, mDbId);
			SparseArray<BitmapContainer> bitmapHash = new SparseArray<BitmapContainer>();
			for(BitmapContainer container : bitmapList){
				bitmapHash.put(container.viewId, container);
			}
			
			ArrayList actionList = refRemoteViews.mActions;
			int size = actionList.size();
			for(int i = 0; i < size; i++){
				RefActionJB action = ReflectorJB.getInstance(actionList.get(i));
				if(RefBitmapReflectionActionJB.class.isInstance(action)){
					RefBitmapReflectionActionJB refBitmapAction = (RefBitmapReflectionActionJB) action;
					BitmapContainer container = bitmapHash.get(refBitmapAction.viewId);
					if(container == null){
						container = new BitmapContainer();
						container.cellId = mDbId;
						container.viewId = refBitmapAction.viewId;
						container.bitmap = refBitmapAction.bitmap;
					}
					else{
						/* ビットマップだけ更新 */
						container.bitmap = refBitmapAction.bitmap;
					}
					BitmapResolver.updateItemInDatabase(context, container);
					
					/* 新しいアクションを追加 */
					refRemoteViews.instance.setImageViewUri(refBitmapAction.viewId, container.uri);
					/* 追加した最後尾のアクションを現在のアクションの位置に移動 */
					actionList.set(i, actionList.get(size));
					actionList.remove(size);
				}
			}
		} catch (Exception e) {/* RuntimeExceptionも含めてここでキャッチ */
			e.printStackTrace();
		}
	}
	
	private void resetMemoryUsageJB(RefRemoteViewsJB refRemoteViews){
		/* BitmapCacheをクリア */
		refRemoteViews.mBitmapCache.mBitmaps.clear();
		
		/* メモリ使用量推定値を更新 */
		refRemoteViews.recalculateMemoryUsage();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void replaceBitmapForICS(Context context, RefRemoteViewsICS refRemoteViews){
		try {
			if(refRemoteViews.mActions == null){
				return;
			}
			ArrayList actionList = refRemoteViews.mActions;
			
			BitmapContainer[] bitmapList = BitmapResolver.loadFromDatabase(context, mDbId);
			SparseArray<BitmapContainer> bitmapHash = new SparseArray<BitmapContainer>();
			for(BitmapContainer container : bitmapList){
				bitmapHash.put(container.viewId, container);
			}
			
			int size = actionList.size();
			for(int i = 0; i < size; i++){
				RefActionICS action = ReflectorICS.getInstance(actionList.get(i));
				if(RefReflectionActionICS.class.isInstance(action)){
					RefReflectionActionICS refAction = (RefReflectionActionICS) action;
					if(refAction.type == RefReflectionActionICS.BITMAP){
						BitmapContainer container = bitmapHash.get(refAction.viewId);
						if(container == null){
							container = new BitmapContainer();
							container.cellId = mDbId;
							container.viewId = refAction.viewId;
							container.bitmap = (Bitmap) refAction.value;
						}
						else{
							/* ビットマップだけ更新 */
							container.bitmap = (Bitmap) refAction.value;
						}
						BitmapResolver.updateItemInDatabase(context, container);
						
						/* 新しいアクションを追加 */
						refRemoteViews.instance.setImageViewUri(refAction.viewId, container.uri);
						/* 追加した最後尾のアクションを現在のアクションの位置に移動 */
						actionList.set(i, actionList.get(size));
						actionList.remove(size);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void resetMemoryUsageICS(RefRemoteViewsICS refRemoteViews){
		/* メモリ使用量推定値を更新 */
		refRemoteViews.recalculateMemoryUsage();
	}
	
	@SuppressWarnings("unchecked")
	public boolean virtualReapplyJB(RefRemoteViewsJB refOriginal, RefRemoteViewsJB refAdditional){
		/* リフレクションだらけでNullPointerExceptionが怖いので、try-catchで囲む */
		try{
			if(!refOriginal.mPackage.equals(refAdditional.mPackage)){
				return false;
			}
			if(refOriginal.mLayoutId != refAdditional.mLayoutId){
				return false;
			}

			int sizeAdd = refAdditional.mActions.size();
			int sizeOrig = refOriginal.mActions.size();
			for(int i = 0; i < sizeAdd; i++){
				RefActionJB actionAdd = ReflectorJB.getInstance(refAdditional.mActions.get(i));
				/* このアクションと同一な対象のアクションがoriginalに含まれていないか調べる */
				boolean replaced = false;
				for(int j = 0; j < sizeOrig; j++){
					RefActionJB actionOrig = ReflectorJB.getInstance(refOriginal.mActions.get(j));
					if(actionOrig.isSameTarget(actionAdd)){
						if(RefViewGroupActionJB.class.isInstance(actionAdd)){
							RefViewGroupActionJB actionViewGroupAdd = (RefViewGroupActionJB) actionAdd;
							if(actionViewGroupAdd.nestedViews != null){
								/* nestedViewがNULLでない場合、対象のviewのnestedViewが追加される仕様なので、
								 * 最後尾に追加する */
								break;
							}
							else{
								/* nullならばremoveAllViewsで削除なので、前のnestedViewは保存する必要なし。 */
							}
						}
						/* 同じ対象に対するアクションなら新しいアクションで上書き */
						refOriginal.mActions.set(j, refAdditional.mActions.get(i));
						replaced = true;
						break;
					}
				}
				if(!replaced){
					refOriginal.mActions.add(refAdditional.mActions.get(i));
				}

				/* ViewGroupActionについてはTBD。どれくらい使っているウィジェット存在する？ */
				sizeOrig = refOriginal.mActions.size();
				DebugHelper.print("ACTION SIZE ", sizeOrig);
			}
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public boolean virtualReapplyICS(RefRemoteViewsICS refOriginal, RefRemoteViewsICS refAdditional){
		/* リフレクションだらけでNullPointerExceptionが怖いので、try-catchで囲む */
		try{
			if(!refOriginal.mPackage.equals(refAdditional.mPackage)){
				return false;
			}
			if(refOriginal.mLayoutId != refAdditional.mLayoutId){
				return false;
			}

			int sizeAdd = refAdditional.mActions.size();
			int sizeOrig = refOriginal.mActions.size();
			for(int i = 0; i < sizeAdd; i++){
				RefActionICS actionAdd = ReflectorICS.getInstance(refAdditional.mActions.get(i));
				/* このアクションと同一な対象のアクションがoriginalに含まれていないか調べる */
				boolean replaced = false;
				for(int j = 0; j < sizeOrig; j++){
					RefActionICS actionOrig = ReflectorICS.getInstance(refOriginal.mActions.get(j));
					if(actionOrig.isSameTarget(actionAdd)){
						if(RefViewGroupActionICS.class.isInstance(actionAdd)){
							RefViewGroupActionICS actionViewGroupAdd = (RefViewGroupActionICS) actionAdd;
							if(actionViewGroupAdd.nestedViews != null){
								/* nestedViewがNULLでない場合、対象のviewにnestedViewが追加される仕様なので、
								 * 最後尾に追加する */
								break;
							}
							else{
								/* nullならばremoveAllViewsで削除なので、前のnestedViewは保存する必要なし。 */
							}
						}
						/* 同じ対象に対するアクションなら新しいアクションで上書き */
						refOriginal.mActions.set(j, refAdditional.mActions.get(i));
						replaced = true;
						break;
					}
				}
				if(!replaced){
					refOriginal.mActions.add(refAdditional.mActions.get(i));
				}

				/* ViewGroupActionについてはTBD。どれくらい使っているウィジェット存在する？ */
				sizeOrig = refOriginal.mActions.size();
				DebugHelper.print("ACTION SIZE ", sizeOrig);
			}
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void replaceBitmap(Context context, RemoteViews remoteViews){
		if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){/* IF JELLY BEAN AND HIGHER */
			RefRemoteViewsJB refRemoteViews = new RefRemoteViewsJB(remoteViews);
			replaceBitmapForJB(context, refRemoteViews);
		}
		else{/* ICS */
			RefRemoteViewsICS refRemoteViews = new RefRemoteViewsICS(remoteViews);
			replaceBitmapForICS(context, refRemoteViews);
		}
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeInt(mHookIntent ? 1 : 0);
		dest.writeInt(mShrink ? 1 : 0);
	}
	
	public static WidgetInfo createFromParcel(Parcel source, int appWidgetId){
		WidgetInfo info = new WidgetInfo(appWidgetId);
		info.mHookIntent = source.readInt() != 0;
		info.mShrink = source.readInt() != 0;
		return info;
	}
}
