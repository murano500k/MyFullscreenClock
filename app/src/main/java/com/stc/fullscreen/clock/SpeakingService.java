package com.stc.fullscreen.clock;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;

import static android.speech.tts.TextToSpeech.QUEUE_FLUSH;
import static android.speech.tts.TextToSpeech.SUCCESS;
import static com.google.android.gms.internal.zzs.TAG;

public class SpeakingService extends Service implements TextToSpeech.OnInitListener {
	public static final String ACTION_SPEAK_TIME = "com.stc.fullscreenclock.ACTION_SPEAK_TIME";
	TextToSpeech tts;
	public SpeakingService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent.getAction()!=null && TextUtils.equals(intent.getAction(), ACTION_SPEAK_TIME)){
			this.speakTime(this);
		}
		return super.onStartCommand(intent, flags, startId);
	}
	private void speakTime(Context context){
		tts  = new TextToSpeech(context,
				this
		);
	}

	@Override
	public void onInit(int status) {
		Log.d(TAG, "onInit: "+status);
		if(status==SUCCESS){
			java.util.Calendar calendar = java.util.Calendar.getInstance();
			int hours = calendar.get(java.util.Calendar.HOUR_OF_DAY);
			String lineToSpeak= "It's "+hours+" o'clock";
			Log.d(TAG, "onInit: "+lineToSpeak);
			tts.speak(lineToSpeak,  QUEUE_FLUSH, null);
		}
	}
}
