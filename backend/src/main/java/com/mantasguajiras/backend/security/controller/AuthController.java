package com.mantasguajiras.backend.security.controller;

import com.mantasguajiras.backend.common.exception.TooManyLoginAttemptsException;
import com.mantasguajiras.backend.security.service.LoginAttemptService;
import org.springframework.security.authentication.BadCredentialsException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.mantasguajiras.backend.security.dto.requests.RegisterRequest;
import com.mantasguajiras.backend.user.service.UserService;
import com.mantasguajiras.backend.security.dto.requests.LoginRequest;
import com.mantasguajiras.backend.security.dto.response.LoginResponse;
import com.mantasguajiras.backend.security.jwt.JwtTokenProvider;
import com.mantasguajiras.backend.user.dto.response.UserResponse;
import com.mantasguajiras.backend.user.entity.User;
import com.mantasguajiras.backend.user.repository.UserRepository;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

        private final AuthenticationManager authenticationManager;
        private final JwtTokenProvider jwtTokenProvider;
        private final UserRepository userRepository;
        private final UserService userService;
        private final LoginAttemptService loginAttemptService;

        @PostMapping("/login")
        public ResponseEntity<LoginResponse> login(
                        @Valid @RequestBody LoginRequest request) {

                String identifier = request.getIdentifier();

                // Bloqueo temporal tras varios intentos fallidos (fuerza bruta).
                loginAttemptService.remainingLock(identifier).ifPresent(remaining -> {
                        throw new TooManyLoginAttemptsException(remaining);
                });

                Authentication authentication;

                try {
                        authentication = authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(
                                                        identifier,
                                                        request.getPassword()));
                } catch (BadCredentialsException wrongPassword) {
                        var lock = loginAttemptService.recordFailure(identifier);
                        if (lock.isPresent()) {
                                throw new TooManyLoginAttemptsException(lock.get());
                        }
                        throw wrongPassword;
                }

                loginAttemptService.recordSuccess(identifier);

                String username = authentication.getName();

                User user = userRepository.findByUsername(username)
                                .orElseThrow();

                String token = jwtTokenProvider.generateToken(user);

                LoginResponse response = LoginResponse.builder()
                                .token(token)
                                .userId(user.getId())
                                .username(user.getUsername())
                                .phone(user.getPhone())
                                .role(user.getRole())
                                .build();

                return ResponseEntity.ok(response);
        }

        @PostMapping("/register")
        public ResponseEntity<LoginResponse> register(
                        @Valid @RequestBody RegisterRequest request) {

                UserResponse user = userService.register(request);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(
                                                LoginResponse.builder()
                                                                .userId(user.getId())
                                                                .username(user.getUsername())
                                                                .phone(user.getPhone())
                                                                .role(user.getRole())
                                                                .build());
        }

}