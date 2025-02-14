package com.michelin.connectedfleet.ELD_Backend.data;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;

public record LoginUserRequest(@NotBlank String username, @NotBlank String password) {
}

