package com.your_namespace.your_app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import com.your_namespace.your_app.model.user.AppUserDto;
import com.your_namespace.your_app.service.user.UserService;
import com.your_namespace.your_app.util.PasswordUtils;

/**
 * Application entry point
 */
@SpringBootApplication(
    scanBasePackages = "com.your_namespace.your_app", // TODO - set your app's package
    exclude = SecurityAutoConfiguration.class
)
public class YourApp
{
    private static final Logger LOGGER = LoggerFactory.getLogger(YourApp.class);

    public static void main(String[] args)
    {
        ConfigurableApplicationContext context = SpringApplication.run(YourApp.class, args);

        // TODO - after renaming your packages, do a text search on "your_app." to discover all properties to rename
        boolean createAdmin = Boolean.parseBoolean(context.getEnvironment().getProperty("your_app.create-admin"));
        String adminPassword = context.getEnvironment().getProperty("your_app.admin-password");


        if (createAdmin)
        {
            try
            {
                PasswordUtils.validate(adminPassword);
            }
            catch (IllegalArgumentException ex)
            {
                LOGGER.error("invalid value set for your_app.prototype.admin-password: [{}]", adminPassword);
                throw ex;
            }

            try
            {
                UserService userService = context.getBean(UserService.class);
                userService.create(AppUserDto.builder()
                    .username("admin")
                    .password(adminPassword)
                    .enabled(true)
                    .build()
                );
            }
            catch (Exception ex)
            {
                LOGGER.error("Unable to create admin for reason: {}", ex.getMessage(), ex);
            }
        }
    }
}