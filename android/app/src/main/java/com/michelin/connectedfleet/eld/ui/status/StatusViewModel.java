package com.michelin.connectedfleet.eld.ui.status;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.michelin.connectedfleet.eld.DriverStatus;

public class StatusViewModel extends ViewModel {

    private static final MutableLiveData<DriverStatus> mStatus = new MutableLiveData<>(DriverStatus.DRIVING);

    public static LiveData<DriverStatus> getStatus() {
        return mStatus;
    }

    public static void setStatus(DriverStatus status) {
        mStatus.setValue(status);
    }
}
