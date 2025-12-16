package com.your_namespace.your_app.config;

import lombok.AllArgsConstructor;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.your_namespace.your_app.interceptor.VersionInterceptor;

@Configuration
@AllArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer
{
    private final VersionInterceptor versionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(versionInterceptor);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry)
    {
        // Maps favicon
        registry.addResourceHandler("/favicon.ico")
            .addResourceLocations("/public/assets/images/icons/favicon/favicon.ico")
            .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS));

        // Public classpath assets already exposed; this adds cache control to public images
        registry.addResourceHandler("/public/assets/images/**")
            .addResourceLocations("/public/assets/images/")
            .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS));
    }
}