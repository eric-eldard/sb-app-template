package com.your_namespace.your_app.config;

import lombok.extern.slf4j.Slf4j;

import jakarta.inject.Inject;
import jakarta.servlet.Filter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.FilterChainProxy;

/**
 * Logs the Spring security filter chain to debug on startup. Very helpful for Spring Security debugging.
 */
@Slf4j
@Configuration
public class FilterChainLogger
{
    @Inject
    public void logFilterChain(FilterChainProxy filterChainProxy)
    {
        if (log.isDebugEnabled())
        {
            AtomicInteger index = new AtomicInteger(1);
            log.debug("\n\n--- Spring Security filter chain for /** ---{}",
                filterChainProxy.getFilters("/**").stream()
                    .map(Filter::getClass)
                    .map(Class::getSimpleName)
                    .map(name -> (index.get() < 10 ? " " : "") + index.getAndIncrement() + ". " + name)
                    .collect(Collectors.joining("\n", "\n", "\n"))
            );
        }
    }
}