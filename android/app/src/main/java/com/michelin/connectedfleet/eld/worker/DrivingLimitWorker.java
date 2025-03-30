package com.michelin.connectedfleet.eld.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.michelin.connectedfleet.eld.ui.data.NotificationHelper;
import com.michelin.connectedfleet.eld.ui.data.util.TimerManager;

public class DrivingLimitWorker extends Worker {

    public DrivingLimitWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        TimerManager timerManager = TimerManager.getInstance();
        long timeLeftMs = timerManager.getTimeLeft("driving");
        Log.d("DrivingLimitWorker", "Time left (ms): " + timeLeftMs);

        // Convert time left from milliseconds to minutes
        long minutesLeft = timeLeftMs / (1000 * 60);
//        int hours = (int) (minutesLeft / 60);
//        int minutes = (int) (minutesLeft % 60);

        // If remaining time is less than 60 minutes post a notification
        if (minutesLeft < 60) {
            String remainingTimeStr = String.format("%d minutes", minutesLeft);
            NotificationHelper.showDrivingLimitNotification(getApplicationContext(), remainingTimeStr);
        }

        return Result.success();
    }
}
