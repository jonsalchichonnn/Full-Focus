package com.jonsalchichonnn.fullfocus;

import static com.jonsalchichonnn.fullfocus.SettingsActivity.BREAK_TIME;
import static com.jonsalchichonnn.fullfocus.SettingsActivity.MODIFIED;
import static com.jonsalchichonnn.fullfocus.SettingsActivity.WORK_TIME;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jonsalchichonnn.fullfocus.util.CountDownTimerService;
import com.jonsalchichonnn.fullfocus.util.ScheduleWork;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MAIN_ACTIVITY";
    public static final String SHARED_PREFS = "com.jonsalchichonnn.fullfocus";
    public static final String TIMER_FINISHED = "timerFinished";
    // show attribution with a link back to https://zenquotes.io/ !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private static final String DAILY_URL = "https://zenquotes.io/api/today";
    private static final String DAILY_QUOTE = "dailyQuote";
    private static final String RND_QUOTES = "rndQuotes";
    private static final String FIRST_TIME = "firstTime";
    private static final int N_FRASES = 50;
    Intent cdtsIntent = null;
    //1500000; // 25 mins
    private long startTimeInMillis;
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
    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateGUI(intent);
        }
    };

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

        // Avoids re-scheduling the work everytime we open the app
        if (firstTime) {
            ScheduleWork.schedule(this);
        }

        String content = sharedPreferences.getString(DAILY_QUOTE, null);
        dailyQuote = content != null ? content : getRndQuote();

        // Init timer state to finished
        sharedPreferences.edit().putBoolean(TIMER_FINISHED, false).apply();
        // Init Main GUI
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                MainActivity.this.startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        registerReceiver(br, new IntentFilter(CountDownTimerService.COUNTDOWN_BR));
        Log.i(TAG, "Registered broacast receiver");

        boolean isCountDownFinished = sharedPreferences.getBoolean(TIMER_FINISHED, false);
        if (isCountDownFinished) {
            countdownFinished();
        }
        // Settings modification detected
        if (!isTimerRunning && sharedPreferences.getBoolean(MODIFIED, false)) {
            updateSessionMode();
            updateCountDownView();
            sharedPreferences.edit().putBoolean(MODIFIED, false).apply();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(br);
        Log.i(TAG, "Unregistered broacast receiver");
    }

    @Override
    public void onStop() {
        try {
            unregisterReceiver(br);
        } catch (Exception e) {
            // Receiver was probably already stopped in onPause()
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        stopService(new Intent(this, CountDownTimerService.class));
        Log.i(TAG, "Stopped service");
        super.onDestroy();
    }

    private void updateGUI(Intent intent) {
        // countdown finished
        if (intent.getBooleanExtra("finished", false)) {
            countdownFinished();
        } else if (intent.getExtras() != null) {
            timeLeftInMillis = intent.getLongExtra("countdown", 0);
            Log.i(TAG, "Countdown seconds remaining: " + timeLeftInMillis / 1000);
            updateCountDownView();
        }

    }


    private void saveData() {
    }

    private void updateQuotesText() {
        tv_quotes.setText(dailyQuote);
    }


    private void startTimer() {
        cdtsIntent = new Intent(this, CountDownTimerService.class);
        //Log.i(TAG, "TIMELEFTINMILLIS = " + timeLeftInMillis);
        cdtsIntent.putExtra("timeLeftInMillis", timeLeftInMillis);
        startService(cdtsIntent);
        Log.i(TAG, "Started service...");

        isTimerRunning = true;
        updateButtons();
    }

    private void pauseTimer() {
        if (cdtsIntent != null)
            stopService(cdtsIntent);
        isTimerRunning = false;
        updateButtons();
    }

    private void resetTimer() {
        if (cdtsIntent != null)
            stopService(cdtsIntent);
        isTimerRunning = false;
        isWorkSession = true;
        finishedSessions = 0;
        updateSessions();
        updateSessionMode();
        updateCountDownView();
        updateButtons();
    }

    private void countdownFinished() {
        isTimerRunning = false;
        timeLeftInMillis = 0;
        if (isWorkSession)
            finishedSessions++;
        Log.i(TAG, "Count down terminado. finishedSessions = " + finishedSessions);
        isWorkSession = !isWorkSession;
        if (isWorkSession && finishedSessions >= 4) {
            finishedSessions = 0;
        }
        updateSessions();
        updateSessionMode();
        updateCountDownView();
        updateButtons();
    }

    // Update view of done working sessions
    private void updateSessions() {
        if (finishedSessions == 0) {
            for (int i = 0; i < sessions.length; i++)
                sessions[i].setImageDrawable(
                        ContextCompat.getDrawable(this, R.drawable.ic_sessions)
                );
        } else {
            sessions[finishedSessions - 1].setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.ic_session_done)
            );
        }
    }

    //Changes Pomodoro mode from work to break and vice versa
    private void updateSessionMode() {
        if (isWorkSession) {
            Log.i("WORK", "WORK SESSION!!!!!!!!!!!!!!!!!!!!");
            /*startTimeInMillis = sharedPreferences.getInt(WORK_TIME, 1) * 60000;*/
            startTimeInMillis = 3000;
            iv_sessionMode.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.ic_work)
            );
        } else {
            Log.i("BREAK", "BREAK SESSION!!!!!!!!!!!!!!!!!!!!");
            startTimeInMillis = sharedPreferences.getInt(BREAK_TIME, 1) * 60000;
            iv_sessionMode.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.ic_break)
            );
        }
        timeLeftInMillis = startTimeInMillis;
    }

    // Updates progress bar and clock text
    private void updateCountDownView() {
        int hours = (int) (timeLeftInMillis / 1000) / 3600;
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeLeftFormatted;
        /*if (hours > 0) {
            timeLeftFormatted = String.format(
                    Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds
            );
        } else {
            timeLeftFormatted = String.format(
                    Locale.getDefault(), "%02d:%02d", minutes, seconds
            );
        }*/
        timeLeftFormatted = String.format(
                Locale.getDefault(), "%02d:%02d", minutes, seconds
        );
        tv_timer.setText(timeLeftFormatted);
        progress = (int) ((double) timeLeftInMillis / startTimeInMillis * 100);
        pb_timer.setProgress(progress);
    }

    // Called always after isTimerRunning changed
    private void updateButtons() {
        if (isTimerRunning) {
            fab_startPause.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.ic_pause)
            );
        } else {
            fab_startPause.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.ic_play_arrow)
            );
        }
        fab_stop.setVisibility(View.VISIBLE);
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