package com.michelin.connectedfleet.eld.ui.data.util;

import android.content.Context;
import android.util.Log;

import java.time.ZoneId;
import java.util.TimeZone;

public class TimeZoneManager {
    private static final String TAG = "TimeZoneManager";
    private static TimeZoneManager instance;
    private final Context context;
    private ZoneId currentZoneId;

    private TimeZoneManager(Context context) {
        this.context = context.getApplicationContext();
        this.currentZoneId = ZoneId.systemDefault();
    }

    public static synchronized TimeZoneManager getInstance(Context context) {
        if (instance == null) {
            instance = new TimeZoneManager(context);
        }
        return instance;
    }

    public void updateTimeZone() {
        ZoneId newZoneId = TimeZone.getDefault().toZoneId();
        if (!newZoneId.equals(currentZoneId)) {
            currentZoneId = newZoneId;
            Log.d(TAG, "TimeZone updated to: " + currentZoneId);
        }
    }

    public ZoneId getCurrentZoneId() {
        return currentZoneId;
    }

    public String getCurrentTimeZoneDisplay() {
        return currentZoneId.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault());
    }
} 