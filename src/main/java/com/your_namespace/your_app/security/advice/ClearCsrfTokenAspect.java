package com.your_namespace.your_app.security.advice;

import lombok.AllArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.stereotype.Component;

import com.your_namespace.your_app.security.annotation.ClearsCsrfToken;
import com.your_namespace.your_app.security.csrf.CsrfTokenRepository;
import com.your_namespace.your_app.util.ReflectionUtils;

/**
 * Automatically wraps any method annotated with {@link ClearsCsrfToken} to cause its successful invocation to drop any
 * CSRF tokens stored for the user.
 */
@Aspect
@Component
@AllArgsConstructor
public class ClearCsrfTokenAspect
{
    private final CsrfTokenRepository csrfTokenRepo;

    @AfterReturning("@annotation(com.your_namespace.your_app.security.annotation.ClearsCsrfToken)")
    public void after(JoinPoint joinPoint)
    {
        ClearsCsrfToken annotationInstance = ReflectionUtils.getAnnotation(joinPoint, ClearsCsrfToken.class);
        long userId = ReflectionUtils.getArgValue(joinPoint, annotationInstance.idParamName(), Long.class);
        csrfTokenRepo.invalidateForUser(userId);
    }
}