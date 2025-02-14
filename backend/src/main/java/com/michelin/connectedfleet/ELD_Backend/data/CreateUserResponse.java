package com.michelin.connectedfleet.ELD_Backend.data;

public class CreateUserResponse {
    String username;
    String token;

    public CreateUserResponse(String username, String token) {
        this.username = username;
        this.token = token;
    }
}
