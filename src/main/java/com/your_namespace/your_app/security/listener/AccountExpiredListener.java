package com.your_namespace.your_app.security.listener;

import lombok.AllArgsConstructor;

import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureExpiredEvent;
import org.springframework.stereotype.Component;

import com.your_namespace.your_app.model.user.enumeration.LoginFailureReason;
import com.your_namespace.your_app.service.user.UserService;

@Component
@AllArgsConstructor
public class AccountExpiredListener implements ApplicationListener<AuthenticationFailureExpiredEvent>
{
    private final UserService userService;

    @Override
    public void onApplicationEvent(AuthenticationFailureExpiredEvent event)
    {
        String username = event.getAuthentication().getName();
        userService.recordFailedLogin(username, LoginFailureReason.ACCOUNT_EXPIRED);
    }
}