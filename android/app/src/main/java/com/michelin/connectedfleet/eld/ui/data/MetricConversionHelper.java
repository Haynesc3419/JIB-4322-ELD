package com.michelin.connectedfleet.eld.ui.data;

import java.text.DecimalFormat;

public class MetricConversionHelper {
    private static final DecimalFormat oneDecimalPoint = new DecimalFormat("#.#");

    public static String getDistanceWithUnit(float distance, boolean useMetric) {
        StringBuilder odometerReading = new StringBuilder();
        if (useMetric) {
            odometerReading.append(oneDecimalPoint.format(distance)).append(" km");
        } else {
            odometerReading.append(oneDecimalPoint.format(convertKMHToMPH(distance))).append(" miles");
        }

        return odometerReading.toString();
    }

    private static double convertKMHToMPH(double kmh) {
        return kmh / 1.609344;
    }
}
