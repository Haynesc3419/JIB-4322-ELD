package com.michelin.connectedfleet.eld.ui.data.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;

public class TimeZoneManager {
    private static final String TAG = "TimeZoneManager";
    private final Context context;
    private final MutableLiveData<String> currentTimeZone;
    private final BroadcastReceiver timeZoneReceiver;

    public TimeZoneManager(Context context) {
        this.context = context;
        this.currentTimeZone = new MutableLiveData<>();
        this.timeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) {
                    Log.d(TAG, "Timezone changed, updating display");
                    updateTimeZoneDisplay();
                }
            }
        };
        registerTimeZoneReceiver();
    }

    private void registerTimeZoneReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
        context.registerReceiver(timeZoneReceiver, filter);
    }

    public void unregisterTimeZoneReceiver() {
        try {
            context.unregisterReceiver(timeZoneReceiver);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error unregistering timezone receiver", e);
        }
    }

    public LiveData<String> getCurrentTimeZone() {
        return currentTimeZone;
    }

    public String getCurrentTimeZoneDisplay() {
        ZoneId zoneId = ZoneId.systemDefault();
        return zoneId.getDisplayName(TextStyle.FULL, Locale.getDefault());
    }

    public void updateTimeZoneDisplay() {
        currentTimeZone.setValue(getCurrentTimeZoneDisplay());
    }
} 