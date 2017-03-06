package com.stc.fullscreen.clock;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by artem on 2/28/17.
 */
public class AlarmReceiver extends BroadcastReceiver {
	public static final String TAG = "AlarmReceiver";

	public static final String ACTION_SET_BRIGHTNESS = "com.stc.fullscreenclock.ACTION_SET_BRIGHTNESS";
	public static final int REQUEST_SET_BRIGHTNESS = 5641;
	public static final int HOUR_DAYTIME_STARTS = 8;
	public static final int HOUR_NIGHTTIME_STARTS = 23;

	public AlarmReceiver() {

	}
	public static Intent getIntent(Context c) {
		Intent intent = new Intent(c, AlarmReceiver.class);
		intent.setAction(ACTION_SET_BRIGHTNESS);
		return intent;
	}

	public static PendingIntent getPendingIntent(Context c) {
		return PendingIntent.getBroadcast(c, 0, getIntent(c), PendingIntent.FLAG_ONE_SHOT);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive: something recieved");
		if (ACTION_SET_BRIGHTNESS.equals(intent.getAction())) {
			changeBrightness(context);
		}

	}

	public void changeBrightness(Context context) {
		int val = -1;
		try {
			Settings.System.putInt(context.getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS_MODE,
					Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
			val = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
			Log.d(TAG, "changeBrightness: currentval " + val);
			//if (val < 253) val = 254;
			//else val = 1;
			val=isDaytime() ? 254 : 1;
			Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, val);
			Toast.makeText(context, "new brigthness: "+val, Toast.LENGTH_SHORT).show();
			Log.d(TAG, "changeBrightness: new val=" + val);
		} catch (Settings.SettingNotFoundException e) {
			Log.e(TAG, "changeBrightness: ", e);
			e.printStackTrace();
		}
	}
	public boolean isDaytime(){
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		if(hour>=HOUR_NIGHTTIME_STARTS && hour<HOUR_DAYTIME_STARTS) return false;
		else return true;
	}

}
