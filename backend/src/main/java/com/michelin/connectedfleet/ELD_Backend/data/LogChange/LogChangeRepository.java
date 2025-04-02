package com.michelin.connectedfleet.ELD_Backend.data.LogChange;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface LogChangeRepository extends MongoRepository<LogChange, String> {
}
