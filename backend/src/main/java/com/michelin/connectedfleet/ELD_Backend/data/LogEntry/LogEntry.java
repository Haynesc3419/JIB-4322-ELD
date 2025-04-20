package com.michelin.connectedfleet.ELD_Backend.data.LogEntry;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;

@Document("log_entries")
public class LogEntry {
    @MongoId
    private String id;
    private String username;
    private LocalDateTime dateTime;
    private String status;

    private String verifiedByDriver;

    public LogEntry(String username, String status, String verifiedByDriver) {
        this.username = username;
        this.dateTime = LocalDateTime.now();
        this.status = status;
        this.verifiedByDriver = verifiedByDriver;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getStatus() {
        return status;
    }

    public String getId() {
        return id;
    }

    public String getVerifiedByDriver() {
        return verifiedByDriver;
    }

    public void setVerifiedByDriver(String verifiedByDriver) {
        this.verifiedByDriver = verifiedByDriver;
    }

}
