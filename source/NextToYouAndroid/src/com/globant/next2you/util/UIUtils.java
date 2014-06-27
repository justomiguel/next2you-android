package com.globant.next2you.util;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.widget.TextView;

public class UIUtils {
	private static final String TAG = "UIUtils";

	public static void prepareTextView(Context ctx, TextView tv) {
		try {
			Typeface font = getAppTypeFace(ctx);
			tv.setTypeface(font);
		} catch (Exception e) {
			Log.e(TAG, "", e);
		}
	}

	public static Typeface getAppTypeFace(Context ctx) {
		Typeface font = Typeface.createFromAsset(ctx.getAssets(),
				Constants.APP_FONT_NAME);
		return font;
	}

}
