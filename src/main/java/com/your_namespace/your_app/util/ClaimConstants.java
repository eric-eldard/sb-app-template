package com.your_namespace.your_app.util;

public final class ClaimConstants
{
    public static final String AUTHORIZED_UNTIL = "authorized_until";

    public static final String ENABLED = "enabled";

    public static final String LOCKED_ON = "locked_on";

    public static final String SERVER_START = "server_start";

    public static final String USER_ID = "sub"; // user ID is the claim's subject

    public static final String USERNAME = "username";

    public static final String GA_STUB = "GrantedAuthority-";

    private ClaimConstants()
    {
        // util ctor
    }
}