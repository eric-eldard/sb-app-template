package com.your_namespace.your_app.security.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.cache.Cache;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.your_namespace.your_app.YourApp;
import com.your_namespace.your_app.model.user.AppUser;
import com.your_namespace.your_app.persistence.user.UserRepository;
import com.your_namespace.your_app.security.util.JwtUtil;
import com.your_namespace.your_app.service.auth.AuthenticationService;
import com.your_namespace.your_app.service.user.UserService;
import com.your_namespace.your_app.test.TestConfig;
import com.your_namespace.your_app.test.TestUtils;
import com.your_namespace.your_app.util.ClaimConstants;
import com.your_namespace.your_app.util.Constants;
import com.your_namespace.your_app.util.ReflectionUtils;

@SpringBootTest(
    classes = {
        TestConfig.class,
        YourApp.class
    }
)
@ActiveProfiles("test")
public class JwsFilterTest
{
    @Inject
    private AuthenticationService authService;

    @Inject
    private JwtUtil jwtUtil;

    @Inject
    private UserService userService;

    @Inject
    private UserRepository userRepo;

    private JwsFilter jwsFilter;


    @BeforeEach
    public void reset()
    {
        jwsFilter = new JwsFilter(authService);
        ((Cache<Long, Date>) ReflectionUtils.getField(authService, "usersRequiringFreshClaims")).invalidateAll();
        userRepo.deleteAll(); /// Much faster than {@link DirtiesContext}
    }


    @Test
    @Transactional
    public void testLockedAccountIsForbidden()
    {
        AppUser user = userService.create(TestUtils.makeUserDto());
        user.setLockedOn(new Date());
        userRepo.save(user);

        MockHttpServletResponse response = makeRequest(authService.issueToken(user));

        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
    }

    @Test
    public void testExpiredAccountIsForbidden()
    {
        AppUser user = userService.create(TestUtils.makeUserDto());
        userService.setAuthorizedUntil(user.getId(), TestUtils.yesterday());

        MockHttpServletResponse response = makeRequest(authService.issueToken(user));

        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
    }

    @Test
    public void testDisabledAccountIsForbidden()
    {
        AppUser user = userService.create(TestUtils.makeUserDto());
        userService.setEnabled(user.getId(), false);

        MockHttpServletResponse response = makeRequest(authService.issueToken(user));

        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
    }

    @Test
    public void testForgedTokenIsUnauthorized()
    {
        AppUser user = userService.create(TestUtils.makeUserDto());
        String randomSigningKey = "c33dda180f768f592440a1129226f246991a351b73be43a4533b7077ab1bedeb";
        JwtUtil sketchyJwtUtil = new JwtUtil(randomSigningKey);

        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put(ClaimConstants.USERNAME, user.getUsername());
        claimsMap.put(ClaimConstants.ENABLED, Boolean.TRUE);
        claimsMap.put(ClaimConstants.GA_STUB + "1", "ROLE_ADMIN");
        claimsMap.put(ClaimConstants.SERVER_START, TestUtils.yesterday()); // attempting to get upgraded to a real token

        String forgedClaims = sketchyJwtUtil.buildToken(
            user.getId().toString(),
            claimsMap,
            new Date(),
            TestUtils.tomorrow()
        );

        MockHttpServletResponse response = makeRequest(forgedClaims);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    }

    @Test
    public void testDeletedAccountIsUnauthorized()
    {
        AppUser user = userService.create(TestUtils.makeUserDto());
        userService.delete(user.getId());

        MockHttpServletResponse response = makeRequest(authService.issueToken(user));

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    }

    @Test
    public void testDamagedAuthTokenIsUnauthorized()
    {
        AppUser user = userService.create(TestUtils.makeUserDto());
        userService.delete(user.getId());

        String damagedClaims = authService.issueToken(user).substring(0, authService.issueToken(user).length() / 2);

        MockHttpServletResponse response = makeRequest(damagedClaims);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    }

