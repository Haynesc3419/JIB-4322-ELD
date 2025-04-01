package com.michelin.connectedfleet.eld.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.michelin.connectedfleet.eld.ui.data.NotificationHelper;
import com.michelin.connectedfleet.eld.ui.data.util.TimerManager;

public class DrivingLimitWorker extends Worker {
    private static final int CRITICAL_THRESHOLD_MINUTES = 15;
    private static final int WARNING_THRESHOLD_MINUTES = 30;
    private static final int NOTICE_THRESHOLD_MINUTES = 60;

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

        // Show notifications based on different thresholds
        if (minutesLeft <= CRITICAL_THRESHOLD_MINUTES) {
            String remainingTimeStr = String.format("%d minutes", minutesLeft);
            NotificationHelper.showDrivingLimitNotification(getApplicationContext(), remainingTimeStr, true);
        } else if (minutesLeft <= WARNING_THRESHOLD_MINUTES) {
            String remainingTimeStr = String.format("%d minutes", minutesLeft);
            NotificationHelper.showDrivingLimitNotification(getApplicationContext(), remainingTimeStr, false);
        } else if (minutesLeft <= NOTICE_THRESHOLD_MINUTES) {
            String remainingTimeStr = String.format("%d minutes", minutesLeft);
            NotificationHelper.showDrivingLimitNotification(getApplicationContext(), remainingTimeStr, false);
        }

        return Result.success();
    }
}
