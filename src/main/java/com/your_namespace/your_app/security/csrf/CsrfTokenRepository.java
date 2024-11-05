package com.your_namespace.your_app.security.csrf;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.UUID;

import com.your_namespace.your_app.service.auth.SecurityContextService;
import com.your_namespace.your_app.util.Constants;

@Component
public class CsrfTokenRepository implements org.springframework.security.web.csrf.CsrfTokenRepository
{
    private final Cache<Long, CsrfToken> tokenCache;

    private final SecurityContextService securityContextService;

    public CsrfTokenRepository(@Value("${your_app.security.jwt.ttl-sec}") long jwtTtlSeconds,
                               SecurityContextService securityContextService
    )
    {
        this.securityContextService = securityContextService;
        this.tokenCache = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(jwtTtlSeconds))
            .build();
    }

    @Override
    public CsrfToken generateToken(HttpServletRequest request)
    {
        Long userId = securityContextService.getCurrentUsersIdNullable();

        CsrfToken token = null;
        if (userId != null)
        {
            if (tokenCache.getIfPresent(userId) != null)
            {
                token = tokenCache.getIfPresent(userId);
            }
            else
            {
                token = new DefaultCsrfToken(Constants.CSRF_HEADER_NAME, "_csrf", UUID.randomUUID().toString());
            }
        }
        return token;
    }

    @Override
    public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response)
    {
        Long userId = securityContextService.getCurrentUsersIdNullable();
        if (userId != null && token != null)
        {
            tokenCache.put(userId, token);
        }
    }

    @Override
    public CsrfToken loadToken(HttpServletRequest request)
    {
        Long userId = securityContextService.getCurrentUsersIdNullable();
        return userId == null ? null : tokenCache.getIfPresent(userId);
    }

    public void invalidateForUser(long userId)
    {
        tokenCache.invalidate(userId);
    }
}