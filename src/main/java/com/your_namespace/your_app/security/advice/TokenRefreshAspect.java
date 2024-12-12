package com.your_namespace.your_app.security.advice;

import lombok.AllArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.stereotype.Component;

import com.your_namespace.your_app.security.annotation.RequiresClaimsRefresh;
import com.your_namespace.your_app.service.auth.AuthenticationService;
import com.your_namespace.your_app.util.ReflectionUtils;

/**
 * Automatically wraps any method annotated with {@link RequiresClaimsRefresh} to flag the user the method was called
 * for (if the method completes successfully). Flagged users will have their claims automatically refreshed upon their
 * next request.
 */
@Aspect
@Component
@AllArgsConstructor
public class TokenRefreshAspect
{
    private final AuthenticationService authenticationService;

    @AfterReturning("@annotation(com.your_namespace.your_app.security.annotation.RequiresClaimsRefresh)")
    public void after(JoinPoint joinPoint)
    {
        RequiresClaimsRefresh annotationInstance = ReflectionUtils.getAnnotation(joinPoint, RequiresClaimsRefresh.class);
        long userId = ReflectionUtils.getArgValue(joinPoint, annotationInstance.idParamName(), Long.class);
        authenticationService.requireFreshClaimsForUser(userId);
    }
}
