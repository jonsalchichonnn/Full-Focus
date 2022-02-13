package com.jonsalchichonnn.fullfocus;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;

/*
    We run  createNotificationChannel() in onCreate of this class so we only create channel
    when the app starts instead of doing it when starting any activity
    (Even though creating an existing channel won't cause errors).
 */

public class App extends Application {
    public static final String DAILY_QUOTE_NOTIFICATION_CHANNEL = "dailyQuoteReminder";
    public static final String TIMER_NOTIFICATION_CHANNEL = "timerNotificationChannel";
    public static final int DAILY_QUOTE_CH_ID = 1;
    public static final int TIMER_NOT_CH_ID = 2;


    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    /*
     Create the NotificationChannel, but only on API 26+(>=8.0) because
     the NotificationChannel class is new and not in the support library
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //define the importance level of the notification
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            //build the actual notification channel, giving it a unique ID and name
            NotificationChannel dailyQuoteChannel = new NotificationChannel(
                    DAILY_QUOTE_NOTIFICATION_CHANNEL,
                    DAILY_QUOTE_NOTIFICATION_CHANNEL,
                    importance
            );
            NotificationChannel timerChannel = new NotificationChannel(
                    TIMER_NOTIFICATION_CHANNEL,
                    TIMER_NOTIFICATION_CHANNEL,
                    importance
            );

            //we can optionally set notification LED colour
            dailyQuoteChannel.setLightColor(Color.BLUE);
            dailyQuoteChannel.enableLights(true);
            dailyQuoteChannel.enableVibration(true);
            dailyQuoteChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            dailyQuoteChannel.setShowBadge(true);
            dailyQuoteChannel.setLightColor(Color.BLUE);

            timerChannel.setLightColor(Color.GREEN);
            timerChannel.enableLights(true);
            timerChannel.setVibrationPattern(new long[]{0});
            timerChannel.enableVibration(true);
            timerChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            timerChannel.setShowBadge(true);
            timerChannel.setLightColor(Color.RED);

            // Register the channel with the system
            //If we submit a new channel with the same name and description, the system will just ignore it as duplicate.
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().
                    getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(dailyQuoteChannel);
                notificationManager.createNotificationChannel(timerChannel);
            }
        }
    }
}