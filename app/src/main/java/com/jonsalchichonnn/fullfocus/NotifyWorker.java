package com.jonsalchichonnn.fullfocus;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.net.Network;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class NotifyWorker extends Worker {
    private static final String DAILY_URL = "https://zenquotes.io/api/today";
    private final static String DAILY_NOTIFICATION_CHANNEL = "Daily Quote Reminder";
    private final static String NOTIFICATION_TITLE = "Daily Quote!!!";
    public static final String workTag = "notificationWork";
    public static final String SHARED_PREFS = "com.jonsalchichonnn.fullfocus";
    public static final String DAILY_QUOTE = "dailyQuote";

    Context applicationContext = getApplicationContext();

    private SharedPreferences sharedPreferences;

    private String dailyQuote;


    public NotifyWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @NonNull
    @Override
    public Result doWork() {
        sharedPreferences = applicationContext.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        getNewDailyQuote(new VolleyResponseListener() {
            @Override
            public void onError(String msj) {
                Toast.makeText(getApplicationContext(), "Something Wrong",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(String dailyQuote) {
                actualizarDaily(dailyQuote);
            }
        });
        return Result.success();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void actualizarDaily(String dailyQuote) {
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

        // get daily quote and fire a notification
        triggerNotification(dailyQuote);

        Constraints constraints =
                new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotifyWorker.class)
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .addTag(workTag)
                .build();


        WorkManager.getInstance(getApplicationContext()).enqueue(notificationWork);
    }

    private void triggerNotification(String notificationText) {
        //safe to call this repeatedly because creating an existing notification channel performs no operation.
        createNotificationChannel();

        //Set the notification's tap action
        //create an intent to open the event details activity
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        //put together the PendingIntent:will start a new activity
        PendingIntent pendingIntent =
                PendingIntent.getActivity(getApplicationContext(), 1, intent, FLAG_UPDATE_CURRENT);

        sharedPreferences.edit().putString(DAILY_QUOTE, notificationText).apply();

        //build the notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(getApplicationContext(), DAILY_NOTIFICATION_CHANNEL)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle(NOTIFICATION_TITLE)
                        .setContentText(notificationText)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        //Triggering the notification
        //we hardcode the id so there will be just 1 notification w/ different content
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(getApplicationContext());

        notificationManager.notify(1, notificationBuilder.build());
    }

    // Create the NotificationChannel, but only on API 26+(>=8.0) because
    // the NotificationChannel class is new and not in the support library
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //define the importance level of the notification
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            //build the actual notification channel, giving it a unique ID and name
            NotificationChannel channel =
                    new NotificationChannel(DAILY_NOTIFICATION_CHANNEL, DAILY_NOTIFICATION_CHANNEL, importance);

            //we can optionally set notification LED colour
            channel.setLightColor(Color.BLUE);

            // Register the channel with the system
            //If we submit a new channel with the same name and description, the system will just ignore it as duplicate.
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().
                    getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }


    public interface VolleyResponseListener{
        void onError(String msj);
        void onResponse(String dailyQuote);
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
        RequestSingleton.getInstance(getApplicationContext()).addToRequestQueue(dailyRequest);
    }
}
