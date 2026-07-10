package org.example.firstapi.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.firstapi.api.contracts.request.LoginUserRequest;
import org.example.firstapi.api.contracts.response.LoginUserResponse;
import org.example.firstapi.api.contracts.response.RefreshAccessTokenResponse;
import org.example.firstapi.application.usecase.loginuser.AuthTokens;
import org.example.firstapi.application.usecase.loginuser.LoginUserCommand;
import org.example.firstapi.application.usecase.loginuser.LoginUserHandler;
import org.example.firstapi.application.usecase.refreshaccesstoken.RefreshAccessTokenCommand;
import org.example.firstapi.application.usecase.refreshaccesstoken.RefreshAccessTokenHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Auth ")
public class AuthController {

    static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private final LoginUserHandler loginUserHandler;
    private final RefreshAccessTokenHandler refreshAccessTokenHandler;
    private final long refreshTokenTtlSeconds;

    public AuthController(LoginUserHandler loginUserHandler, RefreshAccessTokenHandler refreshAccessTokenHandler,
                          @Value("${app.security.jwt.refresh-token-ttl-seconds}") long refreshTokenTtlSeconds) {
        this.loginUserHandler = loginUserHandler;
        this.refreshAccessTokenHandler = refreshAccessTokenHandler;
        this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
    }

    @PostMapping("/login")
    @Operation(summary = "Log in and issue auth tokens")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentication succeeded"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request body validation failed",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    public ResponseEntity<LoginUserResponse> login(@Valid @RequestBody LoginUserRequest request) {
        LoginUserCommand command = new LoginUserCommand(request.email(), request.password());
        AuthTokens tokens = loginUserHandler.handle(command);

        ResponseCookie refreshTokenCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, tokens.refreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api")
                .maxAge(refreshTokenTtlSeconds)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(new LoginUserResponse(tokens.accessToken()));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Issue a new access token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Access token issued"),
            @ApiResponse(
                    responseCode = "401",
                    description = "Refresh token is missing, invalid or expired",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    public RefreshAccessTokenResponse refresh(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken
    ) {
        RefreshAccessTokenCommand command = new RefreshAccessTokenCommand(refreshToken);

        return new RefreshAccessTokenResponse(refreshAccessTokenHandler.handle(command));
    }
}
