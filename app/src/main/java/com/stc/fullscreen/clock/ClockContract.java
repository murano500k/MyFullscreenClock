package com.stc.fullscreen.clock;

import android.content.ComponentName;
import android.content.Context;

/**
 * Created by artem on 2/28/17.
 */

public interface ClockContract {

	interface Presenter{

		void toggleAutoBrightnessChange(Context c);
		void toggleSpeakingClock(ComponentName componentName);

	}

	interface View{
		void setPresenter(Presenter p);
		void showMessage(String m);
	}
}
