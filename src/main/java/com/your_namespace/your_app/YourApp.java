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
        boolean createAdmin = Boolean.parseBoolean(context.getEnvironment().getProperty("your_app.admin.create"));


        if (createAdmin)
        {
            String adminEmail = context.getEnvironment().getProperty("your_app.admin.username");
            if (StringUtils.isBlank(adminEmail))
            {
                throw new IllegalArgumentException(
                    "Invalid value set for your_app.admin.email: [" + adminEmail + "]");
            }

            String adminPassword = context.getEnvironment().getProperty("your_app.admin.password");
            if (!PasswordValidator.isValid(adminPassword))
            {
                throw new IllegalArgumentException(
                    "Invalid value set for your_app.admin.password: [" + adminPassword + "]");
            }

            try
            {
                UserService userService = context.getBean(UserService.class);
                userService.create(AppUserDto.builder()
                    .username("admin")
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
}