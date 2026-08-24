package com.mantasguajiras.backend.user.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.mantasguajiras.backend.user.entity.Role;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserResponse {

    private UUID id;

    private String username;

    private String phone;

    private Role role;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}