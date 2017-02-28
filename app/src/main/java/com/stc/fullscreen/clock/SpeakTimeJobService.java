package com.stc.fullscreen.clock;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;

/**
 * Created by artem on 2/28/17.
 */

public class SpeakTimeJobService extends JobService {
	@Override
	public boolean onStartJob(JobParameters params) {

		return true;
	}

	@Override
	public boolean onStopJob(JobParameters params) {

		return false;
	}
}
