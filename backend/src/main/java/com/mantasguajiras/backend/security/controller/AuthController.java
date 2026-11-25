package com.mantasguajiras.backend.security.controller;

import com.mantasguajiras.backend.security.service.ClientAddress;
import com.mantasguajiras.backend.security.service.LoginAttemptService;
import com.mantasguajiras.backend.security.service.RegistrationLimitService;
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

import jakarta.servlet.http.HttpServletRequest;
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
        private final RegistrationLimitService registrationLimitService;

        @PostMapping("/login")
        public ResponseEntity<LoginResponse> login(
                        @Valid @RequestBody LoginRequest request,
                        HttpServletRequest http) {

                String identifier = request.getIdentifier();

                String address = ClientAddress.of(http);

                // Bloqueo temporal tras varios intentos fallidos (fuerza bruta).
                loginAttemptService.checkAllowed(identifier, address);

                Authentication authentication;

                try {
                        authentication = authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(
                                                        identifier,
                                                        request.getPassword()));
                } catch (BadCredentialsException wrongPassword) {
                        loginAttemptService.recordFailure(identifier, address);
                        throw wrongPassword;
                }

                loginAttemptService.recordSuccess(identifier, address);

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
                        @Valid @RequestBody RegisterRequest request,
                        HttpServletRequest http) {

                // Límite de cuentas nuevas (registros automáticos).
                registrationLimitService.checkAndRecord(ClientAddress.of(http));

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