package com.michelin.connectedfleet.ELD_Backend.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.web.bind.annotation.RequestBody;

@Document("users")
public record User(@Id String username, String passwordHash, String firstName, String lastName) {
}
