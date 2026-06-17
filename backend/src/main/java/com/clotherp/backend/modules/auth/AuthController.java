package com.clotherp.backend.modules.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clotherp.backend.common.ApiResponse;
import com.clotherp.backend.modules.user.UserDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// modules/auth/AuthController.java
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(
            ApiResponse.ok(authService.login(request), "Login successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(
            ApiResponse.ok(authService.refreshToken(request.getRefreshToken())));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(
            Authentication authentication) {
        return ResponseEntity.ok(
            ApiResponse.ok(authService.getCurrentUser(authentication.getName())));
    }
}
