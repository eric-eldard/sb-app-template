package com.your_namespace.your_app.service.auth;

import static com.your_namespace.your_app.util.Constants.JWT_COOKIE_NAME;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;

import com.your_namespace.your_app.config.GlobalConfig;
import com.your_namespace.your_app.model.auth.JwsAuthToken;
import com.your_namespace.your_app.model.user.AppUser;
import com.your_namespace.your_app.security.csrf.CsrfTokenRepository;
import com.your_namespace.your_app.security.exception.InvalidTokenException;
import com.your_namespace.your_app.security.filter.JwsFilter;
import com.your_namespace.your_app.security.util.JwtUtil;
import com.your_namespace.your_app.service.user.UserService;
import com.your_namespace.your_app.service.web.CookieService;
import com.your_namespace.your_app.util.ClaimConstants;
import com.your_namespace.your_app.util.DateUtils;

@Service
public class AuthenticationServiceImpl implements AuthenticationService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    private static final Date SERVER_START = new Date();

    private final CookieService cookieService;

    private final JwtUtil jwtUtil;

    private final long jwtTtlSeconds;

    private final CsrfTokenRepository csrfTokenRepo;

    private final UserService userService;

    private final SecurityContextService securityContextService;

    private final Cache<Long, Date> usersRequiringFreshClaims;

    private AuthenticationManager authenticationManager;


    public AuthenticationServiceImpl(@Value("${your_app.security.jwt.ttl-sec}") long jwtTtlSeconds,
                                     CookieService cookieService,
                                     JwtUtil jwtUtil,
                                     CsrfTokenRepository csrfTokenRepo,
                                     UserService userService,
                                     SecurityContextService securityContextService
    )
    {
        this.cookieService = cookieService;
        this.jwtTtlSeconds = jwtTtlSeconds;
        this.jwtUtil = jwtUtil;
        this.csrfTokenRepo = csrfTokenRepo;
        this.userService = userService;
        this.securityContextService = securityContextService;
        this.usersRequiringFreshClaims = CacheBuilder.newBuilder()
            .expireAfterWrite(jwtTtlSeconds, TimeUnit.SECONDS)
            .build();
    }


    /**
     * The authentication manager is created in {@link GlobalConfig}, which unfortunately requires an authentication
     * service to instantiate a {@link JwsFilter}. Alas, chicken-and-egg results in this grossness.
     */
    @Lazy
    @Inject
    public void setAuthenticationManager(AuthenticationManager authenticationManager)
    {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Authentication authenticate(String username, String password)
    {
        return authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password)
        );
    }

    @Override
    public String issueToken(AppUser user)
    {
        Date issuedAt = new Date();
        Date expiry = new Date(issuedAt.getTime() + (jwtTtlSeconds * 1_000L));
        return issueToken(user, issuedAt, expiry);
    }

    @Override
    public void setAuthenticationForRequest(String incomingClaims,
                                            HttpServletRequest request,
                                            HttpServletResponse response)
    {
        // We wrap the claims immediately to leverage convenience methods in the token class
        JwsAuthToken incomingToken = new JwsAuthToken(resolveClaims(incomingClaims));

        validateToken(incomingToken);

        String claimsToPresent;
        JwsAuthToken tokenToPresent;
        if (refreshRequired(incomingToken))
        {
            LOGGER.info("Token presented by [{}] requires refreshing", incomingToken.username());
            claimsToPresent = refreshTokenClaims(incomingToken);
            tokenToPresent = new JwsAuthToken(resolveClaims(claimsToPresent));
        }
        else
        {
            claimsToPresent = incomingClaims;
            tokenToPresent = incomingToken;
        }

        validateAccount(tokenToPresent);

        tokenToPresent.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        if (!requestHasClaims(request, claimsToPresent))
        {
            // Make cookie with expiry matching expiry of auth token
            setAuthTokenCookie(response, claimsToPresent, tokenToPresent.getSecondsUntilExpiration());
        }

        securityContextService.setAuthentication(tokenToPresent);
    }

    /// Refresh required
    /// - for any token issued prior to {@link #SERVER_START}
    /// - if presented token was issued prior user's addition to {@link #usersRequiringFreshClaims}
    private boolean refreshRequired(JwsAuthToken incomingToken)
    {
        if (incomingToken.serverStart().before(SERVER_START))
        {
            return true;
        }

        Date dateRefreshRequired = usersRequiringFreshClaims.getIfPresent(incomingToken.userId());

        return dateRefreshRequired != null && incomingToken.issuedAt().before(dateRefreshRequired);
    }

    @Override
    public void logUserOut(HttpServletResponse response, @Nullable Authentication authentication)
    {
        response.addCookie(cookieService.makeExpiredCookie(JWT_COOKIE_NAME));
        if (authentication instanceof JwsAuthToken jwsAuthToken)
        {
            csrfTokenRepo.invalidateForUser(jwsAuthToken.userId());
        }
    }

    @Override
    public void requireFreshClaimsForUser(long userId)
    {
        Optional<AppUser> user = userService.findById(userId);
        // Deleted users also go into the require-fresh-lookup cache, so it's possible we won't find them
        Date now = new Date();
        LOGGER.info("Tokens issued to user [{}] prior to {} will require claims refreshing",
            user.isPresent() ? user.get().getUsername() : userId,
            DateUtils.as_yyyyMMddhhmmss(now)
        );
        usersRequiringFreshClaims.put(userId, now);
    }


    private String issueToken(AppUser user, Date issuedAt, Date expiry)
    {
        Map<String, Object> claims = new HashMap<>();
        claims.put(ClaimConstants.USERNAME, user.getUsername());
        claims.put(ClaimConstants.ENABLED, user.isEnabled());
        claims.put(ClaimConstants.AUTHORIZED_UNTIL, user.getAuthorizedUntil());
        claims.put(ClaimConstants.LOCKED_ON, user.getLockedOn());
        claims.put(ClaimConstants.SERVER_START, SERVER_START);
        // "admin" is a granted authority, so no need to add it as a separate claim

        int index = 1;
        for (GrantedAuthority authority : user.getAuthorities())
        {
            claims.put(ClaimConstants.GA_STUB + index++, authority.getAuthority());
        }

        return jwtUtil.buildToken(String.valueOf(user.getId()), claims, issuedAt, expiry);
    }

    private Jws<Claims> resolveClaims(String token)
    {
        try
        {
            return jwtUtil.resolveClaims(token);
        }
        catch (Exception ex)
        {
            throw new InvalidTokenException("User presented a malformed auth token", ex);
        }
    }

    private String refreshTokenClaims(JwsAuthToken originalToken)
    {
        Optional<AppUser> user = userService.findWithAuthoritiesById(originalToken.userId());
        if (user.isEmpty())
        {
            throw new UsernameNotFoundException("No user maps to ID " + originalToken.userId());
        }

        Date originalTokenExpiration = originalToken.getPrincipal().getPayload().getExpiration();
        return issueToken(user.get(), new Date(), originalTokenExpiration);
    }

    private void validateToken(JwsAuthToken token)
    {
        // If user ID is null, it will fail here when parsed (this also verifies the claims container isn't null)
        long userId = token.userId();
        Date now = new Date();

        if (token.isExpired())
        {
            throw new InvalidTokenException("User [" + userId + "] presented an expired auth token");
        }
        if (token.issuedAt() == null)
        {
            throw new InvalidTokenException("User [" + userId + "] presented an auth token with no issued-at timestamp");
        }
        if (token.issuedAt().after(now))
        {
            throw new InvalidTokenException("User [" + userId + "] presented an auth token from the future (" + DateUtils.as_yyyyMMddhhmmss(token.issuedAt()) + ")");
        }
        if (token.serverStart() == null)
        {
            throw new InvalidTokenException("User [" + userId + "] presented an auth token with no server start date");
        }
        if (token.serverStart().after(now))
        {
            throw new InvalidTokenException("User [" + userId + "] presented an auth token from a server that hasn't started yet (" + DateUtils.as_yyyyMMddhhmmss(token.serverStart()) + ")");
        }
        if (token.issuedAt().before(token.serverStart()))
        {
            throw new InvalidTokenException("User [" + userId + "] presented an auth token issued before the issuing server started (" + DateUtils.as_yyyyMMddhhmmss(token.issuedAt()) + " < " + DateUtils.as_yyyyMMddhhmmss(token.serverStart()) + ")");
        }
        if (token.username() == null)
        {
            throw new InvalidTokenException("User [" + userId + "] presented an auth token with no username");
        }
    }

    private void validateAccount(JwsAuthToken token)
    {
        String username = token.username();

        if (token.accountLocked())
        {
            throw new LockedException("The account for user [" + username + "] is locked for too many failed attempts.");
        }
        if (token.accountDisabled())
        {
            throw new DisabledException("The account for user [" + username + "] is permanently disabled.");
        }
        if (token.accountExpired())
        {
            throw new AccountExpiredException("The account for user [" + username + "] has expired.");
        }
    }

    /// Detects whether the caller already has their latest claims.
    /// - For logins, we generally expect that they do not already have an auth token cookie, though we have to cover
    /// the case that they presented an expired or invalid token by comparing it with the latest claims
    /// - For all other calls, we expect that they do, unless they required fresh claims in this request
    private boolean requestHasClaims(HttpServletRequest request, String requiredClaims)
    {
        return request.getCookies() != null && Arrays.stream(request.getCookies())
            .filter(cookie -> cookie.getName().equals(JWT_COOKIE_NAME))
            .map(Cookie::getValue)
            .anyMatch(requiredClaims::equals);
    }

    private void setAuthTokenCookie(HttpServletResponse response, String token, int maxAge)
    {
        Cookie tokenCookie = cookieService.makePersistentCookie(JWT_COOKIE_NAME, token, maxAge);
        response.addCookie(tokenCookie);
    }
}