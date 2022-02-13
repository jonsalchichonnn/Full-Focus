package com.jonsalchichonnn.fullfocus.util;

import static com.jonsalchichonnn.fullfocus.App.DAILY_QUOTE_NOTIFICATION_CHANNEL;
import static com.jonsalchichonnn.fullfocus.App.TIMER_NOTIFICATION_CHANNEL;
import static com.jonsalchichonnn.fullfocus.util.NotificationActionReceiver.ACTION;
import static com.jonsalchichonnn.fullfocus.util.NotificationActionReceiver.ACTION_PAUSE;
import static com.jonsalchichonnn.fullfocus.util.NotificationActionReceiver.ACTION_RESUME;
import static com.jonsalchichonnn.fullfocus.util.NotificationActionReceiver.ACTION_START;
import static com.jonsalchichonnn.fullfocus.util.NotificationActionReceiver.ACTION_STOP;

import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.jonsalchichonnn.fullfocus.MainActivity;
import com.jonsalchichonnn.fullfocus.R;

import java.util.Locale;

public class NotificationHandler extends ContextWrapper {
    public static final int TIMER_NOTIFICATION_ID = 1;
    private final static String DAILY_QUOTE_NOTIFICATION_TITLE = "Daily Quote!!!";
    NotificationManagerCompat manager;

    public NotificationHandler(Context base) {
        super(base);
    }

    public NotificationManagerCompat getManager() {
        if (manager == null)
            manager = NotificationManagerCompat.from(this);
        return manager;
    }

    /*private PendingIntent getPendingIntentWithStack(Context ctx, Class<? extends Activity> cls) {
        Intent resultIntent = new Intent(ctx, cls);
        *//*resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);*//*
        //Intent.FLAG_ACTIVITY_CLEAR_TOP |
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
        stackBuilder.addParentStack(cls);
        stackBuilder.addNextIntent(resultIntent);
        *//*stackBuilder.addNextIntentWithParentStack(resultIntent);*//*


        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }*/

    @RequiresApi(api = Build.VERSION_CODES.O)
    private NotificationCompat.Builder createNotificationChannels(String title, String msg, String channel) {
        // Creamos intent q va a lanzar el MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //Intent.FLAG_ACTIVITY_CLEAR_TOP |
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //creamos pendingIntent
        PendingIntent pit = PendingIntent.getActivity(this,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );


        //crear Action: mejor para versiones + modernas
        //Icon icon = Icon.createWithResource(this, R.drawable.ic_launcher_foreground);
        /*Notification.Action pauseAction = new Notification.Action.Builder(icon, "PAUSE", pit).build();
        Notification.Action stopAction = new Notification.Action.Builder(icon, "STOP", pit).build();*/
        /*Notification.Action[] notActions = new Notification.Action[actions.length];
        for(int i = 0; i < notActions.length; i++){
            notActions[i] = new Notification.Action.Builder(icon, actions[i], pit).build();
        }*/

        return new NotificationCompat.Builder(getApplicationContext(), channel)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(msg)
                .setOngoing(true)
                //.setGroup(GROUP_SUMMARY)
                .setAutoCancel(true)
                .setContentIntent(pit); // Añadimos el pendingIntent a la notificación
        /*.setActions(notActions);*/
        /*.setActions(pauseAction, stopAction);*/// Añadimos la action creada
    }

    private NotificationCompat.Builder createNotificationWithoutChannels(String title, String msg) {
        return new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(msg)
                .setAutoCancel(true);
    }

