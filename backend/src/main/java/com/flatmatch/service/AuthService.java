package com.flatmatch.service;

import com.flatmatch.dto.AuthResponse;
import com.flatmatch.dto.LoginRequest;
import com.flatmatch.dto.RegisterRequest;
import com.flatmatch.exception.ResourceNotFoundException;
import com.flatmatch.model.AuthUser;
import com.flatmatch.model.UserProfile;
import com.flatmatch.repository.AuthUserRepository;
import com.flatmatch.repository.UserProfileRepository;
import com.flatmatch.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthUserRepository users;
    private final UserProfileRepository profiles;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthUserRepository users,
                       UserProfileRepository profiles,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder) {
        this.users = users;
        this.profiles = profiles;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (users.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException("Email is already registered");
        }
        AuthUser user = new AuthUser();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user = users.save(user);
        return new AuthResponse(jwtService.generate(user.getId()), user.getId(), user.getName(), null);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        AuthUser user = users.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        return new AuthResponse(jwtService.generate(user.getId()), user.getId(), user.getName(), profileIdOf(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse me(Long userId) {
        AuthUser user = users.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User " + userId + " not found"));
        return new AuthResponse(null, user.getId(), user.getName(), profileIdOf(user));
    }

    private Long profileIdOf(AuthUser user) {
        return profiles.findByAuthUserId(user.getId()).map(UserProfile::getId).orElse(null);
    }
}
