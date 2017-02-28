package com.stc.fullscreen.clock;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity implements ClockContract.View{
	private static final String TAG = "FullscreenActivity";
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
	private static final int REQUEST_CHANGE_BRIGHTNESS = 19932;

	private ScalableTextView mTextView;
	private ScalableTextView mDateView;
	private View mControlsView;
	private boolean mVisible;
	private static final int TICK_DELAY_MILLIS = 250;


	private final Handler mHandler = new Handler(Looper.getMainLooper());
	private final SimpleDateFormat mTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
	private final SimpleDateFormat mDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.US);

	private final Runnable mTickListener = new Runnable() {
		@Override
		public void run() {
			mTextView.setText(mTimeFormat.format(Calendar.getInstance().getTime()));
			mDateView.setText(mDateFormat.format(Calendar.getInstance().getTime()));
			mHandler.postDelayed(mTickListener, TICK_DELAY_MILLIS);
		}
	};
	private LinearLayout mDateTimeView;
	private ClockContract.Presenter presenter;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_fullscreen);

		mVisible = true;
		mControlsView = findViewById(R.id.fullscreen_content_controls);
		mTextView = (ScalableTextView) findViewById(R.id.fullscreen_content);
		mDateView = (ScalableTextView) findViewById(R.id.date_content);
		mDateTimeView=(LinearLayout) findViewById(R.id.date_time_layout);


		// Set up the user interaction to manually show or hide the system UI.
		mDateTimeView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				toggle();
			}
		});

		findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
		findViewById(R.id.dummy_button).setOnClickListener(mButtonClickListener);
		mTickListener.run();
		new ClockPresenter(this,AlarmReceiver.getPendingIntent(this));
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
			startActivity(intent);
		}
	};
	public void enableBrightnessChange() {
		if(!checkSystemWritePermission()) {
			openAndroidPermissionsMenu();
			return;
		}
		presenter.toggleAutoBrightnessChange(this);
	}
	private void toggleSpeakingClock(){
		presenter.toggleSpeakingClock(new ComponentName(this, SpeakTimeJobService.class));
	}



	private boolean checkSystemWritePermission() {
		boolean retVal = true;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			retVal = Settings.System.canWrite(this);
			Log.d(TAG, "Can Write Settings: " + retVal);
			if(retVal){
				Toast.makeText(this, "Write allowed :-)", Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(this, "Write not allowed :-(", Toast.LENGTH_LONG).show();
			}
		}else if (ContextCompat.checkSelfPermission(FullscreenActivity.this,
				Manifest.permission.WRITE_SETTINGS)
				!= PackageManager.PERMISSION_GRANTED) {
			return false;
		}
		return retVal;
	}
	private void openAndroidPermissionsMenu() {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
			intent.setData(Uri.parse("package:" + this.getPackageName()));
			startActivity(intent);
		}else {
			ActivityCompat.requestPermissions(FullscreenActivity.this,
					new String[]{Manifest.permission.WRITE_SETTINGS},
					REQUEST_CHANGE_BRIGHTNESS);
		}
	}
	@SuppressLint("NewApi")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (Settings.System.canWrite(this)){
			Log.d("TAG", "CODE_WRITE_SETTINGS_PERMISSION success");
			enableBrightnessChange();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (grantResults[0] == PERMISSION_GRANTED) {
			enableBrightnessChange();
			Log.d(TAG, "granted");
		}else {
			//enableBrightnessChange();
			Log.d(TAG, "not granted");
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);


	}

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
			mTextView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
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
		mTextView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
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
		mTextView = null;
		Settings.System.putInt(this.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS_MODE,
				Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
	}


	@Override
	public void setPresenter(ClockContract.Presenter p) {
		this.presenter=p;
	}

	@Override
	public void showMessage(String m) {
		Toast.makeText(this,m, Toast.LENGTH_SHORT).show();
	}
}