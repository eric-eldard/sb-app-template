package com.your_namespace.your_app.service.web;

import lombok.AllArgsConstructor;

import jakarta.servlet.http.Cookie;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.your_namespace.your_app.config.GlobalConfig;

@Service
@AllArgsConstructor
public class CookieServiceImpl implements CookieService
{
    /**
     * Defaults to {@code true} in application.yml; supports override for non-SSL test envs
     */
    @Value("${your_app.cookie.secure}")
    private final boolean secure;

    /**
     * Note: SameSite attribute controlled in {@link GlobalConfig}
     */
    @Override
    public Cookie makeSessionCookie(String name, String content)
    {
        Cookie cookie = new Cookie(name, content);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        return cookie;
    }

    @Override
    public Cookie makePersistentCookie(String name, String content, int ttlSeconds)
    {
        Cookie cookie = makeSessionCookie(name, content);
        cookie.setMaxAge(ttlSeconds);
        return cookie;
    }

    @Override
    public Cookie makeExpiredCookie(String name)
    {
        return makePersistentCookie(name, "" , 0);
    }
}