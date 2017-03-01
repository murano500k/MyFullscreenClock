package com.stc.fullscreen.clock;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.Calendar;
import java.util.Locale;

import static android.content.ContentValues.TAG;

/**
 * Created by artem on 2/28/17.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SpeakTimeJobService extends JobService implements TextToSpeech.OnInitListener {
	private static final String TAG = "SpeakTimeJobService";
	boolean jobFinished=false;
	TextToSpeech tts;
	JobParameters params;
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "Service created");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "Service destroyed");
	}
	@Override
	public boolean onStartJob(JobParameters params) {
		tts=new TextToSpeech(this, this );
		this.params=params;


		return true;
	}

	@Override
	public boolean onStopJob(JobParameters params) {
		if(tts != null){
			tts.stop();
			tts.shutdown();
			return true;
		}
		return false;
	}

	@Override
	public void onInit(int status) {
		if(status!=0){
			SpeakTimeJobService.this.jobFinished(params, true);
		}else {
			SpeakTimeJobService.this.jobFinished(params, speakTime()!=0);
		}
	}
	private int speakTime(){
		tts.setLanguage(Locale.US);
		return tts.speak("Text to say aloud", TextToSpeech.QUEUE_ADD, null);
	}
	private String getStringToSpeak(int hours){
		return "It's "+hours+" o'clock";
	}

	private int getHours(){
		return  Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
	}
}

