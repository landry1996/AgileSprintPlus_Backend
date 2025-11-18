package com.agilesprintplus.security.dto;

import com.agilesprintplus.agilesprint.domain.Role;

public record CreateUserRequest(
    String username,
    String email,
    String firstName,
    String lastName,
    Role role
) {}