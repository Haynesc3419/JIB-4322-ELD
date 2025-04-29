package com.michelin.connectedfleet.eld.ui.data.retrofitinterface;

public record CreateLogEntryRequest(String status, float odometerReading, String token) {
}

