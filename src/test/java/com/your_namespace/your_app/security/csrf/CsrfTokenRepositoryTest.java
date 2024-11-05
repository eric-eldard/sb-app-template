package com.your_namespace.your_app.security.csrf;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.cache.Cache;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;

import com.your_namespace.your_app.service.auth.SecurityContextService;
import com.your_namespace.your_app.util.Constants;
import com.your_namespace.your_app.util.ReflectionUtils;

@ExtendWith(MockitoExtension.class)
public class CsrfTokenRepositoryTest
{
    private static final CsrfToken TEST_GENERATED_TOKEN =
        new DefaultCsrfToken(Constants.CSRF_HEADER_NAME, "_csrf", "test-generated-token");

    private static final long USER_ID = 1;

    @Mock
    private SecurityContextService securityContextService;

    private CsrfTokenRepository csrfTokenRepo;

    private Cache<Long, CsrfToken> spiedTokenCache;


    @BeforeEach
    public void setUp()
    {
        csrfTokenRepo = new CsrfTokenRepository(60, securityContextService);
        spyOnTokenCache();
    }


    @Test
    public void testGenerateTokenCreatesNewTokenWhenNonExists()
    {
        when(securityContextService.getCurrentUsersIdNullable()).thenReturn(USER_ID);
        CsrfToken csrfToken = csrfTokenRepo.generateToken(null);

        assertNotNull(csrfToken);
        verify(spiedTokenCache).getIfPresent(USER_ID);
    }

    @Test
    public void testGenerateReusesTokenWhenExists()
    {
        when(securityContextService.getCurrentUsersIdNullable()).thenReturn(USER_ID);
        spiedTokenCache.put(USER_ID, TEST_GENERATED_TOKEN);

        CsrfToken csrfToken = csrfTokenRepo.generateToken(null);

        assertEquals(TEST_GENERATED_TOKEN, csrfToken);
        verify(spiedTokenCache, atLeastOnce()).getIfPresent(USER_ID);
    }

    @Test
    public void testGenerateReturnsNullForNullUser()
    {
        when(securityContextService.getCurrentUsersIdNullable()).thenReturn(null);

        CsrfToken csrfToken = csrfTokenRepo.generateToken(null);

        assertNull(csrfToken);
        verify(spiedTokenCache, never()).getIfPresent(anyLong());
    }

    @Test
    public void testTokenSavedForNonNullUserAndToken()
    {
        when(securityContextService.getCurrentUsersIdNullable()).thenReturn(USER_ID);

        csrfTokenRepo.saveToken(TEST_GENERATED_TOKEN, null, null);

        assertEquals(TEST_GENERATED_TOKEN, spiedTokenCache.getIfPresent(USER_ID));
        verify(spiedTokenCache).put(USER_ID, TEST_GENERATED_TOKEN);
    }

    @Test
    public void testTokenNotSavedForNullUser()
    {
        when(securityContextService.getCurrentUsersIdNullable()).thenReturn(null);

        csrfTokenRepo.saveToken(TEST_GENERATED_TOKEN, null, null);

        verify(spiedTokenCache, never()).put(anyLong(), any(CsrfToken.class));
    }

    @Test
    public void testTokenNotSavedForNullToken()
    {
        when(securityContextService.getCurrentUsersIdNullable()).thenReturn(USER_ID);

        csrfTokenRepo.saveToken(null, null, null);

        verify(spiedTokenCache, never()).put(anyLong(), any(CsrfToken.class));
    }

    @Test
    public void testTokenLoadedWhenPresent()
    {
        when(securityContextService.getCurrentUsersIdNullable()).thenReturn(USER_ID);
        spiedTokenCache.put(USER_ID, TEST_GENERATED_TOKEN);

        CsrfToken csrfToken = csrfTokenRepo.loadToken(null);

        assertEquals(TEST_GENERATED_TOKEN, csrfToken);
    }

    @Test
    public void testLoadTokenReturnsNullWhenUserIsNull()
    {
        when(securityContextService.getCurrentUsersIdNullable()).thenReturn(null);

        CsrfToken csrfToken = csrfTokenRepo.loadToken(null);

        assertNull(csrfToken);
        verify(spiedTokenCache, never()).getIfPresent(anyLong());
    }

    @Test
    public void testLoadTokenReturnsNullWhenTokenNotPresent()
    {
        when(securityContextService.getCurrentUsersIdNullable()).thenReturn(USER_ID);

        CsrfToken csrfToken = csrfTokenRepo.loadToken(null);

        assertNull(csrfToken);
        verify(spiedTokenCache).getIfPresent(USER_ID);
    }

    @Test
    public void testInvalidateForUserRemovesToken()
    {
        spiedTokenCache.put(USER_ID, TEST_GENERATED_TOKEN);

        csrfTokenRepo.invalidateForUser(USER_ID);

        assertEquals(0, spiedTokenCache.size());
    }

    @Test
    @SneakyThrows
    public void testExpiredTokenNotReturned()
    {
        when(securityContextService.getCurrentUsersIdNullable()).thenReturn(USER_ID);
        csrfTokenRepo = new CsrfTokenRepository(1, securityContextService);
        spyOnTokenCache();

        Thread.sleep(2_000);

        CsrfToken csrfToken = csrfTokenRepo.loadToken(null);

        assertNull(csrfToken);
        verify(spiedTokenCache).getIfPresent(USER_ID); // verify null was result of call to cache
    }


    private void spyOnTokenCache()
    {
        var tokenCache = (Cache<Long, CsrfToken>) ReflectionUtils.getField(csrfTokenRepo, "tokenCache");
        spiedTokenCache = Mockito.spy(tokenCache);
        ReflectionUtils.setField(csrfTokenRepo, "tokenCache", spiedTokenCache);
    }
}
