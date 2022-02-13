package com.jonsalchichonnn.fullfocus.util;

import static com.jonsalchichonnn.fullfocus.MainActivity.SHARED_PREFS;
import static com.jonsalchichonnn.fullfocus.util.CountDownTimerService.COUNTDOWN_BR;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class NotificationActionReceiver extends BroadcastReceiver {
    public static final String ACTION = "action";
    public static final String ACTION_STOP = "stop";
    public static final String ACTION_PAUSE = "pause";
    public static final String ACTION_RESUME = "resume";
    public static final String ACTION_START = "start";
    NotificationHandler notificationHandler;
    SharedPreferences sharedPreferences;
    Intent broadcastIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        notificationHandler = new NotificationHandler(context);
        sharedPreferences = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        broadcastIntent = new Intent(COUNTDOWN_BR);
        Log.e("NotActionReceiver", "GOT BROADCAST...");
        String action = intent.getAction();
        Log.e("NotActionReceiver", "intent.getAction(): " + intent.getAction());
        switch (action) {
            case ACTION_STOP:
                Log.e("NotActionReceiver", "Dentro stop");
                context.stopService(new Intent(context, CountDownTimerService.class));

                sharedPreferences.edit().putBoolean("reset", true).apply();
                notificationHandler.hideTimerNotification(context);

                broadcastIntent.putExtra(ACTION, ACTION_STOP);
                context.sendBroadcast(broadcastIntent);
                break;
            case ACTION_PAUSE:
                Log.e("NotActionReceiver", "Dentro PAUSE");
                context.stopService(new Intent(context, CountDownTimerService.class));

                notificationHandler.showTimerPausedNotification(context);
                sharedPreferences.edit().putBoolean("pause", true).apply();

                broadcastIntent.putExtra(ACTION, ACTION_PAUSE);
                context.sendBroadcast(broadcastIntent);
                break;
            case ACTION_RESUME:
                Log.e("NotActionReceiver", "Dentro RESUME");
                //context.startService(new Intent(context, CountDownTimerService.class));

                //notificationHandler.showTimerRunningNotification(context);
                sharedPreferences.edit().putBoolean("resume", true).apply();

                broadcastIntent.putExtra(ACTION, ACTION_RESUME);
                /*context.sendOrderedBroadcast(broadcastIntent, null);*/
                context.sendBroadcast(broadcastIntent);
                break;
            case ACTION_START:
                Log.e("NotActionReceiver", "Dentro RESUME");

                broadcastIntent.putExtra(ACTION, ACTION_START);
                context.sendBroadcast(broadcastIntent);
        }
    }
}
