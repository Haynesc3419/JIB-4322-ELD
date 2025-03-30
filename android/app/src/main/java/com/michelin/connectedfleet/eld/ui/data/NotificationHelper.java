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

    public static void showDrivingLimitNotification(Context context, String remainingTime) {
        String contentText = "You are approaching your daily driving limit. " +
                "You have " + remainingTime + " left today. Please plan your rest stop accordingly.";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MyApplication.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Driving Limit Alert")
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
            Log.d(TAG, "Driving limit notification posted");
        } else {
            Log.e(TAG, "Notification Manager is null, cannot show notification");
        }
    }
}
