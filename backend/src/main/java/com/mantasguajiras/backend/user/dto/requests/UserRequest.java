package com.mantasguajiras.backend.user.dto.requests;

import com.mantasguajiras.backend.user.entity.Role;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {

    private String username;

    private String password;

    private Role role;

    private Boolean active;
}