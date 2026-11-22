package com.mantasguajiras.backend.user.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mantasguajiras.backend.common.exception.ResourceNotFoundException;
import com.mantasguajiras.backend.security.util.SecurityUtils;
import com.mantasguajiras.backend.user.dto.requests.ChangePasswordRequest;
import com.mantasguajiras.backend.user.dto.response.UserResponse;
import com.mantasguajiras.backend.user.entity.UserProfileImage;
import com.mantasguajiras.backend.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * "Mi perfil": cada usuario con sesión gestiona SUS propios datos.
 * Nunca recibe un id: siempre actúa sobre el usuario del token.
 */
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

        private final UserService userService;

        @GetMapping
        public UserResponse me() {
                return userService.getMe(currentUsername());
        }

        @PatchMapping("/password")
        public ResponseEntity<Void> changePassword(
                        @Valid @RequestBody ChangePasswordRequest request) {
                userService.changeOwnPassword(currentUsername(), request);
                return ResponseEntity.noContent().build();
        }

        /** Sube o reemplaza la foto (máximo 20 MB). Campo del formulario: "file". */
        @PutMapping(path = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public UserResponse uploadPhoto(@RequestParam("file") MultipartFile file) {
                return userService.updateOwnPhoto(currentUsername(), file);
        }

        @GetMapping("/photo")
        public ResponseEntity<byte[]> photo() {

                UserProfileImage image = userService.getOwnPhoto(currentUsername());

                return ResponseEntity.ok()
                                .contentType(MediaType.parseMediaType(image.getContentType()))
                                .cacheControl(CacheControl.noStore())
                                .body(image.getImage());
        }

        @DeleteMapping("/photo")
        public ResponseEntity<Void> deletePhoto() {
                userService.deleteOwnPhoto(currentUsername());
                return ResponseEntity.noContent().build();
        }

        private String currentUsername() {
                return SecurityUtils.currentUsername()
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No hay una sesión activa."));
        }
}
