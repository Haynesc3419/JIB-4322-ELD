package com.michelin.connectedfleet.eld.ui.data;

import com.michelin.connectedfleet.eld.ui.data.retrofitinterface.GetLogEntryResponseItem;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class LogEntry {
    public LocalDateTime dateTime;
    public String status;
    public ZoneId timeZone;

    public LogEntry(GetLogEntryResponseItem item) {
        this.dateTime = item.dateTime();
        this.status = item.status();
        this.timeZone = ZoneId.systemDefault();
    }

    public LogEntry(GetLogEntryResponseItem item, ZoneId timeZone) {
        this.dateTime = item.dateTime();
        this.status = item.status();
        this.timeZone = timeZone;
    }
}
