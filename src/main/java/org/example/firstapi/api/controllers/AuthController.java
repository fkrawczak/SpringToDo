package org.example.firstapi.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.firstapi.api.contracts.request.LoginUserRequest;
import org.example.firstapi.api.contracts.response.LoginUserResponse;
import org.example.firstapi.application.usecase.loginuser.AuthTokens;
import org.example.firstapi.application.usecase.loginuser.LoginUserCommand;
import org.example.firstapi.application.usecase.loginuser.LoginUserHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Auth ")
public class AuthController {

    private final LoginUserHandler loginUserHandler;

    public AuthController(LoginUserHandler loginUserHandler) {
        this.loginUserHandler = loginUserHandler;
    }

    @PostMapping("/login")
    @Operation(summary = "Log in and issue auth tokens")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentication succeeded"),
            @ApiResponse(responseCode = "400", description = "Request body validation failed"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public LoginUserResponse login(@Valid @RequestBody LoginUserRequest request) {
        LoginUserCommand command = new LoginUserCommand(request.email(), request.password());
        AuthTokens tokens = loginUserHandler.handle(command);

        return new LoginUserResponse(
                tokens.accessToken(),
                tokens.refreshToken()
        );
    }
}
