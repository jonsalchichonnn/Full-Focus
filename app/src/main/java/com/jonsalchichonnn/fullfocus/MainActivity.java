package com.jonsalchichonnn.fullfocus;

import android.content.Context;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    // show attribution with a link back to https://zenquotes.io/ !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private static final String DAILY_URL = "https://zenquotes.io/api/today";
    private static final int N_FRASES = 50;
    public static final String SHARED_PREFS = "com.jonsalchichonnn.fullfocus";
    public static final String DAILY_QUOTE = "dailyQuote";
    //we set a tag to be able to cancel all work of this type if needed


    private SharedPreferences sharedPreferences;
    private TextView tv_quotes;
    private Button btn_newQuote;

    private String dailyQuote;
    private Random rnd;



    /*
     * una vez al dia (a las 7:00?) notificacion con frase del dia y guardarla en sharedPreferences
     * además de conseguir 50 frases nuevas y actualizarlas en el json.
     *
     * cada vez q entro al app:
     * 1º intentar setear frase del dia GUARDADA
     * 2º si no hay => poner uno de los 50 offline
     *
     * */

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_quotes = findViewById(R.id.tv_quotes);
        btn_newQuote = findViewById(R.id.btn_newQuote);
        rnd = new Random();
        sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

        firstNotifyWork();
        String content = sharedPreferences.getString(DAILY_QUOTE, null);
        dailyQuote = content != null ? content : pickDefaultQuote();
        updateView();


        btn_newQuote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dailyQuote = pickDefaultQuote();
                updateView();
            }
        });

    }


    private void loadData() {
        // Request a JSON response from the provided URL.
        JsonArrayRequest dailyRequest = new JsonArrayRequest(Request.Method.GET, DAILY_URL, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject JSONQuote;
                String quote = "";
                String author = "";
                try {
                    JSONQuote = response.getJSONObject(0);
                    quote = JSONQuote.getString("q");
                    author = JSONQuote.getString("a");
                } catch (JSONException e) {
                    // mejorar el mensaje de error
                    e.printStackTrace();
                }
                dailyQuote = quote + "\n-" + author;
                updateView();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pickDefaultQuote();
                updateView();
            }
        });

        // Add the request to the RequestQueue(singleton bc we want only one queue for the whole app).
        RequestSingleton.getInstance(this).addToRequestQueue(dailyRequest);
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


    private void saveData() {
    }

    private void updateView() {
        tv_quotes.setText(dailyQuote);
    }

    private String pickDefaultQuote() {
        String rndQuote;
        try {
            JSONArray frases = new JSONArray(loadJSONFromAsset());
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