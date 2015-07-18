package jp.co.shekeen.WidgetHolder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android.widget.RemoteViews;

public class ReflectorICS {
	
	public static class RefRemoteViewsICS{
		private static Field sFieldActions;
		private static Field sFieldPackage;
		private static Field sFieldLayoutId;
		private static Method sMethodRecalculateMemoryUsage;
		
		public RemoteViews instance;
		@SuppressWarnings("rawtypes")
		public ArrayList mActions;
		public String mPackage;
		public int mLayoutId;
		
		@SuppressWarnings("rawtypes")
		public RefRemoteViewsICS(RemoteViews obj){
			loadReflection();
			instance = obj;
			try {
				mActions = (ArrayList) sFieldActions.get(instance);
				mPackage = (String) sFieldPackage.get(instance);
				mLayoutId = sFieldLayoutId.getInt(instance);
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
				sFieldPackage = RemoteViews.class.getDeclaredField("mPackage");
				sFieldLayoutId = RemoteViews.class.getDeclaredField("mLayoutId");
				sMethodRecalculateMemoryUsage = RemoteViews.class.getDeclaredMethod("recalculateMemoryUsage");
				
				sFieldActions.setAccessible(true);
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
	
	public abstract static class RefActionICS{
		private static Class<?> sClassAction;
		
		public Object instance;
		
		private RefActionICS(){}
		
		private RefActionICS(Object obj){
			loadReflection();
			instance = obj;
			try {
				/* 特になし */
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
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		public abstract boolean isSameTarget(RefActionICS action);
	}
	
	public static class RefSetEmptyViewICS extends RefActionICS{
		private static Class<?> sClassSetEmptyView;
		private static Field sFieldViewId;
		/* プリミティブ型の場合、変更しても反映されないので注意 */
		public int viewId;
		
		private RefSetEmptyViewICS(){}
		
		private RefSetEmptyViewICS(Object obj){
			super(obj);
			loadReflection();
			instance = obj;
			try {
				viewId = sFieldViewId.getInt(instance);
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
				sFieldViewId = sClassSetEmptyView.getDeclaredField("viewId");
				sFieldViewId.setAccessible(true);
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
		public boolean isSameTarget(RefActionICS action) {
			if(RefSetEmptyViewICS.class.isInstance(action)){
				return viewId == ((RefSetEmptyViewICS)action).viewId;
			}
			return false;
		}
	}
	
	public static class RefSetOnClickFillInIntentICS extends RefActionICS{
		private static Class<?> sClassSetOnClickFillInIntent;
		private static Field sFieldViewId;
		/* プリミティブ型の場合、変更しても反映されないので注意 */
		public int viewId;
		
		private RefSetOnClickFillInIntentICS(){}
		
		private RefSetOnClickFillInIntentICS(Object obj){
			super(obj);
			loadReflection();
			instance = obj;
			try {
				viewId = sFieldViewId.getInt(instance);
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
				sFieldViewId = sClassSetOnClickFillInIntent.getDeclaredField("viewId");
				sFieldViewId.setAccessible(true);
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
		public boolean isSameTarget(RefActionICS action) {
			if(RefSetOnClickFillInIntentICS.class.isInstance(action)){
				return viewId == ((RefSetOnClickFillInIntentICS) action).viewId;
			}
			return false;
		}
	}
	
	public static class RefSetPendingIntentTemplateICS extends RefActionICS{
		private static Class<?> sClassSetPendingIntentTemplate;
		private static Field sFieldViewId;
		/* プリミティブ型の場合、変更しても反映されないので注意 */
		public int viewId;
		
		private RefSetPendingIntentTemplateICS(){}
		
		private RefSetPendingIntentTemplateICS(Object obj){
			super(obj);
			loadReflection();
			instance = obj;
			try {
				viewId = sFieldViewId.getInt(instance);
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
				sFieldViewId = sClassSetPendingIntentTemplate.getDeclaredField("viewId");
				sFieldViewId.setAccessible(true);
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
		public boolean isSameTarget(RefActionICS action) {
			if(RefSetPendingIntentTemplateICS.class.isInstance(action)){
				return viewId == ((RefSetPendingIntentTemplateICS)action).viewId;
			}
			return false;
		}
	}
	
	public static class RefSetRemoteViewsAdapterIntentICS extends RefActionICS{
		private static Class<?> sClassSetRemoteViewsAdapterIntent;
		private static Field sFieldViewId;
		/* プリミティブ型の場合、変更しても反映されないので注意 */
		public int viewId;
		
		private RefSetRemoteViewsAdapterIntentICS(){}
		
		private RefSetRemoteViewsAdapterIntentICS(Object obj){
			super(obj);
			loadReflection();
			instance = obj;
			try {
				viewId = sFieldViewId.getInt(instance);
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
				sFieldViewId = sClassSetRemoteViewsAdapterIntent.getDeclaredField("viewId");
				sFieldViewId.setAccessible(true);
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
		public boolean isSameTarget(RefActionICS action) {
			if(RefSetRemoteViewsAdapterIntentICS.class.isInstance(action)){
				return viewId == ((RefSetRemoteViewsAdapterIntentICS)action).viewId;
			}
			return false;
		}
	}
	
	public static class RefSetOnClickPendingIntentICS extends RefActionICS{
		private static Class<?> sClassSetOnClickPendingIntent;
		private static Field sFieldViewId;
		/* プリミティブ型の場合、変更しても反映されないので注意 */
		public int viewId;
		
		private RefSetOnClickPendingIntentICS(){}
		
		private RefSetOnClickPendingIntentICS(Object obj){
			super(obj);
			loadReflection();
			instance = obj;
			try {
				viewId = sFieldViewId.getInt(instance);
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
				sFieldViewId = sClassSetOnClickPendingIntent.getDeclaredField("viewId");
				sFieldViewId.setAccessible(true);
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
		public boolean isSameTarget(RefActionICS action) {
			if(RefSetOnClickPendingIntentICS.class.isInstance(action)){
				return viewId == ((RefSetOnClickPendingIntentICS)action).viewId;
			}
			return false;
		}
	}
	
	public static class RefSetDrawableParametersICS extends RefActionICS{
		private static Class<?> sClassSetDrawableParameters;
		private static Field sFieldViewId;
		/* プリミティブ型の場合、変更しても反映されないので注意 */
		public int viewId;
		
		private RefSetDrawableParametersICS(){}
		
		private RefSetDrawableParametersICS(Object obj){
			super(obj);
			loadReflection();
			instance = obj;
			try {
				viewId = sFieldViewId.getInt(instance);
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
				sFieldViewId = sClassSetDrawableParameters.getDeclaredField("viewId");
				sFieldViewId.setAccessible(true);
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
		public boolean isSameTarget(RefActionICS action) {
			if(RefSetDrawableParametersICS.class.isInstance(action)){
				return viewId == ((RefSetDrawableParametersICS)action).viewId;
			}
			return false;
		}
	}
	
	/* ReflectionActionは様々な機能を内包しているので、追加で判定が必要 */
	public static class RefReflectionActionWithoutParamsICS extends RefActionICS{
		private static Class<?> sClassReflectionActionWithoutParams;
		private static Field sFieldViewId;
		private static Field sFieldMethodName;
		/* プリミティブ型の場合、変更しても反映されないので注意 */
		public int viewId;
		public String methodName;
		
		private RefReflectionActionWithoutParamsICS(){}
		
		private RefReflectionActionWithoutParamsICS(Object obj){
			super(obj);
			loadReflection();
			instance = obj;
			try {
				viewId = sFieldViewId.getInt(instance);
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
				sFieldViewId = sClassReflectionActionWithoutParams.getDeclaredField("viewId");
				sFieldMethodName = sClassReflectionActionWithoutParams.getDeclaredField("methodName");
				sFieldViewId.setAccessible(true);
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
		public boolean isSameTarget(RefActionICS action) {
			if(RefReflectionActionWithoutParamsICS.class.isInstance(action)){
				try {
					int viewId2 = ((RefReflectionActionWithoutParamsICS)action).viewId;
					String methodName2 = ((RefReflectionActionWithoutParamsICS)action).methodName;
					return viewId == viewId2 && methodName.equals(methodName2);
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
			return false;
		}
	}
	
	public static class RefReflectionActionICS extends RefActionICS{
		public static final int BOOLEAN = 1;
        public static final int BYTE = 2;
        public static final int SHORT = 3;
        public static final int INT = 4;
        public static final int LONG = 5;
        public static final int FLOAT = 6;
        public static final int DOUBLE = 7;
        public static final int CHAR = 8;
        public static final int STRING = 9;
        public static final int CHAR_SEQUENCE = 10;
        public static final int URI = 11;
        public static final int BITMAP = 12;
        public static final int BUNDLE = 13;
        public static final int INTENT = 14;
		
		private static Class<?> sClassReflectionAction;
		private static Field sFieldViewId;
		private static Field sFieldMethodName;
		private static Field sFieldType;
		private static Field sFieldValue;
		/* プリミティブ型の場合、変更しても反映されないので注意 */
		public int viewId;
		public String methodName;
		public int type;
		public Object value;
		
		private RefReflectionActionICS(){}
		
		private RefReflectionActionICS(Object obj){
			super(obj);
			loadReflection();
			instance = obj;
			try {
				viewId = sFieldViewId.getInt(instance);
				methodName = (String) sFieldMethodName.get(instance);
				type = sFieldType.getInt(instance);
				value = sFieldValue.get(instance);
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
				sFieldViewId = sClassReflectionAction.getDeclaredField("viewId");
				sFieldMethodName = sClassReflectionAction.getDeclaredField("methodName");
				sFieldType = sClassReflectionAction.getDeclaredField("type");
				sFieldValue = sClassReflectionAction.getDeclaredField("value");
				sFieldViewId.setAccessible(true);
				sFieldMethodName.setAccessible(true);
				sFieldType.setAccessible(true);
				sFieldValue.setAccessible(true);
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
		public boolean isSameTarget(RefActionICS action) {
			if(RefReflectionActionICS.class.isInstance(action)){
				int viewId2 = ((RefReflectionActionICS)action).viewId;
				String methodName2 = ((RefReflectionActionICS)action).methodName;
				return viewId == viewId2 && methodName.equals(methodName2);
			}
			return false;
		}
	}
	
	public static class RefViewGroupActionICS extends RefActionICS{
		private static Class<?> sClassViewGroupAction;
		private static Field sFieldViewId;
		private static Field sFieldNestedViews;
		/* プリミティブ型の場合、変更しても反映されないので注意 */
		public int viewId;
		RemoteViews nestedViews;
		
		private RefViewGroupActionICS(){}
		
		private RefViewGroupActionICS(Object obj){
			super(obj);
			loadReflection();
			instance = obj;
			try {
				viewId = sFieldViewId.getInt(instance);
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
				sFieldViewId = sClassViewGroupAction.getDeclaredField("viewId");
				sFieldNestedViews = sClassViewGroupAction.getDeclaredField("nestedViews");
				sFieldViewId.setAccessible(true);
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
		public boolean isSameTarget(RefActionICS action) {
			if(RefViewGroupActionICS.class.isInstance(action)){
				return viewId == ((RefViewGroupActionICS)action).viewId;
			}
			/* nestedViewは再帰的に処理する必要がありそう */
			return false;
		}
	}
	
	/* 不明なアクションな場合にはこのクラスでラップする */
	public static class UnknownActionICS extends RefActionICS{

		private UnknownActionICS(){}
		
		private UnknownActionICS(Object obj){
			super(obj);
			instance = obj;
		}
		
		@Override
		public boolean isSameTarget(RefActionICS action) {
			return false;
		}
		
	}
	
	public static RefActionICS getInstance(Object action){
        if(RefSetEmptyViewICS.isTargetInstance(action)){
                return new RefSetEmptyViewICS(action);
        }
        else if(RefSetOnClickFillInIntentICS.isTargetInstance(action)){
                return new RefSetOnClickFillInIntentICS(action);
        }
        else if(RefSetPendingIntentTemplateICS.isTargetInstance(action)){
                return new RefSetPendingIntentTemplateICS(action);
        }
        else if(RefSetRemoteViewsAdapterIntentICS.isTargetInstance(action)){
                return new RefSetRemoteViewsAdapterIntentICS(action);
        }
        else if(RefSetOnClickPendingIntentICS.isTargetInstance(action)){
                return new RefSetOnClickPendingIntentICS(action);
        }
        else if(RefSetDrawableParametersICS.isTargetInstance(action)){
                return new RefSetDrawableParametersICS(action);
        }
        else if(RefReflectionActionWithoutParamsICS.isTargetInstance(action)){
                return new RefReflectionActionWithoutParamsICS(action);
        }
        else if(RefReflectionActionICS.isTargetInstance(action)){
                return new RefReflectionActionICS(action);
        }
        else if(RefViewGroupActionICS.isTargetInstance(action)){
                return new RefViewGroupActionICS(action);
        }
        return new UnknownActionICS(action);
	}

}
