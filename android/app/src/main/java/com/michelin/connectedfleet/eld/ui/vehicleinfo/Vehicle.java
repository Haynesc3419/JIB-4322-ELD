package com.michelin.connectedfleet.eld.ui.vehicleinfo;

public interface Vehicle {
    String getShortIdentifier();
    /**
     * Get vehicle speed (in km/h)
     * @return current vehicle speed (km/h)
     */
    float getSpeed();
    float getOdometer();
}
