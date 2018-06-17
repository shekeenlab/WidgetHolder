package jp.co.shekeen.WidgetHolder;

import jp.co.shekeen.WidgetHolder.CellLayout.OnCellLayoutChangedListener;
import android.os.Bundle;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Intent;
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
		implements OnClickListener, OnCellLayoutChangedListener, OnDragListener,
		CompoundButton.OnCheckedChangeListener {

	private static final int REQUEST_CODE_ADD_APPWIDGET = 1;
	private static final int REQUEST_CODE_CONFIGURE = 2;
	private static final int REQUEST_CODE_ADD_APP = 3;
	private static final int REQUEST_CODE_ADD_SHORTCUT = 4;

	private Button mButtonAddWidget;
	private Button mButtonAddApp;
	private Button mButtonAddShortcut;
	private NotificationService mNotifService;
	private AppWidgetManager mAppWidgetManager;
	private CellLayout mCellLayout;
	private ImageView mImageTrash;
	private CheckBox mCheckShrink;
	private ResizeLayer mResizeLayer;
	private SettingLoader mSettingLoader;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        /* プロセスが異なるので、staticなinitializeはアクティビティとサービスともに同じものを呼ぶように注意 */
        Config.initialize(this);
        SettingColumns.initialize(this);
		mNotifService = new NotificationService(this);
		
		mSettingLoader = new SettingLoader(this);
    	mAppWidgetManager = AppWidgetManager.getInstance(this);
        
    	mButtonAddWidget = (Button)findViewById(R.id.buttonAddWidget);
    	mButtonAddApp = (Button)findViewById(R.id.buttonAddApp);
    	mButtonAddShortcut = (Button)findViewById(R.id.buttonAddShortcut);
    	mImageTrash = (ImageView)findViewById(R.id.imageTrash);
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
			onWidgetSelected(null);
		}
	}

	@Override
	protected void onDestroy() {
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

	}
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch(requestCode){
		case REQUEST_CODE_ADD_APPWIDGET:
			break;
		case REQUEST_CODE_CONFIGURE:
			break;
		case REQUEST_CODE_ADD_APP:
			onRequestAddApp(requestCode, resultCode, data);
			break;
		case REQUEST_CODE_ADD_SHORTCUT:
			onRequestAddShortcut(requestCode, resultCode, data);
			break;
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
