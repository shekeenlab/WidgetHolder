package jp.co.shekeen.WidgetHolder;

import android.util.Log;

public class DebugHelper {
	
	public static void print(Object... args){
		if(!Config.DEBUG){
			return;
		}
		StringBuffer buffer = new StringBuffer();
		StackTraceElement stack = new Throwable().getStackTrace()[1];
		buffer.append("[");
		buffer.append(stack.getClassName());
		buffer.append("::");
		buffer.append(stack.getMethodName());
		buffer.append("] ");
		for(int i = 0; i < args.length; i++){
			if(i > 0){
				buffer.append(" ");
			}
			buffer.append(args[i]);
		}
		Log.d(Config.TAG, buffer.toString());
	}
	
	public static void printStackTrace(Exception e){
		if(!Config.DEBUG){
			return;
		}
		e.printStackTrace();
	}
}
