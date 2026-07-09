package org.example.firstapi.api.contracts.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterUserRequest(
        @NotBlank @Email String email,
        @NotBlank @JsonAlias({"firstname", "first_name"}) String firstName,
        @NotBlank @JsonAlias({"lastname", "last_name"}) String lastName,
        @NotBlank String password
) {
}
