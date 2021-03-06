package com.stc.fullscreen.clock.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;

import com.rarepebble.colorpicker.ColorPreference;
import com.stc.fullscreen.clock.R;
import com.stc.fullscreen.clock.schedule.SpeakingService;
import com.stc.fullscreen.clock.utils.AppCompatPreferenceActivity;

import java.util.Calendar;
import java.util.Date;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;


/**
 * Created by artem on 3/7/1
 */

public class SettingsActivity extends AppCompatPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
	private static final String TAG = "SettingsActivity";
	public static final String KEY_PREF_USER_BR_VAL = "pref_userBrVal";
	public static final String KEY_PREF_USER_BR_MODE = "pref_userBrMode";
	public static final String KEY_PREF_AUTO_BR = "pref_autoBr";
	public static final String KEY_PREF_SPEAK_TIME = "pref_speakTime";
	public static final String KEY_PREF_FONT = "pref_font";
	public static final String KEY_PREF_COLOR = "pref_color";
	public static final String FONT_MONOSPACE = "FONT_MONOSPACE";
	public static final String FONT_SERIF = "FONT_SERIF";
	public static final String FONT_BOLD = "FONT_BOLD";
	public static final String FONT_DEFAULT = "Roboto-Medium";
	public static final int REQUEST_MANAGE_SYSTEM_SETTINGS = 367;
	public static final String KEY_PREF_SHOW_MANUAL = "pref_showManual";


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
			toggleAutoBrightnessChange();
		}else if (key.equals(KEY_PREF_SPEAK_TIME)) {
			//setActiveSpeakingTime();
		}else if(key.equals(KEY_PREF_COLOR)) {
			ColorPreference colorPreference = (ColorPreference) pref;
			colorPreference.setColor(sharedPreferences.getInt(key, 0xFF));
		}else if(key.equals(KEY_PREF_FONT)) {
			pref.setSummary(sharedPreferences.getString(key, ""));
		}
	}
	public void setActiveSpeakingTime(){
		String action;
		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		PendingIntent pendingIntent= SpeakingService.getPendingIntent(this);
		if(getPreferenceScreen().getSharedPreferences().getBoolean(KEY_PREF_SPEAK_TIME,false)){
			action = "enabled";
			try {
				pendingIntent.send();
			} catch (PendingIntent.CanceledException e) {
				e.printStackTrace();
			}
			AlarmManager am=(AlarmManager) getSystemService(ALARM_SERVICE);
			Calendar calendar=Calendar.getInstance();
			Date date=calendar.getTime();
			date.setMinutes(0);
			int h=date.getHours()+1;
			if(h>23) h=0;
			date.setHours(h);
			am.setRepeating(
					AlarmManager.RTC_WAKEUP,
					date.getTime(),
					AlarmManager.INTERVAL_HOUR,
					pendingIntent
			);
		}else {
			alarmManager.cancel(pendingIntent);
			action="disabled";
		}
		Log.d(TAG, "scheduleSpeakingTime: "+action);
	}
	public static boolean isAutoBrActive(Context c){
		return getDefaultSharedPreferences(c).getBoolean(KEY_PREF_AUTO_BR,false);
	}
	public static String getSelectedFontFilePath(Context c){
		String fontName = PreferenceManager.getDefaultSharedPreferences(c).getString(KEY_PREF_FONT,FONT_DEFAULT);
		return getSelectedFontFilePath(fontName);
	}
	public static String getSelectedFontFilePath(String fontName){
		if(fontName==null) return null;
		return ""+fontName+".ttf";
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
	public void toggleAutoBrightnessChange() {
		if(!checkSystemWritePermission()) {
			openAndroidPermissionsMenu();
			return;
		}
		/*findPreference(KEY_PREF_AUTO_BR)
				.setSummary(
						getPreferenceScreen().getSharedPreferences()
								.getBoolean(KEY_PREF_AUTO_BR, false) ?
								"enabled" : "disabled");*/
	}

	private void disableAutoBrNoPermission(){
		getPreferenceScreen().getSharedPreferences().edit()
				.putBoolean(KEY_PREF_AUTO_BR, false).apply();
	}

	private boolean checkSystemWritePermission() {
		boolean retVal = true;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			retVal = Settings.System.canWrite(this);
			Log.d(TAG, "Can Write Settings: " + retVal);
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
			startActivityForResult(intent, REQUEST_MANAGE_SYSTEM_SETTINGS);
		}else {
			ActivityCompat.requestPermissions(SettingsActivity.this,
					new String[]{Manifest.permission.WRITE_SETTINGS},
					REQUEST_MANAGE_SYSTEM_SETTINGS);
		}
	}
	@SuppressLint("NewApi")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (Settings.System.canWrite(this)){
			Log.e(TAG, "onActivityResult: granted" );
			toggleAutoBrightnessChange();
		}else {
			Log.e(TAG, "onActivityResult: NOT granted" );
			disableAutoBrNoPermission();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (grantResults[0] == PERMISSION_GRANTED) {
			toggleAutoBrightnessChange();
			Log.e(TAG, "onRequestPermissionsResult: granted" );
		}else {
			Log.e(TAG, "onRequestPermissionsResult: NOT granted" );
			disableAutoBrNoPermission();
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	public static boolean shouldShowManual(Context c){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		boolean res = prefs.getBoolean(KEY_PREF_SHOW_MANUAL, true);
		if(res) prefs.edit().putBoolean(KEY_PREF_SHOW_MANUAL, false).apply();
		return res;
	}

}
