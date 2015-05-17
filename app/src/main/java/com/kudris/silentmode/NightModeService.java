package com.kudris.silentmode;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import de.greenrobot.event.EventBus;

public class NightModeService extends IntentService {

    public class NodeModeStateChangedEvent {
        public boolean isNightModeActive = false;
        public NodeModeStateChangedEvent(boolean isActive) {
            isNightModeActive = isActive;
        }
    }

    private AudioManager am;
    private int mId = 12345678;

    public NightModeService() {
        super("NighModeService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("LOL", " intent received");
        Context cntx = getApplicationContext();
        if (cntx != null) {
            CoreUtils.INSTANCE.init(cntx);
        }

        if (CoreUtils.INSTANCE.isNightTime()) {
            setSilentMode();
        } else {
            restoreNormalMode();
        }

        if (cntx != null) {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            PowerManager.WakeLock mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, cntx.getPackageName());
            if (mWakeLock != null && mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }

        stopSelf();
    }

    private void setNotifications(boolean show) {

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (show) {

            Log.d("LOL", "======= Notification created");

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.silent_icon)
                            .setContentTitle("Silent mode is active");

            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(this, MainActivity.class);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            mBuilder.setContentIntent(resultPendingIntent);

            mNotificationManager.notify(mId, mBuilder.build());

        } else {
            mNotificationManager.cancelAll();
        }
    }


    private void setSilentMode() {
        Log.d("LOL", " >>>>>> setSilentMode called");
        SharedPreferences settings = getSharedPreferences(CoreUtils.APP_ID, 0);

        if (settings.getBoolean(CoreUtils.SETTINGS_SILENT_MODE_IS_ACTIVE, false)) {
            Log.d("LOL", "silent is already active");
            return;
        } else {
            Log.d("LOL", "activating silent mode");
            setNotifications(true);

            am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

            int currentMode = am.getRingerMode();
            int currentVolume = am.getStreamVolume(AudioManager.STREAM_SYSTEM);

            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(CoreUtils.SETTINGS_AUDIO_MODE, currentMode);
            editor.putInt(CoreUtils.SETTINGS_AUDIO_VOLUME, currentVolume);
            editor.putBoolean(CoreUtils.SETTINGS_SILENT_MODE_IS_ACTIVE, true);
            editor.commit();

            am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            EventBus.getDefault().post(new NodeModeStateChangedEvent(true));
        }
    }

    private void restoreNormalMode() {
        Log.d("LOL", " << restoreNormalMode called");

        SharedPreferences settings = getSharedPreferences(CoreUtils.APP_ID, 0);
        if (settings.getBoolean(CoreUtils.SETTINGS_SILENT_MODE_IS_ACTIVE, false)) {

            int newMode = settings.getInt(CoreUtils.SETTINGS_AUDIO_MODE, -1);
            int newVolume = settings.getInt(CoreUtils.SETTINGS_AUDIO_VOLUME, -1);

            if (newMode != -1 && newVolume != -1) {

                setNotifications(false);
                Log.d("LOL", "restoring volume");

                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(CoreUtils.SETTINGS_SILENT_MODE_IS_ACTIVE, false);
                editor.commit();

                am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
                am.setRingerMode(newMode);
                am.setStreamVolume(AudioManager.STREAM_SYSTEM, newVolume, 0);
                EventBus.getDefault().post(new NodeModeStateChangedEvent(false));
            }
        }
    }
}
