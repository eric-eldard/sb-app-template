package com.your_namespace.your_app.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;

import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.servlet.http.Cookie;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.util.UriComponentsBuilder;

import com.your_namespace.your_app.YourApp;
import com.your_namespace.your_app.model.auth.JwsAuthToken;
import com.your_namespace.your_app.model.user.AppUser;
import com.your_namespace.your_app.model.user.AppUserDto;
import com.your_namespace.your_app.model.user.enumeration.AppAuthority;
import com.your_namespace.your_app.persistence.user.UserRepository;
import com.your_namespace.your_app.security.filter.JwsFilter;
import com.your_namespace.your_app.service.auth.AuthenticationService;
import com.your_namespace.your_app.service.auth.SecurityContextService;
import com.your_namespace.your_app.service.user.UserService;
import com.your_namespace.your_app.util.Constants;

@SpringBootTest(
    classes = {
        TestConfig.class,
        YourApp.class
    },
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BaseMvcIntegrationTest
{
    private static final String BASE_URL = "http://localhost";

    @LocalServerPort
    private int port;

    @Inject
    private MockMvc mockMvc;

    @Inject
    private AuthenticationService authService;

    @Inject
    private UserService userService;

    @Inject
    private UserRepository userRepo;


    /**
     * Much faster than {@link DirtiesContext}
     */
    @AfterEach
    protected void deleteAllUsers()
    {
        userRepo.deleteAll();
    }


    protected MockMvc mockMvc()
    {
        return mockMvc;
    }

    protected UserService userService()
    {
        return userService;
    }

    /**
     * Makes a dto for a user we want to take action on
     */
    protected AppUserDto makeNonAdminUserDto()
    {
        return TestUtils.makeUserDto();
    }

    /**
     * Creates an admin user for use as a caller, and returns an auth token cookie to present on their behalf
     */
    protected Cookie asAdmin()
    {
        return createUserAndIssueJwtCookie(AppUserDto.builder()
            .username("admin-caller")
            .admin(true)
        );
    }

    /**
     * Creates a non-admin user for use as a caller, and returns an auth token cookie to present on their behalf
     */
    protected Cookie asUser()
    {
        return createUserAndIssueJwtCookie(AppUserDto.builder()
            .username("standard-user-caller")
        );
    }

    /**
     * NOOP - Does not create a user and returns a null auth token cookie
     */
    protected Cookie asUnauthenticated()
    {
        return null;
    }

    private Cookie createUserAndIssueJwtCookie(AppUserDto.AppUserDtoBuilder builder,
                                               AppAuthority... authorities)
    {
        Date nextYear = new Date(System.currentTimeMillis() + Duration.ofDays(365).toMillis());
        AppUser user = userService.create(builder
            .password(TestUtils.makePassword())
            .authorizedUntil(nextYear)
            .enabled(true)
            .build()
        );
        Arrays.stream(authorities).forEach(authority -> userService.toggleAuth(user.getId(), authority));
        return new Cookie(Constants.JWT_COOKIE_NAME, authService.issueToken(user));
    }

    protected AppUser makeAndSaveNonAdminUser()
    {
        return userService.create(makeNonAdminUserDto());
    }

    @SneakyThrows
    protected String jsonify(Object obj)
    {
        return new ObjectMapper().writeValueAsString(obj);
    }

    /**
     * Perform a GET op on the URI, without a CSRF token, and return ResultActions for continued chaining.
     */
    @SneakyThrows
    protected ResultActions get(URI uri, @Nullable Cookie authCookie)
    {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(uri);
        if (authCookie != null)
        {
            request.cookie(authCookie);
        }
        return mockMvc.perform(request);
    }

    /**
     * Perform an application/json POST to the URI, with a CSRF token, and return ResultActions for continued chaining.
     */
    protected ResultActions post(URI uri, Object body, @Nullable Cookie authCookie)
    {
        return performHttpActionWithJwtAndCsrf(
            MockMvcRequestBuilders.post(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonify(body)),
            authCookie
        );
    }

    /**
     * Perform an application/json PATCH to the URI, with a CSRF token, and return ResultActions for continued chaining.
     */
    protected ResultActions patch(URI uri, Object body, @Nullable Cookie authCookie)
    {
        return performHttpActionWithJwtAndCsrf(
            MockMvcRequestBuilders.patch(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonify(body)),
            authCookie
        );
    }

    /**
     * Perform a DELETE op on the URI, with a CSRF token, and return ResultActions for continued chaining.
     */
    protected ResultActions delete(URI uri, @Nullable Cookie authCookie)
    {
        return performHttpActionWithJwtAndCsrf(MockMvcRequestBuilders.delete(uri), authCookie);
    }

    /// We must add real auth tokens to each request (vs annotating with {@link WithMockUser}); otherwise, the test
    /// invokes the {@link JwsFilter}, where we short-circuit and continue the filter chain when no token is present,
    /// expecting the call to fail authorization (when required) down the line. The test then puts its own
    /// {@link Authentication} into the security context (instead of our {@link JwsAuthToken}), and so authorization is
    /// not failed.
    ///
    /// This introduces two problems:
    /// 1. This circumvents all of our authentication logic in {@link AuthenticationService}, which we want to test
    /// 2. We blow up in {@link SecurityContextService} when we retrieve the principal and can't cast it to
    ///    {@link JwsAuthToken}
    @SneakyThrows
    private ResultActions performHttpActionWithJwtAndCsrf(MockHttpServletRequestBuilder request, Cookie authCookie)
    {
        if (authCookie != null)
        {
            request.cookie(authCookie);
        }
        request.with(csrf().asHeader());
        return mockMvc.perform(request);
    }

    protected UriComponentsBuilder makeBaseUri()
    {
        return UriComponentsBuilder
            .fromUriString(BASE_URL)
            .port(port);
    }

    protected void assertRedirectPath(MvcResult result, String path)
    {
        String redirectUrl = result.getResponse().getRedirectedUrl();
        assertNotNull(redirectUrl);

        String redirectPath = redirectUrl.replace(makeBaseUri().toUriString(), "");
        assertEquals(path, redirectPath);
    }

    protected void assertRedirectToLogin(MvcResult result)
    {
        assertRedirectPath(result, "/login");
    }
}