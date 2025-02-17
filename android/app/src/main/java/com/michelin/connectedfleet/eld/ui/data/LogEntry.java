package com.michelin.connectedfleet.eld.ui.data;

import com.michelin.connectedfleet.eld.ui.data.retrofitinterface.GetLogEntryResponseItem;

import java.time.LocalDateTime;

public class LogEntry {
    public LocalDateTime dateTime;
    public String status;

    public LogEntry(GetLogEntryResponseItem item) {
        this.dateTime = item.dateTime();
        this.status = item.status();
    }
}
