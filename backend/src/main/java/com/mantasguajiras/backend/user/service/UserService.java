package com.mantasguajiras.backend.user.service;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.mantasguajiras.backend.security.dto.requests.RegisterRequest;
import com.mantasguajiras.backend.user.dto.requests.ChangePasswordRequest;
import com.mantasguajiras.backend.user.dto.requests.ResetPasswordRequest;
import com.mantasguajiras.backend.user.dto.requests.UpdateUserRequest;
import com.mantasguajiras.backend.user.dto.requests.UserRequest;
import com.mantasguajiras.backend.user.dto.response.UserResponse;
import com.mantasguajiras.backend.user.entity.UserProfileImage;

public interface UserService {

    // ---------- Registro público (queda pendiente de aprobación) ----------

    UserResponse register(RegisterRequest request);

    // ---------- Administración (solo ADMIN) ----------

    UserResponse create(UserRequest request);

    UserResponse findById(UUID id);

    List<UserResponse> findAll();

    /** actingUsername: el admin que hace el cambio (para no bloquearse a sí mismo). */
    UserResponse update(UUID id, UpdateUserRequest request, String actingUsername);

    void resetPassword(UUID id, ResetPasswordRequest request);

    void delete(UUID id);

    UserResponse promoteToAdmin(UUID id);

    // ---------- Mi perfil (cualquier usuario con sesión) ----------

    UserResponse getMe(String username);

    void changeOwnPassword(String username, ChangePasswordRequest request);

    UserResponse updateOwnPhoto(String username, MultipartFile file);

    void deleteOwnPhoto(String username);

    UserProfileImage getOwnPhoto(String username);
}
