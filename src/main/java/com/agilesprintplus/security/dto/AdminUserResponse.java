package com.agilesprintplus.security.dto;

import com.agilesprintplus.agilesprint.domain.Role;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AdminUserResponse(
        UUID id,
        String username,
        String email,
        String firstName,
        String lastName,
        Set<Role> roles,
        boolean enabled,
        boolean passwordChangeRequired,
        Instant createdAt,
        Instant updatedAt
) {}