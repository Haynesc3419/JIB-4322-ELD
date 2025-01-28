package com.michelin.connectedfleet.ELD_Backend.data;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;

public record CreateUserRequest(@NotBlank String username, @NotBlank String password, @NotBlank String firstName, @NotBlank String lastName) {
}
