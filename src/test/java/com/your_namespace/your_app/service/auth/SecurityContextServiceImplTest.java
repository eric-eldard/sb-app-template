package com.your_namespace.your_app.service.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.your_namespace.your_app.model.auth.JwsAuthToken;
import com.your_namespace.your_app.security.util.JwtUtil;
import com.your_namespace.your_app.test.TestUtils;
import com.your_namespace.your_app.util.ClaimConstants;

@ExtendWith(MockitoExtension.class)
class SecurityContextServiceImplTest
{
    private static final String USERNAME = "testUser";

    private static final long USER_ID = 123L;

    private static final JwtUtil JWT_UTIL =
        new JwtUtil("c6f5c0681be02477aec35d927e0b5da7021ffb6dae6cf30521cd47395a53813c"); // random test key

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private SecurityContextServiceImpl securityContextService;


    @BeforeEach
    public void setUp()
    {
        SecurityContextHolder.setContext(securityContext);
    }


    @Test
    public void getCurrentUsersNameNonNull_ReturnsUsernameWhenAuthenticated()
    {
        when(securityContext.getAuthentication()).thenReturn(makeJwsAuthToken());

        String username = securityContextService.getCurrentUsersNameNonNull();

        assertEquals(USERNAME, username);
    }

    @Test
    public void getCurrentUsersNameNonNull_ReturnsAnonymousWhenNotAuthenticated()
    {
        when(securityContext.getAuthentication()).thenReturn(null);

        String username = securityContextService.getCurrentUsersNameNonNull();

        assertEquals("anonymous", username);
    }

    @Test
    public void getCurrentUsersNameNonNull_ThrowsExceptionForUnsupportedAuthenticationType()
    {
        when(securityContext.getAuthentication()).thenReturn(makeUnsupportedAuthToken());

        TestUtils.assertThrowsAndPrintMessage(
            IllegalStateException.class,
            () -> securityContextService.getCurrentUsersNameNonNull()
        );
    }

    @Test
    public void getCurrentUsersNameNullable_ReturnsUsernameWhenAuthenticated()
    {
        when(securityContext.getAuthentication()).thenReturn(makeJwsAuthToken());

        String username = securityContextService.getCurrentUsersNameNullable();

        assertEquals(USERNAME, username);
    }

    @Test
    public void getCurrentUsersNameNullable_ReturnsNullWhenNotAuthenticated()
    {
        when(securityContext.getAuthentication()).thenReturn(null);

        String username = securityContextService.getCurrentUsersNameNullable();

        assertNull(username);
    }

    @Test
    public void getCurrentUsersNameNullable_ThrowsExceptionForUnsupportedAuthenticationType()
    {
        when(securityContext.getAuthentication()).thenReturn(makeUnsupportedAuthToken());

        TestUtils.assertThrowsAndPrintMessage(
            IllegalStateException.class,
            () -> securityContextService.getCurrentUsersNameNullable()
        );
    }

    @Test
    public void getCurrentUsersIdNullable_ReturnsUserIdWhenAuthenticated()
    {
        when(securityContext.getAuthentication()).thenReturn(makeJwsAuthToken());

        Long userId = securityContextService.getCurrentUsersIdNullable();

        assertEquals(USER_ID, userId);
    }

    @Test
    public void getCurrentUsersIdNullable_ReturnsNullWhenNotAuthenticated()
    {
        when(securityContext.getAuthentication()).thenReturn(null);

        Long userId = securityContextService.getCurrentUsersIdNullable();

        assertNull(userId);
    }

    @Test
    public void getCurrentUsersIdNullable_ThrowsExceptionForUnsupportedAuthenticationType()
    {
        when(securityContext.getAuthentication()).thenReturn(makeUnsupportedAuthToken());

        TestUtils.assertThrowsAndPrintMessage(
            IllegalStateException.class,
            () -> securityContextService.getCurrentUsersIdNullable()
        );
    }

    @Test
    public void setAuthenticationSetsTheAuthentication()
    {
        JwsAuthToken authToken = makeJwsAuthToken();

        securityContextService.setAuthentication(authToken);

        verify(securityContext).setAuthentication(authToken);
    }

    private JwsAuthToken makeJwsAuthToken()
    {
        Map<String, Object> claims = new HashMap<>();
        claims.put(ClaimConstants.USERNAME, USERNAME);
        String claimsString = JWT_UTIL.buildToken(String.valueOf(USER_ID), claims, new Date(), TestUtils.tomorrow());
        return new JwsAuthToken(JWT_UTIL.resolveClaims(claimsString));
    }

    private UsernamePasswordAuthenticationToken makeUnsupportedAuthToken()
    {
        return new UsernamePasswordAuthenticationToken(USER_ID, null, null);
    }
}