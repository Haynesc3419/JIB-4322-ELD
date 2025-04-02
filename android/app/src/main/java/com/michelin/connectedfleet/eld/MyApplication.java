package com.michelin.connectedfleet.eld;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
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
    public static final String CHANNEL_ID = "driving_limit_channel";
    private TimeZoneManager timeZoneManager;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        timeZoneManager = new TimeZoneManager(this);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Driving Limit Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications for driving time limits");
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
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
