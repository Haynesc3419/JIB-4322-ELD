package com.michelin.connectedfleet.ELD_Backend.data.User;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(@NotBlank String username, @NotBlank String password, @NotBlank String firstName, @NotBlank String lastName) {
}
