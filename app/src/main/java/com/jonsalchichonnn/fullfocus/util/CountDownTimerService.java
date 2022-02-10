package com.jonsalchichonnn.fullfocus.util;

import static com.jonsalchichonnn.fullfocus.MainActivity.SHARED_PREFS;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import com.jonsalchichonnn.fullfocus.R;

public class CountDownTimerService extends Service {
    public static final String COUNTDOWN_BR = "com.jonsalchichonnn.fullfocus.util.countdown_br";
    private final static String TAG = "CountDownTimerService";
    Intent broadcastIntent = new Intent(COUNTDOWN_BR);

    CountDownTimer cdt = null;
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private long timeLeftInMillis;
    private SharedPreferences sharedPreferences;

    //cdt was in Oncreate


    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = MediaPlayer.create(this, R.raw.rooster);
        mediaPlayer.setLooping(false);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public void onDestroy() {
        cdt.cancel();
        mediaPlayer.stop();
        vibrator.cancel();
        Log.i(TAG, "Timer,mediaPlayer,vibrator cancelled");
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
                    mediaPlayer.start();

                    long[] pattern = {0, 1000, 1000, 1000, 1000, 2000, 1000};
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
                    } else {
                        vibrator.vibrate(pattern, -1);
                    }
                    // TODO: SEND NOTIFICATION----------------------------------------------------------------


                    sharedPreferences.edit().putBoolean("timerFinished", true).apply();
                    broadcastIntent.putExtra("finished", true);
                    sendBroadcast(broadcastIntent);
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
