package com.jonsalchichonnn.fullfocus;

import static com.jonsalchichonnn.fullfocus.MainActivity.SHARED_PREFS;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    public static final String TAG = "SettingsActivity";
    public static final String WORK_TIME = "workTime";
    public static final String BREAK_TIME = "breakTime";
    public static final String MODIFIED = "modified";

    TextView tvTimerSettings;
    TextView tvWorkTime;
    TextView tvBreakTime;
    NumberPicker npWorkTime;
    NumberPicker npBreakTime;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        tvTimerSettings = findViewById(R.id.tv_timer_settings);
        tvWorkTime = findViewById(R.id.tv_work_time);
        tvBreakTime = findViewById(R.id.tv_break_time);
        npWorkTime = findViewById(R.id.np_work_time);
        npBreakTime = findViewById(R.id.np_break_time);
        sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

        npWorkTime.setMinValue(1);
        npWorkTime.setMaxValue(60);
        npBreakTime.setMinValue(1);
        npBreakTime.setMaxValue(60);

        npWorkTime.setValue(sharedPreferences.getInt(WORK_TIME, 25));
        npBreakTime.setValue(sharedPreferences.getInt(BREAK_TIME, 5));

        npWorkTime.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int i) {
                return String.format("%02d", i);
            }
        });

        npWorkTime.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
                /*sharedPreferences.edit().putInt(WORK_TIME, newVal).apply();
                sharedPreferences.edit().putBoolean(MODIFIED, true).apply();*/
                updateSharedPref(WORK_TIME, newVal);
                Log.i(TAG, "npWorkTime New val = " + newVal);
            }
        });

        npBreakTime.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int i) {
                return String.format("%02d", i);
            }
        });

        npBreakTime.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
                /*sharedPreferences.edit().putInt(BREAK_TIME, newVal).apply();
                sharedPreferences.edit().putBoolean(MODIFIED, true).apply();*/
                updateSharedPref(BREAK_TIME, newVal);
                Log.i(TAG, "npBreakTime New val = " + newVal);
            }
        });

    }

    private void updateSharedPref(String key, int value) {
        sharedPreferences.edit().putInt(key, value).apply();
        sharedPreferences.edit().putBoolean(MODIFIED, true).apply();
    }
}