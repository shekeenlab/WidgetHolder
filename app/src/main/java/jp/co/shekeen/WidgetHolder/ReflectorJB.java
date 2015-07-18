package jp.co.shekeen.WidgetHolder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.widget.RemoteViews;

public class ReflectorJB {

	public static class RefRemoteViewsJB{
		private static Field sFieldActions;
		private static Field sFieldBitmapCache;
		private static Field sFieldPackage;
		private static Field sFieldLayoutId;
		private static Method sMethodRecalculateMemoryUsage;
		
		public RemoteViews instance;
		@SuppressWarnings("rawtypes")
		public ArrayList mActions;
		public RefBitmapCacheJB mBitmapCache;
		public String mPackage;
		public int mLayoutId;
		
		@SuppressWarnings("rawtypes")
		public RefRemoteViewsJB(RemoteViews obj){
			loadReflection();
			instance = obj;
			try {
				mActions = (ArrayList) sFieldActions.get(instance);
				mPackage = (String) sFieldPackage.get(instance);
				mLayoutId = sFieldLayoutId.getInt(instance);
				Object bitmapCache = sFieldBitmapCache.get(instance);
				mBitmapCache = new RefBitmapCacheJB(bitmapCache);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private boolean loadReflection(){
			if(sFieldActions != null){
				return true;
			}
			try {
				sFieldActions = RemoteViews.class.getDeclaredField("mActions");
				sFieldBitmapCache = RemoteViews.class.getDeclaredField("mBitmapCache");
				sFieldPackage = RemoteViews.class.getDeclaredField("mPackage");
				sFieldLayoutId = RemoteViews.class.getDeclaredField("mLayoutId");
				sMethodRecalculateMemoryUsage = RemoteViews.class.getDeclaredMethod("recalculateMemoryUsage");
				
				sFieldActions.setAccessible(true);
				sFieldBitmapCache.setAccessible(true);
				sFieldPackage.setAccessible(true);
				sFieldLayoutId.setAccessible(true);
				sMethodRecalculateMemoryUsage.setAccessible(true);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		public void recalculateMemoryUsage(){
			try {
				sMethodRecalculateMemoryUsage.invoke(instance);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static class RefBitmapCacheJB{
		private static Class<?> sClassBitmapCache;
		private static Field sFieldBitmaps;
		
		public Object instance;
		@SuppressWarnings("rawtypes")
		public ArrayList mBitmaps;
		
		@SuppressWarnings("rawtypes")
		public RefBitmapCacheJB(Object obj){
			loadReflection();
			instance = obj;
			try {
				mBitmaps = (ArrayList) sFieldBitmaps.get(instance);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private boolean loadReflection(){
			if(sClassBitmapCache != null){
				return true;
			}
			try {
				sClassBitmapCache = Class.forName("android.widget.RemoteViews$BitmapCache");
				sFieldBitmaps = sClassBitmapCache.getDeclaredField("mBitmaps");
				sFieldBitmaps.setAccessible(true);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
	}
	
	public abstract static class RefActionJB{
		private static Class<?> sClassAction;
		private static Field sFieldViewId;
		
		public Object instance;
		/* プリミティブ型の場合、変更しても反映されないので注意 */
		public int viewId;
		
		private RefActionJB(){}
		
		private RefActionJB(Object obj){
			loadReflection();
			instance = obj;
			try {
				viewId = sFieldViewId.getInt(instance);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private static boolean loadReflection(){
			if(sClassAction != null){
				return true;
			}
			try {
				sClassAction = Class.forName("android.widget.RemoteViews$Action");
				sFieldViewId = sClassAction.getDeclaredField("viewId");
				sFieldViewId.setAccessible(true);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		public abstract boolean isSameTarget(RefActionJB action);
	}
	
	public static class RefSetEmptyViewJB extends RefActionJB{
		private static Class<?> sClassSetEmptyView;
		
		private RefSetEmptyViewJB(){}
		
		private RefSetEmptyViewJB(Object obj){
			super(obj);
			loadReflection();
			instance = obj;
			try {
				/* DO NOTHING */
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private static boolean loadReflection(){
			if(sClassSetEmptyView != null){
				return true;
			}
			try {
				sClassSetEmptyView = Class.forName("android.widget.RemoteViews$SetEmptyView");
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		public static boolean isTargetInstance(Object obj){
			if(loadReflection()){
				return sClassSetEmptyView == obj.getClass();
			}
			return false;
		}
		
		@Override
		public boolean isSameTarget(RefActionJB action) {
			if(RefSetEmptyViewJB.class.isInstance(action)){
				return viewId == action.viewId;
			}
			return false;
		}
	}
	
	public static class RefSetOnClickFillInIntentJB extends RefActionJB{
		private static Class<?> sClassSetOnClickFillInIntent;
		
		private RefSetOnClickFillInIntentJB(){}
		
		private RefSetOnClickFillInIntentJB(Object obj){
			super(obj);
			loadReflection();
			instance = obj;
			try {
				/* DO NOTHING */
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private static boolean loadReflection(){
			if(sClassSetOnClickFillInIntent != null){
				return true;
			}
			try {
				sClassSetOnClickFillInIntent = Class.forName("android.widget.RemoteViews$SetOnClickFillInIntent");
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		public static boolean isTargetInstance(Object obj){
			if(loadReflection()){
				return sClassSetOnClickFillInIntent == obj.getClass();
			}
			return false;
		}
		
		@Override
		public boolean isSameTarget(RefActionJB action) {
			if(RefSetOnClickFillInIntentJB.class.isInstance(action)){
				return viewId == action.viewId;
			}
			return false;
		}
	}
	
	public static class RefSetPendingIntentTemplateJB extends RefActionJB{
		private static Class<?> sClassSetPendingIntentTemplate;
		
		private RefSetPendingIntentTemplateJB(){}
		
		private RefSetPendingIntentTemplateJB(Object obj){
			super(obj);
			loadReflection();
			instance = obj;
			try {
				/* DO NOTHING */
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private static boolean loadReflection(){
			if(sClassSetPendingIntentTemplate != null){
				return true;
			}
			try {
				sClassSetPendingIntentTemplate = Class.forName("android.widget.RemoteViews$SetPendingIntentTemplate");
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		public static boolean isTargetInstance(Object obj){
			if(loadReflection()){
				return sClassSetPendingIntentTemplate == obj.getClass();
			}
			return false;
		}
		
		@Override
		public boolean isSameTarget(RefActionJB action) {
			if(RefSetPendingIntentTemplateJB.class.isInstance(action)){
				return viewId == action.viewId;
			}
			return false;
		}
	}
	
	public static class RefSetRemoteViewsAdapterIntentJB extends RefActionJB{
		private static Class<?> sClassSetRemoteViewsAdapterIntent;
		
		private RefSetRemoteViewsAdapterIntentJB(){}
		
		private RefSetRemoteViewsAdapterIntentJB(Object obj){
			super(obj);
			loadReflection();
			instance = obj;
			try {
				/* DO NOTHING */
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private static boolean loadReflection(){
			if(sClassSetRemoteViewsAdapterIntent != null){
				return true;
			}
			try {
				sClassSetRemoteViewsAdapterIntent = Class.forName("android.widget.RemoteViews$SetRemoteViewsAdapterIntent");
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		public static boolean isTargetInstance(Object obj){
			if(loadReflection()){
				return sClassSetRemoteViewsAdapterIntent == obj.getClass();
			}
			return false;
		}
		
		@Override
		public boolean isSameTarget(RefActionJB action) {
			if(RefSetRemoteViewsAdapterIntentJB.class.isInstance(action)){
				return viewId == action.viewId;
			}
			return false;
		}
	}
	
	public static class RefSetOnClickPendingIntentJB extends RefActionJB{
		private static Class<?> sClassSetOnClickPendingIntent;
		
		private RefSetOnClickPendingIntentJB(){}
		
		private RefSetOnClickPendingIntentJB(Object obj){
			super(obj);
			loadReflection();
			instance = obj;
			try {
				/* DO NOTHING */
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private static boolean loadReflection(){
			if(sClassSetOnClickPendingIntent != null){
				return true;
			}
			try {
				sClassSetOnClickPendingIntent = Class.forName("android.widget.RemoteViews$SetOnClickPendingIntent");
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		public static boolean isTargetInstance(Object obj){
			if(loadReflection()){
				Class <?> ccc = obj.getClass();
				return sClassSetOnClickPendingIntent == ccc;
			}
			return false;
		}
		
		@Override
		public boolean isSameTarget(RefActionJB action) {
			if(RefSetOnClickPendingIntentJB.class.isInstance(action)){
				return viewId == action.viewId;
			}
			return false;
		}
	}
	
	public static class RefSetDrawableParametersJB extends RefActionJB{
		private static Class<?> sClassSetDrawableParameters;
		
		private RefSetDrawableParametersJB(){}
		
		private RefSetDrawableParametersJB(Object obj){
			super(obj);
			loadReflection();
			instance = obj;
			try {
				/* DO NOTHING */
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private static boolean loadReflection(){
			if(sClassSetDrawableParameters != null){
				return true;
			}
			try {
				sClassSetDrawableParameters = Class.forName("android.widget.RemoteViews$SetDrawableParameters");
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		public static boolean isTargetInstance(Object obj){
			if(loadReflection()){
				return sClassSetDrawableParameters == obj.getClass();
			}
			return false;
		}
		
		@Override
		public boolean isSameTarget(RefActionJB action) {
			if(RefSetDrawableParametersJB.class.isInstance(action)){
				return viewId == action.viewId;
			}
			return false;
		}
	}
	
	/* ReflectionActionは様々な機能を内包しているので、追加で判定が必要 */
	public static class RefReflectionActionWithoutParamsJB extends RefActionJB{
		private static Class<?> sClassReflectionActionWithoutParams;
		private static Field sFieldMethodName;
		public String methodName;
		
		private RefReflectionActionWithoutParamsJB(){}
		
		private RefReflectionActionWithoutParamsJB(Object obj){
			super(obj);
			loadReflection();
			instance = obj;
			try {
				methodName = (String) sFieldMethodName.get(instance);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private static boolean loadReflection(){
			if(sClassReflectionActionWithoutParams != null){
				return true;
			}
			try {
				sClassReflectionActionWithoutParams = Class.forName("android.widget.RemoteViews$ReflectionActionWithoutParams");
				sFieldMethodName = sClassReflectionActionWithoutParams.getDeclaredField("methodName");
				sFieldMethodName.setAccessible(true);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		public static boolean isTargetInstance(Object obj){
			if(loadReflection()){
				return sClassReflectionActionWithoutParams == obj.getClass();
			}
			return false;
		}
		
		@Override
		public boolean isSameTarget(RefActionJB action) {
			if(RefReflectionActionWithoutParamsJB.class.isInstance(action)){
				try {
					String methodName2 = ((RefReflectionActionWithoutParamsJB) action).methodName;
					return viewId == action.viewId && methodName.equals(methodName2);
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
			return false;
		}
	}
	
	public static class RefBitmapReflectionActionJB extends RefActionJB{
		private static Class<?> sClassBitmapReflectionAction;
		private static Field sFieldMethodName;
		private static Field sFieldBitmap;
		public String methodName;
		public Bitmap bitmap;
		
		private RefBitmapReflectionActionJB(){}
		
		private RefBitmapReflectionActionJB(Object obj){
			super(obj);
			loadReflection();
			instance = obj;
			try {
				methodName = (String) sFieldMethodName.get(instance);
				bitmap = (Bitmap) sFieldBitmap.get(instance);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private static boolean loadReflection(){
			if(sClassBitmapReflectionAction != null){
				return true;
			}
			try {
				sClassBitmapReflectionAction = Class.forName("android.widget.RemoteViews$BitmapReflectionAction");
				sFieldMethodName = sClassBitmapReflectionAction.getDeclaredField("methodName");
				sFieldBitmap = sClassBitmapReflectionAction.getDeclaredField("bitmap");
				sFieldMethodName.setAccessible(true);
				sFieldBitmap.setAccessible(true);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		public static boolean isTargetInstance(Object obj){
			if(loadReflection()){
				return sClassBitmapReflectionAction == obj.getClass();
			}
			return false;
		}
		
		@Override
		public boolean isSameTarget(RefActionJB action) {
			if(RefBitmapReflectionActionJB.class.isInstance(action)){
				String methodName2 = ((RefBitmapReflectionActionJB) action).methodName;
				return viewId == action.viewId && methodName.equals(methodName2);
			}
			return false;
		}
	}
	
	public static class RefReflectionActionJB extends RefActionJB{
		private static Class<?> sClassReflectionAction;
		private static Field sFieldMethodName;
		private static Field sFieldType;
		public String methodName;
		int type;
		
		private RefReflectionActionJB(){}
		
		private RefReflectionActionJB(Object obj){
			super(obj);
			loadReflection();
			instance = obj;
			try {
				methodName = (String) sFieldMethodName.get(instance);
				type = sFieldType.getInt(instance);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private static boolean loadReflection(){
			if(sClassReflectionAction != null){
				return true;
			}
			try {
				sClassReflectionAction = Class.forName("android.widget.RemoteViews$ReflectionAction");
				sFieldMethodName = sClassReflectionAction.getDeclaredField("methodName");
				sFieldType = sClassReflectionAction.getDeclaredField("type");
				sFieldMethodName.setAccessible(true);
				sFieldType.setAccessible(true);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		public static boolean isTargetInstance(Object obj){
			if(loadReflection()){
				return sClassReflectionAction == obj.getClass();
			}
			return false;
		}
		
		@Override
		public boolean isSameTarget(RefActionJB action) {
			if(RefReflectionActionJB.class.isInstance(action)){
				String methodName2 = ((RefReflectionActionJB) action).methodName;
				return viewId == action.viewId && methodName.equals(methodName2);
			}
			return false;
		}
	}
	
	public static class RefViewGroupActionJB extends RefActionJB{
		private static Class<?> sClassViewGroupAction;
		private static Field sFieldNestedViews;
		public RemoteViews nestedViews;
		
		private RefViewGroupActionJB(){}
		
		private RefViewGroupActionJB(Object obj){
			super(obj);
			loadReflection();
			instance = obj;
			try {
				nestedViews = (RemoteViews) sFieldNestedViews.get(instance);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private static boolean loadReflection(){
			if(sClassViewGroupAction != null){
				return true;
			}
			try {
				sClassViewGroupAction = Class.forName("android.widget.RemoteViews$ViewGroupAction");
				sFieldNestedViews = sClassViewGroupAction.getDeclaredField("nestedViews");
				sFieldNestedViews.setAccessible(true);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		public static boolean isTargetInstance(Object obj){
			if(loadReflection()){
				return sClassViewGroupAction == obj.getClass();
			}
			return false;
		}
		
		@Override
		public boolean isSameTarget(RefActionJB action) {
			if(RefViewGroupActionJB.class.isInstance(action)){
				return viewId == action.viewId;
			}
			return false;
		}
	}
	
	public static class RefTextViewDrawableActionJB extends RefActionJB{
		private static Class<?> sClassTextViewDrawableAction;
		
		private RefTextViewDrawableActionJB(){}
		
		private RefTextViewDrawableActionJB(Object obj){
			super(obj);
			loadReflection();
			instance = obj;
			try {
				/* DO NOTHING */
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private static boolean loadReflection(){
			if(sClassTextViewDrawableAction != null){
				return true;
			}
			try {
				sClassTextViewDrawableAction = Class.forName("android.widget.RemoteViews$TextViewDrawableAction");
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		public static boolean isTargetInstance(Object obj){
			if(loadReflection()){
				return sClassTextViewDrawableAction == obj.getClass();
			}
			return false;
		}
		
		@Override
		public boolean isSameTarget(RefActionJB action) {
			if(RefTextViewDrawableActionJB.class.isInstance(action)){
				return viewId == action.viewId;
			}
			return false;
		}
	}
	
	public static class RefTextViewSizeActionJB extends RefActionJB{
		private static Class<?> sClassTextViewSizeAction;
		
		private RefTextViewSizeActionJB(){}
		
		private RefTextViewSizeActionJB(Object obj){
			super(obj);
			loadReflection();
			instance = obj;
			try {
				/* DO NOTHING */
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private static boolean loadReflection(){
			if(sClassTextViewSizeAction != null){
				return true;
			}
			try {
				sClassTextViewSizeAction = Class.forName("android.widget.RemoteViews$TextViewSizeAction");
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		public static boolean isTargetInstance(Object obj){
			if(loadReflection()){
				return sClassTextViewSizeAction == obj.getClass();
			}
			return false;
		}
		
		@Override
		public boolean isSameTarget(RefActionJB action) {
			if(RefTextViewSizeActionJB.class.isInstance(action)){
				return viewId == action.viewId;
			}
			return false;
		}
	}
	
	public static class RefViewPaddingActionJB extends RefActionJB{
		private static Class<?> sClassViewPaddingAction;
		
		private RefViewPaddingActionJB(){}
		
		private RefViewPaddingActionJB(Object obj){
			super(obj);
			loadReflection();
			instance = obj;
			try {
				/* DO NOTHING */
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private static boolean loadReflection(){
			if(sClassViewPaddingAction != null){
				return true;
			}
			try {
				sClassViewPaddingAction = Class.forName("android.widget.RemoteViews$ViewPaddingAction");
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		public static boolean isTargetInstance(Object obj){
			if(loadReflection()){
				return sClassViewPaddingAction == obj.getClass();
			}
			return false;
		}
		
		@Override
		public boolean isSameTarget(RefActionJB action) {
			if(RefViewPaddingActionJB.class.isInstance(action)){
				return viewId == action.viewId;
			}
			return false;
		}
	}
	
	/* 不明なアクションな場合にはこのクラスでラップする */
	public static class UnknownActionJB extends RefActionJB{

		private UnknownActionJB(){}
		
		private UnknownActionJB(Object obj){
			super(obj);
			instance = obj;
		}
		
		@Override
		public boolean isSameTarget(RefActionJB action) {
			return false;
		}
		
	}
	
	public static RefActionJB getInstance(Object action){
        if(RefSetEmptyViewJB.isTargetInstance(action)){
                return new RefSetEmptyViewJB(action);
        }
        else if(RefSetOnClickFillInIntentJB.isTargetInstance(action)){
                return new RefSetOnClickFillInIntentJB(action);
        }
        else if(RefSetPendingIntentTemplateJB.isTargetInstance(action)){
                return new RefSetPendingIntentTemplateJB(action);
        }
        else if(RefSetRemoteViewsAdapterIntentJB.isTargetInstance(action)){
                return new RefSetRemoteViewsAdapterIntentJB(action);
        }
        else if(RefSetOnClickPendingIntentJB.isTargetInstance(action)){
                return new RefSetOnClickPendingIntentJB(action);
        }
        else if(RefSetDrawableParametersJB.isTargetInstance(action)){
                return new RefSetDrawableParametersJB(action);
        }
        else if(RefReflectionActionWithoutParamsJB.isTargetInstance(action)){
                return new RefReflectionActionWithoutParamsJB(action);
        }
        else if(RefBitmapReflectionActionJB.isTargetInstance(action)){
                return new RefBitmapReflectionActionJB(action);
        }
        else if(RefReflectionActionJB.isTargetInstance(action)){
                return new RefReflectionActionJB(action);
        }
        else if(RefViewGroupActionJB.isTargetInstance(action)){
                return new RefViewGroupActionJB(action);
        }
        else if(RefTextViewDrawableActionJB.isTargetInstance(action)){
                return new RefTextViewDrawableActionJB(action);
        }
        else if(RefTextViewSizeActionJB.isTargetInstance(action)){
                return new RefTextViewSizeActionJB(action);
        }
        else if(RefViewPaddingActionJB.isTargetInstance(action)){
                return new RefViewPaddingActionJB(action);
        }
        return new UnknownActionJB(action);
	}

}
