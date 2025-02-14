package com.michelin.connectedfleet.ELD_Backend.data.LogEntry;

import java.time.LocalDateTime;

public class GetLogEntryResponseItem {
    public String username;
    public LocalDateTime dateTime;
    public String status;

    public GetLogEntryResponseItem(LogEntry logEntry) {
        this.dateTime = logEntry.getDateTime();
        this.status = logEntry.getStatus();
    }

}
