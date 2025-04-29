package com.michelin.connectedfleet.eld.ui.data;

import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.michelin.connectedfleet.eld.MyApplication;
import com.michelin.connectedfleet.eld.R;

public class NotificationHelper {
    private static final int NOTIFICATION_ID = 1001;
    private static final String TAG = "NotificationHelper";

    public static void showDayEndReminderNotification(Context context) {
        String contentText = "Here's your reminder to clock out!!";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MyApplication.DAY_END_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Clock-out reminder")
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
            Log.d(TAG, "Day-end reminder notification posted.");
        } else {
            Log.e(TAG, "Notification Manager is null, cannot show notification");
        }
    }

    public static void showDrivingLimitNotification(Context context, String remainingTime, boolean isCritical) {
        String contentText = "You are approaching your daily driving limit. " +
                "You have " + remainingTime + " left today. Please plan your rest stop accordingly.";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MyApplication.LIMIT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(isCritical ? "Critical: Driving Limit Alert" : "Driving Limit Alert")
                .setContentText(contentText)
                .setPriority(isCritical ? NotificationCompat.PRIORITY_HIGH : NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
            Log.d(TAG, "Driving limit notification posted with " + (isCritical ? "critical" : "normal") + " priority");
        } else {
            Log.e(TAG, "Notification Manager is null, cannot show notification");
        }
    }

    public static void cancelDrivingLimitNotification(Context context) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
            Log.d(TAG, "Driving limit notification cancelled");
        } else {
            Log.e(TAG, "Notification Manager is null, cannot cancel notification");
        }
    }
}
