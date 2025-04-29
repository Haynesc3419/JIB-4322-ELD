package com.michelin.connectedfleet.eld.ui.vehicleinfo;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class VehicleInfoViewModel extends AndroidViewModel {

    private final MutableLiveData<Vehicle> mVehicle;
    private final Application app;

    public VehicleInfoViewModel(Application app) {
        super(app);
        this.app = app;
        mVehicle = new MutableLiveData<>();
        SharedPreferences vehicleData =
                app.getSharedPreferences("vehicle_data", Context.MODE_PRIVATE);
        float odometer = vehicleData.getFloat("odometer_reading", 0f);

        mVehicle.setValue(new MockVehicle(odometer));
    }

    public void storeVehicleData() {
        SharedPreferences vehicleData =
                app.getSharedPreferences("vehicle_data", Context.MODE_PRIVATE);
        vehicleData.edit().putFloat("odometer_reading", mVehicle.getValue().getOdometer()).apply();
    }

    public LiveData<Vehicle> getVehicle() {
        return mVehicle;
    }
}
