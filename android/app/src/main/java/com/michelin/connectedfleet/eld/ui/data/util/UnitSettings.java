package com.michelin.connectedfleet.eld.ui.data.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class UnitSettings {
    private static final String TAG = "UnitSettings";
    private static final String PREF_NAME = "unit_settings";
    private static final String KEY_USE_METRIC = "use_metric";

    private final Context context;
    private final SharedPreferences preferences;
    private final MutableLiveData<Boolean> useMetric;

    // Conversion constants
    private static final double MILES_TO_KM = 1.60934;
    private static final double KM_TO_MILES = 0.621371;

    public UnitSettings(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.useMetric = new MutableLiveData<>(preferences.getBoolean(KEY_USE_METRIC, true));
    }

    public LiveData<Boolean> getUseMetric() {
        return useMetric;
    }

    public boolean isMetric() {
        return Boolean.TRUE.equals(useMetric.getValue());
    }

    public void setUseMetric(boolean useMetric) {
        preferences.edit().putBoolean(KEY_USE_METRIC, useMetric).apply();
        this.useMetric.setValue(useMetric);
        Log.d(TAG, "Unit preference changed to: " + (useMetric ? "metric" : "imperial"));
    }

    // Distance conversions (assuming input is in miles)
    public double convertDistance(double miles) {
        return isMetric() ? milesToKm(miles) : miles;
    }

    public String formatDistance(double miles) {
        double value = convertDistance(miles);
        String unit = isMetric() ? "km" : "mi";
        return String.format("%.2f %s", value, unit);
    }

    // Speed conversions (assuming input is in mph)
    public double convertSpeed(double mph) {
        return isMetric() ? mphToKmh(mph) : mph;
    }

    public String formatSpeed(double mph) {
        double value = convertSpeed(mph);
        String unit = isMetric() ? "km/h" : "mph";
        return String.format("%.1f %s", value, unit);
    }

    // Raw conversion methods
    public static double milesToKm(double miles) {
        return miles * MILES_TO_KM;
    }

    public static double kmToMiles(double km) {
        return km * KM_TO_MILES;
    }

    public static double mphToKmh(double mph) {
        return mph * MILES_TO_KM;
    }

    public static double kmhToMph(double kmh) {
        return kmh * KM_TO_MILES;
    }
} 