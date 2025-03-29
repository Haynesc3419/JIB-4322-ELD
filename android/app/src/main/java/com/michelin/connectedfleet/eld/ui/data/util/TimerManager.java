package com.michelin.connectedfleet.eld.ui.data.util;

import android.os.CountDownTimer;

import androidx.lifecycle.MutableLiveData;

import com.michelin.connectedfleet.eld.ui.home.HomeViewModel;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class TimerManager {
    private static TimerManager instance;
    private final Map<String, TimerData> timers = new HashMap<>();

    public MutableLiveData<HomeViewModel. HoursRemainingViewModel. HoursRemainingContainer> breakHoursRemaining;

    public MutableLiveData<HomeViewModel. HoursRemainingViewModel. HoursRemainingContainer> drivingHoursRemaining;

    public MutableLiveData<HomeViewModel. HoursRemainingViewModel. HoursRemainingContainer> dayResetHoursRemaining;

    private TimerManager() {}

    public static TimerManager getInstance() {
        if (instance == null) {
            instance = new TimerManager();
        }
        return instance;
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

    private CountDownTimer createCountDownTimer(String timerId, long durationMillis) {
        return new CountDownTimer(durationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int breakHours = (int) (instance.getTimeLeft("break") / (1000 * 60 * 60));
                int breakMinutes = (int) ((instance.getTimeLeft("break") % (1000 * 60 * 60)) / (1000 * 60));
                int breakSeconds = (int) ((instance.getTimeLeft("break") % (1000 * 60)) / (1000));
                int drivingHours = (int) (instance.getTimeLeft("driving") / (1000 * 60 * 60));
                int drivingMinutes = (int) ((instance.getTimeLeft("driving") % (1000 * 60 * 60)) / (1000 * 60));
                int drivingSeconds = (int) ((instance.getTimeLeft("driving") % (1000 * 60)) / (1000));
                int dayResetHours = (int) (instance.getTimeLeft("dayReset") / (1000 * 60 * 60));
                int dayResetMinutes = (int) ((instance.getTimeLeft("dayReset") % (1000 * 60 * 60)) / (1000 * 60));
                int dayResetSeconds = (int) ((instance.getTimeLeft("dayReset") % (1000 * 60)) / (1000));
                breakHoursRemaining.setValue(new HomeViewModel.HoursRemainingViewModel.HoursRemainingContainer(LocalTime.of(2, 0, 0), LocalTime.of(breakHours, breakMinutes, breakSeconds)));
                drivingHoursRemaining.setValue(new HomeViewModel.HoursRemainingViewModel.HoursRemainingContainer(LocalTime.of(8, 0, 0), LocalTime.of(drivingHours, drivingMinutes, drivingSeconds)));
                dayResetHoursRemaining.setValue(new HomeViewModel.HoursRemainingViewModel.HoursRemainingContainer(LocalTime.of(23, 0, 0), LocalTime.of(dayResetHours, dayResetMinutes, dayResetSeconds)));
                if (timers.containsKey(timerId)) {
                    timers.get(timerId).timeLeft = millisUntilFinished;
                }
            }

            @Override
            public void onFinish() {
                timers.remove(timerId);
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
