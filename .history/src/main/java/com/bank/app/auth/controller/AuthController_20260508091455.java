package com.bank.app.auth.controller;

import com.bank.app.auth.dto.LoginRequest;
import com.bank.app.auth.dto.RefreshTokenRequest;
import com.bank.app.auth.dto.RegisterRequest;
import com.bank.app.auth.dto.TokenResponse;
import com.bank.app.auth.service.AuthService;
import com.bank.app.auth.service.TokenRevocationService;
import com.bank.app.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenRevocationService tokenRevocationService;

    public AuthController(AuthService authService, TokenRevocationService tokenRevocationService) {
        this.authService = authService;
        this.tokenRevocationService = tokenRevocationService;
    }

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.success("User registered successfully");
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("Login successful", authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success("Token refreshed", authService.refresh(request));
    }

    @PostMapping("/revoke")
    public ApiResponse<Void> revoke(@Valid @RequestBody RefreshTokenRequest request) {
        authService.revoke(request.refreshToken());
        return ApiResponse.success("Token revoked");
    }

    @PostMapping("/revoke-access")
    public ApiResponse<Void> revokeAccessToken(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ApiResponse.failure("No access token provided");
        }
        String token = authorization.substring(7);
        // Best effort: attempt to parse expiry from token
        try {
            java.time.Instant exp = java.time.Instant.ofEpochSecond(0);
            // We don't parse here deeply; store with short expiry to avoid long-lived revocation entries
            tokenRevocationService.revokeAccessToken(token, java.time.LocalDateTime.now().plusMinutes(60));
        } catch (Exception ex) {
            tokenRevocationService.revokeAccessToken(token, java.time.LocalDateTime.now().plusMinutes(60));
        }
        return ApiResponse.success("Access token revoked");
    }
}
