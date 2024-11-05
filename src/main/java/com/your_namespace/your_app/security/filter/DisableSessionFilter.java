package com.your_namespace.your_app.security.filter;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Explicitly prevent http sessions from being created by any framework
 */
@Component
public class DisableSessionFilter extends OncePerRequestFilter
{
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException
    {
        if (request.getSession() != null)
        {
            request.getSession().invalidate(); // Invalidate session if it exists
        }

        HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper(request)
        {
            @Override
            public HttpSession getSession()
            {
                return null; // Disable creation of new session
            }

            @Override
            public HttpSession getSession(boolean create)
            {
                return null; // Disable creation of new session
            }
        };

        filterChain.doFilter(wrappedRequest, response);
    }
}