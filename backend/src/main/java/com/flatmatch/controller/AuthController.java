package com.flatmatch.controller;

import com.flatmatch.dto.AuthResponse;
import com.flatmatch.dto.LoginRequest;
import com.flatmatch.dto.RegisterRequest;
import com.flatmatch.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Public auth endpoints: /api/auth/register, /api/auth/login, /api/auth/me. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /** Who am I? (needs the JWT) — the frontend calls this on every page load. */
    @GetMapping("/me")
    public AuthResponse me(@AuthenticationPrincipal Long userId) {
        return authService.me(userId);
    }
}
