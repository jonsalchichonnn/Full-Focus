package com.jonsalchichonnn.fullfocus;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class NotifyWorker extends Worker {
        public NotifyWorker(@NonNull Context context, @NonNull WorkerParameters params) {
            super(context, params);
        }

        @NonNull
        @Override
        public Result doWork() {
            // Method to trigger an instant notification
            //triggerNotification();

            return Result.success();
            // (Returning RETRY tells WorkManager to try this task again
            // later; FAILURE says not to try again.)
        }
    }
}
