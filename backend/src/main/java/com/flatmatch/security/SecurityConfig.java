package com.flatmatch.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security rules:
 *   - public: /api/auth/**  (register & login) and Swagger docs
 *   - everything else needs a valid JWT (the filter above sets it up)
 *   - stateless: no server sessions, no CSRF needed for token APIs
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                          JwtAuthenticationFilter jwtFilter) throws Exception {
        http
                // .cors() uses the CorsConfigurationSource bean from WebConfig and answers
                // the browser's preflight (OPTIONS) BEFORE authentication rejects it
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/health", "/swagger-ui/**", "/swagger-ui.html",
                                "/v3/api-docs/**", "/ws/**")
                        .permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** BCrypt: passwords stored as one-way hashes, never in plain text. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * No UserDetailsService on purpose: auth is stateless JWT.
     * Declaring this empty bean stops Spring Security from creating a default
     * in-memory user with a random password on every startup.
     */
    @Bean
    public UserDetailsService noOpUserDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("Token-based auth only — no user lookup");
        };
    }
}