    // Crea las notificaciones dependiendo de la versión de android que nos encontramos
    private NotificationCompat.Builder getBasicNotificationBuilder(String title, String msg, String channelID) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Log.i("NotificationHandler", "createNotificationChannels");
            return createNotificationChannels(title, msg, channelID);
        } else {
            Log.i("NotificationHandler", "createNotificationWithoutChannels");
            return createNotificationWithoutChannels(title, msg);
        }
    }

    public void showDailyQuoteNotification(String notificationText) {
        NotificationCompat.Builder notificationBuilder = this.getBasicNotificationBuilder(
                DAILY_QUOTE_NOTIFICATION_TITLE,
                notificationText,
                DAILY_QUOTE_NOTIFICATION_CHANNEL
        ).setOngoing(false);
        this.getManager().notify(TIMER_NOTIFICATION_ID, notificationBuilder.build());
    }

    public void showTimerRunningNotification(Context ctx, long timeLeftInMillis) {
        Intent stopIntent = new Intent(ctx, NotificationActionReceiver.class);
        stopIntent.setAction(ACTION_STOP);
        stopIntent.putExtra(ACTION, ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(ctx,
                0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent = new Intent(ctx, NotificationActionReceiver.class);
        pauseIntent.setAction(ACTION_PAUSE);
        pauseIntent.putExtra(ACTION, ACTION_PAUSE);
        PendingIntent pausePendingIntent = PendingIntent.getBroadcast(ctx,
                0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Log.e("NotificationHandler", "stopIntent: " + stopIntent.getStringExtra(ACTION));
        Log.e("NotificationHandler", "pauseIntent: " + pauseIntent.getStringExtra(ACTION));

        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeLeftFormatted = String.format(
                Locale.getDefault(), "%02d:%02d", minutes, seconds
        );

        NotificationCompat.Builder nBuilder = getBasicNotificationBuilder("Keep Working...",
                timeLeftFormatted,
                TIMER_NOTIFICATION_CHANNEL
        );

        /*NotificationCompat.Action pauseAction = new NotificationCompat.Action.Builder(R.drawable.ic_stop, "PAUSE", pausePendingIntent).build();
        NotificationCompat.Action stopAction = new NotificationCompat.Action.Builder(R.drawable.ic_pause, "STOP", stopPendingIntent).build();
        nBuilder.addAction(pauseAction)
                .addAction(stopAction);*/
        nBuilder.addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)
                .addAction(R.drawable.ic_pause, "Pause", pausePendingIntent);
        this.getManager().notify(TIMER_NOTIFICATION_ID, nBuilder.build());
    }

    public void showTimerPausedNotification(Context ctx) {
        Intent resumeIntent = new Intent(ctx, NotificationActionReceiver.class);
        resumeIntent.setAction(ACTION_RESUME);
        resumeIntent.putExtra(ACTION, ACTION_RESUME);
        PendingIntent resumePendingIntent = PendingIntent.getBroadcast(ctx,
                0, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder nBuilder = getBasicNotificationBuilder("Timer paused...",
                "Resume?",
                TIMER_NOTIFICATION_CHANNEL
        );

        nBuilder.addAction(R.drawable.ic_stop, "Resume", resumePendingIntent);
        this.getManager().notify(TIMER_NOTIFICATION_ID, nBuilder.build());
    }

    public void showTimerExpiredNotification(Context ctx) {
        Intent startIntent = new Intent(ctx, NotificationActionReceiver.class);
        startIntent.setAction(ACTION_START);
        startIntent.putExtra(ACTION, ACTION_START);
        PendingIntent resumePendingIntent = PendingIntent.getBroadcast(ctx,
                0, startIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder nBuilder = getBasicNotificationBuilder("Timer expired!!!",
                "Start again?",
                TIMER_NOTIFICATION_CHANNEL
        );

        nBuilder.addAction(R.drawable.ic_stop, "Start", resumePendingIntent);
        this.getManager().notify(TIMER_NOTIFICATION_ID, nBuilder.build());
    }

    public void hideTimerNotification(Context ctx) {
        this.getManager().cancel(TIMER_NOTIFICATION_ID);
    }

  /*  @RequiresApi(api = Build.VERSION_CODES.M)
    private NotificationCompat.Builder setActions(NotificationCompat.Builder builder, String[] actions){
        Icon icon = Icon.createWithResource(this, R.drawable.ic_launcher_foreground);
        Notification.Action[] notActions = new Notification.Action[actions.length];
        for(int i = 0; i < notActions.length; i++){
            notActions[i] = new Notification.Action.Builder(icon, "PAUSE", pit).build();
        }
    }*/

    // Crear grupo de notificaciones
    /*@RequiresApi(api = Build.VERSION_CODES.O)
    public void createGroup() {
        String canal = CHANNEL_ID;
        Notification grupo = new NotificationCompat.Builder(this, canal)
                .setGroupSummary(true)
                .setGroup(GROUP_SUMMARY)
                .setSmallIcon(R.drawable.ic_launcher_foreground).build();
        getManager().notify(GRUPO_ID, grupo);
    }*/
}