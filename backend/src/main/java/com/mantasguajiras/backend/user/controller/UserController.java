package com.mantasguajiras.backend.user.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mantasguajiras.backend.user.dto.requests.ChangePasswordRequest;
import com.mantasguajiras.backend.user.dto.requests.UpdateUserRequest;
import com.mantasguajiras.backend.user.dto.requests.UserRequest;
import com.mantasguajiras.backend.user.dto.response.UserResponse;
import com.mantasguajiras.backend.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

        private final UserService userService;

        @PostMapping
        public ResponseEntity<UserResponse> create(
                        @Valid @RequestBody UserRequest request) {

                UserResponse response = userService.create(request);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(response);
        }

        @GetMapping("/{id}")
        public ResponseEntity<UserResponse> findById(
                        @PathVariable UUID id) {

                return ResponseEntity.ok(
                                userService.findById(id));
        }

        @PutMapping("/{id}")
        public ResponseEntity<UserResponse> update(
                        @PathVariable UUID id,
                        @Valid @RequestBody UpdateUserRequest request) {

                return ResponseEntity.ok(
                                userService.update(id, request));
        }

        @PatchMapping("/{id}/password")
        public ResponseEntity<Void> changePassword(
                        @PathVariable UUID id,
                        @Valid @RequestBody ChangePasswordRequest request) {

                userService.changePassword(id, request);

                return ResponseEntity.noContent().build();
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> delete(
                        @PathVariable UUID id) {

                userService.delete(id);

                return ResponseEntity.noContent().build();
        }

        @GetMapping
        public ResponseEntity<List<UserResponse>> findAll() {

                return ResponseEntity.ok(
                                userService.findAll());
        }

        @PatchMapping("/{id}/promote")
        public ResponseEntity<UserResponse> promoteToAdmin(
                        @PathVariable UUID id) {

                return ResponseEntity.ok(
                                userService.promoteToAdmin(id));
        }
}