package com.jonsalchichonnn.fullfocus;

import android.content.Context;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    // show attribution with a link back to https://zenquotes.io/ !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private static final String DAILY_URL = "https://zenquotes.io/api/today";
    private static final String SHARED_PREFS = "com.jonsalchichonnn.fullfocus";
    private static final String DAILY_QUOTE = "dailyQuote";
    private static final String RND_QUOTES = "rndQuotes";
    private static final String FIRST_TIME = "firstTime";

    private static final int N_FRASES = 50;
    private long startTimeInMillis;
    //1500000; // 25 mins

    private SharedPreferences sharedPreferences;
    private TextView tv_quotes;
    private Button btn_newQuote;
    private ImageView iv_session1;
    private ImageView iv_session2;
    private ImageView iv_session3;
    private ImageView iv_session4;
    private ImageView[] sessions;
    private ImageView iv_sessionMode;
    private TextView tv_timer;
    private ProgressBar pb_timer;
    private FloatingActionButton fab_startPause;
    private FloatingActionButton fab_stop;
    private CountDownTimer mCountDownTimer;
    private boolean isTimerRunning;
    private boolean isWorkSession = true;
    private long timeLeftInMillis;
    //private long endTime;
    private int finishedSessions = 0;
    private int progress;

    private String dailyQuote;
    private Random rnd;
    private boolean firstTime;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_quotes = findViewById(R.id.tv_quotes);
        btn_newQuote = findViewById(R.id.btn_new_quote);
        tv_timer = findViewById(R.id.tv_timer);
        pb_timer = findViewById(R.id.pb_timer);
        fab_startPause = findViewById(R.id.fab_start_pause);
        fab_stop = findViewById(R.id.fab_stop);
        iv_session1 = findViewById(R.id.iv_session1);
        iv_session2 = findViewById(R.id.iv_session2);
        iv_session3 = findViewById(R.id.iv_session3);
        iv_session4 = findViewById(R.id.iv_session4);
        sessions = new ImageView[]{iv_session1, iv_session2, iv_session3, iv_session4};
        iv_sessionMode = findViewById(R.id.iv_session_mode);

        rnd = new Random();

        sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

        firstTime = sharedPreferences.getBoolean(FIRST_TIME, true);

        // si lo he entendido bien, con esto evitamos q cada vez q abramos la app se programe otro Work
        if (firstTime)
            firstNotifyWork();
        String content = sharedPreferences.getString(DAILY_QUOTE, null);
        dailyQuote = content != null ? content : getRndQuote();
        updateQuotesText();
        updateSessionMode();
        updateSessions();
        updateCountDownView();
        updateButtons();


        btn_newQuote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dailyQuote = getRndQuote();
                updateQuotesText();
            }
        });

        fab_startPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTimerRunning) {
                    pauseTimer();
                } else {
                    startTimer();
                }
            }
        });

        fab_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });

    }


