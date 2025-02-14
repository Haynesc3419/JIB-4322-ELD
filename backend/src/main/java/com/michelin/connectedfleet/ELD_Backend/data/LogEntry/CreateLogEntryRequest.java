package com.michelin.connectedfleet.ELD_Backend.data.LogEntry;

import jakarta.validation.constraints.NotBlank;

public record CreateLogEntryRequest(@NotBlank String status, String token) {
}
