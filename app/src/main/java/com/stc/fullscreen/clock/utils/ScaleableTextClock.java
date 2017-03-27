package com.stc.fullscreen.clock.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.WindowManager;
import android.widget.TextClock;

public class ScaleableTextClock extends TextClock
		implements ScaleGestureDetector.OnScaleGestureListener {



	private static final String TAG = "ScaleableTextView";

	private static final float TEXT_SIZE_INCR_STEP = 5.0f;
	private static final float TEXT_SCALE_TRIGGER_DELTA = 0.05f;

	// larger value - smaller result
	private static final float TEXT_SIZE_DEFAULT_DIVIDER = 7;
	private static final String PREFS_VALUE_TEXT_SIZE = "PREFS_VALUE_TEXT_SIZE";

	private final ScaleGestureDetector mScaleDetector;

	public ScaleableTextClock(Context context, AttributeSet attrs) {
		super(context, attrs);
		mScaleDetector=new ScaleGestureDetector(context, this);
		setInitialTextSize(context);
	}
	public float getSavedTextSize(){
		return PreferenceManager.getDefaultSharedPreferences(getContext()).getFloat(PREFS_VALUE_TEXT_SIZE, Float.MAX_VALUE);
	}
	public void saveTextSize(float val){
		PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putFloat(PREFS_VALUE_TEXT_SIZE, val).apply();
	}
	private void setInitialTextSize(Context context){
		float initialSize;
		if(getSavedTextSize()==Float.MAX_VALUE) {
			WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			DisplayMetrics metrics = new DisplayMetrics();
			display.getMetrics(metrics);
			initialSize = metrics.heightPixels/TEXT_SIZE_DEFAULT_DIVIDER;
		}else initialSize=getSavedTextSize();
		Log.d(TAG, "initialSize: "+initialSize);
		setTextSize(TypedValue.COMPLEX_UNIT_PX, initialSize);
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(mScaleDetector.onTouchEvent(event)) return true;
		return false;
	}

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		Log.d(TAG, "onScale: "+detector.getScaleFactor());
		float scaleFactor=detector.getScaleFactor();
		float upTrigger = 1+TEXT_SCALE_TRIGGER_DELTA;
		float downTrigger = 1-TEXT_SCALE_TRIGGER_DELTA;
		if(scaleFactor>upTrigger) {
			incrementTextSize();
			return true;
		}
		else if(scaleFactor<downTrigger) {
			decrementTextSize();
			return true;
		}
		return false;
	}


	public void incrementTextSize(){
		float oldSize=getTextSize();
		float newSize =oldSize+TEXT_SIZE_INCR_STEP;
		Log.d(TAG, "incrementTextSize: "+oldSize+"->"+newSize+"px");
		setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
		saveTextSize(newSize);
		invalidate();
	}
	public void decrementTextSize(){
		float oldSize=getTextSize();
		float newSize =oldSize-TEXT_SIZE_INCR_STEP;
		Log.d(TAG, "decrementTextSize: "+oldSize+"->"+"px");
		setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
		saveTextSize(newSize);
		invalidate();
	}
	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		Log.d(TAG, "onScaleBegin: ");
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
		Log.d(TAG, "onScaleEnd: ");
	}
}
