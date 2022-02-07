package com.jonsalchichonnn.fullfocus.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.jonsalchichonnn.fullfocus.MainActivity;
import com.jonsalchichonnn.fullfocus.R;

public class NotificationHandler extends ContextWrapper {
    NotificationManager manager;

    public NotificationHandler(Context base) {
        super(base);
    }

    public NotificationManager getManager() {
        if (manager == null)
            manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        return manager;
    }

    // Crea las notificaciones dependiendo de la versión de android que nos encontramos
    public Notification.Builder createNotification(String title, String msg, String channelID) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Log.i("NotificationHandler", "createNotificationChannels");
            return createNotificationChannels(title, msg, channelID);
        } else {
            Log.i("NotificationHandler", "createNotificationWithoutChannels");
            return createNotificationWithoutChannels(title, msg);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification.Builder createNotificationChannels(String title, String msg, String channel) {
        // Creamos intent q va a lanzar el MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        //Configuramos el intent: al volver atrás apaga la app si no estuviera abierta
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
        //creamos pendingIntent
        PendingIntent pit = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        //crear Action: mejor para versiones + modernas
        Icon icon = Icon.createWithResource(this, R.drawable.ic_launcher_foreground);
        Notification.Action pauseAction = new Notification.Action.Builder(icon, "PAUSE", pit).build();
        Notification.Action stopAction = new Notification.Action.Builder(icon, "STOP", pit).build();


        return new Notification.Builder(getApplicationContext(), channel)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(msg)
                //.setGroup(GROUP_SUMMARY)
                .setAutoCancel(true)
                .setContentIntent(pit) // Añadimos el pendingIntent a la notificación
                .setActions(pauseAction, stopAction);// Añadimos la action creada
    }

    private Notification.Builder createNotificationWithoutChannels(String title, String msg) {
        return new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(msg);
    }

    // Crear grupo de notificaciones
    /*@RequiresApi(api = Build.VERSION_CODES.O)
    public void createGroup() {
        String canal = CHANNEL_ID;
        Notification grupo = new Notification.Builder(this, canal)
                .setGroupSummary(true)
                .setGroup(GROUP_SUMMARY)
                .setSmallIcon(R.drawable.ic_launcher_foreground).build();
        getManager().notify(GRUPO_ID, grupo);
    }*/
}