//    @Override
//    protected void onStart() {
//        super.onStart();
//        Log.i("onStart", "ONSTARTTTTTTTTTTTTTTTTTTTTTTTTT");
//        sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
//        timeLeftInMillis = sharedPreferences.getLong("millisLeft", startTimeInMillis);
//        isTimerRunning = sharedPreferences.getBoolean("timerRunning", false);
//        dailyQuote = sharedPreferences.getString(DAILY_QUOTE, null);
//
//        updateQuotesText();
//        updateCountDownView();
//        updateButtons();
//
//        if (isTimerRunning) {
//            endTime = sharedPreferences.getLong("endTime", 0);
//            timeLeftInMillis = endTime - System.currentTimeMillis();
//            // in case we completed the countdown
//            if (timeLeftInMillis < 0) {
//                timeLeftInMillis = 0;
//                isTimerRunning = false;
//                updateCountDownView();
//                updateButtons();
//            } else {
//                startTimer();
//            }
//        }
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putLong("millisLeft", timeLeftInMillis);
//        editor.putBoolean("timerRunning", isTimerRunning);
//        editor.putLong("endTime", endTime);
//        editor.putString("dailyQuote", dailyQuote);
//        editor.apply();
//
//        if (mCountDownTimer != null) {
//            mCountDownTimer.cancel();
//        }
//    }

    private void saveData() {
    }

    private void updateQuotesText() {
        tv_quotes.setText(dailyQuote);
    }


    private void startTimer() {
        //endTime = System.currentTimeMillis() + timeLeftInMillis;
        // we get onTick feedback every second
        mCountDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownView();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                timeLeftInMillis = 0;
                if(isWorkSession)
                    finishedSessions++;
                System.out.println("Count down terminado. finishedSessions = " + finishedSessions);
                isWorkSession = !isWorkSession;
                if (finishedSessions >= 4) {
                    finishedSessions = 0;
                }
                updateSessions();
                updateSessionMode();
                updateCountDownView();
                updateButtons();
            }
        }.start();
        isTimerRunning = true;
        updateButtons();
    }

    private void pauseTimer() {
        mCountDownTimer.cancel();
        isTimerRunning = false;
        updateButtons();
    }

    private void resetTimer() {
        mCountDownTimer.cancel();
        isTimerRunning = false;
        isWorkSession = true;
        finishedSessions = 0;
        updateSessions();
        updateSessionMode();
        updateCountDownView();
        updateButtons();
    }

    // update view of done working sessions
    private void updateSessions() {
        if (finishedSessions == 0) {
            for (int i = 0; i < sessions.length; i++)
                sessions[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_sessions));
        } else{
            sessions[finishedSessions - 1].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_session_done));
        }
    }

    //changes Pomodoro mode from work to break and vice versa
    private void updateSessionMode() {
        if (isWorkSession) {
            Log.i("WORK", "WORK SESSION!!!!!!!!!!!!!!!!!!!!");
            startTimeInMillis = 5000;
            iv_sessionMode.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_work));
        } else {
            Log.i("BREAK", "BREAK SESSION!!!!!!!!!!!!!!!!!!!!");
            startTimeInMillis = 3000;
            iv_sessionMode.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_break));
        }
        timeLeftInMillis = startTimeInMillis;
    }

    // updates progress bar and clock text
    private void updateCountDownView() {
        int hours = (int) (timeLeftInMillis / 1000) / 36000;
        int minutes = (int) (timeLeftInMillis / 36000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeLeftFormatted;
        if (hours > 0) {
            timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
        tv_timer.setText(timeLeftFormatted);
        progress = (int) ((double) timeLeftInMillis / startTimeInMillis * 100);
        pb_timer.setProgress(progress);
    }

    // called always after isTimerRunning changed
    private void updateButtons() {
//        if (isTimerRunning) {
//            fab_startPause.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause));
//            fab_stop.setVisibility(View.VISIBLE);
//        } else {
//            fab_startPause.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play_arrow));
//
//            if (timeLeftInMillis < 1000) {
//                fab_startPause.setVisibility(View.INVISIBLE);
//            } else {
//                fab_startPause.setVisibility(View.VISIBLE);
//            }
//            if (timeLeftInMillis >= startTimeInMillis) {
//                fab_stop.setVisibility(View.INVISIBLE);
//            } else {
//                fab_stop.setVisibility(View.VISIBLE);
//            }
        if (isTimerRunning) {
            fab_startPause.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_pause));
        } else {
            fab_startPause.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_play_arrow));
        }
        fab_stop.setVisibility(View.VISIBLE);
    }

    // set the daily quote task for the very 1st time
    // doesn't show the notification if the day u downloaded is 5AM past
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void firstNotifyWork() {
        Calendar currentDate = Calendar.getInstance();
        Calendar dueDate = Calendar.getInstance();
        // Set Execution around 05:00:00 AM
        dueDate.set(Calendar.HOUR_OF_DAY, 5);
        dueDate.set(Calendar.MINUTE, 0);
        dueDate.set(Calendar.SECOND, 0);

        // Re-scheduling the task for next day
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24);
        }

        long timeDiff = dueDate.getTimeInMillis() - currentDate.getTimeInMillis();
        Constraints constraints =
                new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotifyWorker.class)
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(getApplicationContext()).enqueue(notificationWork);
    }

    private String getRndQuote() {
        String rndQuote;
        try {
            JSONArray frases;
            if (firstTime) {
                frases = new JSONArray(loadJSONFromAsset());
            } else {
                frases = new JSONArray(sharedPreferences.getString(RND_QUOTES, null));
            }
            int indice = rnd.nextInt(N_FRASES);
            JSONObject daily = frases.getJSONObject(indice);
            rndQuote = daily.getString("q") + "\n-" + daily.getString("a");
            return rndQuote;
        } catch (JSONException e) {
            e.printStackTrace();
            return "ERROR en pickDefaultQuote";
        }
    }

    private String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("default_quotes.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return json;
    }
}