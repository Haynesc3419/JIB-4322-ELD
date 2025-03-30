package com.michelin.connectedfleet.eld.ui.home;

import android.app.Application;
import android.content.res.Resources;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.michelin.connectedfleet.eld.R;
import com.michelin.connectedfleet.eld.ui.data.util.TimerManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {
    private final LogsViewModel logs;
    public final HoursRemainingViewModel hoursRemaining;

    public TimerManager timer = TimerManager.getInstance();
    public float drivingRemaining;

    public HomeViewModel(Application app) {
        super(app);
        this.logs = new LogsViewModel();

        this.hoursRemaining = new HoursRemainingViewModel(
                getApplication()
        );

    }

    public LiveData<List<Date>> getDates() {
        return logs.mDates;
    }

    public class HoursRemainingViewModel extends ViewModel {
        protected final MutableLiveData<HoursRemainingContainer> breakHoursRemaining;
        protected final MutableLiveData<HoursRemainingContainer> drivingHoursRemaining;
        protected final MutableLiveData<HoursRemainingContainer> dayResetHoursRemaining;

        @ColorInt
        private final int validColor;
        @ColorInt
        private final int warningColor;
        @ColorInt
        private final int dangerColor;

        private final String hourMinuteFormatString;

        public HoursRemainingViewModel(Application application) {
            breakHoursRemaining = new MutableLiveData<>();
            drivingHoursRemaining = new MutableLiveData<>();
            dayResetHoursRemaining = new MutableLiveData<>();

            this.validColor = application.getResources().getColor(R.color.status_valid, application.getTheme());
            this.warningColor = application.getResources().getColor(R.color.status_warning, getApplication().getTheme());
            this.dangerColor = application.getResources().getColor(R.color.status_danger, getApplication().getTheme());
            this.hourMinuteFormatString = application.getString(R.string.hour_minute_second);

            timer.breakHoursRemaining = breakHoursRemaining;
            timer.drivingHoursRemaining = drivingHoursRemaining;
            timer.dayResetHoursRemaining = dayResetHoursRemaining;

            // Again - bad but fine for the demo
            // 3/13/2025 - this may do absolutely nothing, delete?
            int breakHours = (int) (timer.getTimeLeft("break") / (1000 * 60 * 60));
            int breakMinutes = (int) ((timer.getTimeLeft("break") % (1000 * 60 * 60)) / (1000 * 60));
            int breakSeconds = (int) ((timer.getTimeLeft("break") % (1000 * 60)) / (1000));
            int drivingHours = (int) (timer.getTimeLeft("driving") / (1000 * 60 * 60));
            int drivingMinutes = (int) ((timer.getTimeLeft("driving") % (1000 * 60 * 60)) / (1000 * 60));
            int drivingSeconds = (int) ((timer.getTimeLeft("driving") % (1000 * 60)) / (1000));

            breakHoursRemaining.setValue(new HoursRemainingContainer(LocalTime.of(2, 0, 0), LocalTime.of(breakHours, breakMinutes, breakSeconds)));
            drivingHoursRemaining.setValue(new HoursRemainingContainer(LocalTime.of(8, 0, 0), LocalTime.of(drivingHours, drivingMinutes, drivingSeconds)));
            dayResetHoursRemaining.setValue(new HoursRemainingContainer(LocalTime.of(23, 0, 0), LocalTime.of(9, 22, 1)));
        }

        public MutableLiveData<HoursRemainingContainer> getBreakHoursRemaining() {
            return breakHoursRemaining;
        }

        public MutableLiveData<HoursRemainingContainer> getDrivingHoursRemaining() {
            return drivingHoursRemaining;
        }

        public MutableLiveData<HoursRemainingContainer> getDayResetHoursRemaining() {
            return dayResetHoursRemaining;
        }

        public Observer<HoursRemainingContainer> createObserver(
                CircularProgressIndicator indicator,
                TextView remainingTimeText
        ) {
            return hoursRemainingContainer -> {
                int remainingMinutes = hoursRemainingContainer.remaining.getHour() * 60 + hoursRemainingContainer.remaining.getMinute();
                int limitMinutes = hoursRemainingContainer.limit.getHour() * 60 + hoursRemainingContainer.limit.getMinute();
                remainingTimeText.setText(
                        String.format(
                                hourMinuteFormatString,
                                hoursRemainingContainer.remaining.getHour(),
                                hoursRemainingContainer.remaining.getMinute(),
                                hoursRemainingContainer.remaining.getSecond())
                );
                indicator.setMax(limitMinutes);
                indicator.setProgress(remainingMinutes);

                double percentageRemaining = (double) remainingMinutes / limitMinutes;
                if (percentageRemaining > 0.25) {
                    indicator.setIndicatorColor(validColor);
                } else if (percentageRemaining > 0.125) {
                    indicator.setIndicatorColor(warningColor);
                } else {
                    indicator.setIndicatorColor(dangerColor);
                }
            };
        }

        public static class HoursRemainingContainer {
            public LocalTime limit;
            public LocalTime remaining;

            public HoursRemainingContainer(LocalTime limit, LocalTime remaining) {
                this.limit = limit;
                this.remaining = remaining;
            }
        }
    }

    public class LogsViewModel extends ViewModel {
        protected final MutableLiveData<List<Date>> mDates;

        public LogsViewModel() {
            mDates = new MutableLiveData<>();
            List<Date> dates = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                dates.add(java.sql.Date.valueOf(String.valueOf(LocalDate.now().minusDays(i))));
            }
            mDates.setValue(dates);
        }
    }
}