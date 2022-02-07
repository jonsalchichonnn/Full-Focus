package com.jonsalchichonnn.fullfocus.util;

import static com.jonsalchichonnn.fullfocus.App.DAILY_QUOTE_NOTIFICATION_CHANNEL;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UpdateQuotesWorker extends Worker {
    private static final String DAILY_URL = "https://zenquotes.io/api/today";
    private static final String RND_QUOTES_URL = "https://zenquotes.io/api/quotes";
    private final static String DAILY_NOTIFICATION_CHANNEL = "Daily Quote Reminder";
    private final static String NOTIFICATION_TITLE = "Daily Quote!!!";
    private static final String workTag = "notificationWork";
    private static final String SHARED_PREFS = "com.jonsalchichonnn.fullfocus";
    private static final String DAILY_QUOTE = "dailyQuote";
    private static final String RND_QUOTES = "rndQuotes";
    private static final String FIRST_TIME = "firstTime";

    // mirar si sustituir todos los getAppContext por esta var
    Context applicationContext = getApplicationContext();

    private SharedPreferences sharedPreferences;

    private String dailyQuote;
    private NotificationHandler notificationHandler;


    public UpdateQuotesWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @NonNull
    @Override
    public Result doWork() {
        sharedPreferences = applicationContext.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        getNewRndQuotes();
        getNewDailyQuote(new VolleyResponseListener() {
            @Override
            public void onError(String msj) {
                Toast.makeText(applicationContext, "Something Wrong", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(String dailyQuote) {
                updateDaily(dailyQuote);
            }
        });
        return Result.success();
    }

    // Re-schedules quotes update task for next day and fires a notification with daily quote
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateDaily(String dailyQuote) {
        ScheduleWork.schedule(applicationContext);
        // get daily quote and fire a notification
        notificationHandler = new NotificationHandler(this.getApplicationContext());
        triggerNotification(dailyQuote);
    }

    /* FIGURE OUT HOW TO SET DIFFERENT PENDINGINTENTS FOR DIFF NOTIFICATIONS*/
    private void triggerNotification(String notificationText) {
        sharedPreferences.edit().putString(DAILY_QUOTE, notificationText).apply();

        //build the notification
        Notification.Builder notificationBuilder = notificationHandler.createNotification(
                NOTIFICATION_TITLE,
                notificationText,
                DAILY_QUOTE_NOTIFICATION_CHANNEL
        );
        //Triggering the notification
        //we hardcode the id so there will be just 1 notification w/ different content
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(applicationContext);

        notificationManager.notify(1, notificationBuilder.build());
    }

    private void getNewDailyQuote(VolleyResponseListener volleyResponseListener) {
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
                volleyResponseListener.onResponse(dailyQuote);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dailyQuote = "getNewDaily ERROR";
                volleyResponseListener.onError(dailyQuote);
            }
        });

        // Add the request to the RequestQueue(singleton bc we want only one queue for the whole app).
        RequestSingleton.getInstance(applicationContext).addToRequestQueue(dailyRequest);
    }

    private void getNewRndQuotes() {
        JsonArrayRequest dailyRequest = new JsonArrayRequest(Request.Method.GET, RND_QUOTES_URL, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                sharedPreferences.edit().putString(RND_QUOTES, response.toString()).apply();
                sharedPreferences.edit().putBoolean(FIRST_TIME, false).apply();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        // Add the request to the RequestQueue(singleton bc we want only one queue for the whole app).
        RequestSingleton.getInstance(applicationContext).addToRequestQueue(dailyRequest);
    }

    public interface VolleyResponseListener {
        void onError(String msj);

        void onResponse(String dailyQuote);
    }
}
