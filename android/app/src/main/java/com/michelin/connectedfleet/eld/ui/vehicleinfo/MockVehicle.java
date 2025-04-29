package com.michelin.connectedfleet.eld.ui.vehicleinfo;

import android.os.Handler;

import java.util.Timer;
import java.util.TimerTask;

public class MockVehicle implements Vehicle {
    private static Timer odometerTimer;
    private float odometer;

    private Timer timer;
    private TimerTask timerTask;
    Handler handler = new Handler();
    Runnable timerTrigger;

    public MockVehicle(float odometerReading) {
        this.odometer = odometerReading;

        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                odometer += getSpeed() / 3600.0f;
                if (timerTrigger != null) {
                    handler.post(timerTrigger);
                }
            }
        };

        timer.schedule(timerTask, 1000, 1000);
    }

    public void registerTimerTrigger(Runnable timerTrigger) {
        this.timerTrigger = timerTrigger;
    }

    @Override
    public String getShortIdentifier() {
        return "2020 Sample Truck";
    }

    public float getSpeed() {
        return 100.0f;
    }

    /**
     * Return odometer in km
     * @return odometer reading in km
     */
    @Override
    public float getOdometer() {
        return odometer;
    }
}
