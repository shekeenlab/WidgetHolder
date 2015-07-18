package jp.co.shekeen.WidgetHolder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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

public class ShortcutSelectActivity extends Activity implements OnItemClickListener {
	
	private static class AppAdapter extends ArrayAdapter<ShortcutListItem>{
		
		private LayoutInflater mInflater;
		
		public AppAdapter(Context context, int textViewResourceId, List<ShortcutListItem> objects) {
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
			ShortcutListItem item = getItem(position);
			textView.setText(item.appName);
			imageView.setImageURI(item.iconUri);
			return convertView;
		}
	}
	
	private static class AppComparator implements Comparator<ShortcutListItem>{

		@Override
		public int compare(ShortcutListItem lhs, ShortcutListItem rhs) {
			return lhs.appName.compareToIgnoreCase(rhs.appName);
		}
	}
	
	
	private static final int REQUEST_CODE_PICK_DETAIL = 1;
	public static final String EXTRA_PACKAGE_NAME = "jp.co.shekeen.WidgetHolder.AppInfo.extra.PACKAGE_NAME";
	private ListView mListApps;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.appselect);
		
		mListApps = (ListView)findViewById(R.id.listApps);
		PackageManager packageManager = getPackageManager();
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_CREATE_SHORTCUT);
		List<ResolveInfo> resolveList = packageManager.queryIntentActivities(intent, 0);
		List<ShortcutListItem> appList = new ArrayList<ShortcutListItem>();
		for(ResolveInfo resolveInfo : resolveList){
			appList.add(new ShortcutListItem(packageManager, resolveInfo));
		}
		AppAdapter adapter = new AppAdapter(this, 0, appList);
		adapter.sort(new AppComparator());
		mListApps.setAdapter(adapter);
		mListApps.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ShortcutListItem item = (ShortcutListItem) parent.getItemAtPosition(position);
		Intent intent  = new Intent();
		intent.setAction(Intent.ACTION_CREATE_SHORTCUT);
		intent.setClassName(item.packageName, item.activityName);
		startActivityForResult(intent, REQUEST_CODE_PICK_DETAIL);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode == REQUEST_CODE_PICK_DETAIL){
			onRequestPickDetail(requestCode, resultCode, data);
		}
	}

	private void onRequestPickDetail(int requestCode, int resultCode, Intent data) {
		if(data == null){
			resultCode = RESULT_CANCELED;
		}
		if(resultCode == RESULT_OK){
			setResult(RESULT_OK, data);
		}
		else{
			setResult(RESULT_CANCELED, null);
		}
		finish();
	}

	private static class ShortcutListItem {
		private String appName;
		private String packageName;
		private String activityName;
		private Uri iconUri;
		
		private ShortcutListItem(PackageManager pm, ResolveInfo resolveInfo){
			appName = resolveInfo.loadLabel(pm).toString();
			packageName = resolveInfo.activityInfo.applicationInfo.packageName;
			activityName = resolveInfo.activityInfo.name;
			int iconRes = resolveInfo.activityInfo.getIconResource();
			iconUri = Uri.parse("android.resource://" + packageName + "/" + iconRes);
		}
	}
}
