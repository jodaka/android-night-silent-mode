package com.kudris.silentmode;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.util.Log;

import java.util.Calendar;

public class NightModeService extends IntentService {

    public static final String APP_ID = "NightMode";
    public static final String SETTINGS_START_HOUR = "start_hour";
    public static final String SETTINGS_START_MINUTE = "start_minute";
    public static final String SETTINGS_END_HOUR = "end_hour";
    public static final String SETTINGS_END_MINUTE = "end_minute";
    private static final String SETTINGS_SILENT_MODE_IS_ACTIVE = "silent_is_active";
    private static final String SETTINGS_AUDIO_MODE = "audio_mode";
    private static final String SETTINGS_AUDIO_VOLUME = "audio_volume";

    private AudioManager am;

    public NightModeService() {
        super("NighModeService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("LOL", " intent received");
        checkTime();
    }

    private void checkTime() {
        SharedPreferences settings = getSharedPreferences(APP_ID, 0);

        int startHour = settings.getInt(SETTINGS_START_HOUR, -1);
        int startMinute = settings.getInt(SETTINGS_START_MINUTE, 0);
        int endHour = settings.getInt(SETTINGS_END_HOUR, -1);
        int endMinute = settings.getInt(SETTINGS_END_MINUTE, 0);

        if (startHour == -1 || endHour == -1 ) {
            Log.d("LOL", " settings not found");
            return;
        }

        Calendar currentTime = Calendar.getInstance();
        currentTime.setTimeInMillis(System.currentTimeMillis());

        Calendar startTime = Calendar.getInstance();
        startTime.setTimeInMillis(System.currentTimeMillis());
        startTime.set(Calendar.HOUR_OF_DAY, startHour);
        startTime.set(Calendar.MINUTE, startMinute);

        Calendar endTime = Calendar.getInstance();
        endTime.setTimeInMillis(System.currentTimeMillis());
        endTime.set(Calendar.HOUR_OF_DAY, endHour);
        endTime.set(Calendar.MINUTE, endMinute);


        Log.d("LOL", " ~~ saved settings:  " + startTime.getTime().toString() + " -- " + endTime.getTime().toString() + " ----- current: " + currentTime.getTime().toString());

        if (currentTime.after(startTime) && currentTime.before(endTime)) {
            setSilentMode();
        } else {
            restoreNormalMode();
        }
    }

    private void setSilentMode() {
        Log.d("LOL", " >>>>>> setSilentMode called");
        SharedPreferences settings = getSharedPreferences(APP_ID, 0);

        if (settings.getBoolean(SETTINGS_SILENT_MODE_IS_ACTIVE, false)) {
            Log.d("LOL", "silent is already active");
            return;
        } else {
            Log.d("LOL", "activating silent mode");

            am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

            int currentMode = am.getRingerMode();
            int currentVolume = am.getStreamVolume(AudioManager.STREAM_SYSTEM);

            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(SETTINGS_AUDIO_MODE, currentMode);
            editor.putInt(SETTINGS_AUDIO_VOLUME, currentVolume);
            editor.putBoolean(SETTINGS_SILENT_MODE_IS_ACTIVE, true);
            editor.commit();

            am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        }
    }

    private void restoreNormalMode() {
        Log.d("LOL", " << restoreNormalMode called");

        SharedPreferences settings = getSharedPreferences(APP_ID, 0);
        if (settings.getBoolean(SETTINGS_SILENT_MODE_IS_ACTIVE, false)) {

            int newMode = settings.getInt(SETTINGS_AUDIO_MODE, -1);
            int newVolume = settings.getInt(SETTINGS_AUDIO_VOLUME, -1);

            if (newMode != -1 && newVolume != -1) {

                Log.d("LOL", "restoring volume");

                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(SETTINGS_SILENT_MODE_IS_ACTIVE, false);
                editor.commit();

                am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
                am.setRingerMode(newMode);
                am.setStreamVolume(AudioManager.STREAM_SYSTEM, newVolume, 0);
            }
        }
    }
}
