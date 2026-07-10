package org.example.firstapi.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.firstapi.api.contracts.request.RegisterUserRequest;
import org.example.firstapi.api.contracts.response.RegisterUserResponse;
import org.example.firstapi.application.usecase.registeruser.RegisterUserCommand;
import org.example.firstapi.application.usecase.registeruser.RegisterUserHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@Tag(name = "Auth ")
public class RegisterController {

    private final RegisterUserHandler registerUserHandler;

    public RegisterController(RegisterUserHandler registerUserHandler) {
        this.registerUserHandler = registerUserHandler;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered"),
            @ApiResponse(responseCode = "400", description = "Request body validation failed"),
            @ApiResponse(responseCode = "409", description = "Email is already taken")
    })
    public RegisterUserResponse register(@Valid @RequestBody RegisterUserRequest request) {
        RegisterUserCommand command = new RegisterUserCommand(
                request.email(),
                request.firstName(),
                request.lastName(),
                request.password()
        );
        UUID userId = registerUserHandler.handle(command);

        return new RegisterUserResponse(userId);
    }
}
