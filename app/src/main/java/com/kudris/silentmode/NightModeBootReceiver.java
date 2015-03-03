package com.kudris.silentmode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NightModeBootReceiver extends BroadcastReceiver {

    NightModeBroadcastReceiver alarm = new NightModeBroadcastReceiver();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            alarm.scheduleAlarm(context);
        }
    }
}
