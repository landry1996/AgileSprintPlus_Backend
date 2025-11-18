package com.agilesprintplus.agilesprint.api.dto;

import com.agilesprintplus.agilesprint.domain.Role;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
@AllArgsConstructor
public class AdminUserDtos {

    public record Response(
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
}