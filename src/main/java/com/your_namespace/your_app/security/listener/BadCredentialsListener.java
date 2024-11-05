package com.your_namespace.your_app.security.listener;

import lombok.AllArgsConstructor;

import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import com.your_namespace.your_app.model.user.enumeration.LoginFailureReason;
import com.your_namespace.your_app.service.user.UserService;

@Component
@AllArgsConstructor
public class BadCredentialsListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent>
{
    private final UserService userService;

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event)
    {
        String username = (String) event.getAuthentication().getPrincipal();
        userService.recordFailedLogin(username, LoginFailureReason.BAD_CREDENTIALS);
    }
}