package com.stc.fullscreen.clock.ui;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.stc.fullscreen.clock.R;
import com.stc.fullscreen.clock.schedule.AlarmReceiver;
import com.stc.fullscreen.clock.utils.ScaleableTextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static com.stc.fullscreen.clock.schedule.AlarmReceiver.ACTION_SET_BRIGHTNESS;
import static com.stc.fullscreen.clock.schedule.AlarmReceiver.HOUR_DAYTIME_STARTS;
import static com.stc.fullscreen.clock.schedule.AlarmReceiver.HOUR_NIGHTTIME_STARTS;
import static com.stc.fullscreen.clock.schedule.AlarmReceiver.REQUEST_SET_BRIGHTNESS;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
	public static final String TAG = "FullscreenActivity";
	private static final boolean AUTO_HIDE = true;
	private static final int AUTO_HIDE_DELAY_MILLIS = 2000;
	private static final int UI_ANIMATION_DELAY = 300;
	public static final int REQUEST_CHANGE_BRIGHTNESS = 19932;
	public static final int REQUEST_SPEAK = 74;
	public static final int  REQUEST_CHANGE_SETTINGS = 43265;

	public ScaleableTextView mTimeView;
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
			updateTextViewWithFont(mTimeFormat.format(Calendar.getInstance().getTime()) ,mTimeView);
			updateTextViewWithFont(mDateFormat.format(Calendar.getInstance().getTime()) ,mDateView);
			mHandler.postDelayed(mTickListener, TICK_DELAY_MILLIS);
		}
	};

	private void updateTextViewWithFont(String text, TextView tv){
		SpannableStringBuilder sBuilder = new SpannableStringBuilder();
		sBuilder.append(text); // Default TextView font.
		String font = SettingsActivity.getSelectedFontFilePath(this);
		Typeface typeface=Typeface.createFromAsset(FullscreenActivity.this.getAssets(), font );
		tv.setTypeface(typeface);
		tv.setText(sBuilder, TextView.BufferType.SPANNABLE);
	}

	private View mControlsView;
	private View mAllView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fullscreen);
		mVisible = true;
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
		mAllView.setOnTouchListener(mDelayHideTouchListener);

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
		checkEnableAutoBrightness();
	}



	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
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
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.hide();
		}
		mControlsView.setVisibility(View.GONE);
		mVisible = false;

		mHideHandler.removeCallbacks(mShowPart2Runnable);
		mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
	}

	private final Runnable mHidePart2Runnable = new Runnable() {
		@SuppressLint("InlinedApi")
		@Override
		public void run() {
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
		restoreUserBrSettings();
		cancelAutoBrUpdate();
	}
	private void checkEnableAutoBrightness(){
		if(SettingsActivity.isAutoBrActive(this)){
			saveUserBrSettings();
			scheduleAutoBrUpdate();
		}else {
			restoreUserBrSettings();
			cancelAutoBrUpdate();
		}
		Log.d(TAG, "auto br active = "+SettingsActivity.isAutoBrActive(this));
	}
	private void saveUserBrSettings(){
		try {
			int val = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
			int mode = Settings.System.getInt(getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS_MODE);
			Log.d(TAG, "saveUserBrSettings: val="+val);
			Log.d(TAG, "saveUserBrSettings: mode="+mode);
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
			preferences.edit()
					.putInt(SettingsActivity.KEY_PREF_USER_BR_VAL, val)
					.putInt(SettingsActivity.KEY_PREF_USER_BR_MODE, mode)
					.apply();
		} catch (Settings.SettingNotFoundException e) {
			Log.e(TAG, "saveUserBrValues: ",e );
			e.printStackTrace();
		}
	}
	private void restoreUserBrSettings(){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		int val = preferences.getInt(SettingsActivity.KEY_PREF_USER_BR_VAL, -1);
		int mode = preferences.getInt(SettingsActivity.KEY_PREF_USER_BR_MODE, -1);
		Log.d(TAG, "restoreUserBrSettings: val="+val);
		Log.d(TAG, "restoreUserBrSettings: mode="+mode);
		if(val!=-1) Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, val);
		if(mode!=-1) Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, mode);
	}


	private void scheduleAutoBrUpdate(){
		Calendar calendar = Calendar.getInstance();
		AlarmManager alarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);

		//at day
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, HOUR_DAYTIME_STARTS);
		alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
				AlarmManager.INTERVAL_DAY, getAutoBrPi());

		//at night
		calendar.set(Calendar.HOUR_OF_DAY, HOUR_NIGHTTIME_STARTS);
		alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
				AlarmManager.INTERVAL_DAY, getAutoBrPi());

		//now
		alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime(), getAutoBrPi());
	}

	private void cancelAutoBrUpdate(){
		AlarmManager alarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
		alarmManager.cancel(getAutoBrPi());
	}

		private PendingIntent getAutoBrPi(){
		Intent intent= new Intent(this, AlarmReceiver.class);
		intent.setAction(ACTION_SET_BRIGHTNESS);
		PendingIntent pi = PendingIntent.getBroadcast(this,REQUEST_SET_BRIGHTNESS, intent, 0);
		return pi;
	}

}