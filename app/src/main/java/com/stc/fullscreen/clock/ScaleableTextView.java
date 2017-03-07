package com.stc.fullscreen.clock;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.TextView;

public class ScaleableTextView extends TextView
		implements ScaleGestureDetector.OnScaleGestureListener {



	private static final String TAG = "ScaleableTextView";
	private final ScaleGestureDetector mScaleDetector;

	public ScaleableTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mScaleDetector=new ScaleGestureDetector(context, this);
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
		if(scaleFactor>1.05) {
			incrementTextSize();
			return true;
		}
		else if(scaleFactor<0.95) {
			decrementTextSize();
			return true;
		}
		return false;
	}


	public void incrementTextSize(){
		float oldSize=getTextSize();
		Log.d(TAG, "incrementTextSize: "+oldSize+"+1");
		setTextSize(TypedValue.COMPLEX_UNIT_PX, oldSize+2.0f);
		invalidate();
	}
	public void decrementTextSize(){
		float oldSize=getTextSize();
		Log.d(TAG, "decrementTextSize: "+oldSize+"-1");
		setTextSize(TypedValue.COMPLEX_UNIT_PX, oldSize-2.0f);
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
