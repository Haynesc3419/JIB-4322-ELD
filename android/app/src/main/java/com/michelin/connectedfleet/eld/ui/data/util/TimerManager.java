package com.michelin.connectedfleet.eld.ui.data.util;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.michelin.connectedfleet.eld.ui.home.HomeViewModel;
import com.michelin.connectedfleet.eld.ui.data.NotificationHelper;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class TimerManager {
    private static TimerManager instance;
    private final Map<String, TimerData> timers = new HashMap<>();
    private Context applicationContext;
    private static final long CRITICAL_THRESHOLD = 30 * 60 * 1000;  // 30 minutes
    private static final long WARNING_THRESHOLD = 60 * 60 * 1000;   // 1 hour
    private static final long NOTICE_THRESHOLD = 120 * 60 * 1000;   // 2 hours
    private static final String TAG = "TimerManager";
    private Map<String, Boolean> notificationSent = new HashMap<>();

    public MutableLiveData<HomeViewModel.HoursRemainingViewModel.HoursRemainingContainer> breakHoursRemaining;
    public MutableLiveData<HomeViewModel.HoursRemainingViewModel.HoursRemainingContainer> drivingHoursRemaining;
    public MutableLiveData<HomeViewModel.HoursRemainingViewModel.HoursRemainingContainer> dayResetHoursRemaining;

    private TimerManager() {
        notificationSent.put("critical", false);
        notificationSent.put("warning", false);
        notificationSent.put("notice", false);
    }

    public static TimerManager getInstance() {
        if (instance == null) {
            instance = new TimerManager();
        }
        return instance;
    }

    public void setContext(Context context) {
        this.applicationContext = context.getApplicationContext();
        Log.d(TAG, "Context set successfully");
    }

    private void checkAndSendNotification(long millisUntilFinished) {
        if (applicationContext == null) {
            Log.w(TAG, "Cannot send notification: applicationContext is null");
            return;
        }

        // Only check for notifications if we're in driving mode
        if (!isTimerRunning("driving")) {
            Log.d(TAG, "Cannot send notification: driving timer is not running");
            return;
        }

        String remainingTimeStr = String.format("%d minutes", millisUntilFinished / (60 * 1000));
        Log.d(TAG, "Checking notification thresholds for " + remainingTimeStr + " remaining");

        if (millisUntilFinished <= CRITICAL_THRESHOLD && !notificationSent.get("critical")) {
            Log.w(TAG, "Sending CRITICAL notification: " + remainingTimeStr + " remaining");
            NotificationHelper.showDrivingLimitNotification(applicationContext, remainingTimeStr, true);
            notificationSent.put("critical", true);
        } else if (millisUntilFinished <= WARNING_THRESHOLD && !notificationSent.get("warning")) {
            Log.w(TAG, "Sending WARNING notification: " + remainingTimeStr + " remaining");
            NotificationHelper.showDrivingLimitNotification(applicationContext, remainingTimeStr, false);
            notificationSent.put("warning", true);
        } else if (millisUntilFinished <= NOTICE_THRESHOLD && !notificationSent.get("notice")) {
            Log.i(TAG, "Sending NOTICE notification: " + remainingTimeStr + " remaining");
            NotificationHelper.showDrivingLimitNotification(applicationContext, remainingTimeStr, false);
            notificationSent.put("notice", true);
        }
    }

    private void resetNotificationFlags() {
        notificationSent.clear();
        notificationSent.put("critical", false);
        notificationSent.put("warning", false);
        notificationSent.put("notice", false);
        Log.d(TAG, "Notification flags reset");
    }

    public void startTimer(String timerId, long durationMillis) {
        if (timers.containsKey(timerId) && timers.get(timerId).isRunning) return; // Prevent duplicate running timers

        TimerData timerData = timers.getOrDefault(timerId, new TimerData(durationMillis));
        CountDownTimer countDownTimer = createCountDownTimer(timerId, timerData.timeLeft);
        timerData.countDownTimer = countDownTimer;
        timerData.isRunning = true;
        timers.put(timerId, timerData);

        countDownTimer.start();
    }

    public void pauseTimer(String timerId) {
        if (timers.containsKey(timerId) && timers.get(timerId).isRunning) {
            timers.get(timerId).countDownTimer.cancel();
            timers.get(timerId).isRunning = false;
        }
    }

    public void resumeTimer(String timerId) {
        if (timers.containsKey(timerId) && !timers.get(timerId).isRunning) {
            TimerData timerData = timers.get(timerId);
            CountDownTimer countDownTimer = createCountDownTimer(timerId, timerData.timeLeft);
            timerData.countDownTimer = countDownTimer;
            timerData.isRunning = true;
            timers.put(timerId, timerData);
            countDownTimer.start();
        }
    }

    public void stopTimer(String timerId) {
        if (timers.containsKey(timerId)) {
            timers.get(timerId).countDownTimer.cancel();
            timers.remove(timerId);
        }
    }

    public long getTimeLeft(String timerId) {
        return timers.containsKey(timerId) ? timers.get(timerId).timeLeft : 0;
    }

    public boolean isTimerRunning(String timerId) {
        return timers.containsKey(timerId) && timers.get(timerId).isRunning;
    }

    private CountDownTimer createCountDownTimer(String timerId, long durationInMillis) {
        return new CountDownTimer(durationInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Update the remaining time in the map
                timers.get(timerId).timeLeft = millisUntilFinished;

                // Check for notifications on every tick if this is the driving timer
                if (timerId.equals("driving") && applicationContext != null) {
                    checkAndSendNotification(millisUntilFinished);
                }

                // Update UI with remaining time
                int breakHours = (int) (getTimeLeft("break") / (1000 * 60 * 60));
                int breakMinutes = (int) ((getTimeLeft("break") % (1000 * 60 * 60)) / (1000 * 60));
                int breakSeconds = (int) ((getTimeLeft("break") % (1000 * 60)) / (1000));
                int drivingHours = (int) (getTimeLeft("driving") / (1000 * 60 * 60));
                int drivingMinutes = (int) ((getTimeLeft("driving") % (1000 * 60 * 60)) / (1000 * 60));
                int drivingSeconds = (int) ((getTimeLeft("driving") % (1000 * 60)) / (1000));
                int dayResetHours = (int) (getTimeLeft("dayReset") / (1000 * 60 * 60));
                int dayResetMinutes = (int) ((getTimeLeft("dayReset") % (1000 * 60 * 60)) / (1000 * 60));
                int dayResetSeconds = (int) ((getTimeLeft("dayReset") % (1000 * 60)) / (1000));
                breakHoursRemaining.setValue(new HomeViewModel.HoursRemainingViewModel.HoursRemainingContainer(LocalTime.of(2, 0, 0), LocalTime.of(breakHours, breakMinutes, breakSeconds)));
                drivingHoursRemaining.setValue(new HomeViewModel.HoursRemainingViewModel.HoursRemainingContainer(LocalTime.of(8, 0, 0), LocalTime.of(drivingHours, drivingMinutes, drivingSeconds)));
                dayResetHoursRemaining.setValue(new HomeViewModel.HoursRemainingViewModel.HoursRemainingContainer(LocalTime.of(23, 0, 0), LocalTime.of(dayResetHours, dayResetMinutes, dayResetSeconds)));
            }

            @Override
            public void onFinish() {
                timers.remove(timerId);
                if (timerId.equals("driving")) {
                    resetNotificationFlags();
                    if (applicationContext != null) {
                        NotificationHelper.cancelDrivingLimitNotification(applicationContext);
                        Log.d(TAG, "Driving timer finished, notifications cancelled");
                    }
                }
            }
        };
    }

    private static class TimerData {
        CountDownTimer countDownTimer;
        long timeLeft;
        boolean isRunning;

        TimerData(long timeLeft) {
            this.timeLeft = timeLeft;
            this.isRunning = false;
        }
    }
}
