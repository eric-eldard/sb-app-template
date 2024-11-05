package com.your_namespace.your_app.service.web;

import jakarta.servlet.http.Cookie;

public interface CookieService
{
    Cookie makeSessionCookie(String name, String content);

    Cookie makePersistentCookie(String name, String content, int ttlSeconds);

    Cookie makeExpiredCookie(String name);
}