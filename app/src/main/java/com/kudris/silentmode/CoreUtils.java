package com.kudris.silentmode;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;

public enum CoreUtils {
    INSTANCE;

    public static final String APP_ID = "NightMode";
    public static final String SETTINGS_START_HOUR = "start_hour";
    public static final String SETTINGS_START_MINUTE = "start_minute";
    public static final String SETTINGS_END_HOUR = "end_hour";
    public static final String SETTINGS_END_MINUTE = "end_minute";
    public static final String SETTINGS_SILENT_MODE_IS_ACTIVE = "silent_is_active";
    public static final String SETTINGS_AUDIO_MODE = "audio_mode";
    public static final String SETTINGS_AUDIO_VOLUME = "audio_volume";

    private Context cntx;

    CoreUtils() {}

    public void init(Context context) {
        this.cntx = context;
    }

    public boolean isNightTime() {
        SharedPreferences settings = cntx.getSharedPreferences(APP_ID, 0);

        int startHour = settings.getInt(SETTINGS_START_HOUR, -1);
        int startMinute = settings.getInt(SETTINGS_START_MINUTE, 0);
        int endHour = settings.getInt(SETTINGS_END_HOUR, -1);
        int endMinute = settings.getInt(SETTINGS_END_MINUTE, 0);

        if (startHour == -1 || endHour == -1) {
            Log.d("LOL", " settings not found");
            return false;
        }

        Calendar currentTime = Calendar.getInstance();
        currentTime.setTimeInMillis(System.currentTimeMillis());

        Calendar startTime = Calendar.getInstance();
        startTime.setTimeInMillis(System.currentTimeMillis());
        startTime.set(Calendar.HOUR_OF_DAY, startHour);
        startTime.set(Calendar.MINUTE, startMinute);
        startTime.set(Calendar.SECOND, 0);

        Calendar endTime = Calendar.getInstance();
        endTime.setTimeInMillis(System.currentTimeMillis());
        endTime.set(Calendar.HOUR_OF_DAY, endHour);
        endTime.set(Calendar.MINUTE, endMinute);
        endTime.set(Calendar.SECOND, 0);
        if (startHour > endHour) {
            endTime.add(Calendar.DATE, 1);
        }

        Log.d("LOL", " ~~ saved settings:  " + startTime.getTime().toString() + " -- " + endTime.getTime().toString() + " ----- current: " + currentTime.getTime().toString());

        return (currentTime.after(startTime) && currentTime.before(endTime));
    }


}
