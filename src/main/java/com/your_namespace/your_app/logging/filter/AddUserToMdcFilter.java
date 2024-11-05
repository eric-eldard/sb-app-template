package com.your_namespace.your_app.logging.filter;

import lombok.AllArgsConstructor;
import org.slf4j.MDC;

import jakarta.servlet.FilterChain;
import jakarta.servlet.GenericFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;

import org.springframework.security.web.context.SecurityContextHolderFilter;

import com.your_namespace.your_app.service.auth.SecurityContextService;

/**
 * Filter for adding the calling user's username to the MDC, exposing that info to the logging context. This filter
 * should always be placed immediately after the {@link SecurityContextHolderFilter}, ensuring we know who the user is
 * by the time this filter is invoked.
 */
@AllArgsConstructor
public class AddUserToMdcFilter extends GenericFilter
{
    private final SecurityContextService securityContextService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException
    {
        try
        {
            String username = securityContextService.getCurrentUsersNameNullable();
            if (username != null)
            {
                MDC.put("username", username);
            }
            chain.doFilter(request, response);
        }
        finally
        {
            MDC.remove("username");
        }
    }
}