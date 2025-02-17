package com.michelin.connectedfleet.eld.ui.data.retrofitinterface;

import java.time.LocalDateTime;

public record GetLogEntryResponseItem(LocalDateTime dateTime, String status) {
}
