package com.your_namespace.your_app.security.listener;

import lombok.AllArgsConstructor;

import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import com.your_namespace.your_app.service.user.UserService;

@Component
@AllArgsConstructor
public class LoginSuccessListener implements ApplicationListener<AuthenticationSuccessEvent>
{
    private final UserService userService;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event)
    {
        String username = event.getAuthentication().getName();
        userService.recordSuccessfulLogin(username);
    }
}