package com.your_namespace.your_app;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import com.your_namespace.your_app.model.user.AppUserDto;
import com.your_namespace.your_app.service.user.UserService;
import com.your_namespace.your_app.validation.validator.PasswordValidator;

/**
 * Application entry point
 */
@SpringBootApplication(
    scanBasePackageClasses = YourApp.class,
    exclude = SecurityAutoConfiguration.class
)
@Slf4j
public class YourApp
{
    public static void main(String[] args)
    {
        ConfigurableApplicationContext context = SpringApplication.run(YourApp.class, args);

        // TODO - after renaming your packages, do a text search on "your_app." to discover all properties to rename
        String adminUsername = context.getEnvironment().getProperty("your_app.admin.username");

        if (StringUtils.isBlank(adminUsername))
        {
            log.debug("No admin username provided; skipping admin creation.");
            return;
        }

        UserService userService = context.getBean(UserService.class);

        if (userService.hasAdmin())
        {
            // For security reasons, don't allow new admins to be arbitrarily created on startup in existing systems
            log.warn("New admin can only be created when there are no existing admins. Skipping admin creation.");
            return;
        }

        String adminPassword = context.getEnvironment().getProperty("your_app.admin.password");
        if (!PasswordValidator.isValid(adminPassword))
        {
            throw new IllegalArgumentException(
                "Invalid value set for your_app.admin.password: [" + adminPassword + "]");
        }

        try
        {
            userService.create(AppUserDto.builder()
                .username(adminUsername)
                .password(adminPassword)
                .enabled(true)
                .admin(true)
                .build()
            );
        }
        catch (Exception ex)
        {
            log.error("Unable to create admin for reason: {}", ex.getMessage(), ex);
        }
    }
}