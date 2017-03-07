package com.stc.fullscreen.clock;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.Preference;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.rarepebble.colorpicker.ColorPreference;

import java.util.Calendar;
import java.util.Date;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.stc.fullscreen.clock.AlarmReceiver.ACTION_SET_BRIGHTNESS;
import static com.stc.fullscreen.clock.AlarmReceiver.HOUR_DAYTIME_STARTS;
import static com.stc.fullscreen.clock.AlarmReceiver.HOUR_NIGHTTIME_STARTS;
import static com.stc.fullscreen.clock.AlarmReceiver.REQUEST_SET_BRIGHTNESS;
import static com.stc.fullscreen.clock.FullscreenActivity.REQUEST_CHANGE_BRIGHTNESS;
import static com.stc.fullscreen.clock.FullscreenActivity.REQUEST_SPEAK;
import static com.stc.fullscreen.clock.SpeakingService.ACTION_SPEAK_TIME;

/**
 * Created by artem on 3/7/17.
 */

public class SettingsActivity extends AppCompatPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
	private static final String TAG = "SettingsActivity";
	public static final String KEY_PREF_AUTO_BR = "pref_autoBr";
	public static final String KEY_PREF_SPEAK_TIME = "pref_speakTime";
	public static final String KEY_PREF_FONT = "pref_font";
	public static final String KEY_PREF_COLOR = "pref_color";
	public static final String FONT_MONOSPACE = "FONT_MONOSPACE";
	public static final String FONT_SERIF = "FONT_SERIF";
	public static final String FONT_BOLD = "FONT_BOLD";
	public static final String FONT_DEFAULT = "FONT_DEFAULT";


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupActionBar();
		addPreferencesFromResource(R.xml.pref_general);
	}
	private void setupActionBar() {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			// Show the Up button in the action bar.
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			startActivity(new Intent(this, FullscreenActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Preference pref = findPreference(key);

		if (key.equals(KEY_PREF_AUTO_BR)) {
			pref.setSummary(sharedPreferences.getBoolean(key, false) ? "enabled" : "disabled");
			toggleAutoBrightnessChange(sharedPreferences.getBoolean(key,false));
		}else if (key.equals(KEY_PREF_SPEAK_TIME)) {
			pref.setSummary(sharedPreferences.getBoolean(key, false) ? "enabled" : "disabled");
			toggleSpeakingTime(sharedPreferences.getBoolean(key,false));
		}else if(key.equals(KEY_PREF_COLOR)) {
			ColorPreference colorPreference = (ColorPreference) pref;
			colorPreference.setColor(sharedPreferences.getInt(key, 0xFF));
		}else if(key.equals(KEY_PREF_FONT)) {
			pref.setSummary(sharedPreferences.getString(key, ""));
		}
	}

	public static Typeface getTypefaceFromName(String name){
		if(TextUtils.equals(FONT_SERIF,name))return Typeface.SERIF;
		if(TextUtils.equals(FONT_MONOSPACE,name))return Typeface.MONOSPACE;
		if(TextUtils.equals(FONT_BOLD,name))return Typeface.DEFAULT_BOLD;
		return Typeface.DEFAULT;
	}
	public static String getNameFromTypeface(Typeface typeface){
		if(typeface==Typeface.SERIF)return FONT_SERIF;
		if(typeface==Typeface.MONOSPACE)return FONT_MONOSPACE;
		if(typeface==Typeface.DEFAULT_BOLD)return FONT_BOLD;
		return FONT_DEFAULT;
	}
	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}
	public boolean toggleAutoBrightnessChange(boolean oldVal) {
		if(!checkSystemWritePermission()) {
			openAndroidPermissionsMenu();
			return false;
		}
		String action;
		boolean newVal=false;
		Intent intent= new Intent(this, AlarmReceiver.class);
		intent.setAction(ACTION_SET_BRIGHTNESS);
		PendingIntent pi = PendingIntent.getBroadcast(this,REQUEST_SET_BRIGHTNESS, intent, 0);
		AlarmManager alarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
		Calendar calendar = Calendar.getInstance();
		if(oldVal){
			alarmManager.cancel(pi);
			action="disabled";
			newVal=false;
		}else {
			action = "enabled";
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.HOUR_OF_DAY, HOUR_DAYTIME_STARTS);
			alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
					AlarmManager.INTERVAL_DAY, pi);

			calendar.set(Calendar.HOUR_OF_DAY, HOUR_NIGHTTIME_STARTS);
			alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
					AlarmManager.INTERVAL_DAY, pi);
			alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
					SystemClock.elapsedRealtime() +
							1000, pi);
			newVal=true;
		}
		Log.d(TAG, action);
		return newVal;
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
		}else if (ContextCompat.checkSelfPermission(SettingsActivity.this,
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
			ActivityCompat.requestPermissions(SettingsActivity.this,
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
			toggleAutoBrightnessChange(findPreference(KEY_PREF_AUTO_BR).isEnabled());
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (grantResults[0] == PERMISSION_GRANTED) {
			toggleAutoBrightnessChange(findPreference(KEY_PREF_AUTO_BR).isEnabled());
			Log.d(TAG, "granted");
		}else {
			//enableBrightnessChange();
			Log.d(TAG, "not granted");
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);


	}

	public boolean toggleSpeakingTime(boolean oldVal){
		Intent intent=new Intent(this, SpeakingService.class);
		String action;
		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		PendingIntent pendingIntent= PendingIntent.getService(this, REQUEST_SPEAK, intent, 0);

		if(oldVal){
			alarmManager.cancel(pendingIntent);
			action="disabled";
		}else {
			action = "enabled";
			intent.setAction(ACTION_SPEAK_TIME);
			startService(intent);
			AlarmManager am=(AlarmManager) getSystemService(ALARM_SERVICE);
			Calendar calendar=Calendar.getInstance();
			Date date=calendar.getTime();
			date.setMinutes(0);
			int period = 1000*60*60;
			am.setRepeating(
					AlarmManager.ELAPSED_REALTIME_WAKEUP,
					date.getTime(),
					period,
					pendingIntent
			);
		}
		Log.d(TAG, "scheduleSpeakingTime: "+action);
		return !oldVal;

	}
}
