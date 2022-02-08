package com.jonsalchichonnn.fullfocus.util;

import static com.jonsalchichonnn.fullfocus.MainActivity.SHARED_PREFS;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

public class CountDownTimerService extends Service {
    public static final String COUNTDOWN_BR = "com.jonsalchichonnn.fullfocus.util.countdown_br";
    private final static String TAG = "CountDownTimerService";
    Intent broadcastIntent = new Intent(COUNTDOWN_BR);

    CountDownTimer cdt = null;
    private long timeLeftInMillis;
    private SharedPreferences sharedPreferences;

    //cdt was in Oncreate

    @Override
    public void onDestroy() {
        cdt.cancel();
        Log.i(TAG, "Timer cancelled");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

        if (intent.getExtras() != null) {
            timeLeftInMillis = intent.getLongExtra("timeLeftInMillis", 0);


            Log.i(TAG, "Starting timer... TIMELEFTINMILLIS = " + timeLeftInMillis);
            cdt = new CountDownTimer(timeLeftInMillis, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                    Log.i(TAG, "Countdown seconds remaining: " + millisUntilFinished / 1000);
                    broadcastIntent.putExtra("countdown", millisUntilFinished);
                    broadcastIntent.putExtra("finished", false);
                    sharedPreferences.edit().putBoolean("timerFinished", false).apply();

                    sendBroadcast(broadcastIntent);
                }

                @Override
                public void onFinish() {
                    Log.i(TAG, "Timer finished");
                    sharedPreferences.edit().putBoolean("timerFinished", true).apply();
                    broadcastIntent.putExtra("finished", true);
                    sendBroadcast(broadcastIntent);
                    // TODO: send notification and ringtone
                }
            };

            cdt.start();
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
