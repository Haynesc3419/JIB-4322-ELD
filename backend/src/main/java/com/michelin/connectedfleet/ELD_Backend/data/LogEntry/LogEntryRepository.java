package com.michelin.connectedfleet.ELD_Backend.data.LogEntry;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogEntryRepository extends MongoRepository<LogEntry, String> {
    public List<LogEntry> findAllByUsername(String username);
}
