package com.your_namespace.your_app;

import com.your_namespace.your_app.model.user.AppUserDto;
import com.your_namespace.your_app.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

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

        boolean createAdmin = Boolean.parseBoolean(context.getEnvironment().getProperty("your_app.create_admin"));
        if (createAdmin)
        {
            try
            {
                UserService userService = context.getBean(UserService.class);
                userService.create(AppUserDto.builder()
                    .username("admin")
                    .password("password123")
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