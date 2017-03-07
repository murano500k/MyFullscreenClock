package com.stc.fullscreen.clock;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
	public static final String TAG = "FullscreenActivity";
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * Some older devices needs a small delay between UI widget updates
	 * and a change of the status and navigation bar.
	 */
	private static final int UI_ANIMATION_DELAY = 300;
	public static final int REQUEST_CHANGE_BRIGHTNESS = 19932;
	public static final int REQUEST_SPEAK = 74;
	public static final int  REQUEST_CHANGE_SETTINGS = 43265;

	public  ScaleableTextView mTimeView;
	public  ScaleableTextView mDateView;
	public  boolean mVisible;
	public  static final int TICK_DELAY_MILLIS = 250;
	public  static final String MY_TIME_FORMAT = "HH:mm:ss";
	public  static final String MY_DATE_FORMAT= "EEE dd MMM";


	private final Handler mHandler = new Handler(Looper.getMainLooper());
	private final SimpleDateFormat mTimeFormat = new SimpleDateFormat(MY_TIME_FORMAT, Locale.US);
	private final SimpleDateFormat mDateFormat = new SimpleDateFormat(MY_DATE_FORMAT, Locale.US);

	private final Runnable mTickListener = new Runnable() {
		@Override
		public void run() {
			mTimeView.setText(mTimeFormat.format(Calendar.getInstance().getTime()));
			mDateView.setText(mDateFormat.format(Calendar.getInstance().getTime()));
			mHandler.postDelayed(mTickListener, TICK_DELAY_MILLIS);
		}
	};
	private View mControlsView;
	private View mAllView;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_fullscreen);

		mVisible = true;
		mControlsView = findViewById(R.id.fullscreen_content_controls);
		mTimeView = (ScaleableTextView) findViewById(R.id.time_content);
		mDateView = (ScaleableTextView) findViewById(R.id.date_content);
		mControlsView= findViewById(R.id.fullscreen_content_controls);
		mAllView = findViewById(R.id.fullscreen);
		mAllView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: ");
				toggle();
			}
		});
		findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
		findViewById(R.id.dummy_button).setOnClickListener(mButtonClickListener);
		mTickListener.run();
		applyPrefsValues();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode==REQUEST_CHANGE_SETTINGS){
			applyPrefsValues();
		}
	}

	private void applyPrefsValues(){
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		String fontPref = sharedPref.getString(SettingsActivity.KEY_PREF_FONT, null);
		int colorPref = sharedPref.getInt(SettingsActivity.KEY_PREF_COLOR, -1);
		Log.d(TAG, "applyPrefsValues: color = "+colorPref);
		Log.d(TAG, "applyPrefsValues: font = "+fontPref);

		if(colorPref!=-1) {
			mTimeView.setTextColor(colorPref);
			mDateView.setTextColor(colorPref);
		}
		if(fontPref!=null) {
			mTimeView.setTypeface(SettingsActivity.getTypefaceFromName(fontPref));
			mDateView.setTypeface(SettingsActivity.getTypefaceFromName(fontPref));
		}

	}



	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}

	private final View.OnClickListener mButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent=new Intent(FullscreenActivity.this, SettingsActivity.class);
			startActivityForResult(intent, REQUEST_CHANGE_SETTINGS);
		}
	};


	@Override
	public void onStop() {
		super.onStop();
	}


	private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	private void toggle() {
		if (mVisible) {
			hide();
		} else {
			show();
		}
	}

	private void hide() {
		// Hide UI first
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.hide();
		}
		mControlsView.setVisibility(View.GONE);
		mVisible = false;

		// Schedule a runnable to remove the status and navigation bar after a delay
		mHideHandler.removeCallbacks(mShowPart2Runnable);
		mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
	}

	private final Runnable mHidePart2Runnable = new Runnable() {
		@SuppressLint("InlinedApi")
		@Override
		public void run() {
			// Delayed removal of status and navigation bar

			// Note that some of these constants are new as of API 16 (Jelly Bean)
			// and API 19 (KitKat). It is safe to use them, as they are inlined
			// at compile-time and do nothing on earlier devices.
			mTimeView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
					| View.SYSTEM_UI_FLAG_FULLSCREEN
					| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}
	};

	@SuppressLint("InlinedApi")
	private void show() {
		// Show the system bar
		mTimeView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
		mVisible = true;

		// Schedule a runnable to display UI elements after a delay
		mHideHandler.removeCallbacks(mHidePart2Runnable);
		mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
	}

	private final Runnable mShowPart2Runnable = new Runnable() {
		@Override
		public void run() {
			// Delayed display of UI elements
			ActionBar actionBar = getSupportActionBar();
			if (actionBar != null) {
				actionBar.show();
			}
			mControlsView.setVisibility(View.VISIBLE);
		}
	};

	private final Handler mHideHandler = new Handler();
	private final Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mHandler.removeCallbacks(mTickListener);
		mTimeView = null;
		/*Settings.System.putInt(this.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS_MODE,
				Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);*/
	}

}