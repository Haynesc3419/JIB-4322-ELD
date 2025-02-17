package com.michelin.connectedfleet.eld.ui.data;

import com.michelin.connectedfleet.eld.ui.data.retrofitinterface.GetLogEntryResponseItem;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

public record LoggedDay (
    LocalDate date,
    List<GetLogEntryResponseItem> logEntries,
    Locale locale
) implements Comparable<LoggedDay> {
    @Override
    public int compareTo(LoggedDay o) {
        return this.date.compareTo(o.date);
    }

    public String getTimeDriven() {
        long totalMinutes = 0;
        // drivingStarted will be null if the user has not started driving
        LocalDateTime drivingStarted = null;
        for (GetLogEntryResponseItem logEntry : logEntries) {
            if (logEntry.status().equals("Driving")) {
                drivingStarted = logEntry.dateTime();
            } else {
                if (drivingStarted != null) {
                    totalMinutes += ChronoUnit.MINUTES.between(drivingStarted, logEntry.dateTime());
                    drivingStarted = null;
                }
            }
        }

        return String.format(locale, "%2d:%02d", totalMinutes / 60, totalMinutes % 60);
    }

    public String getMonthAbbreviation() {
        return DateTimeFormatter.ofPattern("MMM", locale).format(date);
    }
}
