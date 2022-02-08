package com.jonsalchichonnn.fullfocus.util;

import android.content.Context;
import android.icu.util.Calendar;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class ScheduleWork {
    // doesn't show the notification if the day u downloaded is 5AM past
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void schedule(Context ctx) {
        Calendar currentDate = Calendar.getInstance();
        Calendar dueDate = Calendar.getInstance();
        // Set Execution around 06:00:00 AM
        dueDate.set(Calendar.HOUR_OF_DAY, 6);
        dueDate.set(Calendar.MINUTE, 0);
        dueDate.set(Calendar.SECOND, 0);

        // Re-scheduling the task for next day
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24);
        }

        long timeDiff = dueDate.getTimeInMillis() - currentDate.getTimeInMillis();
        Constraints constraints =
                new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(UpdateQuotesWorker.class)
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(ctx).enqueue(notificationWork);

    }
}
