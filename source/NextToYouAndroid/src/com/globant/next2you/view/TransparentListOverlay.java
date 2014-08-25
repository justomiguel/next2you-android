package com.globant.next2you.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.globant.next2you.util.Util;

public class TransparentListOverlay extends View {
	@SuppressWarnings("unused")
	private static final String TAG = "TransparentListOverlay";
	private float screenW;
	private float screenH;
	private Paint paint;
	public static int markerSize;
	private Path path;
	public int fillColor = Color.WHITE;

	public TransparentListOverlay(Context context, AttributeSet attr) {
		super(context, attr);
		init();
	}
	
	public void changePaint(int color) {
		fillColor = color;
		paint.setColor(fillColor);
	}

	public void init() {
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		markerSize = Util.dpToPx(getContext(), 35);
		paint = new Paint();
		// http://android.okhelp.cz/drawbitmap-clippath-union-difference-intersect-replace-xor-android-example/
		path = new Path();
		path.addCircle(screenW / 2, screenH / 2, markerSize,
				Path.Direction.CCW);
		paint.setColor(fillColor);
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		screenW = w;
		screenH = h;
		init();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {	
		super.onDraw(canvas);
		canvas.clipPath(path, android.graphics.Region.Op.XOR);
		canvas.drawRect(0, 0, screenW, screenH, paint);
	}

}
