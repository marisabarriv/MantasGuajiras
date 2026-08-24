package com.mantasguajiras.backend.security.controller;

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

        @PostMapping("/login")
        public ResponseEntity<LoginResponse> login(
                        @Valid @RequestBody LoginRequest request) {

                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.getIdentifier(),
                                                request.getPassword()));

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