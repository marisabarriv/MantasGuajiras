package com.mantasguajiras.backend.user.service;

import java.util.List;
import java.util.UUID;

import com.mantasguajiras.backend.security.dto.requests.RegisterRequest;
import com.mantasguajiras.backend.user.dto.requests.ChangePasswordRequest;
import com.mantasguajiras.backend.user.dto.requests.UpdateUserRequest;
import com.mantasguajiras.backend.user.dto.requests.UserRequest;
import com.mantasguajiras.backend.user.dto.response.UserResponse;

public interface UserService {

    UserResponse register(RegisterRequest request);

    UserResponse create(UserRequest request);

    UserResponse findById(UUID id);

    UserResponse update(UUID id, UpdateUserRequest request);

    void changePassword(UUID id, ChangePasswordRequest request);

    void delete(UUID id);

    List<UserResponse> findAll();

    UserResponse promoteToAdmin(UUID id);
}