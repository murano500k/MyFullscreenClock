package com.stc.fullscreen.clock.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.stc.fullscreen.clock.R;

/**
 * Created by artem on 10/19/17.
 */

public class AlertSoundReceiver extends BroadcastReceiver{
    public static final String ACTION_PLAY_SOUND = "com.stc.fullscreenclock.PLAY_SOUND";
    private static final String TAG = "AlertSoundReceiver";

    SoundPool soundPool;
    AudioAttributes audioAttributes;
    AudioManager am;
    private float volume;
    private int soundIdClock;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceive(Context context, Intent intent) {
        if (audioAttributes == null) {
            audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
        }
        if (soundPool == null) {
            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setMaxStreams(10)
                    .build();
            soundIdClock = soundPool.load(context, R.raw.nimbyc_nimbyc_clock,0);
        }
        if (am == null) {
            am=(AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            volume = am.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        }
        soundPool.play(soundIdClock, volume,volume, 0, 0, 1.0f);
        Log.d(TAG, "onReceive: Play sound");
    }
}
