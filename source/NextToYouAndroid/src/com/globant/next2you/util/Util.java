package com.globant.next2you.util;

import android.content.Context;
import android.util.DisplayMetrics;

public class Util {

	public final static boolean isValidEmail(CharSequence target) {
		if (target == null) {
			return false;
		}

		return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
	}
	
	public static int dpToPx(Context ctx, int dp) {
	    DisplayMetrics displayMetrics = ctx.getResources().getDisplayMetrics();
	    int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));       
	    return px;
	}
	
	public static int pxToDp(Context ctx, int px) {
	    DisplayMetrics displayMetrics = ctx.getResources().getDisplayMetrics();
	    int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
	    return dp;
	}

}
