package com.your_namespace.your_app.security.filter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.security.authentication.AccountStatusException;
import org.springframework.web.filter.OncePerRequestFilter;

import com.your_namespace.your_app.service.auth.AuthenticationService;
import com.your_namespace.your_app.util.Constants;

/**
 * Validates the user's JWT on each request. All requests pass through this filter, including unauthenticated and even
 * those for public assets. Requests without a JWT are allowed to continue (supporting unauthenticated endpoints); such
 * requests will fail if they reach authorization.
 */
public class JwsFilter extends OncePerRequestFilter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(JwsFilter.class);

    private final AuthenticationService authenticationService;


    public JwsFilter(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException
    {
        Optional<Cookie> cookie = getJwtCookie(request);
        if (cookie.isEmpty())
        {
            chain.doFilter(request, response);
            return;
        }

        // From this point on, the caller must pass an authentication check. Even if they're
        // requesting a public resource, their token must be well-formed and valid.
        try
        {
            String token = cookie.get().getValue();
            authenticationService.setAuthenticationForRequest(token, request, response);
        }
        catch (Exception ex)
        {
            LOGGER.info("Re-authentication failed for reason: {}", ex.getMessage());
            authenticationService.logUserOut(response, null);
            response.setStatus(ex instanceof AccountStatusException ?
                HttpServletResponse.SC_FORBIDDEN :
                HttpServletResponse.SC_UNAUTHORIZED
            );

            return; // Don't continue filter chain; kill this request now
        }

        chain.doFilter(request, response);
    }

    private Optional<Cookie> getJwtCookie(HttpServletRequest request)
    {
        Cookie[] cookies = request.getCookies();
        if (cookies == null)
        {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
            .filter(cookie -> cookie.getName().equals(Constants.JWT_COOKIE_NAME))
            .filter(cookie -> StringUtils.isNotBlank(cookie.getValue()))
            .findFirst();
    }
}