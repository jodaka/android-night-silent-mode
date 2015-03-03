package com.kudris.silentmode;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;


public class MainActivity extends Activity {

    private TimePicker startTime;
    private TimePicker endTime;
    NightModeBroadcastReceiver alarm = new NightModeBroadcastReceiver();
    private int mNumberPickerInputId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        startTime = (TimePicker) findViewById(R.id.start_time);
        endTime = (TimePicker) findViewById(R.id.end_time);

        startTime.setIs24HourView(true);
        endTime.setIs24HourView(true);

        SharedPreferences settings = getSharedPreferences(NightModeService.APP_ID, 0);
        if (settings.getInt(NightModeService.SETTINGS_START_HOUR, -1) != -1) {
            startTime.setCurrentHour(settings.getInt(NightModeService.SETTINGS_START_HOUR, -1));
            startTime.setCurrentMinute(settings.getInt(NightModeService.SETTINGS_START_MINUTE, 0));
            endTime.setCurrentHour(settings.getInt(NightModeService.SETTINGS_END_HOUR, -1));
            endTime.setCurrentMinute(settings.getInt(NightModeService.SETTINGS_END_MINUTE, 0));
        }

        Button save = (Button) findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });
    }

    private void save() {
        Log.d("LOL", "value saved");

        SharedPreferences settings = getSharedPreferences(NightModeService.APP_ID, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(NightModeService.SETTINGS_START_HOUR, startTime.getCurrentHour());
        editor.putInt(NightModeService.SETTINGS_START_MINUTE, startTime.getCurrentMinute());
        editor.putInt(NightModeService.SETTINGS_END_HOUR, endTime.getCurrentHour());
        editor.putInt(NightModeService.SETTINGS_END_MINUTE, endTime.getCurrentMinute());
        editor.commit();

        alarm.scheduleAlarm(getApplicationContext());
    }
}
