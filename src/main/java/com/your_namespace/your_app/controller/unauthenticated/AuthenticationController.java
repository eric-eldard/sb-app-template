package com.your_namespace.your_app.controller.unauthenticated;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.your_namespace.your_app.model.auth.Credentials;
import com.your_namespace.your_app.model.user.AppUser;
import com.your_namespace.your_app.service.auth.AuthenticationService;

@Slf4j
@RestController
@AllArgsConstructor
public class AuthenticationController
{
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public void login(@RequestBody @Valid Credentials credentials,
                      HttpServletRequest request,
                      HttpServletResponse response
    )
    {
        try
        {
            Authentication authentication = authenticationService.authenticate(
                credentials.getUsername(),
                credentials.getPassword()
            );

            log.info("[{}] successfully logged in.", authentication.getName());

            AppUser user = (AppUser) authentication.getPrincipal();
            String token = authenticationService.issueToken(user);

            authenticationService.setAuthenticationForRequest(token, request, response);

            response.sendRedirect("/your_app"); // TODO - set your app's root path
        }
        catch (Exception ex)
        {
            log.info("Authentication failed for reason: {}", ex.getMessage());
            response.setStatus(ex instanceof AccountStatusException ?
                HttpServletResponse.SC_FORBIDDEN :
                HttpServletResponse.SC_UNAUTHORIZED
            );
        }
    }
}