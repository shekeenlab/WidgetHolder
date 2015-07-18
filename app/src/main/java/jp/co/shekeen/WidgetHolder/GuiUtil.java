package jp.co.shekeen.WidgetHolder;

import android.content.Context;
import android.graphics.Point;
import android.view.WindowManager;

public class GuiUtil {

	private Context mContext;
	
	public GuiUtil(Context context){
		mContext = context;
	}
	
	public Point getDisplaySize(){
		Point point = new Point();
		WindowManager windowManager = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getSize(point);
		return point;
	}
	
	public int getNotificationWidthDp(){
		Point point = new Point();
		WindowManager windowManager = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getSize(point);
		int width = point.x;
		int fixed = mContext.getResources().getDimensionPixelSize(R.dimen.notification_width);
		if(fixed != 0){
			width = fixed;
			DebugHelper.print("tablet width!!!");
		}
		else{
			DebugHelper.print("phone width!!!");
		}
		return px2dp(width);
	}
	
	public int px2dp(int pixel){
		float scale = mContext.getResources().getDisplayMetrics().density;
		return (int)(pixel / scale + 0.5f);
	}
	
	public int dp2px(int dp){
		float scale = mContext.getResources().getDisplayMetrics().density;
		return (int)(dp * scale + 0.5f);
	}
	
	public int getCellCount(int minSize){
		/* APIドキュメントによればminHeight/minWidthの単位はdpのはずだが実際はpxでくるのでdpに変換 */
		int dp = px2dp(minSize);
		int count =  (dp + 30) / 70;
		if(count == 0){
			return 1;
		}
		return count;
	}
	
	public boolean isTablet(){
		int width = mContext.getResources().getDimensionPixelSize(R.dimen.notification_width);
		return width != 0;
	}
}
