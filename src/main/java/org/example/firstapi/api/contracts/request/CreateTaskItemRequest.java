package org.example.firstapi.api.contracts.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record CreateTaskItemRequest(
        @NotBlank String title,
        String description,
        @NotNull OffsetDateTime deadline
) {
}
