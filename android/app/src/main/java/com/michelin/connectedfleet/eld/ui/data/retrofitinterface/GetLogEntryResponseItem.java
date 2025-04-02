package com.michelin.connectedfleet.eld.ui.data.retrofitinterface;

import java.time.LocalDateTime;

public record GetLogEntryResponseItem(String id, String status, LocalDateTime dateTime, String verifiedByDriver) {
}
