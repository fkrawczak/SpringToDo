package org.example.firstapi.api.controllers;

import jakarta.validation.Valid;
import org.example.firstapi.api.contracts.request.LoginUserRequest;
import org.example.firstapi.api.contracts.response.LoginUserResponse;
import org.example.firstapi.application.security.AuthTokens;
import org.example.firstapi.application.usecase.loginuser.LoginUserCommand;
import org.example.firstapi.application.usecase.loginuser.LoginUserHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final LoginUserHandler loginUserHandler;

    public AuthController(LoginUserHandler loginUserHandler) {
        this.loginUserHandler = loginUserHandler;
    }

    @PostMapping("/login")
    public LoginUserResponse login(@Valid @RequestBody LoginUserRequest request) {
        LoginUserCommand command = new LoginUserCommand(request.email(), request.password());
        AuthTokens tokens = loginUserHandler.handle(command);

        return new LoginUserResponse(
                tokens.accessToken(),
                tokens.refreshToken()
        );
    }
}
