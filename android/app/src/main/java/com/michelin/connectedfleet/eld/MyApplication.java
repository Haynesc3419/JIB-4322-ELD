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

import java.util.concurrent.TimeUnit;

public class MyApplication extends Application {
    public static final String CHANNEL_ID = "driving_limit_channel";
    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        scheduleDrivingLimitWorker();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "Driving Limit Notifications";
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications when approaching your daily driving limit (per FMCSA guidelines).");
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created: " + CHANNEL_ID);
            } else {
                Log.e(TAG, "Notification Manager is null, cannot create channel");
            }
        }
    }

    private void scheduleDrivingLimitWorker() {
        PeriodicWorkRequest drivingLimitWorkRequest =
                new PeriodicWorkRequest.Builder(DrivingLimitWorker.class, 15, TimeUnit.MINUTES)
                        .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "DrivingLimitWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                drivingLimitWorkRequest
        );

//        PeriodicWorkRequest drivingLimitWorkRequest =
//                new PeriodicWorkRequest.Builder(DrivingLimitWorker.class, 15, TimeUnit.MINUTES)
//                        .setInitialDelay(10, TimeUnit.SECONDS)
//                        .build();
//
//        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
//                "DrivingLimitWorker",
//                ExistingPeriodicWorkPolicy.KEEP,
//                drivingLimitWorkRequest
//        );
    }
}
