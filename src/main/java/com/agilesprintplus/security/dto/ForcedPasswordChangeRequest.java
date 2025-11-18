package com.agilesprintplus.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ForcedPasswordChangeRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 128) String oldPassword,
        @NotBlank @Size(min = 8, max = 128) String newPassword
) {}