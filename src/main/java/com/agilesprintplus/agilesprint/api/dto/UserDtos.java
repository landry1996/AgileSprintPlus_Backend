package com.agilesprintplus.agilesprint.api.dto;

import com.agilesprintplus.agilesprint.domain.Role;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public class UserDtos {

    public record Create(
            @NotBlank
            @Size(min = 3, max = 64)
            String username,
            @NotBlank
            @Email @Size(max = 160)
            String email,
            @Size(max = 120)
            String firstName,
            @Size(max = 120)
            String lastName,
            @NotNull
            Role role
    ) {}
    public record CreateWithDefaultPassword(
            @NotBlank
            @Size(min = 3, max = 64)
            String username,
            @NotBlank
            @Email @Size(max = 160)
            String email,
            @Size(max = 120)
            String firstName,
            @Size(max = 120)
            String lastName,
            @NotNull
            Role role
    ) {}

    public record Update(
            @Email
            @Size(max = 160)
            String email,
            @Size(max = 120)
            String firstName,
            @Size(max = 120)
            String lastName,
            @Size(min = 8, max = 128)
            String password,
            Set<Role> roles,
            Boolean enabled,
            Set<UUID> taskIds
    ) {}

    public record ChangePassword(
            @NotBlank
            @Size(min = 8, max = 128)
            String oldPassword,
            @NotBlank
            @Size(min = 8, max = 128)
            String newPassword
    ) {}

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
            // PAS de taskIds ici !
    ) {}
}