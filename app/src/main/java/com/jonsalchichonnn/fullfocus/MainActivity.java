package com.jonsalchichonnn.fullfocus;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    // show attribution with a link back to https://zenquotes.io/ !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private static final String DAILY_URL = "https://zenquotes.io/api/today";
    private static final int N_FRASES = 50;
    private final static String DAILY_NOTIFICATION_CHANNEL = "Daily Quote Reminder";
    private final static String NOTIFICATION_TITLE = "Daily Quote!!!";


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_quotes = findViewById(R.id.tv_quotes);
        btn_newQuote = findViewById(R.id.btn_newQuote);
        rnd = new Random();

        loadData();

        btn_newQuote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDefaultQuote();
                updateView();
            }
        });


        //safe to call this repeatedly because creating an existing notification channel performs no operation.
        createNotificationChannel();

        //Set the notification's tap action
        //create an intent to open the event details activity
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        //put together the PendingIntent:will start a new activity
        PendingIntent pendingIntent =
                PendingIntent.getActivity(getApplicationContext(), 1, intent, FLAG_UPDATE_CURRENT);

        //get latest daily quote
        String notificationText = "Ola k ase";

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

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(getApplicationContext());

        //we hardcode the id so there will be just 1 notification w/ different content
        notificationManager.notify(1, notificationBuilder.build());

    }


    private void loadData() {
        // Request a JSON response from the provided URL.
        JsonArrayRequest dailyRequest = new JsonArrayRequest(Request.Method.GET, DAILY_URL, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject JSONQuote = null;
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

    // Create the NotificationChannel, but only on API 26+(>=8.0) because
    // the NotificationChannel class is new and not in the support library
    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //define the importance level of the notification
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            //build the actual notification channel, giving it a unique ID and name
            NotificationChannel channel =
                    new NotificationChannel(DAILY_NOTIFICATION_CHANNEL, DAILY_NOTIFICATION_CHANNEL, importance);

            //we can optionally add a description for the channel
            String description = "A channel which shows notifications about daily Quotes";
            channel.setDescription(description);

            //we can optionally set notification LED colour
            channel.setLightColor(Color.MAGENTA);

            // Register the channel with the system
            //If we submit a new channel with the same name and description, the system will just ignore it as duplicate.
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().
                    getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    private void actualizarDaily() {

    }

    private void saveData() {
    }

    private void updateView() {
        tv_quotes.setText(dailyQuote);
    }

    private void pickDefaultQuote() {
        try {
            JSONArray frases = new JSONArray(loadJSONFromAsset());
            int indice = rnd.nextInt(N_FRASES);
            JSONObject daily = frases.getJSONObject(indice);
            dailyQuote = daily.getString("q") + "\n-" + daily.getString("a");
        } catch (JSONException e) {
            e.printStackTrace();
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
            return null;
        }
        return json;
    }
}