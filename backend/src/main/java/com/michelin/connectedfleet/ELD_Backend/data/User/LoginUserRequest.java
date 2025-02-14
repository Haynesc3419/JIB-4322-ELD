package com.michelin.connectedfleet.ELD_Backend.data.User;

import jakarta.validation.constraints.NotBlank;

public record LoginUserRequest(@NotBlank String username, @NotBlank String password) {
}

