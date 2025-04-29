package com.michelin.connectedfleet.eld;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.michelin.connectedfleet.eld.worker.DrivingLimitWorker;
import com.michelin.connectedfleet.eld.ui.data.util.TimeZoneManager;

import java.util.concurrent.TimeUnit;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";
    public static final String LIMIT_CHANNEL_ID = "driving_limit_channel";
    public static final String DAY_END_CHANNEL_ID = "day_end_reminder_channel";
    private TimeZoneManager timeZoneManager;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        timeZoneManager = new TimeZoneManager(this);
        
        // Schedule the DrivingLimitWorker to run periodically
        PeriodicWorkRequest drivingLimitWork = new PeriodicWorkRequest.Builder(
            DrivingLimitWorker.class,
            15, TimeUnit.MINUTES)  // Check every 15 minutes
            .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "drivingLimitWork",
            ExistingPeriodicWorkPolicy.REPLACE,
            drivingLimitWork
        );
        Log.d(TAG, "DrivingLimitWorker scheduled successfully");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    LIMIT_CHANNEL_ID,
                    "Driving Limit Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications for driving time limits");
            NotificationChannel reminderChannel = new NotificationChannel(
                    DAY_END_CHANNEL_ID,
                    "Day end Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            reminderChannel.setDescription("Reminders to clock out at day end");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                notificationManager.createNotificationChannel(reminderChannel);
                Log.d(TAG, "Notification channel created successfully");
            } else {
                Log.e(TAG, "Failed to create notification channel: NotificationManager is null");
            }
        }
    }

    public TimeZoneManager getTimeZoneManager() {
        return timeZoneManager;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (timeZoneManager != null) {
            timeZoneManager.unregisterTimeZoneReceiver();
        }
    }
}
