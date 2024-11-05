package com.your_namespace.your_app.interceptor;

import lombok.AllArgsConstructor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
@AllArgsConstructor
public class VersionInterceptor implements HandlerInterceptor
{
    @Value("${your_app.app.version:unset}")
    private final String version;

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView)
    {
        if (isNotRedirect(response) && modelAndView != null)
        {
            modelAndView.getModelMap().addAttribute("app_version", version);
        }
    }

    private boolean isNotRedirect(HttpServletResponse response)
    {
        return !HttpStatusCode.valueOf(response.getStatus()).is3xxRedirection();
    }
}