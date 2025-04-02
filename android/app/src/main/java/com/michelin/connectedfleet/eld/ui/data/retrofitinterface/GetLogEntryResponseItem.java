package com.michelin.connectedfleet.eld.ui.data.retrofitinterface;

import java.time.LocalDateTime;

public record GetLogEntryResponseItem(
    String status,
    LocalDateTime dateTime,
    double distanceMiles,  // Distance in miles (stored in imperial)
    double speedMph        // Speed in mph (stored in imperial)
) {}
