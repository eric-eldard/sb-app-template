package com.your_namespace.your_app.controller.advice;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.stereotype.Component;

import com.your_namespace.your_app.service.auth.SecurityContextService;

@Slf4j
@Aspect
@Component
@AllArgsConstructor
public class PageViewAspect
{
    private final SecurityContextService securityContextService;

    /**
     * Advice after GetMappings in MVC Controllers to log page views by user
     * @param page the jsp served to the user
     */
    @AfterReturning(pointcut = "within(@org.springframework.stereotype.Controller *) && " +
        "@annotation(org.springframework.web.bind.annotation.GetMapping)", returning = "page")
    public void logPageView(Object page)
    {
        log.debug("[{}] viewed page [{}]", securityContextService.getCurrentUsersNameNonNull(), page);
    }
}