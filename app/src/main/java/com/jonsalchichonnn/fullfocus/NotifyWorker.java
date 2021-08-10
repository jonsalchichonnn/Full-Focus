package com.jonsalchichonnn.fullfocus;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.net.Network;
import android.os.Build;

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

import java.util.concurrent.TimeUnit;

public class NotifyWorker extends Worker {
    private final static String DAILY_NOTIFICATION_CHANNEL = "Daily Quote Reminder";
    private final static String NOTIFICATION_TITLE = "Daily Quote!!!";
    public static final String workTag = "notificationWork";



    public NotifyWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @NonNull
    @Override
    public Result doWork() {
        actualizarDaily();
        return Result.success();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void actualizarDaily() {
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

        // Method to trigger an instant notification
        triggerNotification();

        Constraints constraints =
                new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotifyWorker.class)
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .addTag(workTag)
                .build();


        WorkManager.getInstance(getApplicationContext()).enqueue(notificationWork);
    }

    private void triggerNotification(){
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
}