    @Test
    public void testTokenWithoutUserIdIsUnauthorized()
    {
        AppUser user = userService.create(TestUtils.makeUserDto());

        Jws<Claims> claims = jwtUtil.resolveClaims(authService.issueToken(user));
        Map<String, Object> claimsMap = new HashMap<>(claims.getPayload());
        claimsMap.remove(ClaimConstants.USER_ID);

        String claimsMissingSubject = jwtUtil.buildToken(
            null,
            claimsMap,
            claims.getPayload().getIssuedAt(),
            claims.getPayload().getExpiration()
        );

        MockHttpServletResponse response = makeRequest(claimsMissingSubject);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    }

    @Test
    public void testExpiredTokenIsUnauthorized()
    {
        AppUser user = userService.create(TestUtils.makeUserDto());

        Jws<Claims> claims = jwtUtil.resolveClaims(authService.issueToken(user));
        String expiredClaims = jwtUtil.buildToken(
            claims.getPayload().getSubject(),
            claims.getPayload(),
            claims.getPayload().getIssuedAt(),
            TestUtils.yesterday()
        );

        MockHttpServletResponse response = makeRequest(expiredClaims);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    }

    @Test
    public void testTokenWithoutIssuedAtIsUnauthorized()
    {
        AppUser user = userService.create(TestUtils.makeUserDto());

        Jws<Claims> claims = jwtUtil.resolveClaims(authService.issueToken(user));
        String claimsMissingIssuedAt = jwtUtil.buildToken(
            claims.getPayload().getSubject(),
            claims.getPayload(),
            null,
            claims.getPayload().getExpiration()
        );

        MockHttpServletResponse response = makeRequest(claimsMissingIssuedAt);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    }

    @Test
    public void testTokenFromFutureIsUnauthorized()
    {
        AppUser user = userService.create(TestUtils.makeUserDto());

        Jws<Claims> claims = jwtUtil.resolveClaims(authService.issueToken(user));
        String claimsFromTheFuture = jwtUtil.buildToken(
            claims.getPayload().getSubject(),
            claims.getPayload(),
            TestUtils.tomorrow(),
            claims.getPayload().getExpiration()
        );

        MockHttpServletResponse response = makeRequest(claimsFromTheFuture);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    }

    @Test
    public void testTokenWithoutServerStartIsUnauthorized()
    {
        AppUser user = userService.create(TestUtils.makeUserDto());

        Jws<Claims> claims = jwtUtil.resolveClaims(authService.issueToken(user));
        Map<String, Object> claimsMap = new HashMap<>(claims.getPayload());
        claimsMap.remove(ClaimConstants.SERVER_START);

        String claimsMissingServerStart = jwtUtil.buildToken(
            claims.getPayload().getSubject(),
            claimsMap,
            claims.getPayload().getIssuedAt(),
            claims.getPayload().getExpiration()
        );

        MockHttpServletResponse response = makeRequest(claimsMissingServerStart);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    }

    @Test
    public void testTokenWithFutureServerStartIsUnauthorized()
    {
        AppUser user = userService.create(TestUtils.makeUserDto());

        Jws<Claims> claims = jwtUtil.resolveClaims(authService.issueToken(user));
        Map<String, Object> claimsMap = new HashMap<>(claims.getPayload());
        claimsMap.put(ClaimConstants.SERVER_START, TestUtils.tomorrow());

        String claimsFromFutureServer = jwtUtil.buildToken(
            claims.getPayload().getSubject(),
            claimsMap,
            claims.getPayload().getIssuedAt(),
            claims.getPayload().getExpiration()
        );

        MockHttpServletResponse response = makeRequest(claimsFromFutureServer);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    }

