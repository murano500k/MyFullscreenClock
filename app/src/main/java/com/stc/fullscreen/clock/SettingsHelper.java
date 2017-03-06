package com.stc.fullscreen.clock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static com.stc.fullscreen.clock.AlarmReceiver.HOUR_DAYTIME_STARTS;
import static com.stc.fullscreen.clock.AlarmReceiver.HOUR_NIGHTTIME_STARTS;

/**
 * Created by artem on 2/28/17.
 */

public class SettingsHelper {

	private static final String TAG = "SettingsPresenter";
	private static final String MY_PREFS = "MY_PREFS";
	private static final String AUTO_BRIGHTNESS_VALUE = "AUTO_BRIGHTNESS_VALUE";



	public static void toggleAutoBrightnessChange(Context c,PendingIntent pi) {
		AlarmManager alarmManager = (AlarmManager) c.getSystemService(ALARM_SERVICE);
		String action;

		Calendar calendar = Calendar.getInstance();
		if(autoBrightnessEnabled(c)){
			alarmManager.cancel(pi);
			action="disabled";
			setAutoBrightness(c,false);
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
			setAutoBrightness(c,true);

		}
		Log.d(TAG, action);
	}


	private static void setAutoBrightness(Context c, boolean val){
		SharedPreferences preferences = c.getSharedPreferences(MY_PREFS, MODE_PRIVATE);
		preferences.edit().putBoolean(AUTO_BRIGHTNESS_VALUE, val).apply();
	}
	private static boolean autoBrightnessEnabled(Context c){
		SharedPreferences preferences = c.getSharedPreferences(MY_PREFS, MODE_PRIVATE);
		return preferences.getBoolean(AUTO_BRIGHTNESS_VALUE, false);
	}
}
