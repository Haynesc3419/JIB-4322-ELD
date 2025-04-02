package com.michelin.connectedfleet.eld.ui.data;

import java.time.LocalDateTime;

public record LogEntry(
    String status,
    LocalDateTime dateTime,
    double distanceMiles,  // Distance in miles (stored in imperial)
    double speedMph        // Speed in mph (stored in imperial)
) {}
