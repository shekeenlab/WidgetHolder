package jp.co.shekeen.WidgetHolder;

import java.util.ArrayList;

import jp.co.shekeen.WidgetHolder.CellInfo.CellPosition;
import jp.co.shekeen.WidgetHolder.CellLayout.OnCellLayoutChangedListener;
import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.CompoundButton;
import android.widget.RemoteViews;
import android.widget.Toast;

public class MainActivity extends Activity 
		implements OnClickListener, ServiceConnection, OnCellLayoutChangedListener, OnDragListener, 
		CompoundButton.OnCheckedChangeListener {

	private static final int REQUEST_CODE_ADD_APPWIDGET = 1;
	private static final int REQUEST_CODE_CONFIGURE = 2;
	private static final int REQUEST_CODE_ADD_APP = 3;
	private static final int REQUEST_CODE_ADD_SHORTCUT = 4;
	private static final int END_FLAG_PICK = 0x01;
	private static final int END_FLAG_UPDATEWIDGET = 0x02;
	private static final int END_FLAG_CONFIGURE = 0x04;
	private static final int END_STAT_WIDGET_AVAILABLE = END_FLAG_PICK | END_FLAG_UPDATEWIDGET | END_FLAG_CONFIGURE;
	
	private Button mButtonAddWidget;
	private Button mButtonAddApp;
	private Button mButtonAddShortcut;
	private NotificationServiceWrapper mNotifService;
	private AppWidgetManager mAppWidgetManager;
	private CellLayout mCellLayout;
	private int mTargetId;
	private int mEndFlag;
	private ImageView mImageTrash;
	private CheckBox mCheckHookIntent;
	private CheckBox mCheckShrink;
	private RemoteViews mTestTarget;
	private ResizeLayer mResizeLayer;
	private WidgetInfo mTargetWidget;
	private SettingLoader mSettingLoader;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        /* プロセスが異なるので、staticなinitializeはアクティビティとサービスともに同じものを呼ぶように注意 */
        Config.initialize(this);
        SettingColumns.initialize(this);
        
        Intent intent = new Intent(this, NotificationService.class);
        startService(intent);
		bindService(intent, this, BIND_AUTO_CREATE);
		
		mSettingLoader = new SettingLoader(this);
    }

    private void initActivity(){
    	mAppWidgetManager = AppWidgetManager.getInstance(this);
        
    	mButtonAddWidget = (Button)findViewById(R.id.buttonAddWidget);
    	mButtonAddApp = (Button)findViewById(R.id.buttonAddApp);
    	mButtonAddShortcut = (Button)findViewById(R.id.buttonAddShortcut);
    	mImageTrash = (ImageView)findViewById(R.id.imageTrash);
    	mCheckHookIntent = (CheckBox)findViewById(R.id.checkHookIntent);
    	mCheckShrink = (CheckBox)findViewById(R.id.checkShrink);
    	
    	FrameLayout cellBase = (FrameLayout)findViewById(R.id.layoutCellBase);
    	mCellLayout = new CellLayout(cellBase, mSettingLoader.getColumnCount(), mSettingLoader.getRowCount(), mSettingLoader.getSmaller());
    	mCellLayout.setOnClickListener(this);
    	mCellLayout.setOnUserLayoutListener(this);
    	CellInfo[] cellInfos = mNotifService.getCellInfos();
    	for(CellInfo info : cellInfos){
    		info.applySetting(mSettingLoader);/* 設定を適用する */
    		mCellLayout.putAt(info.getPosition(), info);
    	}
    	mResizeLayer = new ResizeLayer(cellBase, mSettingLoader.getRowCount(), mSettingLoader.getSmaller());
    	mCellLayout.setup(mResizeLayer);
    	
    	mButtonAddWidget.setOnClickListener(this);
    	mButtonAddApp.setOnClickListener(this);
    	mButtonAddShortcut.setOnClickListener(this);
    	mNotifService.registerCallbacks(new AppWidgetCallbackWrapper(this));
    	mImageTrash.setOnDragListener(this);
    	
    	if(!mSettingLoader.getSmaller()){
    		mCheckShrink.setVisibility(View.GONE);
    	}
    }
    
	@Override
	protected void onResume() {
		super.onResume();
		
		/* サービスと接続済みかどうかをmAppWidgetManagerのインスタンスの有無で判断する */
		if(mAppWidgetManager != null){
			mSettingLoader = new SettingLoader(this);/* 設定を再読み込みする */
			regenerateCellLayout();
			mTargetWidget = null;
			onWidgetSelected(null);
		}
	}

	@Override
	protected void onDestroy() {
		unbindService(this);
		mSettingLoader = null;
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == R.id.menuSetting){
			Intent intent = new Intent(this, SettingActivity.class);
			startActivity(intent);
		}
		return true;
	}

	@Override
	public void onClick(View v) {
		if(v == mButtonAddWidget){
			createNewWidget();
		}
		else if(v == mButtonAddApp){
			Intent intent = new Intent(this, AppSelectActivity.class);
			startActivityForResult(intent, REQUEST_CODE_ADD_APP);
		}
		else if(v == mButtonAddShortcut){
			Intent intent = new Intent(this, ShortcutSelectActivity.class);
			startActivityForResult(intent, REQUEST_CODE_ADD_SHORTCUT);
		}
	}

    @Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    	if(buttonView == mCheckHookIntent){
			if(mTargetWidget != null){
				mTargetWidget.setHookIntent(isChecked);
				mNotifService.updateCellInfos(new CellInfo[]{ mTargetWidget });
			}
		}
		else if(buttonView == mCheckShrink){
			if(mTargetWidget != null){
				mTargetWidget.setShrink(isChecked);
				mNotifService.updateCellInfos(new CellInfo[]{ mTargetWidget });
				/* WidgetのViewを作り直す */
				mCellLayout.remove(mTargetWidget.getAppWidgetId());
				CellPosition pos = mTargetWidget.getPosition();
				mCellLayout.putAt(pos, mTargetWidget);
			}
		}
	}
    
	private void createNewWidget(){
		if(mTargetId > 0){
			onRequestAddWidgetCanceled(mTargetId);
		}
    	int appWidgetId = mNotifService.allocateAppWidgetId();
    	ArrayList<AppWidgetProviderInfo> appWidgetProviderInfoList = new ArrayList<AppWidgetProviderInfo>();
    	mEndFlag = 0;
    	mTargetId = appWidgetId;
    	
    	ArrayList<Bundle> bundleList = new ArrayList<Bundle>();
    	Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
    	intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
    	intent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, appWidgetProviderInfoList);
    	intent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, bundleList);
    	
    	startActivityForResult(intent, REQUEST_CODE_ADD_APPWIDGET);
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch(requestCode){
		case REQUEST_CODE_ADD_APPWIDGET:
			onRequestAddWidget(requestCode, resultCode, data);
			break;
		case REQUEST_CODE_CONFIGURE:
			onRequestConfigure(requestCode, resultCode, data);
			break;
		case REQUEST_CODE_ADD_APP:
			onRequestAddApp(requestCode, resultCode, data);
			break;
		case REQUEST_CODE_ADD_SHORTCUT:
			onRequestAddShortcut(requestCode, resultCode, data);
			break;
		}
	}
	
	private void onRequestAddWidget(int requestCode, int resultCode, Intent data){
		int appWidgetId = -1;
		if(data != null){
			appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
			if(appWidgetId <= 0){
				resultCode = RESULT_CANCELED;
				appWidgetId = mTargetId;
			}
		}
		else{
			resultCode = RESULT_CANCELED;
			appWidgetId = mTargetId;
		}
		
		if(resultCode == RESULT_OK){
			onRequestAddWidgetOk(appWidgetId);
		}
		else if(resultCode == RESULT_CANCELED){
			onRequestAddWidgetCanceled(appWidgetId);
		}
	}
	
	private void onRequestAddWidgetOk(int appWidgetId){
		mEndFlag |= END_FLAG_PICK;
		AppWidgetProviderInfo widgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
		if(widgetInfo == null){
			DebugHelper.print("UNKNOWN APPWIDGET ID: " + String.valueOf(appWidgetId));
			onRequestAddWidgetCanceled(appWidgetId);
			return;
		}
		if(!isSupportedSize(widgetInfo)){
			onRequestAddWidgetCanceled(appWidgetId);
			Toast.makeText(this, getString(R.string.error_multiline_is_not_supported), Toast.LENGTH_LONG).show();
			return;
		}
		if (widgetInfo.configure != null) {
			Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
			intent.setComponent(widgetInfo.configure);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			startActivityForResult(intent, REQUEST_CODE_CONFIGURE);
		}
		else{
			onRequestConfigure(REQUEST_CODE_CONFIGURE, RESULT_OK, appWidgetId);
		}
	}
	
	private void onRequestAddWidgetCanceled(int appWidgetId){
		mCellLayout.remove(appWidgetId);
		mNotifService.deleteAppWidgetId(appWidgetId);
		mEndFlag = 0;
		mTargetId = 0;
	}
	
	private void onRequestConfigure(int requestCode, int resultCode, Intent data){
		int appWidgetId = -1;
		if(data != null){
			appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
			if(appWidgetId <= 0){
				resultCode = RESULT_CANCELED;
				appWidgetId = mTargetId;
			}
		}
		else{
			resultCode = RESULT_CANCELED;
			appWidgetId = mTargetId;
		}
		onRequestConfigure(requestCode, resultCode, appWidgetId);
	}
	
	private void onRequestConfigure(int requestCode, int resultCode, int appWidgetId){
		if(resultCode == RESULT_OK){
			mEndFlag |= END_FLAG_CONFIGURE;
			DebugHelper.print("CONFIGURE FINISHED " + String.valueOf(appWidgetId));
		}
		else if(resultCode == RESULT_CANCELED){
			mCellLayout.remove(appWidgetId);
			mNotifService.deleteAppWidgetId(appWidgetId);
			mEndFlag = 0;
			mTargetId = 0;
		}
		if((mEndFlag & END_STAT_WIDGET_AVAILABLE) == END_STAT_WIDGET_AVAILABLE){
			onWidgetAvailable(appWidgetId);
		}
	}
	
	private void onRequestAddApp(int requestCode, int resultCode, Intent data){
		if(resultCode == RESULT_OK){
			Intent intent = data.getExtras().getParcelable(AppSelectActivity.EXTRA_INTENT);
			String appName = data.getExtras().getString(AppSelectActivity.EXTRA_APP_NAME);
			byte[] array = data.getExtras().getByteArray(AppSelectActivity.EXTRA_BITMAP);
			Bitmap bitmap = BitmapUtil.decode(array);
			AppInfo appInfo = new AppInfo(this, intent, appName, bitmap);
			appInfo.applySetting(mSettingLoader);
			
			/* DBIDを付与しないと、ImageViewを表示できないのでまずDBに登録する */
			SettingResolver.addItemToDatabase(this, appInfo);
			
			boolean result = mCellLayout.append(appInfo);
			if(result){
				mNotifService.addCellInfo(appInfo);
			}
			else{
				/* DBから削除する */
				SettingResolver.deleteItemFromDatabase(this, appInfo);
				Toast.makeText(this, getString(R.string.error_no_space), Toast.LENGTH_LONG).show();
			}
		}
	}
	
	private void onRequestAddShortcut(int requestCode, int resultCode, Intent data){
		if(resultCode == RESULT_OK && data != null){
			ShortcutInfo shortcutInfo = new ShortcutInfo(this, data);
			shortcutInfo.applySetting(mSettingLoader);
			
			/* DBIDを付与しないと、ImageViewを表示できないのでまずDBに登録する */
			SettingResolver.addItemToDatabase(this, shortcutInfo);
			
			boolean result = mCellLayout.append(shortcutInfo);
			if(result){
				mNotifService.addCellInfo(shortcutInfo);
			}
			else{
				/* DBから削除する */
				SettingResolver.deleteItemFromDatabase(this, shortcutInfo);
				Toast.makeText(this, getString(R.string.error_no_space), Toast.LENGTH_LONG).show();
			}
		}
	}
	
	private void onWidgetAvailable(int appWidgetId){
		boolean test = testWidget(mTestTarget);
		if(test){
			CellInfo widgetInfo = new WidgetInfo(this, appWidgetId);
			widgetInfo.applySetting(mSettingLoader);
			
			/* DBIDを付与しないと、ImageViewを表示できないのでまずDBに登録する */
			SettingResolver.addItemToDatabase(this, widgetInfo);
			
			boolean result = mCellLayout.append(widgetInfo);
			if(result){
				mNotifService.addCellInfo(widgetInfo);
			}
			else{
				/* DBから削除する */
				SettingResolver.deleteItemFromDatabase(this, widgetInfo);
				mNotifService.deleteAppWidgetId(appWidgetId);
				Toast.makeText(this, getString(R.string.error_no_space), Toast.LENGTH_LONG).show();
			}
		}
		else{
			mNotifService.deleteAppWidgetId(appWidgetId);
			Toast.makeText(this, getString(R.string.error_not_supported), Toast.LENGTH_LONG).show();
		}
		mEndFlag = 0;
		mTargetId = 0;
		mTestTarget = null;
	}
	
	private boolean testWidget(RemoteViews remoteViews){
		if(remoteViews == null){
			return false;
		}
		try{
			FrameLayout testLayout = new FrameLayout(this);
			remoteViews.apply(this, testLayout);
		}
		catch(RuntimeException e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private boolean isSupportedSize(AppWidgetProviderInfo info){
		if(!mSettingLoader.getIcsCompat()){/* IF JELLY BEAN AND HIGHER */
			return true;
		}
		GuiUtil guiUtil = new GuiUtil(this);
		int rowCount = guiUtil.getCellCount(info.minHeight);
		return rowCount == 1;
	}
	
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		mNotifService = new NotificationServiceWrapper(service);
		initActivity();
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		mNotifService = null;
	}
	
	public void onUpdateAppWidgetView(int appWidgetId, RemoteViews views) {
		DebugHelper.print("ACTIVITY onUpdateAppWidgetView");
		if(views == null){
			mCellLayout.remove(appWidgetId);
			mNotifService.deleteAppWidgetId(appWidgetId);
			return;
		}
		if(appWidgetId == mTargetId){
			mEndFlag |= END_FLAG_UPDATEWIDGET;
			mTestTarget = views;
			if((mEndFlag & END_STAT_WIDGET_AVAILABLE) == END_STAT_WIDGET_AVAILABLE){
				onWidgetAvailable(appWidgetId);
			}
		}
		mCellLayout.updateWidgetView(appWidgetId);
	}

	public void onProviderChanged(int appWidgetId, AppWidgetProviderInfo appWidget) {
		DebugHelper.print("ACTIVITY onProviderChanged");
	}

	public void onViewDataChanged(int appWidgetId, int viewId) {
		DebugHelper.print("ACTIVITY onViewDataChanged");
	}

	@Override
	public void onCellLayoutChanged(CellInfo[] cellInfos) {
		mNotifService.updateCellInfos(cellInfos);
	}

	public void onWidgetSelected(CellInfo cellInfo){
		mCheckHookIntent.setOnCheckedChangeListener(null);
		mCheckShrink.setOnCheckedChangeListener(null);
		mTargetWidget = null;

		if(cellInfo instanceof WidgetInfo){
			mTargetWidget = (WidgetInfo)cellInfo;
			mCheckHookIntent.setVisibility(View.VISIBLE);
			mCheckHookIntent.setChecked(mTargetWidget.getHookIntent());
			mCheckHookIntent.setOnCheckedChangeListener(this);
			if(mSettingLoader.getSmaller()){
				mCheckShrink.setVisibility(View.VISIBLE);
				mCheckShrink.setChecked(mTargetWidget.getShrink());
				mCheckShrink.setOnCheckedChangeListener(this);
			}
			else{
				mCheckShrink.setVisibility(View.INVISIBLE);
			}
		}
		else{
			mCheckHookIntent.setVisibility(View.INVISIBLE);
			mCheckShrink.setVisibility(View.INVISIBLE);
		}
	}
	
	@Override
	public boolean onDrag(View v, DragEvent event) {
		Object localState = event.getLocalState();
		if(localState == null || !(localState instanceof CellView)){
			return false;
		}
		
		int action = event.getAction();
		
		switch(action){
		case DragEvent.ACTION_DRAG_STARTED:
			break;

		case DragEvent.ACTION_DRAG_ENTERED:
			onDragEntered();
			break;
		
		case DragEvent.ACTION_DRAG_LOCATION:
			break;
			
		case DragEvent.ACTION_DRAG_EXITED:
			onDragExited();
			break;
			
		case DragEvent.ACTION_DROP:
			onDrop((CellView)localState);
			break;
			
		case DragEvent.ACTION_DRAG_ENDED:
			break;
		}
		return true;
	}
	
	private void onDragEntered(){
		Drawable drawable = getResources().getDrawable(R.drawable.trashcan_active);
		mImageTrash.setImageDrawable(drawable);
		
	}
	
	private void onDragExited(){
		Drawable drawable = getResources().getDrawable(R.drawable.trashcan_normal);
		mImageTrash.setImageDrawable(drawable);
	}
	
	private void onDrop(CellView cellView){
		Drawable drawable = getResources().getDrawable(R.drawable.trashcan_normal);
		mImageTrash.setImageDrawable(drawable);
		mCellLayout.dragDrop();
		mNotifService.deleteCellInfo(cellView.getCellInfo());
	}
	
	private void regenerateCellLayout(){
		FrameLayout cellBase = (FrameLayout)findViewById(R.id.layoutCellBase);
		cellBase.removeAllViews();
    	mCellLayout = new CellLayout(cellBase, mSettingLoader.getColumnCount(), mSettingLoader.getRowCount(), mSettingLoader.getSmaller());
    	mCellLayout.setOnClickListener(this);
    	mCellLayout.setOnUserLayoutListener(this);
    	CellInfo[] cellInfos = mNotifService.getCellInfos();
    	for(CellInfo info : cellInfos){
    		info.applySetting(mSettingLoader);/* 設定を適用する */
    		mCellLayout.putAt(info.getPosition(), info);
    	}
    	mResizeLayer = new ResizeLayer(cellBase, mSettingLoader.getRowCount(), mSettingLoader.getSmaller());
    	mCellLayout.setup(mResizeLayer);
	}
}
