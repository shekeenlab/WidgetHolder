package jp.co.shekeen.WidgetHolder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class AppSelectActivity extends Activity implements OnItemClickListener {
	
	public static final String EXTRA_INTENT = "jp.co.shekeen.WidgetHolder.AppSelectActivity.extra.INTENT";
	public static final String EXTRA_APP_NAME = "jp.co.shekeen.WidgetHolder.AppSelectActivity.extra.APP_NAME";
	public static final String EXTRA_BITMAP = "jp.co.shekeen.WidgetHolder.AppSelectActivity.extra.BITMAP";
	
	private static class AppAdapter extends ArrayAdapter<AppListItem>{
		
		private LayoutInflater mInflater;
		
		public AppAdapter(Context context, int textViewResourceId, List<AppListItem> objects) {
			super(context, textViewResourceId, objects);
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			/* Viewは再利用される */
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.selection, null);
			}
			TextView textView = (TextView)convertView.findViewById(R.id.textAppName);
			ImageView imageView = (ImageView)convertView.findViewById(R.id.imageIcon);
			AppListItem info = getItem(position);
			textView.setText(info.appName);
			imageView.setImageURI(info.iconUri);
			return convertView;
		}
	}
	
	private static class AppComparator implements Comparator<AppListItem>{

		@Override
		public int compare(AppListItem lhs, AppListItem rhs) {
			return lhs.appName.compareToIgnoreCase(rhs.appName);
		}
	}
	
	private ListView mListApps;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.appselect);
		
		mListApps = (ListView)findViewById(R.id.listApps);
		PackageManager packageManager = getPackageManager();
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> resolveList = packageManager.queryIntentActivities(intent, 0);
		List<AppListItem> appList = new ArrayList<AppListItem>();
		for(ResolveInfo resolveInfo : resolveList){
			appList.add(new AppListItem(this, resolveInfo));
		}
		AppAdapter adapter = new AppAdapter(this, 0, appList);
		adapter.sort(new AppComparator());
		mListApps.setAdapter(adapter);
		mListApps.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		AppListItem info = (AppListItem) parent.getItemAtPosition(position);
		Intent intent  = new Intent();
		intent.putExtra(EXTRA_INTENT, info.intent);
		intent.putExtra(EXTRA_APP_NAME, info.appName);
		Bitmap bitmap = info.loadIcon(this);
		intent.putExtra(EXTRA_BITMAP, BitmapUtil.flatten(bitmap));
		bitmap.recycle();
		setResult(RESULT_OK, intent);
		finish();
	}

	private static class AppListItem {
		private String appName;
		private Intent intent;
		private Uri iconUri;
		private ActivityInfo activityInfo;
		
		private AppListItem(Context context, ResolveInfo resolveInfo){
			super();
			PackageManager pm = context.getPackageManager();
			appName = resolveInfo.loadLabel(pm).toString();
			activityInfo = resolveInfo.activityInfo;
			
			/* パッケージ名とアクティビティ名はここだけでしか使わない。 */
			String packageName = resolveInfo.activityInfo.applicationInfo.packageName;
			String activityName = resolveInfo.activityInfo.name;
			
			/* ここではSDカードの問題を気にせずPackageManagerにアクセスできる。（仮にまだ読まれていなかったら、mListAppsに含まれていない。） */
			intent = pm.getLaunchIntentForPackage(packageName);/* 自力でIntent作ると失敗するアプリが存在するので、作ってもらってから上書き */
			/* ただし、nullを返すアプリがある？念のため、nullのときも考えておく。 */
			if(intent == null){
				intent = new Intent(Intent.ACTION_MAIN);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
			}
			if(activityName != null && activityName.length() > 0){
				intent.setClassName(packageName, activityName);
			}
			
			int iconRes = resolveInfo.activityInfo.getIconResource();
			iconUri = Uri.parse("android.resource://" + packageName + "/" + iconRes);
			
			if(appName == null || appName.length() == 0){
				appName = AppInfo.getDefaultTitle(context);
			}
		}
		
		private Bitmap loadIcon(Context context){
			PackageManager pm = context.getPackageManager();
			Drawable drawable = activityInfo.loadIcon(pm);
			if(drawable instanceof BitmapDrawable){
				return ((BitmapDrawable)drawable).getBitmap();
			}
			return AppInfo.getDefaultIcon(pm);
		}
	}

}
