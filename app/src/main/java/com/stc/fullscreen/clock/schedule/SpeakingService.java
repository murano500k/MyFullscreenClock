package com.stc.fullscreen.clock.schedule;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

import static android.speech.tts.TextToSpeech.QUEUE_FLUSH;
import static android.speech.tts.TextToSpeech.SUCCESS;

public class SpeakingService extends IntentService implements TextToSpeech.OnInitListener {
	TextToSpeech tts;
	private static final String TAG = "SpeakingService";

	public SpeakingService() {
		super(TAG);

	}
	public static PendingIntent getPendingIntent(Context context) {
		Intent action = new Intent(context, SpeakingService.class);
		return PendingIntent.getService(context, 0, action, PendingIntent.FLAG_UPDATE_CURRENT);
	}
	@Override
	protected void onHandleIntent(Intent intent) {
		tts  = new TextToSpeech(getApplicationContext(),
				this
		);
	}

	@Override
	public void onInit(int status) {
		Log.d(TAG, "onInit: "+status);
		if(status==SUCCESS){
			java.util.Calendar calendar = java.util.Calendar.getInstance();
			int hours = calendar.get(java.util.Calendar.HOUR_OF_DAY);
			int mins = calendar.get(Calendar.MINUTE);

			String partOfTheDay;

			if(hours > 0 && hours<7)        partOfTheDay="at night";
			else if(hours>=7 && hours<12)   partOfTheDay="in the morning";
			else if(hours>=12 && hours<18)  partOfTheDay="in the afternoon";
			else                            partOfTheDay="int the evening";

			String lineToSpeak= "It's "+hours;
			if(mins !=0 ) lineToSpeak+=" hours, "+mins+" minutes";
			lineToSpeak+=" o'clock "+partOfTheDay;
			Log.d(TAG, "onInit: "+lineToSpeak);
			Toast.makeText(this, lineToSpeak, Toast.LENGTH_SHORT).show();
			tts.speak(lineToSpeak,  QUEUE_FLUSH, null);
		}
	}
}
