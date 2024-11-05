package com.your_namespace.your_app.util;

public final class Constants
{
    public static final String CSRF_HEADER_NAME = "X-Csrf-Token";

    public static final int FAILED_LOGINS_BEFORE_ACCOUNT_LOCK = 10;

    public static final String JWT_COOKIE_NAME = "authToken";

    public static final int MIN_PASSWORD_CHARS = 10;

    private Constants()
    {
        // constants ctor
    }
}