    @Test
    public void testTokenIssuedBeforeServerStartIsUnauthorized()
    {
        AppUser user = userService.create(TestUtils.makeUserDto());

        Jws<Claims> claims = jwtUtil.resolveClaims(authService.issueToken(user));
        Map<String, Object> claimsMap = new HashMap<>(claims.getPayload());

        String claimsIssuedBeforeServerStart = jwtUtil.buildToken(
            claims.getPayload().getSubject(),
            claimsMap,
            TestUtils.yesterday(),
            claims.getPayload().getExpiration()
        );

        MockHttpServletResponse response = makeRequest(claimsIssuedBeforeServerStart);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    }

    @Test
    public void testTokenWithoutUsernameIsUnauthorized()
    {
        AppUser user = userService.create(TestUtils.makeUserDto());

        Jws<Claims> claims = jwtUtil.resolveClaims(authService.issueToken(user));
        Map<String, Object> claimsMap = new HashMap<>(claims.getPayload());
        claimsMap.remove(ClaimConstants.USERNAME);

        String claimsMissingUsername = jwtUtil.buildToken(
            claims.getPayload().getSubject(),
            claimsMap,
            claims.getPayload().getIssuedAt(),
            claims.getPayload().getExpiration()
        );

        MockHttpServletResponse response = makeRequest(claimsMissingUsername);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    }

    @Test
    public void testTokenNotRefreshedWhenNotNecessary()
    {
        AppUser user = userService.create(TestUtils.makeUserDto());

        MockHttpServletResponse response = makeRequest(authService.issueToken(user));

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertNull(response.getCookie(Constants.JWT_COOKIE_NAME));
    }

    @Test
    @SneakyThrows
    public void testTokenRefreshedWhenUserRequiresFreshClaims()
    {
        AppUser user = userService.create(TestUtils.makeUserDto());
        String claims = authService.issueToken(user);

        Thread.sleep(1_000); // sleep required to ensure refresh-required timestamp is after claims issuedAt time
        authService.requireFreshClaimsForUser(user.getId());

        MockHttpServletResponse response = makeRequest(claims);

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        Claims originalClaims = jwtUtil.resolveClaims(claims).getPayload();
        Claims newClaims = jwtUtil.resolveClaims(response.getCookie(Constants.JWT_COOKIE_NAME).getValue()).getPayload();

        assertTrue(newClaims.getIssuedAt().after(originalClaims.getIssuedAt()));
        assertEquals(originalClaims.getExpiration(), newClaims.getExpiration());
    }

    @Test
    public void testTokenRefreshedAfterSystemRestart()
    {
        AppUser user = userService.create(TestUtils.makeUserDto());

        Jws<Claims> jwsClaims = jwtUtil.resolveClaims(authService.issueToken(user));
        Map<String, Object> claimsFromYesterday = new HashMap<>(jwsClaims.getPayload());
        claimsFromYesterday.put(ClaimConstants.SERVER_START, TestUtils.twoDaysAgo());

        String authToken = jwtUtil.buildToken(
            jwsClaims.getPayload().getSubject(),
            claimsFromYesterday,
            TestUtils.yesterday(),
            jwsClaims.getPayload().getExpiration()
        );

        MockHttpServletResponse response = makeRequest(authToken);

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        Claims originalClaims = jwtUtil.resolveClaims(authToken).getPayload();
        Claims newClaims = jwtUtil.resolveClaims(response.getCookie(Constants.JWT_COOKIE_NAME).getValue()).getPayload();

        assertTrue(newClaims.get(ClaimConstants.SERVER_START, Date.class).after(
            originalClaims.get(ClaimConstants.SERVER_START, Date.class)
        ));
        assertEquals(originalClaims.getExpiration(), newClaims.getExpiration());
    }


    @SneakyThrows
    private MockHttpServletResponse makeRequest(String authToken)
    {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/some-asset");
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setCookies(new Cookie(Constants.JWT_COOKIE_NAME, authToken));

        jwsFilter.doFilterInternal(request, response, new MockFilterChain());
        return response;
    }
}