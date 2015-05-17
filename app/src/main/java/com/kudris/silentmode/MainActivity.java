package com.kudris.silentmode;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import de.greenrobot.event.EventBus;


public class MainActivity extends Activity {

    private TimePicker startTime;
    private TimePicker endTime;
    private TextView status;
    NightModeBroadcastReceiver alarm = new NightModeBroadcastReceiver();
    private Button save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CoreUtils.INSTANCE.init(getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        updateStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    private void updateStatus(boolean isActive) {
        status.setText(isActive
                ? getString(R.string.night_time_active)
                : getString(R.string.night_time_inactive));

        status.setTextColor(getResources().getColor(isActive ? R.color.status_active : R.color.status_inactive)

        );
    }

    private void updateStatus() {
        updateStatus(CoreUtils.INSTANCE.isNightTime());
    }

    public void onEventMainThread(NightModeService.NodeModeStateChangedEvent event) {
        Log.d("LOL", "got state changed event");
        updateStatus(event.isNightModeActive);
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        startTime = (TimePicker) findViewById(R.id.start_time);
        endTime = (TimePicker) findViewById(R.id.end_time);
        status = (TextView) findViewById(R.id.current_status);

        startTime.setIs24HourView(true);
        endTime.setIs24HourView(true);

        SharedPreferences settings = getSharedPreferences(CoreUtils.APP_ID, 0);
        if (settings.getInt(CoreUtils.SETTINGS_START_HOUR, -1) != -1) {
            startTime.setCurrentHour(settings.getInt(CoreUtils.SETTINGS_START_HOUR, -1));
            startTime.setCurrentMinute(settings.getInt(CoreUtils.SETTINGS_START_MINUTE, 0));
            endTime.setCurrentHour(settings.getInt(CoreUtils.SETTINGS_END_HOUR, -1));
            endTime.setCurrentMinute(settings.getInt(CoreUtils.SETTINGS_END_MINUTE, 0));
        }

        save = (Button) findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });

        startTime.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                showSaveButton();
            }
        });

        endTime.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                showSaveButton();
            }
        });
    }

    private void showSaveButton() {
        save.setVisibility(View.VISIBLE);
    }

    private void hideSaveButton() {
        save.setVisibility(View.INVISIBLE);
    }

    private void save() {
        Log.d("LOL", "value saved");

        SharedPreferences settings = getSharedPreferences(CoreUtils.APP_ID, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(CoreUtils.SETTINGS_START_HOUR, startTime.getCurrentHour());
        editor.putInt(CoreUtils.SETTINGS_START_MINUTE, startTime.getCurrentMinute());
        editor.putInt(CoreUtils.SETTINGS_END_HOUR, endTime.getCurrentHour());
        editor.putInt(CoreUtils.SETTINGS_END_MINUTE, endTime.getCurrentMinute());
        editor.commit();

        alarm.scheduleAlarm(getApplicationContext());
        hideSaveButton();
    }
}
