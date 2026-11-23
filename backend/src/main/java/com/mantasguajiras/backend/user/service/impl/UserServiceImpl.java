package com.mantasguajiras.backend.user.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.mantasguajiras.backend.common.exception.BusinessException;
import com.mantasguajiras.backend.common.exception.DuplicateResourceException;
import com.mantasguajiras.backend.common.exception.ResourceNotFoundException;
import com.mantasguajiras.backend.security.dto.requests.RegisterRequest;
import com.mantasguajiras.backend.user.dto.requests.ChangePasswordRequest;
import com.mantasguajiras.backend.user.dto.requests.ResetPasswordRequest;
import com.mantasguajiras.backend.user.dto.requests.UpdateUserRequest;
import com.mantasguajiras.backend.user.dto.requests.UserRequest;
import com.mantasguajiras.backend.user.dto.response.UserResponse;
import com.mantasguajiras.backend.user.entity.Role;
import com.mantasguajiras.backend.user.entity.User;
import com.mantasguajiras.backend.user.entity.UserProfileImage;
import com.mantasguajiras.backend.user.mapper.UserMapper;
import com.mantasguajiras.backend.user.repository.UserProfileImageRepository;
import com.mantasguajiras.backend.user.repository.UserRepository;
import com.mantasguajiras.backend.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

        /** Regla de negocio: la foto de perfil pesa como máximo 20 MB. */
        public static final long MAX_PHOTO_BYTES = 20L * 1024 * 1024;

        private static final Set<String> ALLOWED_PHOTO_TYPES = Set.of(
                        "image/jpeg", "image/png", "image/webp", "image/gif");

        private final UserRepository userRepository;
        private final UserProfileImageRepository profileImageRepository;
        private final UserMapper userMapper;
        private final PasswordEncoder passwordEncoder;

        // =========================================================
        // REGISTRO PÚBLICO
        // =========================================================

        /**
         * La cuenta queda INACTIVA hasta que un administrador la
         * active desde "Administrar" (registro con aprobación).
         */
        @Override
        public UserResponse register(RegisterRequest request) {

                String username = request.getUsername().trim();
                String phone = request.getPhone().trim();

                validateUniqueFields(username, phone, null);

                User user = User.builder()
                                .username(username)
                                .phone(phone)
                                .password(passwordEncoder.encode(request.getPassword()))
                                .role(Role.SELLER)
                                .active(false)
                                .build();

                return toResponse(userRepository.save(user));
        }

        // =========================================================
        // ADMINISTRACIÓN
        // =========================================================

        @Override
        public UserResponse create(UserRequest request) {

                String username = request.getUsername().trim();
                String phone = request.getPhone().trim();

                validateUniqueFields(username, phone, null);

                User user = userMapper.toEntity(request);

                user.setUsername(username);
                user.setPhone(phone);
                user.setRole(request.getRole() != null ? request.getRole() : Role.SELLER);
                user.setPassword(passwordEncoder.encode(request.getPassword()));

                return toResponse(userRepository.save(user));
        }

        @Override
        @Transactional(readOnly = true)
        public UserResponse findById(UUID id) {

                return toResponse(findEntity(id));
        }

        /** Primero los inactivos (pendientes de aprobación), luego por nombre. */
        @Override
        @Transactional(readOnly = true)
        public List<UserResponse> findAll() {

                return userRepository.findAll()
                                .stream()
                                .sorted(Comparator
                                                .comparing((User user) -> Boolean.TRUE.equals(user.getActive()))
                                                .thenComparing(user -> user.getUsername().toLowerCase()))
                                .map(this::toResponse)
                                .toList();
        }

        @Override
        public UserResponse update(
                        UUID id,
                        UpdateUserRequest request,
                        String actingUsername) {

                User user = findEntity(id);

                String username = request.getUsername().trim();
                String phone = request.getPhone().trim();

                validateUniqueFields(username, phone, id);

                boolean isSelf = user.getUsername().equals(actingUsername);

                boolean losesAdmin = user.getRole() == Role.ADMIN
                                && (request.getRole() != Role.ADMIN
                                                || !Boolean.TRUE.equals(request.getActive()));

                if (isSelf && request.getRole() != user.getRole()) {
                        throw new BusinessException(
                                        "No puedes cambiar tu propio rol. "
                                                        + "Pídeselo a otro administrador.");
                }

                if (isSelf && !Boolean.TRUE.equals(request.getActive())) {
                        throw new BusinessException(
                                        "No puedes desactivar tu propia cuenta.");
                }

                if (losesAdmin && Boolean.TRUE.equals(user.getActive())
                                && countActiveAdmins() <= 1) {
                        throw new BusinessException(
                                        "Debe quedar al menos un administrador activo. "
                                                        + "Nombra otro administrador antes de hacer este cambio.");
                }

                userMapper.updateEntity(request, user);
                user.setUsername(username);
                user.setPhone(phone);

                return toResponse(userRepository.save(user));
        }

        @Override
        public void resetPassword(UUID id, ResetPasswordRequest request) {

                User user = findEntity(id);

                user.setPassword(passwordEncoder.encode(request.getNewPassword()));

                userRepository.save(user);
        }

        /**
         * Regla de negocio: los usuarios no se eliminan (se perdería
         * quién hizo cada venta, compra o movimiento). Se desactivan.
         */
        @Override
        public void delete(UUID id) {

                findEntity(id);

                throw new BusinessException(
                                "Los usuarios no se eliminan para conservar el historial. "
                                                + "Desactívalo: ya no podrá iniciar sesión.");
        }

        @Override
        public UserResponse promoteToAdmin(UUID id) {

                User user = findEntity(id);

                if (user.getRole() == Role.ADMIN) {
                        throw new BusinessException("El usuario ya es administrador.");
                }

                user.setRole(Role.ADMIN);

                return toResponse(userRepository.save(user));
        }

        // =========================================================
        // MI PERFIL
        // =========================================================

        @Override
        @Transactional(readOnly = true)
        public UserResponse getMe(String username) {

                return toResponse(findByUsername(username));
        }

        @Override
        public void changeOwnPassword(String username, ChangePasswordRequest request) {

                User user = findByUsername(username);

                if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                        throw new BusinessException("La contraseña actual es incorrecta.");
                }

                if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
                        throw new BusinessException(
                                        "La contraseña nueva debe ser distinta de la actual.");
                }

                user.setPassword(passwordEncoder.encode(request.getNewPassword()));

                userRepository.save(user);
        }

        @Override
        public UserResponse updateOwnPhoto(String username, MultipartFile file) {

                User user = findByUsername(username);

                if (file == null || file.isEmpty()) {
                        throw new BusinessException("Elige una foto para subir.");
                }

                if (file.getSize() > MAX_PHOTO_BYTES) {
                        throw new BusinessException("La foto no puede pesar más de 20 MB.");
                }

                String contentType = file.getContentType() != null
                                ? file.getContentType().toLowerCase()
                                : "";

                if (!ALLOWED_PHOTO_TYPES.contains(contentType)) {
                        throw new BusinessException(
                                        "La foto debe ser una imagen JPG, PNG, WEBP o GIF.");
                }

                byte[] bytes;

                try {
                        bytes = file.getBytes();
                } catch (IOException exception) {
                        throw new BusinessException(
                                        "No fue posible leer la foto. Intenta con otra imagen.");
                }

                // El tipo lo declara el navegador y se puede falsificar:
                // se comprueba también la firma real del archivo.
                String detectedType = detectImageType(bytes);

                if (detectedType == null) {
                        throw new BusinessException(
                                        "El archivo no es una imagen JPG, PNG, WEBP o GIF válida.");
                }

                contentType = detectedType;

                UserProfileImage image = profileImageRepository.findById(user.getId())
                                .orElseGet(() -> UserProfileImage.builder()
                                                .userId(user.getId())
                                                .build());

                image.setContentType(contentType);
                image.setImage(bytes);
                image.setUpdatedAt(LocalDateTime.now());

                profileImageRepository.save(image);

                return toResponse(user);
        }

        @Override
        public void deleteOwnPhoto(String username) {

                User user = findByUsername(username);

                profileImageRepository.deleteById(user.getId());
        }

        @Override
        @Transactional(readOnly = true)
        public UserProfileImage getOwnPhoto(String username) {

                User user = findByUsername(username);

                return profileImageRepository.findById(user.getId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Todavía no tienes foto de perfil."));
        }

        // =========================================================
        // AYUDANTES
        // =========================================================

        private UserResponse toResponse(User user) {

                UserResponse response = userMapper.toResponse(user);

                response.setHasPhoto(profileImageRepository.existsById(user.getId()));

                return response;
        }

        private long countActiveAdmins() {

                return userRepository.findAll()
                                .stream()
                                .filter(user -> user.getRole() == Role.ADMIN
                                                && Boolean.TRUE.equals(user.getActive()))
                                .count();
        }

        private User findEntity(UUID id) {

                return userRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Usuario no encontrado."));
        }

        private User findByUsername(String username) {

                return userRepository.findByUsername(username)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Usuario no encontrado."));
        }

        private void validateUniqueFields(
                        String username,
                        String phone,
                        UUID currentId) {

                userRepository.findByUsername(username)
                                .filter(user -> currentId == null
                                                || !user.getId().equals(currentId))
                                .ifPresent(user -> {
                                        throw new DuplicateResourceException(
                                                        "El nombre de usuario ya está registrado.");
                                });

                userRepository.findByPhone(phone)
                                .filter(user -> currentId == null
                                                || !user.getId().equals(currentId))
                                .ifPresent(user -> {
                                        throw new DuplicateResourceException(
                                                        "El número de teléfono ya está registrado.");
                                });
        }

        /**
         * Identifica la imagen por sus primeros bytes ("firma" o magic
         * number). Devuelve el tipo MIME o null si no es un formato
         * permitido.
         */
        static String detectImageType(byte[] bytes) {

                if (bytes == null || bytes.length < 12) {
                        return null;
                }

                // JPEG: FF D8 FF
                if ((bytes[0] & 0xFF) == 0xFF
                                && (bytes[1] & 0xFF) == 0xD8
                                && (bytes[2] & 0xFF) == 0xFF) {
                        return "image/jpeg";
                }

                // PNG: 89 50 4E 47 0D 0A 1A 0A
                if ((bytes[0] & 0xFF) == 0x89
                                && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G'
                                && bytes[4] == 0x0D && bytes[5] == 0x0A
                                && bytes[6] == 0x1A && bytes[7] == 0x0A) {
                        return "image/png";
                }

                // GIF: "GIF87a" o "GIF89a"
                if (bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F'
                                && bytes[3] == '8'
                                && (bytes[4] == '7' || bytes[4] == '9')
                                && bytes[5] == 'a') {
                        return "image/gif";
                }

                // WEBP: "RIFF" .... "WEBP"
                if (bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                                && bytes[8] == 'W' && bytes[9] == 'E'
                                && bytes[10] == 'B' && bytes[11] == 'P') {
                        return "image/webp";
                }

                return null;
        }
}
