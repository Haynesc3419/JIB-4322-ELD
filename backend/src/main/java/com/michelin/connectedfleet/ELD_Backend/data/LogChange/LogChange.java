package com.michelin.connectedfleet.ELD_Backend.data.LogChange;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document("log_changes")
public class LogChange {
    String body;

    public LogChange(String body) {
        this.body = body;
    }
}
