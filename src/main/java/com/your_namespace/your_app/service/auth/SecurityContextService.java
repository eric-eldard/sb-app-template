package com.your_namespace.your_app.service.auth;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import org.springframework.security.core.Authentication;

/**
 * Utilities for retrieving info from the Spring Security Context
 */
public interface SecurityContextService
{
    @Nonnull
    String getCurrentUsersNameNonNull();

    @Nullable
    String getCurrentUsersNameNullable();

    @Nullable
    Long getCurrentUsersIdNullable();

    void setAuthentication(Authentication authentication);
}