package com.mantasguajiras.backend.security.dto.response;

import java.util.UUID;

import com.mantasguajiras.backend.user.entity.Role;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String token;

    private UUID userId;

    private String username;

    private String phone;

    private Role role;
}