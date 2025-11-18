package com.agilesprintplus.security.dto;

public record PasswordResetResponse(
        String message,
        String temporaryPassword,
        String note
    ) {}